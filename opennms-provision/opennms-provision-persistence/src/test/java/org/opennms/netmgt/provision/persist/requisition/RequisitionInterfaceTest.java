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
import org.opennms.netmgt.model.PrimaryType;

import javax.xml.bind.ValidationException;
import java.util.Collections;

import static org.mockito.Mockito.when;

public class RequisitionInterfaceTest {

    private RequisitionInterface iface;

    private RequisitionNode nodeMock;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        iface = new RequisitionInterface();
        iface.setIpAddr("0.0.0.0");

        nodeMock = Mockito.mock(RequisitionNode.class);
        when(nodeMock.getInterfaces()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testRequisitionInterfaceValid() throws ValidationException {
        iface.validate(nodeMock);
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionInterfaceIpAddrInvalid() throws ValidationException {
        iface = new RequisitionInterface(); // Get a new instance with a null ipAddr
        iface.validate(nodeMock);
    }

    @Test
    public void testRequisitionInterfaceValidatesServices() throws ValidationException {
        RequisitionMonitoredService serviceMock = Mockito.mock(RequisitionMonitoredService.class);

        when(serviceMock.getServiceName()).thenReturn("foo");

        iface.getMonitoredServices().add(serviceMock);

        iface.validate(nodeMock);
        Mockito.verify(serviceMock).validate();
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionInterfaceValidatesServiceDuplicates() throws ValidationException {
        RequisitionMonitoredService serviceMock1 = Mockito.mock(RequisitionMonitoredService.class);
        RequisitionMonitoredService serviceMock2 = Mockito.mock(RequisitionMonitoredService.class);

        when(serviceMock1.getServiceName()).thenReturn("foo");
        when(serviceMock2.getServiceName()).thenReturn("foo");

        iface.getMonitoredServices().add(serviceMock1);
        iface.getMonitoredServices().add(serviceMock2);

        iface.validate(nodeMock);
    }

    @Test
    public void testRequisitionInterfaceExistingPrimarySNMPSameInterface() throws ValidationException {
        RequisitionInterface ifaceMock = Mockito.mock(RequisitionInterface.class);
        when(ifaceMock.getSnmpPrimary()).thenReturn(PrimaryType.PRIMARY);
        when(ifaceMock.getIpAddr()).thenReturn("0.0.0.0"); // Same as tested object, duplication check should pass
        when(nodeMock.getInterfaces()).thenReturn(Collections.singletonList(ifaceMock));

        iface.setSnmpPrimary(PrimaryType.PRIMARY);
        iface.validate(nodeMock);
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionInterfaceValidatesPrimarySNMPDuplicates() throws ValidationException {
        RequisitionInterface ifaceMock = Mockito.mock(RequisitionInterface.class);

        when(ifaceMock.getSnmpPrimary()).thenReturn(PrimaryType.PRIMARY);
        when(ifaceMock.getIpAddr()).thenReturn("1.1.1.1"); // Different from tested object, duplication check should fail
        when(nodeMock.getInterfaces()).thenReturn(Collections.singletonList(ifaceMock));

        iface.setSnmpPrimary(PrimaryType.PRIMARY);
        iface.validate(nodeMock);
    }
}
