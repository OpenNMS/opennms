/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jrobin.core.RrdDb;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Test Class for JmxRrdMigratorOffline.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class JmxRrdMigratorOfflineTest {

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        FileUtils.copyDirectory(new File("src/test/resources/rrd"), new File("target/home/rrd"));
        FileUtils.copyDirectory(new File("src/test/resources/WEB-INF"), new File("target/home/jetty-webapps/opennms/WEB-INF/"));
        System.setProperty("opennms.home", "target/home");
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
    }

    /**
     * Test upgrade.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgrade() throws Exception {
        JmxRrdMigratorOffline obj = new JmxRrdMigratorOffline();
        obj.preExecute();
        obj.execute();
        obj.postExecute();
        Assert.assertEquals(60, obj.badMetrics.size());
        File rrdDir = new File("target/home/rrd/1/opennms-jvm/");
        for (File file : getFiles(rrdDir, ".jrb")) {
            RrdDb jrb = new RrdDb(file, true);
            String ds = jrb.getDsNames()[0];
            jrb.close();
            Assert.assertFalse(ds.contains("."));
            Assert.assertEquals(file.getName(), ds + ".jrb");
        }
        for (File file : getFiles(rrdDir, ".meta")) {
            String ds = file.getName().replaceFirst("\\.meta", "");
            Properties p = new Properties();
            p.load(new FileReader(file));
            for (Object o : p.keySet()) {
                String key = (String) o;
                Assert.assertTrue(key.endsWith(ds));
                Assert.assertEquals(ds, p.getProperty(key));
            }
        }
    }

    /**
     * Gets the files.
     *
     * @param resourceDir the resource directory
     * @param ext the extension
     * @return the files
     */
    private File[] getFiles(final File resourceDir, final String ext) {
        return resourceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(ext);
            }
        });
    }

}
