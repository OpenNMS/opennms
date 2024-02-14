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
package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.LdapDetectorClient;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;

import com.novell.ldap.LDAPConnection;

/**
 * <p>LdapDetector class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */

public class LdapDetector extends LineOrientedDetector {
    
    private static final String DEFAULT_SERVICE_NAME = "LDAP";
    /**
     * <P>
     * The default ports on which the host is checked to see if it supports
     * LDAP.
     * </P>
     */
    private static final int DEFAULT_PORT = LDAPConnection.DEFAULT_PORT;

    /**
     * Default constructor
     */
    public LdapDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public LdapDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        //expectClose();
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, LineOrientedResponse> getClient(){
        return new LdapDetectorClient();
        
    }

}
