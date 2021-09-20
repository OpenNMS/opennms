/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.camel;

import org.junit.Test;
import org.opennms.core.rpc.echo.EchoClient;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EchoRpcBlueprintIT extends CamelBlueprintTest {

    public static final String REMOTE_LOCATION_NAME = "remote";

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private EchoClient echoClient;

    @Test(timeout=60000)
    public void canExecuteRpcViaCurrentLocation() throws Exception {
        // Execute a request via the current location
        EchoRequest request = new EchoRequest("HELLO!");
        EchoResponse expectedResponse = new EchoResponse("HELLO!");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(timeout=60000)
    public void canExecuteRpcViaRemoteLocation() throws Exception {
        // Execute a request via a remote location
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());
        EchoRequest request = new EchoRequest("HELLO!!!");
        request.setLocation(REMOTE_LOCATION_NAME);
        EchoResponse expectedResponse = new EchoResponse("HELLO!!!");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }
}
