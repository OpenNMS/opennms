//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess“
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.domain;

import java.io.Serializable;
import java.util.Map;

import org.opennms.acl.util.Constants;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.User;

/**
 * This entity is a ACL application user, extends the {@link org.springframework.security.userdetails.User} to use authentication and authorization Spring Security infrastructure.
 * 
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@SuppressWarnings("serial")
public class AclUser extends User implements Serializable {

    /**
     * Entity that represents an acl application user. Used by Spring Security during authentication. In this constructor you can add additional custom information to AclUser
     * 
     * @param username
     * @param password
     * @param isEnabled
     * @param authorities
     * @param userInfo
     */
    public AclUser(String username, String password, boolean isEnabled, GrantedAuthority[] authorities, Map<String, ?> userInfo) {
        super(username, password, isEnabled, true, true, true, authorities);
        this.userInfo = userInfo;
    }

    /**
     * The additional custom informations kept in AclUser
     * 
     * @return
     */
    public Map<String, ?> getUserInfo() {
        return userInfo;
    }

    /**
     * The AclUser's unique identifier
     * 
     * @return
     */
    public long getId() {
        return new Long(userInfo.get(Constants.USER_SID).toString());
    }

    private Map<String, ?> userInfo;
}
