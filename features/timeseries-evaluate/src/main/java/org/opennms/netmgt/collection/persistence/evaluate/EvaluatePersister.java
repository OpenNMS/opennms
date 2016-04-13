/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.collection.persistence.evaluate;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;

/**
 * The Class EvaluatePersister.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluatePersister implements Persister {

    /** The evaluation statistics. */
    private EvaluateStats stats;

    /**
     * Instantiates a new evaluate persister.
     *
     * @param stats the evaluation statistics object
     */
    public EvaluatePersister(EvaluateStats stats) {
        super();
        this.stats = stats;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#visitCollectionSet(org.opennms.netmgt.collection.api.CollectionSet)
     */
    @Override
    public void visitCollectionSet(CollectionSet set) {
        stats.checkCollectionSet();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#visitResource(org.opennms.netmgt.collection.api.CollectionResource)
     */
    @Override
    public void visitResource(CollectionResource resource) {
        stats.checkResource(getResourceId(resource));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#visitGroup(org.opennms.netmgt.collection.api.AttributeGroup)
     */
    @Override
    public void visitGroup(AttributeGroup group) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getResourceId(group.getResource())).append('.');
        sb.append(group.getName());
        stats.checkGroup(sb.toString());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#visitAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
     */
    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getResourceId(attribute.getResource())).append('.');
        sb.append(attribute.getName());
        stats.checkMetric(sb.toString());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#completeAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
     */
    @Override
    public void completeAttribute(CollectionAttribute attribute) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#completeGroup(org.opennms.netmgt.collection.api.AttributeGroup)
     */
    @Override
    public void completeGroup(AttributeGroup group) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#completeResource(org.opennms.netmgt.collection.api.CollectionResource)
     */
    @Override
    public void completeResource(CollectionResource resource) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionSetVisitor#completeCollectionSet(org.opennms.netmgt.collection.api.CollectionSet)
     */
    @Override
    public void completeCollectionSet(CollectionSet set) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.Persister#persistNumericAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
     */
    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.Persister#persistStringAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
     */
    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
    }

    /**
     * Gets the resource id.
     *
     * @param resource the resource
     * @return the resource id
     */
    private String getResourceId(CollectionResource resource) {
        final StringBuffer sb = new StringBuffer();
        sb.append(resource.getParent()).append('.');
        sb.append(resource.getResourceTypeName()).append('.');
        sb.append(resource.getInterfaceLabel());
        return sb.toString();
    }
}
