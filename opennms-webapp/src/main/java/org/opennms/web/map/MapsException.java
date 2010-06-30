/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 28, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map;


/**
 * Generic maps exception.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class MapsException extends Exception {
    private static final long serialVersionUID = 1L;

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
    public MapsException(String msg, Exception e) {
        super(msg, e);
    }

    /**
     * Create a new MapsException with a given Exception base cause of the exception.
     *
     * @param e     The exception to be encapsulated in a MapsException
     */
    public MapsException(Exception e) {
        super(e);
    }

}
