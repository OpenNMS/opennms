/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic.template;

import static org.opennms.netmgt.flows.elastic.ElasticFlowRepositoryInitializer.TEMPLATE_RESOURCE;

import java.io.IOException;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.plugins.elasticsearch.rest.template.DefaultTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.IndexSettings;
import org.opennms.plugins.elasticsearch.rest.template.MergingTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.Version;

public class MergingTemplateLoaderTest {

    private static final Version version = new Version(6,2,3);

    @Test
    public void verifyMergingEmpty() throws IOException {
        final String merged = new MergingTemplateLoader(new DefaultTemplateLoader(), new IndexSettings()).load(version, TEMPLATE_RESOURCE);
        final String expected = new DefaultTemplateLoader().load(version, TEMPLATE_RESOURCE);
        JsonTest.assertJsonEquals(expected, merged);
    }

    @Test
    public void verifyMergingFull() throws IOException {
        final IndexSettings IndexSettings = new IndexSettings();
        IndexSettings.setNumberOfReplicas(10);
        IndexSettings.setNumberOfShards(20);
        IndexSettings.setRefreshInterval("60s");
        IndexSettings.setRoutingPartitionSize(100);

        final String merged = new MergingTemplateLoader(new DefaultTemplateLoader(), IndexSettings).load(version, TEMPLATE_RESOURCE);
        final String expected = new DefaultTemplateLoader().load(version,"/netflow-template-merged");
        JsonTest.assertJsonEquals(expected, merged);
    }

}
