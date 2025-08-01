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
package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.policies.NodeCategorySettingPolicy;
import org.opennms.netmgt.provision.support.PluginWrapper;


public class PluginWrapperTest {
    
    @Test
    public void testChoices() throws Exception {
        PluginWrapper wrapper = new PluginWrapper(MatchingSnmpInterfacePolicy.class);
        assertTrue("required keys must contain matchBehavior", wrapper.getRequiredItems().containsKey("matchBehavior"));
        assertTrue("action must contain DISABLE_COLLECTION", wrapper.getRequiredItems().get("action").contains("DISABLE_COLLECTION"));
    }

    @Test
    public void testRequired() throws Exception {
        PluginWrapper wrapper = new PluginWrapper(NodeCategorySettingPolicy.class);
        assertTrue("category should be required", wrapper.getRequired().get("category"));
        assertFalse("type should not be required", wrapper.getRequired().get("type"));
    }
}
