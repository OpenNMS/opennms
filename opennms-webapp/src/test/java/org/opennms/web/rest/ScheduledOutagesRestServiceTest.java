/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.io.File;

import javax.xml.bind.JAXBContext;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.poller.Outage;
import org.springframework.core.io.FileSystemResource;

public class ScheduledOutagesRestServiceTest extends AbstractSpringJerseyRestTestCase {

    private JAXBContext m_jaxbContext;
    private File m_outagesConfig;

    @Override
    public void beforeServletStart() throws Exception {
        File dir = new File("target/test-work-dir");
        dir.mkdirs();

        m_outagesConfig = File.createTempFile("poll-outages-", "xml");

        FileUtils.writeStringToFile(m_outagesConfig, 
                "<?xml version=\"1.0\"?>" +
                "<outages>\n" +
                "  <outage name='my-junit-test' type='weekly'>\n" +
                "    <time day='monday' begins='13:30:00' ends='14:45:00'/>\n" +
                "    <interface address='match-any'/>\n" +
                "  </outage>\n" +
                "</outages>\n");

        PollOutagesConfigFactory factory = new PollOutagesConfigFactory(new FileSystemResource(m_outagesConfig));
        PollOutagesConfigFactory.setInstance(factory);
        m_jaxbContext = JAXBContext.newInstance(Outage.class);
    }

    @Test
    public void testGetOutage() throws Exception {
        String url = "/sched-outages/my-junit-test";
        Outage outage = getXmlObject(m_jaxbContext, url, 200, Outage.class);
        Assert.assertNotNull(outage);
        Assert.assertEquals("match-any", outage.getInterface(0).getAddress());
    }

}
