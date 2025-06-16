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
package org.opennms.netmgt.collection.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LatencyCollectionAttribute class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyCollectionAttribute implements CollectionAttribute {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyCollectionAttribute.class);

    private LatencyCollectionResource m_resource;
    private LatencyCollectionAttributeType m_type;
    private Double m_value;
    private String m_name;

    /**
     * <p>Constructor for LatencyCollectionAttribute.</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.LatencyCollectionResource} object.
     * @param type a {@link org.opennms.netmgt.collection.api.LatencyCollectionAttributeType} object.
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.Double} object.
     */
    public LatencyCollectionAttribute(LatencyCollectionResource resource, LatencyCollectionAttributeType type, String name, Double value) {
        super();
        m_resource = resource;
        m_type = type;
        m_name = name;
        m_value = value;
    }

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.LatencyCollectionAttributeType} object.
     */
    @Override
    public LatencyCollectionAttributeType getAttributeType() {
        return m_type;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public Double getNumericValue() {
        return m_value;
    }

    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     */
    @Override
    public CollectionResource getResource() {
        return m_resource;
    }

    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStringValue() {
        return null;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link AttributeType} object.
     */
    @Override
    public AttributeType getType() {
        return AttributeType.GAUGE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        LOG.debug("Visiting attribute {}", this);
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
    }

    @Override
    public String getMetricIdentifier() {
        return String.format("%s/%s", m_resource.getServiceName(), m_resource.getIpAddress());
    }

    @Override
    public String toString() {
        return String.format("%s: %f", m_name, m_value);
    }
}
