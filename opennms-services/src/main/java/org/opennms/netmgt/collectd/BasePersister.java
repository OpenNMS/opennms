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
// Modifications:
//
// 2008 Mar 04: Allow us to get the Builder object for tests. - dj@opennms.org
// 2006 Aug 15: Format the code a little bit - dj@opennms.org
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
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdException;

/**
 * <p>BasePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BasePersister extends AbstractCollectionSetVisitor implements Persister {

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
     * @param params a {@link org.opennms.netmgt.collectd.ServiceParameters} object.
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
            log().error("Unable to persist data for " + name + ": " + e, e);
    
        }
    }

    private boolean isPersistDisabled() {
        return m_params != null &&
               m_params.getParameters().containsKey("storing-enabled") &&
               m_params.getParameters().get("storing-enabled").equals("false");
    }

    /** {@inheritDoc} */
    public void completeAttribute(CollectionAttribute attribute) {
        popShouldPersist();
    }

    /** {@inheritDoc} */
    public void completeGroup(AttributeGroup group) {
        popShouldPersist();
    }

    /** {@inheritDoc} */
    public void completeResource(CollectionResource resource) {
        popShouldPersist();
    }
    
    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.collectd.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeType a {@link org.opennms.netmgt.collectd.AttributeDefinition} object.
     */
    protected void createBuilder(CollectionResource resource, String name, AttributeDefinition attributeType) {
        createBuilder(resource, name, Collections.singleton(attributeType));
    }

    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.collectd.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeTypes a {@link java.util.Set} object.
     */
    protected void createBuilder(CollectionResource resource, String name, Set<AttributeDefinition> attributeTypes) {
        m_builder = new PersistOperationBuilder(getRepository(), resource, name);
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

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /** {@inheritDoc} */
    public void persistNumericAttribute(CollectionAttribute attribute) {
    	log().debug("Persisting "+attribute + (isIgnorePersist() ? ". Ignoring value because of sysUpTime changed" : ""));
    	String value = isIgnorePersist() ? "U" : attribute.getNumericValue();
        m_builder.setAttributeValue(attribute.getAttributeType(), value);
    }

    /** {@inheritDoc} */
    public void persistStringAttribute(CollectionAttribute attribute) {
            log().debug("Persisting "+attribute);
            CollectionResource resource = attribute.getResource();
            String value = attribute.getStringValue();
    
            File resourceDir = resource.getResourceDir(getRepository());
    
            //String attrVal = (value == null ? null : value.toString());
            //if (attrVal == null) {
            if (value == null) {
                log().info("No data collected for attribute "+attribute+".  Skipping.");
                return;
            }
            String attrName = attribute.getName();
            try {
                ResourceTypeUtils.updateStringProperty(resourceDir, value, attrName);
            } catch(IOException e) {
                log().error("Unable to save string attribute " + attribute + ": " + e, e);
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
     * @param attribute a {@link org.opennms.netmgt.collectd.CollectionAttribute} object.
     */
    protected void pushShouldPersist(CollectionAttribute attribute) {
        pushShouldPersist(attribute.shouldPersist(m_params));
    }

    /**
     * <p>pushShouldPersist</p>
     *
     * @param group a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
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
     * @param resource a {@link org.opennms.netmgt.collectd.CollectionResource} object.
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
     * @param attribute a {@link org.opennms.netmgt.collectd.CollectionAttribute} object.
     */
    protected void storeAttribute(CollectionAttribute attribute) {
        if (shouldPersist()) {
            attribute.storeAttribute(this);
            log().debug("Storing attribute "+attribute);
        } else {
            log().debug("Not persisting attribute "+attribute + "because shouldPersist is false");
        }
    }
    
    private boolean top() {
        return m_stack.getLast();
    }

    /** {@inheritDoc} */
    public void visitAttribute(CollectionAttribute attribute) {
        pushShouldPersist(attribute);
        storeAttribute(attribute);
    }

    /** {@inheritDoc} */
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
    }

    /** {@inheritDoc} */
    public void visitResource(CollectionResource resource) {
        log().info("Persisting data for resource "+resource);
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
