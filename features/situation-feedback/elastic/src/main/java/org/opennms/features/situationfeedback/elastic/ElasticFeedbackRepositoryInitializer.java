/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
package org.opennms.features.situationfeedback.elastic;

import org.opennms.plugins.elasticsearch.rest.template.DefaultTemplateInitializer;
import org.opennms.plugins.elasticsearch.rest.template.DefaultTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.IndexSettings;
import org.opennms.plugins.elasticsearch.rest.template.MergingTemplateLoader;
import org.osgi.framework.BundleContext;

import io.searchbox.client.JestClient;

public class ElasticFeedbackRepositoryInitializer extends DefaultTemplateInitializer {

    public static final String TEMPLATE_RESOURCE = "/feedback-template";

    private static final String FEEDBACK_TEMPLATE_NAME = "feedback";

    public ElasticFeedbackRepositoryInitializer(BundleContext bundleContext, JestClient client, IndexSettings indexSettings) {
        super(bundleContext, client, TEMPLATE_RESOURCE, FEEDBACK_TEMPLATE_NAME, indexSettings);
    }

    protected ElasticFeedbackRepositoryInitializer(JestClient client, IndexSettings indexSettings) {
        super(client, TEMPLATE_RESOURCE, FEEDBACK_TEMPLATE_NAME, new MergingTemplateLoader(new DefaultTemplateLoader(), indexSettings));
    }

    protected ElasticFeedbackRepositoryInitializer(JestClient client) {
        super(client, TEMPLATE_RESOURCE, FEEDBACK_TEMPLATE_NAME, new DefaultTemplateLoader());
    }

}
