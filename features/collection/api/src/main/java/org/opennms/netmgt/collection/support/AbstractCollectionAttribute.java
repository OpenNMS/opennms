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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract AbstractCollectionAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractCollectionAttribute implements CollectionAttribute {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCollectionAttribute.class);

    protected final CollectionAttributeType m_attribType;
    protected final CollectionResource m_resource;

    public AbstractCollectionAttribute(CollectionAttributeType attribType, CollectionResource resource) {
        m_attribType = attribType;
        m_resource = resource;
    }

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     */
    @Override
    public final CollectionAttributeType getAttributeType() {
        return m_attribType;
    }

    @Override
    public final CollectionResource getResource() {
        return m_resource;
    }

    @Override
    public final String getName() {
        return m_attribType.getName();
    }

    @Override
    public final AttributeType getType() {
        return m_attribType.getType();
    }

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.Number} object.
     */
    @Override
    public abstract Number getNumericValue();

    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getStringValue();

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    /** 
     * Since a {@link CollectionAttribute} is a terminal value, we just visit and
     * complete it since it doesn't have any "children".
     */
    @Override
    public final void visit(CollectionSetVisitor visitor) {
        LOG.debug("Visiting attribute {}", this);
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
    }   

}
