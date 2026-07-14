/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates iplike match expressions into native-inet SQL predicates that
 * PostgreSQL can answer through an expression index, instead of calling the
 * iplike() stored procedure per row.
 *
 * <p>An iplike pattern is a per-field cross-product of integer segments
 * (specific value, range {@code a-b}, comma list, {@code *}), which always
 * denotes a union of contiguous address ranges. This class renders that union
 * as {@code opennms_safe_inet(col) BETWEEN ... OR ...} range comparisons,
 * matching the expression index created on the same function.
 *
 * <p>{@link #toSqlPredicate(String, String)} returns {@code null} whenever the
 * pattern cannot be translated with identical semantics, and callers MUST fall
 * back to emitting {@code iplike(col, ?)} exactly as before. Untranslatable
 * cases: patterns carrying an IPv6 zone id (no inet representation), patterns
 * whose range expansion exceeds {@link #MAX_RANGES}, and anything malformed
 * (iplike evaluates those to false at runtime; the fallback preserves that).
 *
 * <p>Semantics are defined by the iplike C implementation's test corpus
 * (tests.dat, mirrored by IPLikeCoverageIT); the unit test evaluates every
 * corpus case against the translated ranges.
 */
public abstract class IplikeSqlTranslator {

    /** The garbage-tolerant cast function the expression indexes are built on. */
    public static final String SAFE_INET_FUNCTION = "opennms_safe_inet";

    /**
     * Expansion cap. Patterns exceeding this many address ranges fall back to
     * iplike(): with the expression index present even thousands of ranges
     * answer quickly, but an OR-chain this size evaluated per row (index
     * missing or dropped) degrades far below iplike, so the cap bounds the
     * worst case.
     */
    public static final int MAX_RANGES = 1024;

    /** Escape hatch: set to "false" to disable translation globally. */
    public static final String ENABLED_PROPERTY = "org.opennms.iplike.native";

    private static final String V4_MATCH_ALL = "*.*.*.*";
    private static final String V6_MATCH_ALL = "*:*:*:*:*:*:*:*";

    private static class Untranslatable extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private IplikeSqlTranslator() {
    }

    /**
     * @param pattern the iplike match expression (user-supplied filter text)
     * @param columnExpression SQL text for the address column, e.g.
     *        {@code "ipaddr"} or {@code "{alias}.ipaddr"}
     * @return a self-contained SQL predicate built only from re-rendered
     *         numeric literals (never from the raw pattern text), or
     *         {@code null} if the caller must fall back to iplike()
     */
    public static String toSqlPredicate(final String pattern, final String columnExpression) {
        if (!Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY, "true"))) {
            return null;
        }
        if (pattern == null || columnExpression == null) {
            return null;
        }
        if (V4_MATCH_ALL.equals(pattern) || V6_MATCH_ALL.equals(pattern)) {
            // the shipped PL/pgSQL iplike fast-paths both match-all forms to
            // true for every non-null value; NULL never matches
            return columnExpression + " IS NOT NULL";
        }

        final List<BigInteger[]> ranges;
        final int family;
        try {
            final Parsed parsed = parse(pattern);
            family = parsed.family;
            ranges = buildRanges(parsed);
        } catch (final Untranslatable e) {
            return null;
        }
        if (ranges.isEmpty()) {
            return null;
        }

        final String func = SAFE_INET_FUNCTION + "(" + columnExpression + ")";
        final StringBuilder sb = new StringBuilder();
        // the func IS NOT NULL conjunct forces the predicate to FALSE (not
        // NULL) for values opennms_safe_inet() cannot parse, so callers that
        // wrap it in NOT include those rows exactly like NOT iplike() does
        sb.append("(").append(columnExpression).append(" IS NOT NULL AND ")
          .append(func).append(" IS NOT NULL AND (");
        for (int i = 0; i < ranges.size(); i++) {
            if (i > 0) {
                sb.append(" OR ");
            }
            final BigInteger lo = ranges.get(i)[0];
            final BigInteger hi = ranges.get(i)[1];
            if (lo.equals(hi)) {
                sb.append(func).append(" = inet '").append(render(family, lo)).append("'");
            } else {
                sb.append("(").append(func).append(" >= inet '").append(render(family, lo))
                  .append("' AND ").append(func).append(" <= inet '").append(render(family, hi))
                  .append("')");
            }
        }
        sb.append("))");
        return sb.toString();
    }

    /**
     * Evaluates a pattern against an address value using the same range
     * translation the SQL predicate is built from, mirroring
     * opennms_safe_inet()'s treatment of the value (zone id stripped on IPv6
     * only, strict dotted-quad or 8-group format). Exists so tests can verify corpus parity
     * without a database; returns null when the pattern is untranslatable.
     */
    static Boolean matches(final String value, final String pattern) {
        if (V4_MATCH_ALL.equals(pattern) || V6_MATCH_ALL.equals(pattern)) {
            return value != null;
        }
        final Parsed parsed;
        final List<BigInteger[]> ranges;
        try {
            parsed = parse(pattern);
            ranges = buildRanges(parsed);
        } catch (final Untranslatable e) {
            return null;
        }
        final BigInteger n = safeInetValue(value, parsed.family);
        if (n == null) {
            return false;
        }
        for (final BigInteger[] r : ranges) {
            if (n.compareTo(r[0]) >= 0 && n.compareTo(r[1]) <= 0) {
                return true;
            }
        }
        return false;
    }

    /** The value side of opennms_safe_inet(), restricted to one family. */
    private static BigInteger safeInetValue(final String value, final int family) {
        if (value == null) {
            return null;
        }
        final int pct = value.indexOf('%');
        if (family == 4) {
            // zone ids are IPv6-only; iplike rejects IPv4 values carrying one
            if (pct >= 0) {
                return null;
            }
            final String[] toks = value.split("\\.", -1);
            if (toks.length != 4) {
                return null;
            }
            long n = 0;
            for (final String t : toks) {
                if (!t.matches("0|[1-9][0-9]{0,2}")) {
                    return null;
                }
                final int octet = Integer.parseInt(t);
                if (octet > 255) {
                    return null;
                }
                n = (n << 8) | octet;
            }
            return BigInteger.valueOf(n);
        }
        final String v = pct >= 0 ? value.substring(0, pct) : value;
        final String[] toks = v.split(":", -1);
        if (toks.length != 8) {
            return null;
        }
        BigInteger n = BigInteger.ZERO;
        for (final String t : toks) {
            if (!t.matches("[0-9a-fA-F]{1,4}")) {
                return null;
            }
            n = n.shiftLeft(16).or(BigInteger.valueOf(Integer.parseInt(t, 16)));
        }
        return n;
    }

    private static class Parsed {
        final int family;
        final int bits;
        final List<List<long[]>> fields;

        Parsed(final int family, final int bits, final List<List<long[]>> fields) {
            this.family = family;
            this.bits = bits;
            this.fields = fields;
        }
    }

    private static Parsed parse(final String pattern) throws Untranslatable {
        if (pattern.indexOf('%') >= 0) {
            throw new Untranslatable(); // zone ids have no inet representation
        }
        if (pattern.indexOf(':') >= 0) {
            final String[] toks = pattern.toLowerCase().split(":", -1);
            if (toks.length != 8) {
                throw new Untranslatable();
            }
            final List<List<long[]>> fields = new ArrayList<>(8);
            for (final String tok : toks) {
                fields.add(parseField(tok, 0xffff, true));
            }
            return new Parsed(6, 16, fields);
        }
        final String[] toks = pattern.split("\\.", -1);
        if (toks.length != 4) {
            throw new Untranslatable();
        }
        final List<List<long[]>> fields = new ArrayList<>(4);
        for (final String tok : toks) {
            fields.add(parseField(tok, 255, false));
        }
        return new Parsed(4, 8, fields);
    }

    private static List<long[]> parseField(final String token, final long maxValue, final boolean hex) throws Untranslatable {
        final List<long[]> segments = new ArrayList<>();
        for (final String part : token.split(",", -1)) {
            if ("*".equals(part)) {
                final List<long[]> all = new ArrayList<>(1);
                all.add(new long[] {0, maxValue});
                return all;
            }
            final long lo, hi;
            final int dash = part.indexOf('-');
            if (dash >= 0) {
                lo = parseValue(part.substring(0, dash), hex);
                hi = parseValue(part.substring(dash + 1), hex);
            } else {
                lo = hi = parseValue(part, hex);
            }
            if (lo > maxValue) {
                continue; // rule element above the field maximum never matches
            }
            segments.add(new long[] {lo, Math.min(hi, maxValue)});
        }
        if (segments.isEmpty()) {
            throw new Untranslatable();
        }
        return segments;
    }

    private static long parseValue(final String s, final boolean hex) throws Untranslatable {
        if (s.isEmpty() || s.length() > 8 || !s.matches(hex ? "[0-9a-f]+" : "[0-9]+")) {
            throw new Untranslatable();
        }
        return Long.parseLong(s, hex ? 16 : 10);
    }

    private static List<BigInteger[]> buildRanges(final Parsed parsed) throws Untranslatable {
        final List<BigInteger[]> out = new ArrayList<>();
        recurse(parsed, 0, BigInteger.ZERO, out);
        out.sort((a, b) -> a[0].compareTo(b[0]));
        final List<BigInteger[]> merged = new ArrayList<>();
        for (final BigInteger[] r : out) {
            if (!merged.isEmpty() && r[0].compareTo(merged.get(merged.size() - 1)[1].add(BigInteger.ONE)) <= 0) {
                final BigInteger[] last = merged.get(merged.size() - 1);
                last[1] = last[1].max(r[1]);
            } else {
                merged.add(r);
            }
        }
        return merged;
    }

    private static void recurse(final Parsed parsed, final int pos, final BigInteger acc,
            final List<BigInteger[]> out) throws Untranslatable {
        final int n = parsed.fields.size();
        final int shift = parsed.bits * (n - 1 - pos);
        final long maxValue = (1L << parsed.bits) - 1;

        boolean restFull = true;
        for (int q = pos + 1; q < n && restFull; q++) {
            final List<long[]> segs = parsed.fields.get(q);
            restFull = segs.size() == 1 && segs.get(0)[0] == 0 && segs.get(0)[1] == maxValue;
        }

        if (restFull) {
            final BigInteger lowOnes = BigInteger.ONE.shiftLeft(shift).subtract(BigInteger.ONE);
            for (final long[] seg : parsed.fields.get(pos)) {
                out.add(new BigInteger[] {
                        acc.or(BigInteger.valueOf(seg[0]).shiftLeft(shift)),
                        acc.or(BigInteger.valueOf(seg[1]).shiftLeft(shift)).or(lowOnes)});
                if (out.size() > MAX_RANGES) {
                    throw new Untranslatable();
                }
            }
            return;
        }
        for (final long[] seg : parsed.fields.get(pos)) {
            for (long v = seg[0]; v <= seg[1]; v++) {
                recurse(parsed, pos + 1, acc.or(BigInteger.valueOf(v).shiftLeft(shift)), out);
            }
        }
    }

    /**
     * Rendered by hand rather than through InetAddress: getByAddress()
     * collapses IPv4-mapped IPv6 addresses (::ffff:0:0/96) to Inet4Address,
     * which would emit a family-4 inet literal that never compares equal to
     * the family-6 value opennms_safe_inet() produces.
     */
    private static String render(final int family, final BigInteger n) {
        final StringBuilder sb = new StringBuilder();
        if (family == 4) {
            final long v = n.longValueExact();
            sb.append((v >> 24) & 0xff).append('.').append((v >> 16) & 0xff)
              .append('.').append((v >> 8) & 0xff).append('.').append(v & 0xff);
        } else {
            for (int group = 7; group >= 0; group--) {
                sb.append(Long.toHexString(n.shiftRight(group * 16).intValue() & 0xffff));
                if (group > 0) {
                    sb.append(':');
                }
            }
        }
        return sb.toString();
    }
}
