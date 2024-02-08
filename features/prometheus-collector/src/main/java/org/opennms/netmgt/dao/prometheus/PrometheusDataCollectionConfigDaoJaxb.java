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
package org.opennms.netmgt.dao.prometheus;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.config.prometheus.Collection;
import org.opennms.netmgt.config.prometheus.Group;
import org.opennms.netmgt.config.prometheus.PrometheusDatacollectionConfig;
import org.opennms.netmgt.dao.prometheus.PrometheusDataCollectionConfigDao;

public class PrometheusDataCollectionConfigDaoJaxb extends AbstractMergingJaxbConfigDao<PrometheusDatacollectionConfig, PrometheusDatacollectionConfig> implements PrometheusDataCollectionConfigDao {

    public PrometheusDataCollectionConfigDaoJaxb() {
        super(PrometheusDatacollectionConfig.class, "Prometheus Data Collection Configuration",
                Paths.get("etc", "prometheus-datacollection-config.xml"),
                Paths.get("etc", "prometheus-datacollection.d"));
    }

    @Override
    public PrometheusDatacollectionConfig translateConfig(PrometheusDatacollectionConfig config) {
        return config;
    }

    @Override
    public PrometheusDatacollectionConfig mergeConfigs(PrometheusDatacollectionConfig source, PrometheusDatacollectionConfig target) {
        if (target == null) {
            target = new PrometheusDatacollectionConfig();
        }
        return target.merge(source);
    }

    @Override
    public PrometheusDatacollectionConfig getConfig() {
        return getObject();
    }

    @Override
    public Collection getCollectionByName(String name) {
        return getConfig().getCollection().stream()
            .filter(c -> Objects.equals(name, c.getName())).findFirst().orElse(null);
    }

    @Override
    public List<Group> getGroupsForCollection(Collection collection) {
        if (collection == null) {
            return Collections.emptyList();
        }

        // Resolve the group references and add them to the clone
        final Set<String> referencedGroupNames = new HashSet<>(collection.getGroupRef());
        return getConfig().getGroup().stream()
                .filter(g -> referencedGroupNames.contains(g.getName()))
                .collect(Collectors.toList());
    }

}
