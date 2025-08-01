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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.network.IPAddress;
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
        when(ifaceMock.getIpAddr()).thenReturn(new IPAddress("0.0.0.0").toInetAddress()); // Same as tested object, duplication check should pass
        when(nodeMock.getInterfaces()).thenReturn(Collections.singletonList(ifaceMock));

        iface.setSnmpPrimary(PrimaryType.PRIMARY);
        iface.validate(nodeMock);
    }

    @Test(expected = ValidationException.class)
    public void testRequisitionInterfaceValidatesPrimarySNMPDuplicates() throws ValidationException {
        RequisitionInterface ifaceMock = Mockito.mock(RequisitionInterface.class);

        when(ifaceMock.getSnmpPrimary()).thenReturn(PrimaryType.PRIMARY);
        when(ifaceMock.getIpAddr()).thenReturn(new IPAddress("1.1.1.1").toInetAddress()); // Different from tested object, duplication check should fail
        when(nodeMock.getInterfaces()).thenReturn(Collections.singletonList(ifaceMock));

        iface.setSnmpPrimary(PrimaryType.PRIMARY);
        iface.validate(nodeMock);
    }
}
