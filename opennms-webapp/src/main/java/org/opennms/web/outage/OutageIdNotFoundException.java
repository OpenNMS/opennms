//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
//
// Original Code Base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.outage;

/**
 * <p>OutageIdNotFoundException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class OutageIdNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected String badId;

    protected String message;

    /**
     * <p>Constructor for OutageIdNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param id a {@link java.lang.String} object.
     */
    public OutageIdNotFoundException(String msg, String id) {
        this.message = msg;
        this.badId = id;
    }

    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * <p>getBadID</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBadID() {
        return this.badId;
    }
}
