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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.opennms.netmgt.flows.elastic.RawIndexInitializer;
import org.opennms.features.jest.client.template.CachingTemplateLoader;
import org.opennms.features.jest.client.template.DefaultTemplateLoader;
import org.opennms.features.jest.client.template.TemplateLoader;
import org.opennms.features.jest.client.template.Version;

public class CachingTemplateLoaderTest {

    private static final Version version = new Version(6,2,3);

    @Test
    public void verifyCaching() throws IOException {
        // Spy on loader
        final TemplateLoader original = new DefaultTemplateLoader();
        final TemplateLoader actualTemplateLoader = spy(original);

        // Make it cache
        final TemplateLoader cachingTemplateLoader = new CachingTemplateLoader(actualTemplateLoader);

        // Ask the caching loader
        cachingTemplateLoader.load(version, RawIndexInitializer.TEMPLATE_RESOURCE);
        cachingTemplateLoader.load(version, RawIndexInitializer.TEMPLATE_RESOURCE);
        cachingTemplateLoader.load(version, "/netflow-template-merged");
        cachingTemplateLoader.load(version, "/netflow-template-merged");

        // Verify that, actual loader was only be invoked twice
        verify(actualTemplateLoader, times(2)).load(any(), anyString());
    }
}
