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
package org.opennms.netmgt.enlinkd.model;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTest;

/**
 * Ensure that we can marshal the links to/from XML since we
 * use the entities directly in the REST API.
 */
public class UserDefinedLinkTest extends XmlTest<UserDefinedLink> {

    public UserDefinedLinkTest(final UserDefinedLink sampleObject, final String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        UserDefinedLink udl = new UserDefinedLink();
        udl.setNodeIdA(1);
        udl.setNodeIdZ(2);
        udl.setComponentLabelA("a");
        udl.setComponentLabelZ("z");
        udl.setLinkId("id");
        udl.setLinkLabel("label");
        udl.setOwner("me");
        udl.setDbId(42);

        return Arrays.asList(new Object[][]{
                {
                        new UserDefinedLink(),
                        "<user-defined-link/>"
                },
                {
                        udl,
                        "<user-defined-link>\n" +
                        "   <node-id-a>1</node-id-a>\n" +
                        "   <component-label-a>a</component-label-a>\n" +
                        "   <node-id-z>2</node-id-z>\n" +
                        "   <component-label-z>z</component-label-z>\n" +
                        "   <link-id>id</link-id>\n" +
                        "   <link-label>label</link-label>\n" +
                        "   <owner>me</owner>\n" +
                        "   <db-id>42</db-id>\n" +
                        "</user-defined-link>"
                }
        });
    }

    @Test
    @Override
    public void marshalJaxbUnmarshalJaxb() {
        // ignore this since we don't implement equals and hashCode on the Hibernate entity bean
    }

    @Test
    @Override
    public void unmarshalXmlAndCompareToJaxb() {
        // ignore this since we don't implement equals and hashCode on the Hibernate entity bean
    }
}
