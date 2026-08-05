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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pins the Elasticsearch term-matching rules this class stands in for.
 *
 * <p>The two fuzzy cases at the end are the ones the recorded reference actually exercises, and they
 * are the reason the AUTO schedule has to be reproduced exactly rather than approximated: they differ
 * from each other only in the length of the search term.
 */
public class ElasticFuzzinessTest {

    @Test
    public void autoScheduleFollowsTermLength() {
        assertEquals("a one-character term must match exactly", 0, ElasticFuzziness.maxEdits(1));
        assertEquals(0, ElasticFuzziness.maxEdits(2));
        assertEquals(1, ElasticFuzziness.maxEdits(3));
        assertEquals(1, ElasticFuzziness.maxEdits(5));
        assertEquals(2, ElasticFuzziness.maxEdits(6));
        assertEquals(2, ElasticFuzziness.maxEdits(20));
    }

    /** Lucene charges one edit for swapping neighbours, not the two a plain Levenshtein would. */
    @Test
    public void countsATranspositionAsOneEdit() {
        // "htpt" is "http" with the last two characters swapped.
        assertEquals(1, ElasticFuzziness.damerauLevenshtein("htpt", "http", 2));
        assertEquals(1, ElasticFuzziness.damerauLevenshtein("hpttlow", "hpttlwo", 2));
    }

    @Test
    public void reportsDistancesWithinTheBound() {
        assertEquals(0, ElasticFuzziness.damerauLevenshtein("http", "http", 2));
        assertEquals(1, ElasticFuzziness.damerauLevenshtein("http", "htp", 2));
        assertEquals(2, ElasticFuzziness.damerauLevenshtein("httz", "https", 2));
    }

    /** Beyond the bound the exact distance is not computed, only that it was exceeded. */
    @Test
    public void abandonsOnceTheBoundIsExceeded() {
        assertTrue(ElasticFuzziness.damerauLevenshtein("http", "completely-different", 2) > 2);
    }

    @Test
    public void anEmptyTermMatchesEverything() {
        assertTrue(ElasticFuzziness.matchesPrefixOrFuzzy("", "http"));
        assertTrue(ElasticFuzziness.matchesPrefixOrFuzzy(null, "http"));
    }

    @Test
    public void matchesOnPrefix() {
        assertTrue(ElasticFuzziness.matchesPrefixOrFuzzy("h", "http"));
        assertTrue(ElasticFuzziness.matchesPrefixOrFuzzy("h", "https"));
        assertFalse(ElasticFuzziness.matchesPrefixOrFuzzy("h", "ssh-alt"));
    }

    /**
     * A short term is matched strictly: "ht" is two characters, so AUTO allows no edits at all and
     * only the prefix rule can match.
     */
    @Test
    public void shortTermsAreNotFuzzed() {
        assertTrue(ElasticFuzziness.matchesPrefixOrFuzzy("ht", "http"));
        assertFalse(ElasticFuzziness.matchesPrefixOrFuzzy("hz", "http"));
    }

    /** "httz" is one edit from "http" but two from "https", and four characters allows one. */
    @Test
    public void httzMatchesOnlyHttp() {
        assertTrue(ElasticFuzziness.matchesPrefixOrFuzzy("httz", "http"));
        assertFalse(ElasticFuzziness.matchesPrefixOrFuzzy("httz", "https"));
    }

    /** "hyyps" is two edits from "https", and five characters still allows only one. */
    @Test
    public void hyypsMatchesNothing() {
        assertFalse(ElasticFuzziness.matchesPrefixOrFuzzy("hyyps", "https"));
        assertFalse(ElasticFuzziness.matchesPrefixOrFuzzy("hyyps", "http"));
    }
}
