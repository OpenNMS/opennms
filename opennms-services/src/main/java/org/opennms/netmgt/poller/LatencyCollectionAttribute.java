//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.CollectionSetVisitor;
import org.opennms.netmgt.collectd.Persister;
import org.opennms.netmgt.collectd.ServiceParameters;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class LatencyCollectionAttribute implements CollectionAttribute {

    private LatencyCollectionResource m_resource;
    private Double m_value;
    private String m_name;
    
    public LatencyCollectionAttribute(LatencyCollectionResource resource, String name, Double value) {
        super();
        m_resource = resource;
        m_name = name;
        m_value = value;
    }

    public CollectionAttributeType getAttributeType() {
        return null;
    }

    public String getName() {
        return m_name;
    }

    public String getNumericValue() {
        return m_value.toString();
    }

    public CollectionResource getResource() {
        return m_resource;
    }

    public String getStringValue() {
        return null;
    }

    public String getType() {
        return "gauge";
    }

    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    public void storeAttribute(Persister persister) {
    }

    public void visit(CollectionSetVisitor visitor) {
    }

}
