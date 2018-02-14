/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.monitor;

import java.net.InetSocketAddress;

import org.opennms.test.mock.MockUtil;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

public class MockRadiusServer extends RadiusServer {
    @Override
    public String getSharedSecret(InetSocketAddress arg0) {
        return "testing123";
    }

    @Override
    public String getUserPassword(String usename) {
        return "password";
    }

    @Override
    public RadiusPacket accessRequestReceived(AccessRequest ar,
            InetSocketAddress client) throws RadiusException {
        MockUtil.println(ar.getAuthProtocol());
        return super.accessRequestReceived(ar, client);
    }

    @Override
    public void start(boolean hasAuth, boolean hasAcct) {
        MockUtil.println("Mock radius server starting");
        super.start(hasAuth, hasAcct);
    }

    @Override
    public void stop() {
        MockUtil.println("Stopping Radius server");
        super.stop();
    }

}
