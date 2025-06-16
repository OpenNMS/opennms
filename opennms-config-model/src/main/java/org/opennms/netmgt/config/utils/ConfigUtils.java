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
package org.opennms.netmgt.config.utils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);

    public static <T> T assertNotNull(final T value, final String name) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("'" + name + "' cannot be null!");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> T assertNotEmpty(final T value, final String name) throws IllegalArgumentException {
        final T check = value instanceof String? (T)normalizeString((String) value) : value;
        assertNotNull(check, name);
        return check;
    }

    public static String normalizeString(final String s) {
        if ("".equals(s)) return null;
        return s;
    }

    public static String normalizeAndTrimString(final String value) {
        final String ret = normalizeString(value);
        if (ret == null) return null;
        return ret.trim();
    }

    public static String normalizeAndInternString(final String value) {
        final String ret = normalizeString(value);
        if (ret == null) return null;
        return ret.intern();
    }

    public static <T extends Number> T assertMinimumInclusive(final T value, final long minimum, final String name) {
        if (value != null && value.longValue() < minimum) {
            throw new IllegalArgumentException("'" + name + "' must be at least " + minimum);
        }
        return value;
    }

    public static <T extends Collection<?>> T assertMinimumSize(final T value, final long minimum, final String name) {
        if (value != null && value.size() < minimum) {
            throw new IllegalArgumentException("'" + name + "' must contain at least " + minimum + " entry!");
        }
        return value;
    }

    @SuppressWarnings("unlikely-arg-type")
    public static <K,T> T assertOnlyContains(final T value, final Collection<K> in, final String name) {
        if (value == null) return value;
        if (value instanceof String) {
            if (!in.contains(value)) {
                throw new IllegalStateException("'" + name + "': found '" + value.toString() + "' but expected one of: " + in.toString());
            }
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            final Collection<K> col = (Collection<K>)value;
            for (final K entry : col) {
                if (!in.contains(entry)) {
                    throw new IllegalStateException("'" + name + "': found '" + entry.toString() + "' but expected one of: " + in.toString());
                }
            }
        } else {
            LOG.warn("Unsure how to deal with value type {}", value.getClass());
        }
        return value;
    }

    public static String assertMatches(final String value, final Pattern pattern, final String name) {
        if (value == null) return value;
        final Matcher m = pattern.matcher(value);
        if (!m.matches()) {
            throw new IllegalStateException("'" + name + "': value '" + value + "' did no match pattern '" + pattern + "'");
        }
        return value;
    }
}
