/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.MockLogAppender;

import javax.xml.bind.ValidationException;

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
