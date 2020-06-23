/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support.breadcrumbs;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.features.topology.api.topo.DefaultVertexRef;

public class BreadcrumbJaxbTest extends XmlTestNoCastor<Breadcrumb> {

    public BreadcrumbJaxbTest(Breadcrumb sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Object[][] data() {
        Breadcrumb breadcrumb = new Breadcrumb("this", new DefaultVertexRef("other", "1", "custom"));
        return new Object[][]{{
                breadcrumb,
                "<breadcrumb>\n" +
                "     <source-vertices>\n" +
                "       <source-vertex namespace=\"other\" id=\"1\" label=\"custom\"/>\n" +
                "     </source-vertices>\n" +
                "     <target-namespace>this</target-namespace>\n" +
                "</breadcrumb>",
                null
            }
        };
    }
}
