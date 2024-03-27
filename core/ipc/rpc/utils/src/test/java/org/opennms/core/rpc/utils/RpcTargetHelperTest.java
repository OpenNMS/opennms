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
package org.opennms.core.rpc.utils;

import org.junit.Test;
import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RpcTargetHelperTest {

    @Test
    public void usesNullLocationAndSystemIdWhenNothingIsSet() {
        final RpcTarget target = new RpcTargetHelper().target().build();
        assertThat(target.getLocation(), nullValue());
        assertThat(target.getSystemId(), nullValue());
    }

    @Test
    public void usesGivenLocationAndSystemId() {
        final RpcTarget target = new RpcTargetHelper().target()
                .withLocation("x")
                .withSystemId("y")
                .build();
        assertThat(target.getLocation(), equalTo("x"));
        assertThat(target.getSystemId(), equalTo("y"));
    }

    @Test
    public void overridesGivenLocationAndSystemIdUsingAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(RpcTargetHelper.LOCATION_KEY, "xx");
        attributes.put(RpcTargetHelper.SYSTEM_ID_KEY, "yy");

        final RpcTarget target = new RpcTargetHelper().target()
                .withLocation("x")
                .withSystemId("y")
                .withServiceAttributes(attributes)
                .build();
        assertThat(target.getLocation(), equalTo("xx"));
        assertThat(target.getSystemId(), equalTo("yy"));
    }

    @Test
    public void overridesGivenLocationUsingCallback() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(RpcTargetHelper.LOCATION_KEY, "xx");

        final RpcTarget target = new RpcTargetHelper().target()
                .withLocation("x")
                .withServiceAttributes(attributes)
                .withLocationOverride(l -> "xx".equals(l) ? "aa" : "bb")
                .build();
        assertThat(target.getLocation(), equalTo("aa"));
        assertThat(target.getSystemId(), nullValue());
    }

    @Test
    public void canUseForeignIdAsSystemId() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(RpcTargetHelper.SYSTEM_ID_KEY, "yy");
        attributes.put(RpcTargetHelper.USE_FOREIGN_ID_AS_SYSTEM_ID_KEY, Boolean.TRUE.toString());

        final OnmsNode node = mock(OnmsNode.class);
        when(node.getForeignId()).thenReturn("aa");
        final NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(1)).thenReturn(node);

        final RpcTarget target = new RpcTargetHelper().target()
                .withSystemId("y")
                .withServiceAttributes(attributes)
                .withNodeId(1)
                .withNodeDao(nodeDao)
                .build();
        assertThat(target.getLocation(), nullValue());
        assertThat(target.getSystemId(), equalTo("aa"));
    }
}
