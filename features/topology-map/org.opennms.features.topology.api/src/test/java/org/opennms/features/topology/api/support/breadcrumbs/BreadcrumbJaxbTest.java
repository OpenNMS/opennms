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
