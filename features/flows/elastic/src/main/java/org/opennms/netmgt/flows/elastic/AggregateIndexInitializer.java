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
package org.opennms.netmgt.flows.elastic;

import org.opennms.features.jest.client.template.DefaultTemplateInitializer;
import org.opennms.features.jest.client.template.DefaultTemplateLoader;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.features.jest.client.template.MergingTemplateLoader;
import org.osgi.framework.BundleContext;

import io.searchbox.client.JestClient;

import java.nio.file.Paths;

public class AggregateIndexInitializer extends DefaultTemplateInitializer {

    public static final String TEMPLATE_RESOURCE = "/netflow_agg-template";

    private static final String FLOW_TEMPLATE_NAME = "netflow_agg";

    public AggregateIndexInitializer(BundleContext bundleContext, JestClient client, IndexSettings indexSettings) {
        super(bundleContext, client, TEMPLATE_RESOURCE, FLOW_TEMPLATE_NAME, indexSettings);
    }

    public AggregateIndexInitializer(JestClient client, IndexSettings indexSettings) {
        super(client, TEMPLATE_RESOURCE, FLOW_TEMPLATE_NAME, new MergingTemplateLoader(new DefaultTemplateLoader(), indexSettings), indexSettings);
    }

    public AggregateIndexInitializer(JestClient client) {
        super(client, TEMPLATE_RESOURCE, FLOW_TEMPLATE_NAME, new DefaultTemplateLoader(), new IndexSettings());
    }
}

