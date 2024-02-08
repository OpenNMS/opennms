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
package org.opennms.netmgt.flows.processing.enrichment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.processing.ConversationKeyUtils;

public class ConversationKeyUtilsTest {

    @Test
    public void canCreateAndParseConversationKey() {
        String key = ConversationKeyUtils.getConvoKeyAsJsonString("SomeLoc", 1, "1.1.1.1", "2.2.2.2", "ulf");

        // We should have generated some key, and both should match
        assertThat(key, notNullValue());

        ConversationKey expectedKey = new ConversationKey(
                "SomeLoc",
                1,
                "1.1.1.1",
                "2.2.2.2",
                "ulf");
        ConversationKey actualKey = ConversationKeyUtils.fromJsonString(key);
        assertThat(actualKey, equalTo(expectedKey));
    }
}
