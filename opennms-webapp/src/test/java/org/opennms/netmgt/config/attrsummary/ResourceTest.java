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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.attrsummary;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ResourceTest extends XmlTestNoCastor<Resource> {

    public ResourceTest(final Resource sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Resource emptyResource = new Resource("foo");
        final Attribute emptyAttr = new Attribute("afoo");
        final Attribute attrWithStuff = new Attribute("abar", 1.0d, 2.0d, 3.0d);

        final Resource withAttribute = new Resource("bar");
        withAttribute.addAttribute(emptyAttr);

        final Resource withMultipleAttributes = new Resource("baz");
        withMultipleAttributes.addAttribute(emptyAttr);
        withMultipleAttributes.addAttribute(attrWithStuff);

        final Resource withSubResource = new Resource("quux");
        withSubResource.addAttribute(emptyAttr);
        withSubResource.addResource(emptyResource);
        withSubResource.addResource(withMultipleAttributes);

        return Arrays.asList(new Object[][] {
            {
                emptyResource,
                "<resource>"
                + "<name>foo</name>"
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withAttribute,
                "<resource>"
                + "<name>bar</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withMultipleAttributes,
                "<resource>"
                + "<name>baz</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "<attribute>"
                +   "<name>abar</name>"
                +   "<min>1.0</min>"
                +   "<average>2.0</average>"
                +   "<max>3.0</max>"
                + "</attribute>"
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withSubResource,
                "<resource>"
                + "<name>quux</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "<resource>"
                +   "<name>foo</name>"
                + "</resource>"
                + "<resource>"
                +   "<name>baz</name>"
                +   "<attribute>"
                +     "<name>afoo</name>"
                +   "</attribute>"
                +   "<attribute>"
                +     "<name>abar</name>"
                +     "<min>1.0</min>"
                +     "<average>2.0</average>"
                +     "<max>3.0</max>"
                +   "</attribute>"
                + "</resource>"
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            }
        });
    }
}