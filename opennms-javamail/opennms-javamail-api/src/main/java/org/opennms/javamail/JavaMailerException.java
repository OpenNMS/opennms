/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.javamail;

/**
 * Exception used to create proper return code
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @version $Id: $
 */
public class JavaMailerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5889585419229061711L;

    /**
     * <p>Constructor for JavaMailerException.</p>
     */
    public JavaMailerException() {
        super();
    }

    /**
     * <p>Constructor for JavaMailerException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public JavaMailerException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for JavaMailerException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public JavaMailerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for JavaMailerException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public JavaMailerException(Throwable cause) {
        super(cause);
    }

}
