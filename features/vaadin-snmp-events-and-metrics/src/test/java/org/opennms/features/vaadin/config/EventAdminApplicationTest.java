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
