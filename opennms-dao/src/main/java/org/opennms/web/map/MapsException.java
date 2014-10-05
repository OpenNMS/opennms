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

package org.opennms.web.map;


/**
 * Generic maps exception.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class MapsException extends Exception {

    private static final long serialVersionUID = 4685820627958124083L;

    /**
     * Create a new MapsException with no detail mesage.
     */
    public MapsException() {
        super();
    }

    /**
     * Create a new MapsException with the String specified as an error message.
     *
     * @param msg   The error message for the exception.
     */
    public MapsException(String msg) {
        super(msg);
    }

    /**
     * Create a new MapsException with the given Exception base cause and detail message.
     *
     * @param msg   The detail message.
     * @param e     The exception to be encapsulated in a MapsException
     */
    public MapsException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Create a new MapsException with a given Exception base cause of the exception.
     *
     * @param e     The exception to be encapsulated in a MapsException
     */
    public MapsException(Throwable e) {
        super(e);
    }

}
