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

package org.opennms.netmgt;

/**
 * This class holds all OpenNMS related constants - has the property names to be
 * read from the config files, the various config file names etc.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 */
public final class PropertyConstants {
    /**
     * The property string in the properties file to get the XML configuration
     * files
     */
    public static final String PROP_XML_REPOSITORY = "org.opennms.bluebird.dp.xml.directory";

    /**
     * The property string in the properties file to get the Properties files
     * directory
     */
    public static final String PROP_PROPERTIES_REPOSITORY = "org.opennms.bluebird.dp.properties.directory";

    /**
     * The property string in the properties file which specifies the method to
     * use for determining which interface is primary on a multi-interface box.
     */
    public static final String PROP_PRIMARY_INTERFACE_SELECT_METHOD = "org.opennms.bluebird.dp.primaryInterfaceSelectMethod";

    /**
     * These properties control how blocking socket I/O is handled in OpenNMS
     * processes
     */
    public final static String PROP_SOCKET_TIMEOUT_REQUIRED = "org.opennms.bluebird.vmhacks.socketSoTimeoutRequired";

    /**
     * These properties control how blocking socket I/O is handled in OpenNMS
     * processes
     */
    public final static String PROP_SOCKET_TIMEOUT_PERIOD = "org.opennms.bluebird.vmhacks.socketSoTimeoutPeriod";

    /**
     * This is just a convience method that forwards the lookup of the property
     * to the {@link java.lang.System System}class. This method may be modified
     * in the furture to search additional property locations for the properties
     * defined in this class.
     *
     * @param property
     *            The property key used to lookup the result.
     * @return The value that is mapped by the passed property key.
     */
    public static String lookup(String property) {
        return System.getProperty(property);
    }
}
