/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.junit.Test;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class DeviceConfigTest {

    @Test
    public void testMarshalling() throws javax.xml.bind.JAXBException {
        String scriptOutput = "HP J8692A Switch 3500yl-24G\r\r\nSoftware revision K.16.02.0026\r\r\n\r\r\n" +
                " (C) Copyright 2018 Hewlett Packard Enterprise Development LP\r\n" +
                "RESTRICTED RIGHTS LEGEND\r\n Confidential computer software. Valid license from Hewlett Packard Enterprise\r\n" +
                "Development LP required for possession, use or copying. Consistent with FAR\r\n" +
                "12.211 and 12.212, Commercial Computer Software, Computer Software\r\n" +
                "Documentation, and Technical Data for Commercial Items are licensed to the\r\n" +
                "U.S. Government under vendor's standard commercial license.\r\n" +
                "^[[1;13r^[[1;1H^[[24;1HPress any key to continue\r\n" +
                "^[[13;1H^[[?25h^[[24;27H^[[?6l^[[1;24r^[[?7h^[[2J^[[1;1H^[[1920;1920H^[[6n^[[1;1H";

        DeviceConfig deviceConfig = new DeviceConfig(scriptOutput);
        JAXBContext  jaxbContext = (JAXBContext) JAXBContextFactory.createContext(new Class[]{DeviceConfig.class}, null);

        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(deviceConfig, stringWriter);
        String xmlOutput = stringWriter.toString();

        assertNotNull(xmlOutput);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><device-config scriptOutput=\"HP J8692A Switch 3500yl-24G&amp;#xd;&amp;#xd;&amp;#xa;Software revision K.16.02.0026&amp;#xd;&amp;#xd;&amp;#xa;&amp;#xd;&amp;#xd;&amp;#xa; (C) Copyright 2018 Hewlett Packard Enterprise Development LP&amp;#xd;&amp;#xa;RESTRICTED RIGHTS LEGEND&amp;#xd;&amp;#xa; Confidential computer software. Valid license from Hewlett Packard Enterprise&amp;#xd;&amp;#xa;Development LP required for possession, use or copying. Consistent with FAR&amp;#xd;&amp;#xa;12.211 and 12.212, Commercial Computer Software, Computer Software&amp;#xd;&amp;#xa;Documentation, and Technical Data for Commercial Items are licensed to the&amp;#xd;&amp;#xa;U.S. Government under vendor's standard commercial license.&amp;#xd;&amp;#xa;^[[1;13r^[[1;1H^[[24;1HPress any key to continue&amp;#xd;&amp;#xa;^[[13;1H^[[?25h^[[24;27H^[[?6l^[[1;24r^[[?7h^[[2J^[[1;1H^[[1920;1920H^[[6n^[[1;1H\"/>",xmlOutput);

    }

    @Test
    public void testUnmarshalling() throws javax.xml.bind.JAXBException {

        String xmlInput = "<device-config content=\"\" filename=\"startup-config\" " +
                "scriptOutput=\"HP J8692A Switch 3500yl-24G&#xd;&#xd;&#xa;Software revision K.16.02.0026&#xd;&#xd;&#xa;&#xd;&#xd;&#xa; " +
                "(C) Copyright 2018 Hewlett Packard Enterprise Development LP&#xd;&#xa;&#xd;&#xa;RESTRICTED RIGHTS LEGEND&#xd;&#xa;" +
                " Confidential computer software. Valid license from Hewlett Packard Enterprise&#xd;&#xa;Development LP required for possession, use or copying. Consistent with FAR&#xd;&#xa;" +
                "12.211 and 12.212, Commercial Computer Software, Computer Software&#xd;&#xa;Documentation, and Technical Data for Commercial Items are licensed to the&#xd;&#xa;" +
                "U.S. Government under vendor's standard commercial license.&#xd;&#xa;&#xd;&#xa;^[[1;13r^[[1;1H^[[24;1HPress any key to continue^[[13;1H^[[?25h^[[24;27H^[[?6l^[[1;24r^[[?7h^[[2J^[[1;1H^[[1920;1920H^[[6n^[[1;1H\"/>";

        JAXBContext jaxbContext = (JAXBContext) JAXBContextFactory.createContext(new Class[]{DeviceConfig.class}, null);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        StringReader stringReader = new StringReader(xmlInput);
        DeviceConfig deviceConfig = (DeviceConfig) unmarshaller.unmarshal(stringReader);

        String expectedOutputStartWith = "HP J8692A Switch 3500yl-24G\r\r\nSoftware revision K.16.02.0026\r\r\n\r\r\n" +
                " (C) Copyright 2018 Hewlett Packard Enterprise Development LP\r\n" ;
        String expectedOutputEndWith =  "^[[1;13r^[[1;1H^[[24;1HPress any key to continue^[[13;1H^[[?25h^[[24;27H^[[?6l^[[1;24r^[[?7h^[[2J^[[1;1H^[[1920;1920H^[[6n^[[1;1H";
        String expectedOutputMiddleString = "12.211 and 12.212, Commercial Computer Software, Computer Software\r\n" +
                "Documentation, and Technical Data for Commercial Items are licensed to the\r\n" ;

        assertNotNull(deviceConfig);
        assertTrue(deviceConfig.getScriptOutput().contains(expectedOutputStartWith));
        assertTrue(deviceConfig.getScriptOutput().contains(expectedOutputMiddleString));
        assertTrue(deviceConfig.getScriptOutput().contains(expectedOutputEndWith));
    }
}
