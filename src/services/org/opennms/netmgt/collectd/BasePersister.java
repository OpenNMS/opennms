//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.snmp.SnmpValue;

public class BasePersister extends AbstractCollectionSetVisitor implements Persister {

    private ServiceParameters m_params;
    private RrdRepository m_repository;
    private LinkedList stack = new LinkedList();
    private PersistOperationBuilder m_builder;

    public BasePersister() {
        super();
    }

    public BasePersister(ServiceParameters params) {
        m_params = params;
        m_repository = new RrdRepository(params.getCollectionName());
    }

    public void visitResource(CollectionResource resource) {
        push(resource.shouldPersist(m_params));
    }

    protected void push(boolean b) {
        stack.addLast(Boolean.valueOf(b));
    }

    protected boolean top() {
        return ((Boolean)stack.getLast()).booleanValue();
    }

    protected boolean pop() {
        boolean top = top();
        stack.removeLast();
        return top;
    }

    protected void saveUpdatedProperties(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(propertiesFile);
            props.store(fileOutputStream, null);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                log().error("performUpdate: Error closing file.", e);
            }
        }
    }

    protected Properties getCurrentProperties(File propertiesFile) throws FileNotFoundException, IOException {
        Properties props = new Properties();
    
        FileInputStream fileInputStream = null;
        //Preload existing data
        if (propertiesFile.exists()) {
            try {
                fileInputStream = new FileInputStream(propertiesFile);
                props.load(fileInputStream);
            } finally {
                try {
                    if (fileInputStream != null) fileInputStream.close();
                } catch (IOException e) {
                    log().error("performUpdate: Error closing file.", e);
                }
            }
        }
        return props;
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void completeResource(CollectionResource resource) {
        pop();
    }

    public void visitGroup(AttributeGroup group) {
        boolean shouldPersist = top() && group.shouldPersist(m_params);
        push(shouldPersist);
    }

    public void completeGroup(AttributeGroup group) {
        pop();
    }

    public void visitAttribute(Attribute attribute) {
        if (top() && attribute.shouldPersist(m_params))
            attribute.storeAttribute(this);
    }

    public void persistNumericAttribute(Attribute attribute) {
        m_builder.setAttributeValue(attribute.getAttributeType(), attribute.getNumericValue());
    }

    public void persistStringAttribute(Attribute attribute) {
        try {
            RrdRepository repository = m_repository;
            CollectionResource resource = attribute.getResource();
            SnmpValue value = attribute.getValue();
    
            File resourceDir = resource.getResourceDir(repository);
    
            String val = (value == null ? null : value.toString());
            if (val == null) {
                log().info("No data collected for attribute "+attribute+". Skipping");
                return;
            }
            File propertiesFile = new File(resourceDir,"strings.properties");
            Properties props = getCurrentProperties(propertiesFile);
            props.setProperty(attribute.getName(), val);
            saveUpdatedProperties(propertiesFile, props);
        } catch(IOException e) {
            log().error("Unable to save string attribute "+attribute, e);
        }
    }

    protected void createBuilder(CollectionResource resource, String name, Set attributeTypes) {
        m_builder = new PersistOperationBuilder(m_repository, resource, name);
        for (Iterator iter = attributeTypes.iterator(); iter.hasNext();) {
            AttributeType attrType = (AttributeType) iter.next();
            m_builder.declareAttribute(attrType);
        }
    }
    
    protected void createBuilder(CollectionResource resource, String name, AttributeType attributeType) {
        createBuilder(resource, name, Collections.singleton(attributeType));
    }

    protected void commitBuilder() {
        String name = m_builder.getName();
        try {
            m_builder.commit();
            m_builder = null;
        } catch (RrdException e) {
            log().error("Unable to persist data for group "+name, e);
    
        }
    }

}