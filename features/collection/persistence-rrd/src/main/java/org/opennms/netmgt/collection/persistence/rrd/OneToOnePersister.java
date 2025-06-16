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
