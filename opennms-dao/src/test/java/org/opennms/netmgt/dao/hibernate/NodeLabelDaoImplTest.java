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
package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.dao.api.NodeLabel;
import org.springframework.test.util.ReflectionTestUtils;

public class NodeLabelDaoImplTest {

    private NodeDao nodeDao;
    private NodeLabelDaoImpl nodeLabelDao;

    @Before
    public void setUp() {
        nodeDao = mock(NodeDao.class);
        nodeLabelDao = new NodeLabelDaoImpl();
        ReflectionTestUtils.setField(nodeLabelDao, "nodeDao", nodeDao);
    }

    private void givenNode(final int id, final String label, final NodeLabelSource source) {
        final OnmsNode node = new OnmsNode();
        node.setId(id);
        node.setLabel(label);
        node.setLabelSource(source);
        when(nodeDao.get(id)).thenReturn(node);
    }

    @Test
    public void retrievesTheLabelAndItsSource() throws Exception {
        givenNode(42, "core-sw1", NodeLabelSource.USER);

        final NodeLabel label = nodeLabelDao.retrieveLabel(42);

        assertEquals("core-sw1", label.getLabel());
        assertEquals(NodeLabelSource.USER, label.getSource());
    }

    // Nodes added through the REST API can have no label source. The Change Node Label page
    // (admin/nodelabel.jsp) failed with a NullPointerException for them.
    @Test
    public void treatsAMissingLabelSourceAsUnknown() throws Exception {
        givenNode(43, "rest-node", null);

        final NodeLabel label = nodeLabelDao.retrieveLabel(43);

        assertEquals("rest-node", label.getLabel());
        assertEquals(NodeLabelSource.UNKNOWN, label.getSource());
    }
}
