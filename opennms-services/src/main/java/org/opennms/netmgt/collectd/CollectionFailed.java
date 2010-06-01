//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 29: Copied from CollectionWarning. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

public class CollectionFailed extends CollectionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3306639630332715369L;

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
}
