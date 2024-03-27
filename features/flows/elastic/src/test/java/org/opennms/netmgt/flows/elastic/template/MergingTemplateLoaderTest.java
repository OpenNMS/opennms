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
package org.opennms.netmgt.flows.elastic.template;

import static org.opennms.netmgt.flows.elastic.RawIndexInitializer.TEMPLATE_RESOURCE;

import java.io.IOException;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.features.jest.client.template.DefaultTemplateLoader;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.features.jest.client.template.MergingTemplateLoader;
import org.opennms.features.jest.client.template.Version;

public class MergingTemplateLoaderTest {

    private static final Version version = new Version(7,2,0);

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
