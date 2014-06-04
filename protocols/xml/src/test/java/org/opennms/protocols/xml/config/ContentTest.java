/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.protocols.xml.config;

import net.sf.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.http.FormFields;
import org.opennms.protocols.xml.config.Request;

/**
 * The Test Class for Content.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ContentTest {

    /**
     * Test XML content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testXmlContent() throws Exception {
        String xml = "<request method='POST'>\n"
                + "  <parameter name='retries' value='3'/>\n"
                + "  <parameter name='timeout' value='5000'/>\n"
                + "  <header name='User-Agent' value='Chrome'/>\n"
                + "  <header name='Host' value='{nodeLabel}'/>\n"
                + "  <content type='application/xml'><![CDATA[\n"
                + "    <person><firstName>Alejandro</firstName><lastName>Galue</lastName></person>\n"
                + "  ]]></content>\n"
                + "</request>";
        Request request = JaxbUtils.unmarshal(Request.class, xml);
        Assert.assertNotNull(request);
        Assert.assertEquals("Chrome", request.getHeaders().get(0).getValue());
        Assert.assertEquals("3", request.getParameters().get(0).getValue());
        Assert.assertEquals("application/xml", request.getContent().getType());
        String subXml = request.getContent().getData();
        Assert.assertNotNull(subXml);
        Person p = JaxbUtils.unmarshal(Person.class, subXml);
        Assert.assertEquals("Alejandro", p.getFirstName());
    }

    /**
     * Test JSON content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testJsonContent() throws Exception {
        String xml = "<request method='POST'>\n"
                + "  <parameter name='retries' value='3'/>\n"
                + "  <parameter name='timeout' value='5000'/>\n"
                + "  <header name='User-Agent' value='Chrome'/>\n"
                + "  <header name='Host' value='{nodeLabel}'/>\n"
                + "  <content type='application/json'><![CDATA[\n"
                + "    { person: { firstName: 'Alejandro', lastName: 'Galue' } }"
                + "  ]]></content>\n"
                + "</request>";
        Request request = JaxbUtils.unmarshal(Request.class, xml);
        Assert.assertNotNull(request);
        Assert.assertEquals("Chrome", request.getHeaders().get(0).getValue());
        Assert.assertEquals("3", request.getParameters().get(0).getValue());
        Assert.assertEquals("application/json", request.getContent().getType());
        String json = request.getContent().getData();
        JSONObject o = JSONObject.fromObject(json);
        Person p = (Person) JSONObject.toBean(o.getJSONObject("person"), Person.class);
        Assert.assertNotNull(p);
        Assert.assertEquals("Alejandro", p.getFirstName());
    }

    /**
     * Test Form Urlencoded content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testFormUrlencodedContent() throws Exception {
        String xml = "<request method='POST'>\n"
                + "  <parameter name='retries' value='3'/>\n"
                + "  <parameter name='timeout' value='5000'/>\n"
                + "  <header name='User-Agent' value='Chrome'/>\n"
                + "  <header name='Host' value='{nodeLabel}'/>\n"
                + "  <content type='application/x-www-form-urlencoded'><![CDATA[\n"
                + "    <form-fields>\n"
                + "      <form-field name='firstName'>Alejandro</form-field>\n"
                + "      <form-field name='lastName'>Galue</form-field>\n"
                + "    </form-fields>\n"
                + "  ]]></content>\n"
                + "</request>";
        Request request = JaxbUtils.unmarshal(Request.class, xml);
        Assert.assertNotNull(request);
        Assert.assertEquals("Chrome", request.getHeaders().get(0).getValue());
        Assert.assertEquals("3", request.getParameters().get(0).getValue());
        Assert.assertEquals("application/x-www-form-urlencoded", request.getContent().getType());
        FormFields fields = JaxbUtils.unmarshal(FormFields.class, request.getContent().getData());
        Assert.assertNotNull(fields);
        Assert.assertNotNull(fields.getEntity());
        Assert.assertEquals("Alejandro", fields.get(0).getValue());
    }

}
