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
