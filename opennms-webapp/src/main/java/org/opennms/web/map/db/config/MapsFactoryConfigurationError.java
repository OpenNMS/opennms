/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    /**
     * 
     */
    private static final long serialVersionUID = -173670914545632388L;

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
