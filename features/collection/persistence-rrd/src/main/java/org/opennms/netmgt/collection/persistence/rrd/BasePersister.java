/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.rrd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BasePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BasePersister extends AbstractCollectionSetVisitor implements Persister {
    
    protected static final Logger LOG = LoggerFactory.getLogger(BasePersister.class);
    
    private boolean m_ignorePersist = false;
    private ServiceParameters m_params;
    private RrdRepository m_repository;
    private final LinkedList<Boolean> m_stack = new LinkedList<Boolean>();
    private PersistOperationBuilder m_builder;

    /**
     * <p>Constructor for BasePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public BasePersister(ServiceParameters params, RrdRepository repository) {
        super();
        m_params = params;
        m_repository = repository;
    }
    
    /**
     * <p>commitBuilder</p>
     */
    public void commitBuilder() {
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
               "false".equals(m_params.getParameters().get("storing-enabled"));
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
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeType a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     */
    public void createBuilder(CollectionResource resource, String name, CollectionAttributeType attributeType) {
        createBuilder(resource, name, Collections.singleton(attributeType));
    }

    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeTypes a {@link java.util.Set} object.
     */
    protected void createBuilder(CollectionResource resource, String name, Set<CollectionAttributeType> attributeTypes) {
        m_builder = new PersistOperationBuilder(getRepository(), resource, name);
        if (resource.getTimeKeeper() != null) {
            m_builder.setTimeKeeper(resource.getTimeKeeper());
        }
        for (Iterator<CollectionAttributeType> iter = attributeTypes.iterator(); iter.hasNext();) {
            CollectionAttributeType attrType = iter.next();
            if (attrType instanceof NumericCollectionAttributeType) {
                m_builder.declareAttribute(attrType);
            }
        }
    }

    /**
     * <p>getRepository</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public RrdRepository getRepository() {
        return m_repository;
    }

    /**
     * <p>setRepository</p>
     *
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public void setRepository(RrdRepository repository) {
        m_repository = repository;
    }

    /** {@inheritDoc} */
    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        boolean persist = isIgnorePersist() && attribute.getType().toLowerCase().startsWith("counter");
        LOG.debug("Persisting {} {}", attribute, (persist ? ". Ignoring value because of sysUpTime changed." : ""));
        String value = persist ? "U" : attribute.getNumericValue();
        m_builder.setAttributeValue(attribute.getAttributeType(), value);
        m_builder.setAttributeMetadata(attribute.getMetricIdentifier(), attribute.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
            LOG.debug("Persisting {}", attribute);
            CollectionResource resource = attribute.getResource();
            String value = attribute.getStringValue();
            
            //String attrVal = (value == null ? null : value.toString());
            //if (attrVal == null) {
            if (value == null) {
                LOG.info("No data collected for attribute {}.  Skipping.", attribute);
                return;
            }
            String attrName = attribute.getName();
            try {
                File resourceDir = resource.getResourceDir(getRepository());
                ResourceTypeUtils.updateStringProperty(resourceDir, value, attrName);
            } catch(FileNotFoundException e) {
                LOG.error("Unable to save string attribute {}", attribute, e);
            } catch(IOException e) {
                LOG.error("Unable to save string attribute {}", attribute, e);
            }
    }

    /**
     * <p>popShouldPersist</p>
     *
     * @return a boolean.
     */
    public boolean popShouldPersist() {
        boolean top = top();
        m_stack.removeLast();
        return top;
    }
    
    private void push(boolean b) {
        m_stack.addLast(Boolean.valueOf(b));
    }

    /**
     * <p>pushShouldPersist</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collection.api.CollectionAttribute} object.
     */
    public void pushShouldPersist(CollectionAttribute attribute) {
        pushShouldPersist(attribute.shouldPersist(m_params));
    }

    /**
     * <p>pushShouldPersist</p>
     *
     * @param group a {@link org.opennms.netmgt.collection.api.AttributeGroup} object.
     */
    protected void pushShouldPersist(AttributeGroup group) {
        pushShouldPersist(group.shouldPersist(m_params));
    }

    private void pushShouldPersist(boolean shouldPersist) {
        push(top() && shouldPersist);
    }

    /**
     * Push {@link CollectionResource} instances directly onto the stack without checking
     * {@link #top()} since they are the top-level resources.
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     */
    public void pushShouldPersist(CollectionResource resource) {
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
     * @param attribute a {@link org.opennms.netmgt.collection.api.CollectionAttribute} object.
     */
    public void storeAttribute(CollectionAttribute attribute) {
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

}
