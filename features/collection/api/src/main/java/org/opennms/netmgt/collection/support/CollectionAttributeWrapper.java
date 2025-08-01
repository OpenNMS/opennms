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
package org.opennms.netmgt.collection.support;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;

/**
 * Delegates the visit calls to the wrapped attribute.
 * Allows individual functions to be intercepted.
 *
 * @author jwhite
 */
public class CollectionAttributeWrapper implements CollectionAttribute {

    private final CollectionAttribute m_wrapped;

    public CollectionAttributeWrapper(CollectionAttribute wrapped) {
        m_wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        m_wrapped.visit(visitor);
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return m_wrapped.shouldPersist(params);
    }

    /** {@inheritDoc} */
    @Override
    public CollectionResource getResource() {
        return m_wrapped.getResource();
    }

    /** {@inheritDoc} */
    @Override
    public String getStringValue() {
        return m_wrapped.getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    public Number getNumericValue() {
        return m_wrapped.getNumericValue();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return m_wrapped.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getMetricIdentifier() {
        return m_wrapped.getMetricIdentifier();
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        m_wrapped.storeAttribute(persister);
    }

    /** {@inheritDoc} */
    @Override
    public CollectionAttributeType getAttributeType() {
        return m_wrapped.getAttributeType();
    }

    /** {@inheritDoc} */
    @Override
    public AttributeType getType() {
        return m_wrapped.getType();
    }
}
