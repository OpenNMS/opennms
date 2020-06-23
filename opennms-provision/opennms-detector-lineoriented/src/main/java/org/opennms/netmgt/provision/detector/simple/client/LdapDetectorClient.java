/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.utils.DefaultSocketWrapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.SocketWrapper;
import org.opennms.core.utils.TimeoutSocketFactory;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPSocketFactory;

/**
 * <p>LdapDetectorClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class LdapDetectorClient extends LineOrientedClient {

    /**
     * A class to add a timeout to the socket that the LDAP code uses to access
     * an LDAP server
     */
    private class TimeoutLDAPSocket extends TimeoutSocketFactory implements LDAPSocketFactory {
        public TimeoutLDAPSocket(int timeout) {
            super(timeout, getSocketWrapper());
        }
    }

    protected SocketWrapper getSocketWrapper() {
        return new DefaultSocketWrapper();
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        super.connect(address, port, timeout);
        final LDAPConnection lc = new LDAPConnection(new TimeoutLDAPSocket(timeout));
        lc.connect(InetAddressUtils.str(address), port);
    }
}
