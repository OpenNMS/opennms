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
