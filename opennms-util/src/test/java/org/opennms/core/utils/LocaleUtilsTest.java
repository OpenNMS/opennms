/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public abstract class LocaleUtilsTest {

    @Before
    public void setUp() {
        
    }

    @Test(expected=IllegalArgumentException.class)
    public void singleUnderscore() {
        System.err.println(LocaleUtils.parseLocale("_").getDisplayName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void doubleUnderscore() {
        System.err.println(LocaleUtils.parseLocale("__").getDisplayName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void tripleUnderscore() {
        System.err.println(LocaleUtils.parseLocale("_-_").getDisplayName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void spaces1() {
        System.err.println(LocaleUtils.parseLocale("_US").getDisplayName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void spaces2() {
        System.err.println(LocaleUtils.parseLocale("  _US").getDisplayName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void spaces3() {
        System.err.println(LocaleUtils.parseLocale("en  -  US__ -_").getDisplayName());
    }

    @Test
    public void testParseLocale() {
        assertEquals(
            new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()), 
            LocaleUtils.parseLocale("en-us")
        );
        assertEquals(
            new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()), 
            LocaleUtils.parseLocale("EN-us")
        );
        assertEquals(
            new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()), 
            LocaleUtils.parseLocale("en-US")
        );
        assertEquals(
            new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()), 
            LocaleUtils.parseLocale("EN-US")
        );
        

        // Test underscore
        assertEquals(
            new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()), 
            LocaleUtils.parseLocale("en_us")
        );
        // This is a strange example... should we throw an exception on cases like this?
        assertEquals(
            new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()), 
            LocaleUtils.parseLocale("en  -  US_")
        );

        // Variant test
        assertEquals(
            new Locale(Locale.FRENCH.getLanguage(), Locale.CANADA.getCountry(), "Quebec"), 
            LocaleUtils.parseLocale("fr_ca_Quebec")
        );

        // Make sure that the language-only locale doesn't match the full locale
        assertFalse(
            new Locale(Locale.ENGLISH.getLanguage()).equals( 
                LocaleUtils.parseLocale("en_us")
            )
        );
        assertFalse(
            new Locale(Locale.GERMAN.getLanguage(), Locale.US.getCountry()).equals( 
                LocaleUtils.parseLocale("en_us")
            )
        );
    }

    @Test
    public void testTrivialCase() {
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en-us") },
                new Locale[] { LocaleUtils.parseLocale("en-us") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("de-de"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("de-de") },
                new Locale[] { LocaleUtils.parseLocale("de-de") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en") },
                new Locale[] { LocaleUtils.parseLocale("en") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("de"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("de") },
                new Locale[] { LocaleUtils.parseLocale("de") }
            )
        );
    }

    @Test
    public void testLanguageOnly() {
        assertEquals(
            LocaleUtils.parseLocale("en"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en-us") },
                new Locale[] { LocaleUtils.parseLocale("en") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("de"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("de-de") },
                new Locale[] { LocaleUtils.parseLocale("de") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en") },
                new Locale[] { LocaleUtils.parseLocale("en-us") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("de-de"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("de") },
                new Locale[] { LocaleUtils.parseLocale("de-de") }
            )
        );
    }

    @Test
    public void testCaseInsensitivity() {
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("EN-us") },
                new Locale[] { LocaleUtils.parseLocale("en-us") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en-US") },
                new Locale[] { LocaleUtils.parseLocale("en-us") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en-us") },
                new Locale[] { LocaleUtils.parseLocale("EN-us") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en-us") },
                new Locale[] { LocaleUtils.parseLocale("en-US") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("EN-US") },
                new Locale[] { LocaleUtils.parseLocale("en-us") }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("en-us"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("en-us") },
                new Locale[] { LocaleUtils.parseLocale("EN-US") }
            )
        );
    }
    
    @Test
    public void testAcceptableVariant() {
        assertEquals(
            LocaleUtils.parseLocale("fr-ca"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca"), 
                    LocaleUtils.parseLocale("fr-ca"), 
                    LocaleUtils.parseLocale("fr") 
                }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("fr-ca"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("fr-ca"), 
                    LocaleUtils.parseLocale("fr"), 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca") 
                }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("fr-ca"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("fr"), 
                    LocaleUtils.parseLocale("fr-ca"), 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca") 
                }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("fr-ca"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca"), 
                    LocaleUtils.parseLocale("fr"), 
                    LocaleUtils.parseLocale("fr-ca") 
                }
            )
        );

        assertEquals(
            LocaleUtils.parseLocale("fr"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca"), 
                    LocaleUtils.parseLocale("fr") 
                }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("fr"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("fr"), 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca") 
                }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("fr"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("fr"), 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca") 
                }
            )
        );
        assertEquals(
            LocaleUtils.parseLocale("fr"),
            LocaleUtils.bestLocale(
                new Locale[] { LocaleUtils.parseLocale("fr-ca-Quebec") },
                new Locale[] { 
                    LocaleUtils.parseLocale("en-us"), 
                    LocaleUtils.parseLocale("en-ca"), 
                    LocaleUtils.parseLocale("fr") 
                }
            )
        );
    }
}
