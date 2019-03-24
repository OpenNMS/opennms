/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
