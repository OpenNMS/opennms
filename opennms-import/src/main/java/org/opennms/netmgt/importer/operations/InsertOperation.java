/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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


package org.opennms.netmgt.importer.operations;

import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.xml.event.Event;

public class InsertOperation extends AbstractSaveOrUpdateOperation {
    
    public InsertOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city) {
		super(foreignSource, foreignId, nodeLabel, building, city);
	}

	public List<Event> doPersist() {
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        getNode().setDistPoller(distPoller);
        getNodeDao().save(getNode());
        
    	final List<Event> events = new LinkedList<Event>();

    	EntityVisitor eventAccumlator = new AddEventVisitor(events);

    	getNode().visit(eventAccumlator);
        
    	return events;
    }

    public String toString() {
	return "INSERT: Node: "+getNode().getLabel();
    }

}
