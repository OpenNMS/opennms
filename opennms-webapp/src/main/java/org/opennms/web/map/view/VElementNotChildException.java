/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;

/**
 * Signals that an attempt to obtain the node denoted by a specified identifier has failed.
 */
public class VElementNotChildException extends MapsException {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new NetworkNodeNotFoundException with no detail message.
     */
    public VElementNotChildException() {
        super();
    }

    /**
     * Create a new NetworkNodeNotFoundException with the String specified as an error message.
     * @param msg   The error message for the exception.
     */
    public VElementNotChildException(String msg) {
        super(msg);
    }

}
