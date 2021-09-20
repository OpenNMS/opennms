/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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
        FileUtils.copyDirectory(new File("src/test/resources/jetty-webapps/opennms/WEB-INF"), new File("target/home/jetty-webapps/opennms/WEB-INF/"));
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
     * Test upgrade (single-metric JRBs, i.e. storeByGroup=false).
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgradeSingleMetric() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/rrd"), new File("target/home/rrd"));

        JmxRrdMigratorOffline jmxMigrator = executeMigrator();

        File rrdDir = new File("target/home/rrd/1/opennms-jvm/");
        for (File file : getFiles(rrdDir, ".jrb")) {
            RrdDb jrb = new RrdDb(file, true);
            String ds = jrb.getDsNames()[0];
            jrb.close();
            Assert.assertFalse(ds.contains("."));
            Assert.assertEquals(file.getName(), ds + ".jrb");
            Assert.assertEquals(ds, jmxMigrator.getFixedDsName(ds));
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
     * Test upgrade (multi-metric JRBs, i.e. storeByGroup=true).
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgradeMultiMetric() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/rrd2"), new File("target/home/rrd"));

        File config = new File("target/home/etc/opennms.properties");
        Properties p = new Properties();
        p.load(new FileReader(config));
        p.setProperty("org.opennms.rrd.storeByGroup", "true");
        p.store(new FileWriter(config), null);

        executeMigrator();

        File jrbFile = new File("target/home/rrd/1/opennms-jvm/java_lang_type_MemoryPool_name_Survivor_Space.jrb");
        Assert.assertTrue(jrbFile.exists());
        RrdDb jrb = new RrdDb(jrbFile, true);
        String[] dataSources = jrb.getDsNames();
        jrb.close();

        Properties dsProp = new Properties();
        dsProp.load(new FileReader("target/home/rrd/1/opennms-jvm/ds.properties"));

        Properties meta = new Properties();
        meta.load(new FileReader("target/home/rrd/1/opennms-jvm/java_lang_type_MemoryPool_name_Survivor_Space.meta"));

        for (String ds : dataSources) {
            Assert.assertFalse(ds.contains("."));
            Assert.assertEquals("java_lang_type_MemoryPool_name_Survivor_Space", dsProp.getProperty(ds));
            Assert.assertEquals(ds, meta.getProperty("JMX_java.lang:type=MemoryPool.name.Survivor Space." + ds));
        }
    }

    /**
     * Executes the JMX Migrator.
     *
     * @return the JMX Migrator object
     * @throws Exception the exception
     */
    private JmxRrdMigratorOffline executeMigrator() throws Exception {
        JmxRrdMigratorOffline jmxMigrator = new JmxRrdMigratorOffline();
        jmxMigrator.preExecute();
        jmxMigrator.execute();
        jmxMigrator.postExecute();
        Assert.assertEquals(60, jmxMigrator.badMetrics.size());

        // Verify graph templates
        File templates = new File("target/home/etc/snmp-graph.properties.d/jvm-graph.properties");
        Pattern defRegex = Pattern.compile("DEF:.+:(.+\\..+):");
        Pattern colRegex = Pattern.compile("\\.columns=(.+)$");
        for (LineIterator it = FileUtils.lineIterator(templates); it.hasNext();) {
            String line = it.next();
            Matcher m = defRegex.matcher(line);
            if (m.find()) {
                String ds = m.group(1);
                if (jmxMigrator.badMetrics.contains(ds)) {
                    Assert.fail("Bad metric found");
                }
            }
            m = colRegex.matcher(line);
            if (m.find()) {
                String[] badColumns = m.group(1).split(",(\\s)?");
                if (jmxMigrator.badMetrics.containsAll(Arrays.asList(badColumns))) {
                    Assert.fail("Bad metric found");
                }
            }
        }

        // Verify metric definitions
        File metrics = new File("target/home/etc/jmx-datacollection-config.xml");
        Pattern aliasRegex = Pattern.compile("alias=\"([^\"]+\\.[^\"]+)\"");
        for (LineIterator it = FileUtils.lineIterator(metrics); it.hasNext();) {
            String line = it.next();
            Matcher m = aliasRegex.matcher(line);
            if (m.find()) {
                String ds = m.group(1);
                if (jmxMigrator.badMetrics.contains(ds)) {
                    Assert.fail("Bad metric found");
                }
            }
        }

        return jmxMigrator;
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
