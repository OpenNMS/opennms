/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.activemq.auth;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class OpenNMSJaasBrokerPluginTest {

    @Test
    public void canGetAddressFromConnectionString() throws UnknownHostException {
        assertEquals(InetAddress.getByName("127.0.0.1"),
                OpenNMSJaasAuthenticationBroker.getAddressFromConnectionString("tcp://127.0.0.1:8888"));
        assertEquals(InetAddress.getByName("::1"),
                OpenNMSJaasAuthenticationBroker.getAddressFromConnectionString("tcp://0:0:0:0:0:0:0:1:40730"));
        assertEquals(null, OpenNMSJaasAuthenticationBroker.getAddressFromConnectionString("not-tcp://some-string"));
    }
}
