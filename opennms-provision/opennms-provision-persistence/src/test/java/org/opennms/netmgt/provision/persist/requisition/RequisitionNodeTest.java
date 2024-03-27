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

import javax.xml.bind.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.MockLogAppender;

public class RequisitionNodeTest {

    private RequisitionNode node;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        node = new RequisitionNode();
        node.setNodeLabel("nodeLabel");
        node.setForeignId("foreignId");
    }

    @Test
    public void testRequisitionNodeValid() throws ValidationException {
        node.validate();
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionNodeLabelValidation() throws ValidationException {
        node.setNodeLabel(null);
        node.validate();
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionNodeForeignIdValidation() throws ValidationException {
        node.setForeignId(null);
        node.validate();
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionNodeForeignIdCharacterValidation() throws ValidationException {
        node.setForeignId("node:2");
        node.validate();
    }

    @Test
    public void testRequisitionNodeValidatesInterfaces() throws ValidationException {
        RequisitionInterface ifaceMock = Mockito.mock(RequisitionInterface.class);

        node.getInterfaces().add(ifaceMock);

        node.validate();
        Mockito.verify(ifaceMock).validate(node);
    }

    @Test
    public void testRequisitionNodeValidatesCategories() throws ValidationException {
        RequisitionCategory categoryMock = Mockito.mock(RequisitionCategory.class);

        node.getCategories().add(categoryMock);

        node.validate();
        Mockito.verify(categoryMock).validate();
    }

    @Test
    public void testRequisitionNodeValidatesAssets() throws ValidationException {
        RequisitionAsset assetMock = Mockito.mock(RequisitionAsset.class);

        node.getAssets().add(assetMock);

        node.validate();
        Mockito.verify(assetMock).validate();
    }
}
