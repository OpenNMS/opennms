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

import java.util.Collections;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;


/**
 * <p>OneToOnePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OneToOnePersister extends BasePersister {

    private String m_group = null;

    /**
     * <p>Constructor for OneToOnePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    protected OneToOnePersister(ServiceParameters params,  RrdRepository repository, RrdStrategy<?, ?> rrdStrategy, ResourceStorageDao resourceStorageDao) {
        super(params, repository, rrdStrategy, resourceStorageDao);
    }

    /** {@inheritDoc} */
    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        pushShouldPersist(attribute);
        if (shouldPersist()) {
            final RrdPersistOperationBuilder builder = createBuilder(attribute.getResource(),
                                                                     attribute.getName(),
                                                                     Collections.singleton(attribute.getAttributeType()));
            builder.setAttributeMetadata("GROUP", m_group);

            setBuilder(builder);
            storeAttribute(attribute);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        if (shouldPersist()) {
        	commitBuilder();
        }
        popShouldPersist();
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        super.visitGroup(group);

        m_group = group.getName();
    }
}
