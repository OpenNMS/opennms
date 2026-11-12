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
package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.service.impl.NodeTopologyServiceImpl;

/**
 * A node criteria that is neither numeric nor foreignSource:foreignId must
 * yield null (node not found), not ArrayIndexOutOfBoundsException.
 */
public class NodeTopologyServiceGetSnmpNodeTest {

    @Test
    public void malformedCriteriaReturnsNull() {
        final NodeTopologyServiceImpl service = new NodeTopologyServiceImpl();
        assertNull(service.getSnmpNode("some-node-label"));
        assertNull(service.getSnmpNode("fs:"));
        assertNull(service.getSnmpNode(":fid"));
        assertNull(service.getSnmpNode(":"));
    }

    @Test
    public void foreignIdKeepsItsColons() {
        final NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.findMatching(any(Criteria.class))).thenReturn(List.of());
        final NodeTopologyServiceImpl service = new NodeTopologyServiceImpl();
        service.setNodeDao(nodeDao);

        assertNull(service.getSnmpNode("a:b:c"));

        final ArgumentCaptor<Criteria> criteria = ArgumentCaptor.forClass(Criteria.class);
        verify(nodeDao).findMatching(criteria.capture());
        final List<Object> values = criteria.getValue().getRestrictions().stream()
                .filter(EqRestriction.class::isInstance)
                .map(r -> ((EqRestriction) r).getValue())
                .collect(Collectors.toList());
        assertEquals(true, values.contains("a"));
        assertEquals(true, values.contains("b:c"));
    }
}
