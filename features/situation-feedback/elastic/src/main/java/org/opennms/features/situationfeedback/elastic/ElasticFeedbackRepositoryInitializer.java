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
package org.opennms.features.situationfeedback.elastic;

import org.opennms.features.jest.client.template.DefaultTemplateInitializer;
import org.opennms.features.jest.client.template.DefaultTemplateLoader;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.features.jest.client.template.MergingTemplateLoader;
import org.osgi.framework.BundleContext;

import io.searchbox.client.JestClient;

public class ElasticFeedbackRepositoryInitializer extends DefaultTemplateInitializer {

    public static final String TEMPLATE_RESOURCE = "/feedback-template";

    private static final String FEEDBACK_TEMPLATE_NAME = "feedback";

    public ElasticFeedbackRepositoryInitializer(BundleContext bundleContext, JestClient client, IndexSettings indexSettings) {
        super(bundleContext, client, TEMPLATE_RESOURCE, FEEDBACK_TEMPLATE_NAME, indexSettings);
    }

    protected ElasticFeedbackRepositoryInitializer(JestClient client, IndexSettings indexSettings) {
        super(client, TEMPLATE_RESOURCE, FEEDBACK_TEMPLATE_NAME, new MergingTemplateLoader(new DefaultTemplateLoader(), indexSettings), indexSettings);
    }

    protected ElasticFeedbackRepositoryInitializer(JestClient client) {
        super(client, TEMPLATE_RESOURCE, FEEDBACK_TEMPLATE_NAME, new DefaultTemplateLoader(), new IndexSettings());
    }



}
