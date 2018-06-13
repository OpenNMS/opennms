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

import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BasePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BasePersister extends AbstractPersister {
    
    protected static final Logger LOG = LoggerFactory.getLogger(BasePersister.class);

    private final RrdStrategy<?, ?> m_rrdStrategy;
    protected final ResourceStorageDao m_resourceStorageDao;
    private boolean m_dontReorderAttributes = false;
    private Persister kafkaPersister;

    /**
     * <p>Constructor for BasePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    protected BasePersister(ServiceParameters params, RrdRepository repository, RrdStrategy<?, ?> rrdStrategy, ResourceStorageDao resourceStorageDao) {
        super(params, repository);
        m_rrdStrategy = rrdStrategy;
        m_resourceStorageDao = resourceStorageDao;
    }

    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeTypes a {@link java.util.Set} object.
     */
    protected RrdPersistOperationBuilder createBuilder(CollectionResource resource, String name, Set<CollectionAttributeType> attributeTypes) {
        RrdPersistOperationBuilder builder  = new RrdPersistOperationBuilder(getRrdStrategy(), getRepository(), resource, name, m_dontReorderAttributes);
        if (resource.getTimeKeeper() != null) {
            builder.setTimeKeeper(resource.getTimeKeeper());
        }
        for (Iterator<CollectionAttributeType> iter = attributeTypes.iterator(); iter.hasNext();) {
            CollectionAttributeType attrType = iter.next();
            if (attrType instanceof NumericCollectionAttributeType) {
                builder.declareAttribute(attrType);
            }
        }
        return builder;
    }

    protected void setBuilder(RrdPersistOperationBuilder builder) {
        super.setBuilder(builder);
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
        try {
            m_resourceStorageDao.setStringAttribute(path, key, value);
        } catch (Throwable t) {
            throw new PersistException(t);
        }
    }

    public RrdStrategy<?, ?> getRrdStrategy() {
        return m_rrdStrategy;
    }

    public void setDontReorderAttributes(boolean dontReorderAttributes) {
        m_dontReorderAttributes = dontReorderAttributes;
    }

    public boolean getDontReorderAttributes() {
        return m_dontReorderAttributes;
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
