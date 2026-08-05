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

package org.opennms.netmgt.flows.victorialogs;

/**
 * Reproduces the term matching Elasticsearch performs for the application lookup.
 *
 * <p>That lookup is a {@code prefix} query OR a {@code fuzzy} query with {@code fuzziness: AUTO}
 * (see {@code aggregate_by_fuzzed_field.ftl}). <strong>LogsQL has no fuzzy operator at all</strong>,
 * so unlike every other part of this backend the work cannot be pushed into the server — the distinct
 * values are fetched and matched here. That is the whole reason this class exists.
 *
 * <p>{@code AUTO} is a schedule keyed on the length of the <em>search term</em>, not of the candidate:
 * up to two characters must match exactly, three to five allow one edit, longer allows two. The
 * distance is Damerau–Levenshtein — Lucene counts a transposition of adjacent characters as one edit
 * rather than two, and leaving that out would reject terms Elasticsearch accepts.
 *
 * <p>Worth knowing what this costs: because nothing is pushed down, the caller must pull every
 * distinct application value back before filtering. Application names come from the classification
 * engine and so are naturally low-cardinality, which is what makes that acceptable.
 */
public final class ElasticFuzziness {

    private ElasticFuzziness() {
    }

    /** Matches when the candidate starts with the term or is within the AUTO edit distance of it. */
    public static boolean matchesPrefixOrFuzzy(final String term, final String candidate) {
        if (term == null || term.isEmpty()) {
            // An empty prefix matches everything, which is how the "list them all" call is spelled.
            return true;
        }
        if (candidate == null) {
            return false;
        }
        if (candidate.startsWith(term)) {
            return true;
        }
        final int maxEdits = maxEdits(term.length());
        return maxEdits > 0 && damerauLevenshtein(term, candidate, maxEdits) <= maxEdits;
    }

    /**
     * Elasticsearch's {@code AUTO} fuzziness schedule.
     *
     * @param length length of the search term
     */
    static int maxEdits(final int length) {
        if (length <= 2) {
            return 0;
        }
        return length <= 5 ? 1 : 2;
    }

    /**
     * Damerau–Levenshtein distance, abandoned once it is known to exceed {@code max}.
     *
     * <p>The early exit is not just an optimisation: callers only care whether the distance is within
     * the threshold, and the length check below rejects the common case without doing any work.
     *
     * @return the distance, or a value greater than {@code max} if it exceeds it
     */
    static int damerauLevenshtein(final String a, final String b, final int max) {
        if (Math.abs(a.length() - b.length()) > max) {
            return max + 1;
        }
        if (a.equals(b)) {
            return 0;
        }

        int[] twoBack = new int[b.length() + 1];
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            int rowBest = current[0];
            for (int j = 1; j <= b.length(); j++) {
                final int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int value = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
                // Adjacent characters swapped: one edit, not the two a plain Levenshtein would charge.
                if (i > 1 && j > 1
                        && a.charAt(i - 1) == b.charAt(j - 2)
                        && a.charAt(i - 2) == b.charAt(j - 1)) {
                    value = Math.min(value, twoBack[j - 2] + 1);
                }
                current[j] = value;
                rowBest = Math.min(rowBest, value);
            }
            if (rowBest > max) {
                // No later row can improve on the best in this one, so the answer is already known.
                return max + 1;
            }
            final int[] spare = twoBack;
            twoBack = previous;
            previous = current;
            current = spare;
        }
        return previous[b.length()];
    }
}
