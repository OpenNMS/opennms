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
package org.opennms.netmgt.collection.distributed;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.ResourceTypesDao;

public class Utils {

    public static Path getSentinelHome() {
        return Paths.get(System.getProperty("karaf.home", "/opt/sentinel"));
    }

    public static ResourceTypeMapper createReseourceTypeMapper(ResourceTypesDao resourceTypesDao) {
        Objects.requireNonNull(resourceTypesDao);
        ResourceTypeMapper.getInstance().setResourceTypeMapper(
                (type) -> resourceTypesDao.getResourceTypeByName(type));
        return ResourceTypeMapper.getInstance();
    }

    public static DataCollectionConfigFactory createDataCollectionConfigFactory(DataCollectionConfigDao configDao) {
        DataCollectionConfigFactory.setInstance(configDao);
        return new DataCollectionConfigFactory() {};
    }
}
