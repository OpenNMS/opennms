/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * The Class JMXDataCollectionConfigDaoTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JMXDataCollectionConfigDaoTest {

    /**
     * Test after properties set with no configuration set.
     */
    @Test
    public void testAfterPropertiesSetWithNoConfigSet() {
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();

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
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();
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
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();

        File jmxCollectionConfig= new File("src/test/resources/org/opennms/netmgt/config/jmx-datacollection-testdata.xml");
        assertTrue("JMX configuration file is readable", jmxCollectionConfig.canRead());
        InputStream in = new FileInputStream(jmxCollectionConfig);

        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();

        Assert.assertNotNull("JMX data collection should not be null", dao.getConfig());
    }

    /**
     * Test after properties set with nested files (external references to XML groups).
     * 
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithNestedFiles() throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();

        File jmxCollectionConfig= new File("src/test/resources/etc/jmx-datacollection-split.xml");
        assertTrue("JMX configuration file is not readable", jmxCollectionConfig.canRead());
        InputStream in = new FileInputStream(jmxCollectionConfig);

        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        JmxDatacollectionConfig config = dao.getConfig();
        Assert.assertNotNull("JMX data collection should not be null", config);
        Assert.assertEquals(8, config.getJmxCollection(0).getMbeanCount());
        Assert.assertEquals(4, config.getJmxCollection(1).getMbeanCount());

        // test access via name
        Assert.assertEquals(8, config.getJmxCollection("jboss").getMbeanCount());
        Assert.assertEquals(4, config.getJmxCollection("jsr160").getMbeanCount());
    }

    @Test
    public void testLoadConfigFile() {
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();
        dao.setConfigResource(new FileSystemResource("src/test/resources/etc/jmx-datacollection-config.xml"));
        dao.afterPropertiesSet();

        JmxDatacollectionConfig config = dao.getConfig();
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getJmxCollectionList());
        Assert.assertEquals(2, config.getJmxCollectionCount());
    }

}
