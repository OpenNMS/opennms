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
package org.opennms.netmgt.collection.persistence.newts;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.PersistException;
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
}
