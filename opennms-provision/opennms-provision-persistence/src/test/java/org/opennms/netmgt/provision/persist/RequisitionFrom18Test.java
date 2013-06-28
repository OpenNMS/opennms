/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        m_foreignSourceRepository.setForeignSourcePath("target/test-classes/empty");
        m_foreignSourceRepository.setRequisitionPath("target/test-classes/1.8-upgrade-test");
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
