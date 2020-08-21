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

package org.opennnms.netmgt.collectd.prometheus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Map;

import org.junit.Rule;
import org.opennms.netmgt.collectd.prometheus.PrometheusCollector;
import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.prometheus.Collection;
import org.opennms.netmgt.config.prometheus.PrometheusDatacollectionConfig;
import org.opennms.netmgt.config.prometheus.Rrd;
import org.opennms.netmgt.dao.prometheus.PrometheusDataCollectionConfigDao;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;

public class PrometheusCollectorComplianceIT extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .withRootDirectory(Paths.get("src", "test", "resources").toString())
            .dynamicPort());

    public PrometheusCollectorComplianceIT() {
        super(PrometheusCollector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
                .put("collection", COLLECTION)
                .put("url", String.format("http://127.0.0.1:%d/metrics", wireMockRule.port()))
                .build();
    }

    @Override
    public Map<String, Object> getRequiredBeans() {
        Collection collection = new Collection();
        Rrd rrd = new Rrd();
        collection.setRrd(rrd);

        PrometheusDatacollectionConfig dataCollectionConfig = new PrometheusDatacollectionConfig();
        dataCollectionConfig.setRrdRepository("target");
        dataCollectionConfig.getCollection().add(collection);

        PrometheusDataCollectionConfigDao prometheusCollectionDao = mock(PrometheusDataCollectionConfigDao.class);
        when(prometheusCollectionDao.getCollectionByName(COLLECTION)).thenReturn(collection);
        when(prometheusCollectionDao.getConfig()).thenReturn(dataCollectionConfig);

        return new ImmutableMap.Builder<String, Object>()
                .put("prometheusDataCollectionConfigDao", prometheusCollectionDao)
                .build();
    }
}
