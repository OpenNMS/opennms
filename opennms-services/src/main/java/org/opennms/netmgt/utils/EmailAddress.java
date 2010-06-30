//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is used to hold an email address
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @version $Id: $
 */
public class EmailAddress {
    private String address;

    private String username;

    private String server;

    private String domain;

    private String tld;

    /**
     * <p>Constructor for EmailAddress.</p>
     */
    public EmailAddress() {
    }

    /**
     * <p>Constructor for EmailAddress.</p>
     *
     * @param newAddress a {@link java.lang.String} object.
     */
    public EmailAddress(String newAddress) {
        address = newAddress;
        username = address.substring(0, address.indexOf("@"));

        String addressTail = address.substring(address.indexOf("@") + 1, address.length());
        StringTokenizer tokens = new StringTokenizer(addressTail, ".");

        List tokenList = new ArrayList();
        while (tokens.hasMoreTokens()) {
            tokenList.add(tokens.nextToken());
        }

        // walk this list backward filling in tld, domain and server
        tld = (String) tokenList.get(tokenList.size() - 1);
        domain = (String) tokenList.get(tokenList.size() - 2);

        if (tokenList.size() - 3 >= 0) {
            String serverParts = (String) tokenList.get(tokenList.size() - 3);
            for (int i = tokenList.size() - 4; i >= 0; i--) {
                serverParts = (String) tokenList.get(i) + "." + serverParts;
            }
            server = serverParts;
        } else {
            server = null;
        }
    }

    /**
     * <p>Getter for the field <code>address</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAddress() {
        return username + "@" + (server != null ? server + "." : "") + domain + "." + tld;
    }

    /**
     * <p>Getter for the field <code>username</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUsername() {
        return username;
    }

    /**
     * <p>Getter for the field <code>server</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServer() {
        return server;
    }

    /**
     * <p>Getter for the field <code>domain</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * <p>Getter for the field <code>tld</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTld() {
        return tld;
    }

    /**
     * <p>Setter for the field <code>username</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setUsername(String name) {
        username = name;
    }

    /**
     * <p>Setter for the field <code>server</code>.</p>
     *
     * @param newServer a {@link java.lang.String} object.
     */
    public void setServer(String newServer) {
        server = newServer;
    }

    /**
     * <p>Setter for the field <code>domain</code>.</p>
     *
     * @param newDomain a {@link java.lang.String} object.
     */
    public void setDomain(String newDomain) {
        domain = newDomain;
    }

    /**
     * <p>Setter for the field <code>tld</code>.</p>
     *
     * @param newTld a {@link java.lang.String} object.
     */
    public void setTld(String newTld) {
        tld = newTld;
    }
}
