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
//      http://www.blast.com/
//

package org.opennms.web.authenticate;

import org.apache.log4j.Category;


/**
 * An uninstantiatable class that provides a servlet container-independent
 * interface to the authentication system and a list of useful constants.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public final class Authentication extends Object
{
    /** The Log4J category for logging web authentication messages. */
    public static Category log = Category.getInstance("OpenNMS.WEB.AUTH");
    
    /** The name of the role defined for users in the web.xml file. */
    public static final String USER_ROLE  = "OpenNMS User";

    /** The name of the role defined for administrators in the web.xml file. */
    public static final String ADMIN_ROLE = "OpenNMS Administrator";

    /** Private, empty constructor so this class cannot be instantiated. */
    private Authentication() {}

}
