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
package org.opennms.features.config.osgi.cm;

import static org.junit.Assert.assertEquals;
import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import org.junit.Test;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;

public class CmIdentifierUtilTest {

    @Test
    public void shouldParse() {
        checkParse("org.opennms.features.datachoices",
                "org.opennms.features.datachoices", DEFAULT_CONFIG_ID); // single instance
        // This part of the test is commented until MigratedServices contain "org.opennms.netmgt.graph.provider.graphml"
//        checkParse("org.opennms.netmgt.graph.provider.graphml-someid",
//                "org.opennms.netmgt.graph.provider.graphml", "someid"); // multi instance
    }

    private void checkParse(String pid, String expectedName, String expectedId) {
        ConfigUpdateInfo id = CmIdentifierUtil.pidToCmIdentifier(pid);
        assertEquals(expectedName, id.getConfigName());
        assertEquals(expectedId, id.getConfigId());
    }

    @Test(expected = ConfigRuntimeException.class)
    public void shouldThrowExceptionForEmptyInput() {
        CmIdentifierUtil.pidToCmIdentifier("");
    }

    @Test(expected = ConfigRuntimeException.class)
    public void shouldThrowExceptionForMissingSuffix() {
        CmIdentifierUtil.pidToCmIdentifier("abc");
    }

    @Test
    public void shouldCreatePid() {
        checkCreate("abc", "def", "abc-def");
        checkCreate("abc", "default", "abc");
    }

    private void checkCreate(String name, String id, String expectedPid) {
        String pid = CmIdentifierUtil.cmIdentifierToPid(new ConfigUpdateInfo(name, id));
        assertEquals(expectedPid, pid);
    }
}