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
package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.test.JUnitConfigurationEnvironment;


@JUnitConfigurationEnvironment
public class RequisitionFrom18Test {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionFrom18Test.class);
    private FilesystemForeignSourceRepository m_foreignSourceRepository;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_foreignSourceRepository = new FilesystemForeignSourceRepository();
        m_foreignSourceRepository.setForeignSourcePath(this.getClass().getResource("/empty").getPath());
        m_foreignSourceRepository.setRequisitionPath(this.getClass().getResource("/1.8-upgrade-test").getPath());
    }

    @Test
    public void test18Requisitions() {
        final Set<Requisition> requisitions = m_foreignSourceRepository.getRequisitions();
        assertEquals(11, requisitions.size());
        int nodeCount = 0;
        int interfaceCount = 0;
        for (final Requisition r : requisitions) {
            LOG.debug("got requisition: {}", r);
            nodeCount += r.getNodeCount();
            for (RequisitionNode node : r.getNode()) {
                interfaceCount += node.getInterfaceCount();
                if ("pgvip-master.somemediathing.net".equals(node.getNodeLabel())) {
                    // Make sure that parent-foreign-source and parent-foreign-id work
                    assertEquals("postgres", node.getParentForeignSource());
                    assertEquals("1241674181872", node.getParentForeignId());
                    assertEquals("barbacoa.somemediathing.net", node.getParentNodeLabel());
                }
            }
        }
        assertEquals("There is an unexpected number of nodes in the test requisitions", 49, nodeCount);
        assertEquals("There is an unexpected number of interfaces in the test requisitions", 60, interfaceCount);
    }

}
