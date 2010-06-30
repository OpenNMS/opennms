/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.LdapDetectorClient;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novell.ldap.LDAPConnection;

/**
 * <p>LdapDetector class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */

@Component
@Scope("prototype")
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
    protected LdapDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected LdapDetector(String serviceName, int port) {
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
