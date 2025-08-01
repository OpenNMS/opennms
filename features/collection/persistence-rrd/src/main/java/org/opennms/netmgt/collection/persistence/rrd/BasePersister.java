/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.collection.persistence.rrd;

import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.collection.api.AbstractPersister;
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
public class BasePersister extends AbstractPersister {
    
    protected static final Logger LOG = LoggerFactory.getLogger(BasePersister.class);

    private final RrdStrategy<?, ?> m_rrdStrategy;
    protected final ResourceStorageDao m_resourceStorageDao;
    private boolean m_dontReorderAttributes = false;

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
}
