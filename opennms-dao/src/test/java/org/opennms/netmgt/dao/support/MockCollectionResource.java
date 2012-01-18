/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

/**
 * MockCollectionResource
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockCollectionResource implements CollectionResource {
    
    private String parent;
    private String instance;
    private String type;
    private Map<String,String> attributes = new HashMap<String,String>();
    
    public MockCollectionResource(String parent, String instance, String type) {
        this.parent = parent;
        this.instance = instance;
        this.type = type;
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public File getResourceDir(RrdRepository repository) {
        return null;
    }

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return false;
    }

    @Override
    public boolean rescanNeeded() {
        return false;
    }

    @Override
    public void visit(CollectionSetVisitor visitor) {
        for (Entry<String,String> entry : attributes.entrySet()) {
            final CollectionResource resource = this;
            final String attrName = entry.getKey();
            final String attrValue = entry.getValue();
            CollectionAttribute attribute = new CollectionAttribute() {
                public CollectionResource getResource() { return resource; }
                public String getStringValue() { return attrValue; }
                public String getNumericValue() { return attrValue; }
                public String getName() { return attrName; }
                public void storeAttribute(Persister persister) {}
                public boolean shouldPersist(ServiceParameters params) { return true; }
                public CollectionAttributeType getAttributeType() { return null; }
                public void visit(CollectionSetVisitor visitor) { }
                public String getType() { return "string"; }
            };
            visitor.visitAttribute(attribute);
        }
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getResourceTypeName() {
        return type;
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public String getInstance() {
        return instance;
    }
    
    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public String getLabel() {
        return null;
    }
    
    public Map<String,String> getAttribtueMap() {
        return attributes;
    }

    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
