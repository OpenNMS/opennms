/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.internal.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.config.api.collection.IExpression;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCollectionConfigConverter extends AbstractDatacollectionConfigVisitor {
    private static Logger LOG = LoggerFactory.getLogger(DataCollectionConfigConverter.class);

    private DataCollectionConfigImpl m_config = new DataCollectionConfigImpl();

    private SnmpCollectionImpl m_currentSnmpCollection = null;
    private DataCollectionGroupImpl m_currentDataCollectionGroup = null;
    private String m_groupName = null;
    private GroupImpl m_currentGroup = null;
    private TableImpl m_currentTable = null;
    private SystemDefImpl m_currentSystemDef = null;
    
    private Map<String,ColumnImpl> m_columns = new HashMap<String,ColumnImpl>();
    private Map<String,MibObjectImpl> m_mibs = new HashMap<String,MibObjectImpl>();

    public DataCollectionConfigImpl getDataCollectionConfig() {
        return m_config;
    }

    @Override
    public void visitSnmpCollection(final SnmpCollection collection) {
        //LOG.debug("visitSnmpCollection({})", collection);

        final ResourceTypeImpl ifIndexResourceType = new ResourceTypeImpl("ifIndex", "Interfaces (MIB-2 ifTable)");
        ifIndexResourceType.setResourceNameTemplate("${ifDescr}-${ifPhysAddr}");
        ifIndexResourceType.setResourceLabelTemplate("${ifDescr}-${ifPhysAddr}");
        ifIndexResourceType.setResourceKindTemplate("${ifType}");
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.2.2.1.2", "ifDescr", "string");
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.2.2.1.6", "ifPhysAddr", "string", "1x:"); 
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.2.2.1.3", "ifType", "string");
        ifIndexResourceType.addColumn(".1.3.6.1.2.1.31.1.1.1.1", "ifName", "string");

        m_currentSnmpCollection = new SnmpCollectionImpl(collection.getName());
        m_currentDataCollectionGroup = new DataCollectionGroupImpl(collection.getName() + "-all");
        m_currentSnmpCollection.addDataCollectionGroup(m_currentDataCollectionGroup);
        
        m_currentDataCollectionGroup.addResourceType(ifIndexResourceType);
    }

    @Override
    public void visitSnmpCollectionComplete() {
        m_config.addSnmpCollection(m_currentSnmpCollection);
        m_currentDataCollectionGroup = null;
        m_currentSnmpCollection = null;
    }

    @Override
    public void visitIncludeCollection(final IncludeCollection includeCollection) {
        m_currentSnmpCollection.addIncludedGroup(includeCollection.getDataCollectionGroup());
    }

    @Override
    public void visitGroup(final Group group) {
        //LOG.debug("visitGroup({})", group);
        m_groupName = group.getName();
    }

    @Override
    public void visitGroupComplete() {
        //LOG.debug("visitGroupComplete()");
        if (m_currentTable != null) {
            m_currentDataCollectionGroup.addTable(m_currentTable);
            m_currentTable = null;
        } else if (m_currentGroup != null) {
            m_currentDataCollectionGroup.addGroup(m_currentGroup);
            m_currentGroup = null;
        } else {
            LOG.warn("No table or group in-process!");
        }
        m_groupName = null;
    }

    @Override
    public void visitMibObj(final MibObj mibObj) {
        //LOG.debug("visitMibObj({})", mibObj);
        final String instance = mibObj.getInstance();
        if (m_currentTable == null && m_currentGroup == null) {
            if (m_groupName == null) {
                throw new IllegalStateException("visitMibObj called, but no current group name has been set!");
            }
            try {
                Integer.parseInt(instance, 10);
                // this has an instance number, so it's a normal "group"
                m_currentGroup = new GroupImpl(m_groupName);
            } catch (final NumberFormatException e) {
                // this has an instance string, so it's a table
                m_currentTable = new TableImpl(m_groupName, instance);
            }
        }
        
        if (m_currentTable != null) {
            final ColumnImpl column = new ColumnImpl(mibObj.getOid(), mibObj.getAlias(), mibObj.getType());
            m_currentTable.addColumn(column);
            m_columns.put(column.getAlias(), column);
        } else if (m_currentGroup != null) {
            final MibObjectImpl obj = new MibObjectImpl();
            obj.setOid(mibObj.getOid());
            obj.setInstance(instance);
            obj.setAlias(mibObj.getAlias());
            obj.setType(mibObj.getType());
            m_currentGroup.addMibObject(obj);
            m_mibs.put(obj.getAlias(), obj);
        } else {
            throw new IllegalStateException("visitMibObj called, but no current table or group initialized!");
        }
    }

    @Override
    public void visitSystemDef(final SystemDef systemDef) {
        //LOG.debug("visitSystemDef({})", systemDef);
        final SystemDefImpl def = new SystemDefImpl(systemDef.getName());
        if (systemDef.getSysoid() != null) {
            def.setSysoid(systemDef.getSysoid());
        } else if (systemDef.getSysoidMask() != null) {
            def.setSysoidMask(systemDef.getSysoidMask());
        }
        m_currentSystemDef = def;
    }

    @Override
    public void visitSystemDefComplete() {
        m_currentDataCollectionGroup.addSystemDef(m_currentSystemDef);
        m_currentSystemDef = null;
    }

    @Override
    public void visitIpList(final IpList ipList) {
        LOG.debug("visitIpList({})", ipList);
    }

    @Override
    public void visitCollect(final Collect collect) {
        //LOG.debug("visitCollect({})", collect);
        if (m_currentSystemDef == null) {
            throw new IllegalStateException("visitCollect called, but we don't have a current SystemDefImpl in-progress!");
        }
        m_currentSystemDef.setIncludes(collect.getIncludeGroups().toArray(new String[0]));
    }

    @Override
    public void visitResourceType(final ResourceType resourceType) {
        //LOG.debug("visitResourceType({})", resourceType);
        final ResourceTypeImpl type = new ResourceTypeImpl(resourceType.getName(), resourceType.getLabel());
        if (resourceType.getResourceLabel() != null && !resourceType.getResourceLabel().trim().isEmpty()) {
            type.setResourceLabelTemplate(resourceType.getResourceLabel());
        }
        final String strategy = resourceType.getStorageStrategy().getClazz();
        if (strategy.endsWith(".SiblingColumnStorageStrategy")) {
            for (final Parameter parameter : resourceType.getStorageStrategy().getParameters()) {
                if ("sibling-column-name".equals(parameter.getKey())) {
                    final String name = parameter.getValue();
                    if (name != null && !name.trim().isEmpty()) {
                        type.setResourceNameTemplate("${" + name + "}");
                        break;
                    }
                }
            }
        } else if (strategy.endsWith(".IndexStorageStrategy")) {
            type.setResourceNameTemplate("${index}");
            if (type.getResourceLabelExpression() == null || type.getResourceLabelExpression().getTemplate() == null) {
                type.setResourceLabelTemplate("${index}");
            }
        }
        
        final Set<String> parameters = new TreeSet<>();
        parameters.addAll(getParameters(type.getResourceKindExpression()));
        parameters.addAll(getParameters(type.getResourceLabelExpression()));
        parameters.addAll(getParameters(type.getResourceNameExpression()));

        for (final String parameter : parameters) {
            if (m_columns.containsKey(parameter)) {
                final ColumnImpl column = m_columns.get(parameter);
                //LOG.debug("adding column {} to resource type {}", column, type.getTypeName());
                type.addColumn(column);
            } else if (m_mibs.containsKey(parameter)) {
                final MibObjectImpl mib = m_mibs.get(parameter);
                //LOG.debug("adding mib {} to resource type {}", mib, type.getTypeName());
                type.addColumn(new ColumnImpl(mib));
            } else {
                if (!"index".equals(parameter)) {
                    LOG.warn("Unable to locate column/mibObject for parameter: {}", parameter);
                }
            }
        }
        
        m_currentDataCollectionGroup.addResourceType(type);
    }

    static Collection<String> getParameters(final IExpression expression) {
        if (expression == null) {
            return Collections.emptySet();
        }
        final String template = expression.getTemplate();
        if (template == null) {
            return Collections.emptySet();
        }

        final Pattern p = Pattern.compile("\\$\\{([^\\}]*)\\}");
        final Matcher m = p.matcher(template);
        final Set<String> matches = new TreeSet<>();
        while (m.find()) {
            for (int i=0; i < m.groupCount(); i++) {
                matches.add(m.group(i+1));
            }
        }
        return matches;
    }

}
