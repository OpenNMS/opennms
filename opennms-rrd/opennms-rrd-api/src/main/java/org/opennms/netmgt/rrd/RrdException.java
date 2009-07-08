/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.rrd;

/**
 * This exception indicates an error has occurred creating, updating, or
 * fetching data from an Rrd file
 * 
 * @author brozow
 */
public class RrdException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5251168945484451493L;

    /**
     * 
     */
    public RrdException() {
        super();
        // FIXME Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public RrdException(String message) {
        super(message);
        // FIXME Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public RrdException(String message, Throwable cause) {
        super(message, cause);
        // FIXME Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public RrdException(Throwable cause) {
        super(cause);
        // FIXME Auto-generated constructor stub
    }

}
