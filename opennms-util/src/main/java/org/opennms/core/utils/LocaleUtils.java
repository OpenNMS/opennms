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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * @author Seth
 */
public abstract class LocaleUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocaleUtils.class);

    private static final Locale DEFAULT_LOCALE = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());

    public static Locale parseLocale(String string) {
        String[] segments = string.split("[-_]");
        if (segments.length == 1) {
            // Language only
            if ("".equals(segments[0].trim())) {
                throw new IllegalArgumentException("Invalid locale string: " + string);
            } else {
                return new Locale(segments[0].trim());
            }
        } else if (segments.length == 2) {
            // Language and country
            if ("".equals(segments[0].trim()) || "".equals(segments[1].trim())) {
                throw new IllegalArgumentException("Invalid locale string: " + string);
            } else {
                return new Locale(segments[0].trim(), segments[1].trim());
            }
        } else if (segments.length == 3) {
            // Language, country, and variant
            if ("".equals(segments[0].trim()) || "".equals(segments[1].trim()) || "".equals(segments[2].trim())) {
                throw new IllegalArgumentException("Invalid locale string: " + string);
            } else {
                return new Locale(segments[0].trim(), segments[1].trim(), segments[2].trim());
            }
        } else {
            throw new IllegalArgumentException("Invalid locale string: " + string);
        }
    }

    public static Locale bestLocale(Locale[] preferredLocales, Locale[] availableLocales) {
        boolean hasCountry = false;
        boolean hasVariant = false;
        // Precise match (language, locale, and country)
        for (Locale prefer : preferredLocales) {
            if (prefer != null) {
                if (prefer.getVariant() != null && !"".equals(prefer.getVariant())) {
                    hasVariant = true;
                }
                if (prefer.getCountry() != null && !"".equals(prefer.getCountry())) {
                    hasCountry = true;
                }
                for (Locale avail : availableLocales) {
                    if (avail != null) {
                        if (avail.getVariant() != null && !"".equals(avail.getVariant())) {
                            hasVariant = true;
                        }
                        if (avail.getCountry() != null && !"".equals(avail.getCountry())) {
                            hasCountry = true;
                        }
                        if (prefer.equals(avail)) {
                            return avail;
                        }
                    } else {
                    	LOG.trace("Null locale in available list");
                    }
                }
            } else {
            	LOG.trace("Null locale in preferred list");
            }
        }
        // Only perform this match if one of the locales had a variant,
        // otherwise it is unnecessary
        if (hasVariant) {
            // Language and country match
            for (Locale prefer : preferredLocales) {
                if (prefer != null) {
                    for (Locale avail : availableLocales) {
                        if (avail != null) {
                            if (
                                prefer.getLanguage().equalsIgnoreCase(avail.getLanguage())
                                    && (prefer.getCountry() == null || prefer.getCountry().equalsIgnoreCase(avail.getCountry()))
                            ) {
                                return avail;
                            }
                        } else {
                        	LOG.trace("Null locale in available list");
                        }
                    }
                } else {
                	LOG.trace("Null locale in preferred list");
                }
            }
        }
        // Only perform this match if one of the locales had a country,
        // otherwise it is unnecessary
        if (hasCountry) {
            // Language-only match
            for (Locale prefer : preferredLocales) {
                if (prefer != null) {
                    for (Locale avail : availableLocales) {
                        if (avail != null) {
                            if (prefer.getLanguage().equalsIgnoreCase(avail.getLanguage())) {
                                return avail;
                            }
                        } else {
                        	LOG.trace("Null locale in available list");
                        }
                    }
                } else {
                	LOG.trace("Null locale in preferred list");
                }
            }
        }
        // Fall back to US English
        return DEFAULT_LOCALE;
    }
}
