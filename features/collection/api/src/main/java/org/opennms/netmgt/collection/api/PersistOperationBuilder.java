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

/**
 * Used by the {@link AbstractPersister} to persist numeric attributes.
 *
 * @author jwhite
 */
public interface PersistOperationBuilder {

    /**
     * Used to identify the builder in log messages in an exception is thrown during a call to commit().
     */
    public String getName();

    public void setAttributeValue(CollectionAttributeType attributeType, Number value);

    public void setAttributeMetadata(String metricIdentifier, String name);

    /**
     * Persists the attribute values and meta-data.
     *
     * @throws PersistException if an error occurs while persisting the attribute
     */
    public void commit() throws PersistException;

}
