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
package org.opennms.netmgt.collection.persistence.evaluate;

import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.PersistOperationBuilder;

/**
 * The Class EvaluatorPersistOperationBuilder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluatorPersistOperationBuilder implements PersistOperationBuilder {

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Sets the attribute value.
     *
     * @param attributeType the attribute type
     * @param value the value
     */
    @Override
    public void setAttributeValue(CollectionAttributeType attributeType, Number value) {}

    /**
     * Sets the attribute metadata.
     *
     * @param metricIdentifier the metric identifier
     * @param name the name
     */
    @Override
    public void setAttributeMetadata(String metricIdentifier, String name) {}

    /**
     * Commit.
     *
     * @throws PersistException the persist exception
     */
    @Override
    public void commit() throws PersistException {}

}
