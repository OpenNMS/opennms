/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seth
 */
public abstract class LocaleUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocaleUtils.class);

    public static final Locale DEFAULT_LOCALE = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());

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
                                prefer.getLanguage().equalsIgnoreCase(avail.getLanguage()) &&
                                (prefer.getCountry() == null ?
                                    true : 
                                    prefer.getCountry().equalsIgnoreCase(avail.getCountry())
                                )
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
