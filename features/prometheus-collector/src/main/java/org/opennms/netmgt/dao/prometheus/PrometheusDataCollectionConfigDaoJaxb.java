/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
