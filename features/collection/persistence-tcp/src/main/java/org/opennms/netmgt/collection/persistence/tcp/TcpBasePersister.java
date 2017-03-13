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

package org.opennms.netmgt.collection.persistence.tcp;

import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
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
public class TcpBasePersister extends AbstractPersister {
    
    protected static final Logger LOG = LoggerFactory.getLogger(TcpBasePersister.class);

    private final TcpOutputStrategy m_tcpStrategy;
    private boolean m_dontReorderAttributes = false;

    /**
     * <p>Constructor for TcpBasePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     */
    protected TcpBasePersister(ServiceParameters params, RrdRepository repository, TcpOutputStrategy tcpStrategy) {
        super(params, repository);
        m_tcpStrategy = tcpStrategy;
    }

    /** {@inheritDoc} */
    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        boolean shouldIgnorePersist = isIgnorePersist() && attribute.getType().toLowerCase().startsWith("counter");
        LOG.debug("Persisting {} {}", attribute, (shouldIgnorePersist ? ". Ignoring value because of sysUpTime changed." : ""));
        Number value = shouldIgnorePersist ? Double.NaN : attribute.getNumericValue();
        getBuilder().setNumericAttributeValue(attribute.getAttributeType(), value);
    }

    /** {@inheritDoc} */
    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        LOG.debug("Persisting {}", attribute);
        String value = attribute.getStringValue();
        getBuilder().setStringAttributeValue(attribute.getAttributeType(), value);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#persistStringAttribute(org.opennms.netmgt.model.ResourcePath, java.lang.String, java.lang.String)
     */
    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
    }

    /**
     * <p>createBuilder</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     * @param name a {@link java.lang.String} object.
     * @param attributeTypes a {@link java.util.Set} object.
     */
    protected TcpPersistOperationBuilder createBuilder(CollectionResource resource, String name, Set<CollectionAttributeType> attributeTypes) {
        TcpPersistOperationBuilder builder  = new TcpPersistOperationBuilder(getTcpStrategy(), resource, name, m_dontReorderAttributes);
        if (resource.getTimeKeeper() != null) {
            builder.setTimeKeeper(resource.getTimeKeeper());
        }
        for (Iterator<CollectionAttributeType> iter = attributeTypes.iterator(); iter.hasNext();) {
            CollectionAttributeType attrType = iter.next();
            if (attrType instanceof NumericCollectionAttributeType) {
                //builder.declareAttribute(attrType);
            }
        }
        return builder;
    }

    protected void setBuilder(TcpPersistOperationBuilder builder) {
        super.setBuilder(builder);
    }

    public TcpOutputStrategy getTcpStrategy() {
        return m_tcpStrategy;
    }

    public void setDontReorderAttributes(boolean dontReorderAttributes) {
        m_dontReorderAttributes = dontReorderAttributes;
    }

    public boolean getDontReorderAttributes() {
        return m_dontReorderAttributes;
    }
}
