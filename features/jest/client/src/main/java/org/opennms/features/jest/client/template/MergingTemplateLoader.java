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
package org.opennms.features.jest.client.template;

import java.io.IOException;
import java.util.Objects;

/**
 * Merges a template which is loaded from a delegate {@link TemplateLoader} with optional {@link IndexSettings}.
 */
public class MergingTemplateLoader implements TemplateLoader {

    private final TemplateLoader delegate;
    private final IndexSettings indexSettings;

    public MergingTemplateLoader(TemplateLoader delegate, IndexSettings indexSettings) {
        this.delegate = Objects.requireNonNull(delegate);
        this.indexSettings = indexSettings;
    }

    @Override
    public String load(Version serverVersion, String resource) throws IOException {
        final String template = delegate.load(serverVersion, resource);
        return merge(template);
    }

    private String merge(String template) {
        final String mergedTemplate = new TemplateMerger().merge(template, indexSettings);
        return mergedTemplate;
    }
}
