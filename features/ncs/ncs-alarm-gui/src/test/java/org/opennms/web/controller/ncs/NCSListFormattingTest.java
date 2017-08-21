/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.ncs;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerConfigurationException;

import org.junit.Test;
import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;

public class NCSListFormattingTest {
    
    @Test
    public void testMarshallComponent() throws JAXBException, UnsupportedEncodingException, TransformerConfigurationException {
        NCSComponent svc = new NCSBuilder("Service", "NA-Service", "123")
        .setName("CokeP2P")
        .pushComponent("ServiceElement", "NA-ServiceElement", "8765")
            .setName("PE1:SE1")
            .setNodeIdentity("space", "1111-PE1")
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:jnxVpnIf")
                .setName("jnxVpnIf")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
                .setAttribute("jnxVpnIfVpnType", "5")
                .setAttribute("jnxVpnIfVpnName", "ge-1/0/2.50")
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:link")
                    .setName("link")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
                    .setAttribute("linkName", "ge-1/0/2")
                .popComponent()
            .popComponent()
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:jnxVpnPw-vcid(50)")
                .setName("jnxVpnPw-vcid(50)")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
                .setAttribute("jnxVpnPwVpnType", "5")
                .setAttribute("jnxVpnPwVpnName", "ge-1/0/2.50")
                .setDependenciesRequired(DependencyRequirements.ANY)
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:lspA-PE1-PE2")
                    .setName("lspA-PE1-PE2")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspA-PE1-PE2")
                .popComponent()
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:lspB-PE1-PE2")
                    .setName("lspB-PE1-PE2")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspB-PE1-PE2")
                .popComponent()
            .popComponent()
        .popComponent()
        .pushComponent("ServiceElement", "NA-ServiceElement", "9876")
            .setName("PE2:SE1")
            .setNodeIdentity("space", "2222-PE2")
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnIf")
                .setName("jnxVpnIf")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
                .setAttribute("jnxVpnIfVpnType", "5")
                .setAttribute("jnxVpnIfVpnName", "ge-3/1/4.50")
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:link")
                    .setName("link")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
                    .setAttribute("linkName", "ge-3/1/4")
                .popComponent()
            .popComponent()
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnPw-vcid(50)")
                .setName("jnxVpnPw-vcid(50)")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
                .setAttribute("jnxVpnPwVpnType", "5")
                .setAttribute("jnxVpnPwVpnName", "ge-3/1/4.50")
                .setDependenciesRequired(DependencyRequirements.ANY)
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:lspA-PE2-PE1")
                    .setName("lspA-PE2-PE1")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspA-PE2-PE1")
                .popComponent()
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:lspB-PE2-PE1")
                    .setName("lspB-PE2-PE1")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspB-PE2-PE1")
                .popComponent()
            .popComponent()
        .popComponent()
        .get();
        
        
        
        String result = "<ul>\n" + getComponentHTML(svc) + "</ul>";
        assertEquals(getExpectedList(), result);
    }
    
    private String getExpectedList() {
        return "<ul>\n" +
        		"<li>CokeP2P<ul>\n" +
        		"<li>PE1:SE1<ul>\n" +
        		"<li>jnxVpnIf<ul>\n" +
        		"<li>link</li>\n" +
        		"</ul></li>\n" +
        		"<li>jnxVpnPw-vcid(50)<ul>\n" +
        		"<li>lspA-PE1-PE2</li>\n" +
        		"<li>lspB-PE1-PE2</li>\n" +
        		"</ul></li>\n" +
        		"</ul></li>\n" +
        		"<li>PE2:SE1<ul>\n" +
        		"<li>jnxVpnIf<ul>\n" +
        		"<li>link</li>\n" +
        		"</ul></li>\n" +
        		"<li>jnxVpnPw-vcid(50)<ul>\n" +
        		"<li>lspA-PE2-PE1</li>\n" +
        		"<li>lspB-PE2-PE1</li>\n" +
        		"</ul></li>\n" +
        		"</ul></li>\n" +
        		"</ul></li>\n" +
        		"</ul>";
    }

    @Test
    public void testRecursiveMethod() {
        NCSComponent svc = new NCSBuilder("Service", "NA-Service", "123")
        .setName("CokeP2P")
        .pushComponent("ServiceElement", "NA-ServiceElement", "8765")
            .setName("PE1:SE1")
            .setNodeIdentity("space", "1111-PE1")
        .popComponent()
        .pushComponent("ServiceElement", "NA-ServiceElement", "8766")
            .setName("PE2:SE2")
        .popComponent()
        .get();
        
        String expected = "<ul>\n" +
        		"<li>CokeP2P<ul>\n" +
        		"<li>PE1:SE1</li>\n" +
        		"<li>PE2:SE2</li>\n" +
        		"</ul>" +
        		"</li>\n" +
        		"</ul>";
        
        String result = "<ul>\n" + getComponentHTML(svc) + "</ul>";
        assertEquals(expected, result);
        System.err.println("recursive: " + result);
    }
    
    
    private String getComponentHTML(NCSComponent component) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("<li>");
        buffer.append(component.getName());
        
        Set<NCSComponent> subcomponents = component.getSubcomponents();
        if(subcomponents.size() > 0) {
            buffer.append("<ul>\n");
            for(NCSComponent c : subcomponents) {
                buffer.append(getComponentHTML(c));
            }
            buffer.append("</ul>");
        }
        
        buffer.append("</li>\n");
        return buffer.toString();
        
    }
    
    private void printJaxbXML(NCSComponent svc) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(NCSComponent.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        // save the output in a byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // marshall the output
        marshaller.marshal(svc, out);

        // verify its matches the expected results
        byte[] utf8 = out.toByteArray();

        String result = new String(utf8, StandardCharsets.UTF_8);
        
        System.err.println(result);
    }

}
