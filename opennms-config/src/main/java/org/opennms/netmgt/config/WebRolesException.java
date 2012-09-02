/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

/**
 * <p>WebRolesException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebRolesException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 7520091278102237241L;

    /**
     * <p>Constructor for WebRolesException.</p>
     */
    public WebRolesException() {
        super();
    }

    /**
     * <p>Constructor for WebRolesException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public WebRolesException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for WebRolesException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public WebRolesException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for WebRolesException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public WebRolesException(Throwable cause) {
        super(cause);
    }

}
