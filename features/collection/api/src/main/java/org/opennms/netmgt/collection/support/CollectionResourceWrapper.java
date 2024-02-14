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

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.model.ResourcePath;

/**
 * Delegates the visit calls to the wrapped resource.
 * Allows individual functions to be intercepted.
 *
 * @author jwhite
 */
public class CollectionResourceWrapper implements CollectionResource {

    private final CollectionResource m_wrapped;

    public CollectionResourceWrapper(CollectionResource wrapped) {
        m_wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public String getOwnerName() {
        return m_wrapped.getOwnerName();
    }

    /** {@inheritDoc} */
    @Override
    public ResourcePath getPath() {
        return m_wrapped.getPath();
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
    public boolean rescanNeeded() {
        return m_wrapped.rescanNeeded();
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceTypeName() {
        return m_wrapped.getResourceTypeName();
    }

    /** {@inheritDoc} */
    @Override
    public ResourcePath getParent() {
        return m_wrapped.getParent();
    }

    /** {@inheritDoc} */
    @Override
    public String getInstance() {
        return m_wrapped.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public String getUnmodifiedInstance() {
        return m_wrapped.getUnmodifiedInstance();
    }

    /** {@inheritDoc} */
    @Override
    public String getInterfaceLabel() {
        return m_wrapped.getInterfaceLabel();
    }

    /** {@inheritDoc} */
    @Override
    public TimeKeeper getTimeKeeper() {
        return m_wrapped.getTimeKeeper();
    }
}
