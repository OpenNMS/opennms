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

package org.opennms.plugins.elasticsearch.rest.template;

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
