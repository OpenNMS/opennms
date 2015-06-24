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

package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;

/**
 * Signals that an attempt to obtain the node denoted by a specified identifier has failed.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class VElementNotChildException extends MapsException {
    /**
     * 
     */
    private static final long serialVersionUID = 1534658567478899130L;

    /**
     * Create a new NetworkNodeNotFoundException with no detail message.
     */
    public VElementNotChildException() {
        super();
    }

    /**
     * Create a new NetworkNodeNotFoundException with the String specified as an error message.
     *
     * @param msg   The error message for the exception.
     */
    public VElementNotChildException(String msg) {
        super(msg);
    }

}
