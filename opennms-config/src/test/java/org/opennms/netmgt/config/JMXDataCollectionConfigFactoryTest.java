package org.opennms.netmgt.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class JMXDataCollectionConfigFactoryTest {

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
