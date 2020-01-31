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

package org.opennms.netmgt.timeseries.integration.persistence;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.timeseries.integration.TimeseriesWriter;
import org.opennms.newts.api.Context;

/**
 * TimeseriesPersister persistence strategy.
 *
 * Both string and numeric attributes are persisted via {@link TimeseriesPersistOperationBuilder}.
 *
 */
public class TimeseriesPersister extends AbstractPersister {

    private final RrdRepository repository;
    private final TimeseriesWriter writer;
    private final Context context;
    private TimeseriesPersistOperationBuilder builder;

    protected TimeseriesPersister(ServiceParameters params, RrdRepository repository, TimeseriesWriter newtsWriter, Context context) {
        super(params, repository);
        this.repository = repository;
        writer = newtsWriter;
        this.context = context;
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            // Set the builder before any calls to persistNumericAttribute are made
            CollectionResource resource = group.getResource();
            builder = new TimeseriesPersistOperationBuilder(writer, context, repository, resource, group.getName());
            if (resource.getTimeKeeper() != null) {
                builder.setTimeKeeper(resource.getTimeKeeper());
            }
            setBuilder(builder);
        }
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
        builder.persistStringAttribute(path, key, value);
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
