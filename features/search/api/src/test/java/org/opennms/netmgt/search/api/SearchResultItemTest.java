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
package org.opennms.netmgt.search.api;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SearchResultItemTest {

    @Test
    public void verifyGeolocationTest() {
        final List<Matcher> matcherList = Lists.newArrayList(
                new Matcher("Country", null),
                new Matcher("City", "Fulda"),
                new Matcher("State", null),
                new Matcher("Zip", "36039"),
                new Matcher("Address 1", "Leipziger Strasse 123"),
                new Matcher("Address 2", null)
        );

        final SearchResultItem item = new SearchResultItem();
        item.addMatches(matcherList, "Fuld");
        assertThat(item.getMatches(), hasSize(1));
    }

}