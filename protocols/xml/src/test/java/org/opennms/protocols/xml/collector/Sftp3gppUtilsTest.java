/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * The Test Class for Sftp3gppUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppUtilsTest {

    /**
     * Test parser.
     *
     * @throws Exception the exception
     */
    @Test
    public void testParser() throws Exception {
        String format = Sftp3gppUtils.get3gppFormat("cdmaSc");
        Assert.assertEquals("system|/=/v=1/sg-name=<mmeScSgName>|", format);
        Map<String,String> properties = Sftp3gppUtils.get3gppProperties(format, "system|/=/v=1/sg-name=GA|");
        Assert.assertEquals(3, properties.size());
        Assert.assertEquals("system|/=/v=1/sg-name=GA|", properties.get("instance"));
        Assert.assertEquals("GA", properties.get("sg-name"));
        Assert.assertEquals("sg-name=GA", properties.get("label"));

        format = Sftp3gppUtils.get3gppFormat("gbBssgp");
        Assert.assertEquals("nse|/=/v=1/nse-id=<nseNumber>|/=/v=1/sg-name=<sgsnGtlSgName>/su-number=<n>", format);
        properties = Sftp3gppUtils.get3gppProperties(format, "nse|/=/v=1/nse-id=1201|/=/v=1/sg-name=GB71/su-number=1");
        Assert.assertEquals(5, properties.size());
        Assert.assertEquals("nse|/=/v=1/nse-id=1201|/=/v=1/sg-name=GB71/su-number=1", properties.get("instance"));
        Assert.assertEquals("1201", properties.get("nse-id"));
        Assert.assertEquals("GB71", properties.get("sg-name"));
        Assert.assertEquals("1", properties.get("su-number"));
        Assert.assertEquals("nse-id=1201, sg-name=GB71, su-number=1", properties.get("label"));

        format = Sftp3gppUtils.get3gppFormat("platformSystemFilesystem");
        Assert.assertEquals("disk|/=/v=1/frame=<frame>/shelf=<shelf>/slot=<slot>/sub-slot=<sub-slot>/name=<directory path>|", format);
        properties = Sftp3gppUtils.get3gppProperties(format, "disk|/=/v=1/frame=0/shelf=0/slot=2/sub-slot=0/name=\\/opt\\/hitachi\\/agw\\/data\\/trace|");
        Assert.assertEquals(7, properties.size());
        Assert.assertEquals("0", properties.get("frame"));
        Assert.assertEquals("0", properties.get("shelf"));
        Assert.assertEquals("2", properties.get("slot"));
        Assert.assertEquals("0", properties.get("sub-slot"));
        Assert.assertEquals("/opt/hitachi/agw/data/trace", properties.get("name"));
        Assert.assertEquals("frame=0, shelf=0, slot=2, sub-slot=0, name=/opt/hitachi/agw/data/trace", properties.get("label"));
    }

    /**
     * Test NMS-6365 (measObjLdn without PM Group information)
     *
     * @throws Exception the exception
     */
    @Test
    public void testNMS6365() throws Exception {
        String format = Sftp3gppUtils.get3gppFormat("dnsDns");
        Map<String,String> properties = Sftp3gppUtils.get3gppProperties(format, "system|/service=callp1|");
        Assert.assertEquals("system|/service=callp1|", properties.get("label"));

        System.setProperty("org.opennms.collectd.xml.3gpp.useSimpleParserForMeasObjLdn", "true");
        properties = Sftp3gppUtils.get3gppProperties(format, "system|/service=callp1|");
        Assert.assertEquals("/service=callp1", properties.get("label"));
    }
}
