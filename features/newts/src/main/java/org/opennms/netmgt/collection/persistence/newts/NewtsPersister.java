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
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.support.NewtsResourceStorageDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.newts.NewtsWriter;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * Newts persistence strategy.
 *
 * Numeric attributes are collected and persisted via {@link org.opennms.netmgt.collection.persistence.newts.NewtsPersistOperationBuilder}.
 * String attributes are persisted via {@link org.opennms.netmgt.dao.support.NewtsResourceStorageDao}.
 *
 * @author jwhite
 */
public class NewtsPersister extends AbstractPersister {

    private final RrdRepository m_repository;
    private final NewtsWriter m_newtsWriter;
    private final NewtsResourceStorageDao m_resourceStorageDao;

    protected NewtsPersister(ServiceParameters params, RrdRepository repository, NewtsWriter newtsWriter, NewtsResourceStorageDao resourceStorageDao) {
        super(params, repository);
        m_repository = repository;
        m_newtsWriter = newtsWriter;
        m_resourceStorageDao = resourceStorageDao;
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
        m_resourceStorageDao.setStringAttribute(path, key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            // Set the builder before any calls to persistNumericAttribute are made
            CollectionResource resource = group.getResource();
            NewtsPersistOperationBuilder builder  = new NewtsPersistOperationBuilder(m_newtsWriter, m_repository, resource, group.getName());
            if (resource.getTimeKeeper() != null) {
                builder.setTimeKeeper(resource.getTimeKeeper());
            }
            setBuilder(builder);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }
}
