/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.prometheus;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class PrometheusCollectionRequestTest extends XmlTestNoCastor<PrometheusCollectionRequest> {
    public PrometheusCollectionRequestTest(PrometheusCollectionRequest sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        getPrometheusCollectionRequest(),
                        new File("src/test/resources/prometheus-collection-request.xml")
                }
        });
    }

    private static PrometheusCollectionRequest getPrometheusCollectionRequest() {
        PrometheusCollectionRequest collectionRequestDTO = new PrometheusCollectionRequest();

        Group group = new Group();
        group.setName("node_exporter_loadavg");
        group.setResourceType("node");
        group.setFilterExp("name matches 'node_load.*'");
        NumericAttribute attr = new NumericAttribute();
        attr.setAliasExp("name.substring('node_'.length())");
        group.getNumericAttribute().add(attr);

        collectionRequestDTO.getGroups().add(group);
        return collectionRequestDTO;
    }
}
