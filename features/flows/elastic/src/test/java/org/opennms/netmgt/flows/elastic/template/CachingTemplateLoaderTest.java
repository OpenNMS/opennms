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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.opennms.netmgt.flows.elastic.ElasticFlowRepositoryInitializer;
import org.opennms.plugins.elasticsearch.rest.template.CachingTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.DefaultTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.TemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.Version;

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
        cachingTemplateLoader.load(version, ElasticFlowRepositoryInitializer.TEMPLATE_RESOURCE);
        cachingTemplateLoader.load(version, ElasticFlowRepositoryInitializer.TEMPLATE_RESOURCE);
        cachingTemplateLoader.load(version, "/netflow-template-merged");
        cachingTemplateLoader.load(version, "/netflow-template-merged");

        // Verify that, actual loader was only be invoked twice
        verify(actualTemplateLoader, times(2)).load(any(), anyString());
    }
}
