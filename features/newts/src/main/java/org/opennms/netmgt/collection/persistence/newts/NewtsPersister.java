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

package org.opennms.netmgt.collection.persistence.newts;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.newts.NewtsWriter;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.newts.api.Context;

/**
 * Newts persistence strategy.
 *
 * Both string and numeric attributes are persisted via {@link org.opennms.netmgt.collection.persistence.newts.NewtsPersistOperationBuilder}.
 *
 * @author jwhite
 */
public class NewtsPersister extends AbstractPersister {

    private final RrdRepository m_repository;
    private final NewtsWriter m_newtsWriter;
    private final Context m_context;
    private NewtsPersistOperationBuilder m_builder;
    private Persister kafkaPersister;

    protected NewtsPersister(ServiceParameters params, RrdRepository repository, NewtsWriter newtsWriter, Context context) {
        super(params, repository);
        m_repository = repository;
        m_newtsWriter = newtsWriter;
        m_context = context;
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            // Set the builder before any calls to persistNumericAttribute are made
            CollectionResource resource = group.getResource();
            m_builder = new NewtsPersistOperationBuilder(m_newtsWriter, m_context, m_repository, resource, group.getName());
            if (resource.getTimeKeeper() != null) {
                m_builder.setTimeKeeper(resource.getTimeKeeper());
            }
            setBuilder(m_builder);
        }
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
        m_builder.persistStringAttribute(path, key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitCollectionSet(CollectionSet set) {
        if (kafkaPersister != null) {
            kafkaPersister.visitCollectionSet(set);
        }
    }

    public void setKafkaPersister(Persister kafkaPersister) {
        this.kafkaPersister = kafkaPersister;
    }
}
