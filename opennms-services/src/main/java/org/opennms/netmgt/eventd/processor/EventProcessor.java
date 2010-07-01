//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 27: Created this file. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.eventd.processor;

import java.sql.SQLException;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;


/**
 * Event processor interface.  Classes that want to modify or react to
 * events within eventd implement this interface and are dependency
 * injected into the eventProcessors List in EventHandler.
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventProcessor {
    /**
     * <p>process</p>
     *
     * @param eventHeader a {@link org.opennms.netmgt.xml.event.Header} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws java.sql.SQLException if any.
     */
    public void process(Header eventHeader, Event event) throws SQLException;
}
