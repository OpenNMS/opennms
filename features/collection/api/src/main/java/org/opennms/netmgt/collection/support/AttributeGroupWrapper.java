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

import java.util.Collection;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;

/**
 * Delegates the visit calls to the wrapped attribut group.
 * Allows individual functions to be intercepted.
 *
 * @author jwhite
 */
public class AttributeGroupWrapper extends AttributeGroup {

    private final AttributeGroup m_wrapped;

    public AttributeGroupWrapper(AttributeGroup wrapped) {
        super(wrapped.getResource(), wrapped.getGroupType());
        m_wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return m_wrapped.getName();
    }

    /** {@inheritDoc} */
    @Override
    public CollectionResource getResource() {
        return m_wrapped.getResource();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<CollectionAttribute> getAttributes() {
        return m_wrapped.getAttributes();
    }

    /** {@inheritDoc} */
    @Override
    public void addAttribute(CollectionAttribute attr) {
        m_wrapped.addAttribute(attr);
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
    public AttributeGroupType getGroupType() {
        return m_wrapped.getGroupType();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_wrapped.toString();
    }
}
