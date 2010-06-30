//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.web.map.db.config;

/**
 * Thrown when a problem with configuration with the Maps Factories exists.
 * This error will typically be thrown when the class of a maps factory specified in
 * the property file 'appmap.properties' cannot be found or instantiated.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MapsFactoryConfigurationError extends Error {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new MapsFactoryConfigurationError with no detail mesage.
     */
    public MapsFactoryConfigurationError() {
    }

    /**
     * Create a new MapsFactoryConfigurationError with the String specified as an error message.
     *
     * @param msg   The error message for the exception.
     */
    public MapsFactoryConfigurationError(String msg) {
        super(msg);
    }

    /**
     * Create a new MapsFactoryConfigurationError with the given Exception base cause and detail message.
     *
     * @param msg   The detail message.
     * @param e     The exception to be encapsulated in a FactoryConfigurationError
     */
    public MapsFactoryConfigurationError(String msg, Exception e) {
        super(msg, e);
    }

    /**
     * Create a new MapsFactoryConfigurationError with a given Exception base cause of the error.
     *
     * @param e     The exception to be encapsulated in a FactoryConfigurationError
     */
    public MapsFactoryConfigurationError(Exception e) {
        super(e);
    }

}
