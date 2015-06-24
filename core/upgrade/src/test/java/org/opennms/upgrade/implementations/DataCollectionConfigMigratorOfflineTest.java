package org.opennms.upgrade.implementations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class DataCollectionConfigMigratorOfflineTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void canSubstitute() throws OnmsUpgradeException, IOException {
        File routersXml = Paths.get(tempFolder.getRoot().getAbsolutePath(), "etc", "datacollection", "routers.xml").toFile();
        assertTrue(routersXml.exists());

        String originalContents = FileUtils.readFileToString(routersXml);

        DataCollectionConfigMigratorOffline task = new DataCollectionConfigMigratorOffline();
        task.preExecute();
        task.execute();
        task.postExecute();

        String updatedContents = FileUtils.readFileToString(routersXml);

        // The original file contains the strings we want to substitute
        assertTrue(originalContents.contains("org.opennms.netmgt.collectd.PersistAllSelectorStrategy"));
        assertTrue(originalContents.contains("org.opennms.netmgt.dao.support.IndexStorageStrategy"));

        // The update file does not contain these
        assertFalse(updatedContents.contains("org.opennms.netmgt.collectd.PersistAllSelectorStrategy"));
        assertFalse(updatedContents.contains("org.opennms.netmgt.dao.support.IndexStorageStrategy"));

        // But it does contain the substituted versions
        assertTrue(updatedContents.contains("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"));
        assertTrue(updatedContents.contains("org.opennms.netmgt.collection.support.IndexStorageStrategy"));
    }
}
