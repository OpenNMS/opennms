//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//

package org.opennms.web.authenticate;

import java.security.Principal;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.users.User;


/**
 * Package protected class representing an individual user's Principal object.
 * It is built from a {@link User User} data structure.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
class OpenNMSPrincipal extends Object implements Principal 
{

    /**
     * The internal representation of all user information.
     */        
    protected User userInfo;


    /**
     * Construct a new OpenNMSPrincipal instance.
     *
     * @param username The username for this Principal
     * @param password The password for this Principal
     */
    public OpenNMSPrincipal( User userInfo ) {
        this.userInfo = userInfo;
    }


    /**
     * Return the name of this Principal.
     */
    public String getName() {
        return( this.userInfo.getUserId() );
    }


    /**
     * Return the password of this Principal.
     */
    public boolean comparePasswords( String password ) {
        return( this.userInfo.getPassword().equals(UserFactory.encryptPassword(password)) );        
    }

}

