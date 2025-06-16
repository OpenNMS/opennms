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
