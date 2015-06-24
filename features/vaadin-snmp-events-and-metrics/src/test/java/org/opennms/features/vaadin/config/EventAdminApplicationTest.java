/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.config;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * The Class EventAdminApplicationTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class EventAdminApplicationTest {

    /**
     * Test normalize filename.
     * <p>This is related to issue <a href="http://issues.opennms.org/browse/SPC-482">SPC-482</a></p>
     *
     * @throws Exception the exception
     */
    @Test
    public void testNormalizeFilename() throws Exception {
        EventAdminApplication app = new EventAdminApplication();
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data"));
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data."));
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data.Xml"));
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data.events"));
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data.events."));
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data.events.xml"));
        Assert.assertEquals("data.events.xml", app.normalizeFilename("data.events.XML"));
    }

    /**
     * Test get relative file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRelativeFile() throws Exception {
        File f = new File("/opt/opennms/etc/events/JuniperEvents/syslog/tca_syslog.xml");
        String file = f.getAbsolutePath().replaceFirst(".*\\/events\\/(.*)", "events/$1");
        Assert.assertEquals("events/JuniperEvents/syslog/tca_syslog.xml", file);
    }

}
