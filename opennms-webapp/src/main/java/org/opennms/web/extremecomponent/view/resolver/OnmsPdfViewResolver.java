/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.extremecomponent.view.resolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.MimeConstants;
import org.extremecomponents.table.core.Preferences;
import org.extremecomponents.table.filter.ViewResolver;
import org.opennms.reporting.availability.render.PDFReportRenderer;

public class OnmsPdfViewResolver implements ViewResolver {

    @Override
    public void resolveView(ServletRequest request, ServletResponse response, Preferences preferences, Object viewData) throws Exception {
        try (
             final var is = new ByteArrayInputStream(((String) viewData).getBytes(StandardCharsets.UTF_8));
        ) {
            final var out = new ByteArrayOutputStream();

            final var base = Files.createTempDirectory("fop-pdf-view-resolver-");
            final var fopFactory = PDFReportRenderer.getFopFactoryForBase(base);

            final var foUserAgent = fopFactory.newFOUserAgent();
            final var fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            final var tfact = TransformerFactory.newInstance();
            final var  transformer = tfact.newTransformer();

            final var src = new StreamSource(is);
            final var res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);

            final var contents = out.toByteArray();
            response.setContentLength(contents.length);
            response.getOutputStream().write(contents);
        }
    }

}
