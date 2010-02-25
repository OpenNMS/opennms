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
 * @author <A HREF="mailto:jason@opennms.org">Jason</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 */
public class EmailAddress {
    private String address;

    private String username;

    private String server;

    private String domain;

    private String tld;

    public EmailAddress() {
    }

    public EmailAddress(String newAddress) {
        address = newAddress;
        username = address.substring(0, address.indexOf("@"));

        String addressTail = address.substring(address.indexOf("@") + 1, address.length());
        StringTokenizer tokens = new StringTokenizer(addressTail, ".");

        List<String> tokenList = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            tokenList.add(tokens.nextToken());
        }

        // walk this list backward filling in TLD, domain and server
        tld = tokenList.get(tokenList.size() - 1);
        domain = tokenList.get(tokenList.size() - 2);

        if (tokenList.size() - 3 >= 0) {
            String serverParts = (String) tokenList.get(tokenList.size() - 3);
            for (int i = tokenList.size() - 4; i >= 0; i--) {
                serverParts = tokenList.get(i) + "." + serverParts;
            }
            server = serverParts;
        } else {
            server = null;
        }
    }

    public String getAddress() {
        return username + "@" + (server != null ? server + "." : "") + domain + "." + tld;
    }

    public String getUsername() {
        return username;
    }

    public String getServer() {
        return server;
    }

    public String getDomain() {
        return domain;
    }

    public String getTld() {
        return tld;
    }

    public void setUsername(String name) {
        username = name;
    }

    public void setServer(String newServer) {
        server = newServer;
    }

    public void setDomain(String newDomain) {
        domain = newDomain;
    }

    public void setTld(String newTld) {
        tld = newTld;
    }
}
