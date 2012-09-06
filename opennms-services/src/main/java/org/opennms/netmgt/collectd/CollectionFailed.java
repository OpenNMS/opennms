/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

/**
 * <p>CollectionFailed class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectionFailed extends CollectionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3306639630332715369L;

    /**
     * <p>Constructor for CollectionFailed.</p>
     *
     * @param code a int.
     */
    public CollectionFailed(int code) {
        super("Collection failed for an unknown reason (code " + code + ".  Please review previous logs for this thread for details.  You can also open up an enhancement bug report (include your logs) to request that failure messages are logged for this type of error.");
    }

}
