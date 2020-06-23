/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.reporting.availability.render;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

public class PDFReportRendererTest extends TestCase {
    public void testPdfRendering() throws Exception {
        new PDFReportRenderer().render(
                new InputStreamReader(
                        // This is a freely-licensed sample XSL-FO document from IBM developerWorks
                        Thread.currentThread().getContextClassLoader().getResourceAsStream("org/opennms/reporting/availability/render/currency.fo"), 
                        StandardCharsets.UTF_8
                ),
                new FileOutputStream("target/sampleDocument.pdf"),
                new InputStreamReader(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream("org/opennms/reporting/availability/render/identity.xsl"), 
                        StandardCharsets.UTF_8
                )
        );
    }
}
