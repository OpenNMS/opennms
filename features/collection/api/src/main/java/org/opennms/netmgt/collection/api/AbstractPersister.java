/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import java.util.LinkedList;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common persister code that performs the following:
 *   1) Determines which attributes should be persisted. A stack is used to keep track of the "persist" flag
 *      for resources and groups as they are visited.
 *   2) Delegates the persistence of numeric attributes via {@link PersistOperationBuilder}. Subclasses
 *      must call {@link #setBuilder} with an implementation before any attributes are visited.
 *   3) Delegates the persistence of string attributes via calls to {@link #persistStringAttribute}.
 *
 * @author jwhite
 */
public abstract class AbstractPersister extends AbstractCollectionSetVisitor implements Persister {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractPersister.class);

    private boolean m_ignorePersist = false;
    private ServiceParameters m_params;
    private RrdRepository m_repository;
    private final LinkedList<Boolean> m_stack = new LinkedList<>();
    private PersistOperationBuilder m_builder;

    protected abstract void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException;

    /**
     * <p>Constructor for BasePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    protected AbstractPersister(ServiceParameters params, RrdRepository repository) {
        super();
        m_params = params;
        m_repository = repository;
    }

    /**
     * <p>commitBuilder</p>
     */
    public void commitBuilder() {
        if (isPersistDisabled()) {
            LOG.debug("Persist disabled for {}", m_builder.getName());
            return;
        }
        try {
            m_builder.commit();
        } catch (PersistException e) {
            LOG.error("Unable to persist data for {}", m_builder.getName(), e);
        }
        m_builder = null;
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
        boolean shouldIgnorePersist = isIgnorePersist() && AttributeType.COUNTER.equals(attribute.getType());
        LOG.debug("Persisting {} {}", attribute, (shouldIgnorePersist ? ". Ignoring value because of sysUpTime changed." : ""));
        Number value = shouldIgnorePersist ? Double.NaN : attribute.getNumericValue();
        m_builder.setAttributeValue(attribute.getAttributeType(), value);
        m_builder.setAttributeMetadata(attribute.getMetricIdentifier(), attribute.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        LOG.debug("Persisting {}", attribute);
        CollectionResource resource = attribute.getResource();
        String value = attribute.getStringValue();

        if (value == null) {
            LOG.info("No data collected for attribute {}.  Skipping.", attribute);
            return;
        }

        try {
            ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(m_repository, resource.getPath());
            persistStringAttribute(path, attribute.getName(), value);
        } catch(PersistException e) {
            LOG.error("Unable to save string attribute {}.", attribute, e);
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

    protected void setBuilder(PersistOperationBuilder builder) {
        m_builder = builder;
    }
}
