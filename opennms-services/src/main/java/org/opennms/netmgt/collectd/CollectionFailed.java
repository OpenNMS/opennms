/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.collectd;

public class CollectionFailed extends CollectionException {
    private static final long serialVersionUID = 1L;

    public CollectionFailed() {
        super();
    }

    public CollectionFailed(int code) {
        super("Collection failed for an unknown reason (code " + code + ".  Please review previous logs for this thread for details.  You can also open up an enhancement bug report (include your logs) to request that failure messages are logged for this type of error.");
    }

    public CollectionFailed(String message) {
        super(message);
    }

    public CollectionFailed(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionFailed(Throwable cause) {
        super(cause);
    }

    protected void logError() {
        if (getCause() == null) {
            log().warn(getMessage());
        } else {
            log().warn(getMessage(), getCause());
        }
    }
}
