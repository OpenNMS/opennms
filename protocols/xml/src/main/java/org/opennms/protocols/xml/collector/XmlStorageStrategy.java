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
package org.opennms.protocols.xml.collector;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;

/**
 * The Class XML StorageStrategy.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlStorageStrategy extends IndexStorageStrategy {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.support.IndexStorageStrategy#getResourceNameFromIndex(org.opennms.netmgt.config.collector.CollectionResource)
     */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        // Normalize the resource's instance and use it as resource's label
        return resource.getInstance().replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_").replaceAll("[|/]", "_").replaceAll("=", "").replaceAll("[_]+$", "").replaceAll("___", "_");
    }

}
