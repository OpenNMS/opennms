package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JMXDataCollectionConfigFactoryTest {
    @Before
    public void setUp() {
        System.setProperty("opennms.home", new File("target/test-classes").getAbsolutePath());
    }

    @Test
    // Tests that the JmxDataCollectionConfigFactory also supports/implements the split config feature.
    public void shouldSupportSplitConfig() throws FileNotFoundException {
        File jmxCollectionConfig = new File("src/test/resources/etc/jmx-datacollection-split.xml");
        Assert.assertTrue("JMX configuration file is not readable", jmxCollectionConfig.canRead());
        FileInputStream configFileStream = new FileInputStream(jmxCollectionConfig);

        JMXDataCollectionConfigFactory factory = new JMXDataCollectionConfigFactory(configFileStream);

        Assert.assertNotNull(factory.getJmxCollection("jboss"));
        Assert.assertNotNull(factory.getJmxCollection("jsr160"));
        Assert.assertEquals(8, factory.getJmxCollection("jboss").getMbeanCount());
        Assert.assertEquals(4, factory.getJmxCollection("jsr160").getMbeanCount());

    }
}
