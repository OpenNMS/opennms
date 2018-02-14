/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist.requisition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;

public class RequisitionTest {

	@Before
	public void setUp() {
		MockLogAppender.setupLogging();
	}

	@Test
	public void testRequisitionValidation() {
		final List<RequisitionNode> nodes = new ArrayList<>();
		
		final Requisition req = new Requisition();
		req.updateDateStamp();
		req.updateLastImported();
		
		req.setForeignSource("foreignSource1");
		
		final RequisitionNode foreignId1 = new RequisitionNode();
		foreignId1.setForeignId("foreignId1");
		foreignId1.setNodeLabel("foreign ID 1");
		
		nodes.add(foreignId1);
		req.setNodes(nodes);
		
		try {
			req.validate();
		} catch (final ValidationException e) {
			fail();
		}
		
		final RequisitionNode foreignId2 = new RequisitionNode();
		foreignId2.setForeignId("foreignId2");
		foreignId2.setNodeLabel("foreign ID 2");

		nodes.add(foreignId2);
		req.setNodes(nodes);

		try {
			req.validate();
		} catch (final ValidationException e) {
			fail();
		}
		
		final RequisitionNode duplicateId = new RequisitionNode();
		duplicateId.setForeignId("foreignId1");
		duplicateId.setNodeLabel("foreign ID 1 (duplicate)");

		nodes.add(duplicateId);
		req.setNodes(nodes);

		try {
			req.validate();
			fail();
		} catch (final ValidationException e) {
			assertTrue("error should say foreignId1 has a duplicate", e.getMessage().contains("foreignId1"));
			assertTrue("error should it found 2 errors", e.getMessage().contains("foreignId1 (2 found)"));
		}

		nodes.add(duplicateId);
		req.setNodes(nodes);

		try {
			req.validate();
			fail();
		} catch (final ValidationException e) {
			assertTrue("error should say foreignId1 has a duplicate", e.getMessage().contains("foreignId1"));
			assertTrue("error count should now be 3", e.getMessage().contains("foreignId1 (3 found)"));
		}
		
		nodes.add(foreignId2);
		req.setNodes(nodes);
		
		try {
			req.validate();
			fail();
		} catch (final ValidationException e) {
			assertTrue("error should say foreignId1 & 2 have duplicates", e.getMessage().contains("foreignId1") && e.getMessage().contains("foreignId2"));
			assertTrue("foreignId1 should still have 3 errors", e.getMessage().contains("foreignId1 (3 found)"));
			assertTrue("foreignId2 should have 2 errors", e.getMessage().contains("foreignId2 (2 found)"));
		}
	}
}
