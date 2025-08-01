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
package org.opennms.protocols.xml.dao.jaxb;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.test.ThrowableAnticipator;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * The Class XmlDataCollectionConfigDaoJaxbTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigDaoJaxbTest {

    /**
     * Test after properties set with no configuration set.
     */
    @Test
    public void testAfterPropertiesSetWithNoConfigSet() {
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property configResource must be set and be non-null"));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    /**
     * Test after properties set with bogus file resource.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithBogusFileResource() throws Exception {
        Resource resource = new FileSystemResource("/bogus-file");
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();
        dao.setConfigResource(resource);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new MarshallingResourceFailureException(ThrowableAnticipator.IGNORE_MESSAGE));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    /**
     * Test after properties set with good configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();

        File xmlCollectionConfig= new File("src/test/resources/", XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE);
        assertTrue(XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE + " is readable", xmlCollectionConfig.canRead());
        InputStream in = new FileInputStream(xmlCollectionConfig);

        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();

        Assert.assertNotNull("xml data collection should not be null", dao.getConfig());
    }

    /**
     * Test after properties set with nested files (external references to XML groups).
     * 
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithNestedFiles() throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();

        File xmlCollectionConfig= new File("src/test/resources/etc", XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE);
        assertTrue(XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE + " is readable", xmlCollectionConfig.canRead());
        InputStream in = new FileInputStream(xmlCollectionConfig);

        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        XmlDataCollectionConfig config = dao.getConfig();
        Assert.assertNotNull("xml data collection should not be null", config);
        Assert.assertEquals(2, config.getXmlDataCollections().get(0).getXmlSources().get(0).getXmlGroups().size());
        Assert.assertEquals(5, config.getXmlDataCollections().get(1).getXmlSources().get(0).getXmlGroups().size());
    }

}
