/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.opennms.netmgt.config.collector.AttributeDefinition;
import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BasePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BasePersister extends AbstractCollectionSetVisitor implements Persister {
    
    private static final Logger LOG = LoggerFactory.getLogger(BasePersister.class);
    
    private boolean m_ignorePersist = false;
    private ServiceParameters m_params;
    private RrdRepository m_repository;
    private LinkedList<Boolean> m_stack = new LinkedList<Boolean>();
    private PersistOperationBuilder m_builder;

    /**
     * <p>Constructor for BasePersister.</p>
     */
    public BasePersister() {
        super();
    }

    /**
     * <p>Constructor for BasePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public BasePersister(ServiceParameters params, RrdRepository repository) {
        super();
        m_params = params;
        m_repository = repository;
    }
    
    /**
     * <p>commitBuilder</p>
     */
    protected void commitBuilder() {
        if (isPersistDisabled())
            return;
        String name = m_builder.getName();
        try {
            m_builder.commit();
            m_builder = null;
        } catch (RrdException e) {
            LOG.error("Unable to persist data for {}", name, e);
    
        }
    }

    private boolean isPersistDisabled() {
        return m_params != null &&
               m_params.getParameters().containsKey("storing-enabled") &&
               m_params.getParameters().get("storing-enabled").equals("false");
    }

    /** {@inheritDoc} */
    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        popShouldPersist();
    }

    /** {@inheritDoc} */
    @Override
    public void completeGroup(AttributeGroup group) {
        popShouldPersist();
    }

    /** {@inheritDoc} */
    @Override
    public void completeResource(CollectionResource resource) {
        popShouldPersist();
    }
    
    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeType a {@link org.opennms.netmgt.config.collector.AttributeDefinition} object.
     */
    protected void createBuilder(CollectionResource resource, String name, AttributeDefinition attributeType) {
        createBuilder(resource, name, Collections.singleton(attributeType));
    }

    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeTypes a {@link java.util.Set} object.
     */
    protected void createBuilder(CollectionResource resource, String name, Set<AttributeDefinition> attributeTypes) {
        m_builder = new PersistOperationBuilder(getRepository(), resource, name);
        if (resource.getTimeKeeper() != null)
            m_builder.setTimeKeeper(resource.getTimeKeeper());
        for (Iterator<AttributeDefinition> iter = attributeTypes.iterator(); iter.hasNext();) {
            AttributeDefinition attrType = iter.next();
            if (attrType instanceof NumericAttributeType) {
                m_builder.declareAttribute(attrType);
            }
        }
    }

    /**
     * <p>getRepository</p>
     *
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public RrdRepository getRepository() {
        return m_repository;
    }

    /**
     * <p>setRepository</p>
     *
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public void setRepository(RrdRepository repository) {
        m_repository = repository;
    }

    /** {@inheritDoc} */
    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
	LOG.debug("Persisting {} {}", attribute, (isIgnorePersist() ? ". Ignoring value because of sysUpTime changed" : ""));
    	String value = isIgnorePersist() ? "U" : attribute.getNumericValue();
        m_builder.setAttributeValue(attribute.getAttributeType(), value);
        m_builder.setAttributeMetadata(attribute.getMetricIdentifier(), attribute.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
            LOG.debug("Persisting {}", attribute);
            CollectionResource resource = attribute.getResource();
            String value = attribute.getStringValue();
    
            File resourceDir = resource.getResourceDir(getRepository());
    
            //String attrVal = (value == null ? null : value.toString());
            //if (attrVal == null) {
            if (value == null) {
                LOG.info("No data collected for attribute {}.  Skipping.", attribute);
                return;
            }
            String attrName = attribute.getName();
            try {
                ResourceTypeUtils.updateStringProperty(resourceDir, value, attrName);
            } catch(IOException e) {
                LOG.error("Unable to save string attribute {}", attribute, e);
            }
    }

    private boolean pop() {
        boolean top = top();
        m_stack.removeLast();
        return top;
    }

    /**
     * <p>popShouldPersist</p>
     *
     * @return a boolean.
     */
    protected boolean popShouldPersist() {
        return pop();
    }
    
    private void push(boolean b) {
        m_stack.addLast(Boolean.valueOf(b));
    }

    /**
     * <p>pushShouldPersist</p>
     *
     * @param attribute a {@link org.opennms.netmgt.config.collector.CollectionAttribute} object.
     */
    protected void pushShouldPersist(CollectionAttribute attribute) {
        pushShouldPersist(attribute.shouldPersist(m_params));
    }

    /**
     * <p>pushShouldPersist</p>
     *
     * @param group a {@link org.opennms.netmgt.config.collector.AttributeGroup} object.
     */
    protected void pushShouldPersist(AttributeGroup group) {
        pushShouldPersist(group.shouldPersist(m_params));
    }

    private void pushShouldPersist(boolean shouldPersist) {
        push(top() && shouldPersist);
    }

    /**
     * <p>pushShouldPersist</p>
     *
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    protected void pushShouldPersist(CollectionResource resource) {
        push(resource.shouldPersist(m_params));
    }

    /**
     * <p>shouldPersist</p>
     *
     * @return a boolean.
     */
    protected boolean shouldPersist() { return top(); }

    /**
     * <p>storeAttribute</p>
     *
     * @param attribute a {@link org.opennms.netmgt.config.collector.CollectionAttribute} object.
     */
    protected void storeAttribute(CollectionAttribute attribute) {
        if (shouldPersist()) {
            attribute.storeAttribute(this);
            LOG.debug("Storing attribute {}", attribute);
        } else {
            LOG.debug("Not persisting attribute {} because shouldPersist is false", attribute);
        }
    }
    
    private boolean top() {
        return m_stack.getLast();
    }

    /** {@inheritDoc} */
    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        pushShouldPersist(attribute);
        storeAttribute(attribute);
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
    }

    /** {@inheritDoc} */
    @Override
    public void visitResource(CollectionResource resource) {
        LOG.info("Persisting data for resource {}", resource);
        pushShouldPersist(resource);
    }

	/**
	 * <p>isIgnorePersist</p>
	 *
	 * @return a boolean.
	 */
	public boolean isIgnorePersist() {
		return m_ignorePersist;
	}

	/**
	 * <p>setIgnorePersist</p>
	 *
	 * @param ignore a boolean.
	 */
	public void setIgnorePersist(boolean ignore) {
		m_ignorePersist = ignore;
	}

    /**
     * <p>getBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.PersistOperationBuilder} object.
     */
    public PersistOperationBuilder getBuilder() {
        return m_builder;
    }

}
