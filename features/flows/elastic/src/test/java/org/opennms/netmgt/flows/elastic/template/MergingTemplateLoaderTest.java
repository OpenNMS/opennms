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

import static org.junit.Assert.assertEquals;
import static org.opennms.netmgt.flows.elastic.ElasticFlowRepositoryInitializer.TEMPLATE_RESOURCE;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class MergingTemplateLoaderTest {

    @Test
    public void verifyMergingEmpty() throws IOException {
        final String merged = new MergingTemplateLoader(new DefaultTemplateLoader(), new IndexSettings()).load(TEMPLATE_RESOURCE);
        final String expected = new DefaultTemplateLoader().load(TEMPLATE_RESOURCE);
        assertEquals(toJson(expected), toJson(merged)); // Use gson to verify, otherwise strings may be formatted, etc.
    }

    @Test
    public void verifyMergingFull() throws IOException {
        final IndexSettings IndexSettings = new IndexSettings();
        IndexSettings.setNumberOfReplicas(10);
        IndexSettings.setNumberOfShards(20);
        IndexSettings.setRefreshInterval("60s");
        IndexSettings.setRoutingPartitionSize(100);

        final String merged = new MergingTemplateLoader(new DefaultTemplateLoader(), IndexSettings).load(TEMPLATE_RESOURCE);
        final String expected = new DefaultTemplateLoader().load("/netflow-template-merged.json");
        assertEquals(toJson(merged), toJson(expected)); // Use gson to verify, otherwise strings may be formatted, etc.
    }
    
    private static JsonElement toJson(String input) {
        return new JsonParser().parse(input);
    }
}
