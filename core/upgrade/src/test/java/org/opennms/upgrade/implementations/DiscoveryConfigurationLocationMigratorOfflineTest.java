package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryConfigurationLocationMigratorOfflineTest {
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryConfigurationMigratorOfflineTest.class);

    @Rule
    public TemporaryFolder m_tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc3"), m_tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", m_tempFolder.getRoot().getAbsolutePath());
        final List<File> files = new ArrayList<>(FileUtils.listFilesAndDirs(new File(m_tempFolder.getRoot(), "etc"), TrueFileFilter.TRUE, TrueFileFilter.INSTANCE));
        Collections.sort(files);
    }

    @Test
    public void testRemoveAttribute() throws Exception {
        final DiscoveryConfigurationLocationMigratorOffline task = new DiscoveryConfigurationLocationMigratorOffline();
        task.preExecute();
        task.execute();
        task.postExecute();

        final File configFile = new File(m_tempFolder.getRoot(), "etc/discovery-configuration.xml");
        final DiscoveryConfiguration discoveryConfiguration = CastorUtils.unmarshal(DiscoveryConfiguration.class, new FileReader(configFile));
        Assert.assertNotNull(discoveryConfiguration);
        Assert.assertEquals(3, discoveryConfiguration.getIncludeRangeCount());
        Assert.assertEquals(3, discoveryConfiguration.getExcludeRangeCount());
        Assert.assertEquals(3, discoveryConfiguration.getSpecificCount());
        Assert.assertEquals(3, discoveryConfiguration.getIncludeUrlCount());

        int pittsboroLocation = 0;
        int oldDefaultLocation = 0;
        int newDefaultLocation = 0;
        int nullLocation = 0;

        for(IncludeRange e :discoveryConfiguration.getIncludeRangeCollection()) {
            if (DiscoveryConfigurationLocationMigratorOffline.NEW_DEFAULT_LOCATION.equals(e.getLocation())) {
                newDefaultLocation++;
            }

            if (DiscoveryConfigurationLocationMigratorOffline.OLD_DEFAULT_LOCATION.equals(e.getLocation())) {
                oldDefaultLocation++;
            }

            if (e.getLocation() == null) {
                nullLocation++;
            }

            if ("pittsboro".equals(e.getLocation())) {
                pittsboroLocation++;
            }
        }

        Assert.assertEquals(1, newDefaultLocation);
        Assert.assertEquals(1, pittsboroLocation);
        Assert.assertEquals(1, nullLocation);
        Assert.assertEquals(0, oldDefaultLocation);

        pittsboroLocation = 0;
        oldDefaultLocation = 0;
        newDefaultLocation = 0;
        nullLocation = 0;

        for(Specific e : discoveryConfiguration.getSpecificCollection()) {
            if (DiscoveryConfigurationLocationMigratorOffline.NEW_DEFAULT_LOCATION.equals(e.getLocation())) {
                newDefaultLocation++;
            }

            if (DiscoveryConfigurationLocationMigratorOffline.OLD_DEFAULT_LOCATION.equals(e.getLocation())) {
                oldDefaultLocation++;
            }

            if (e.getLocation() == null) {
                nullLocation++;
            }

            if ("pittsboro".equals(e.getLocation())) {
                pittsboroLocation++;
            }
        }

        Assert.assertEquals(1, newDefaultLocation);
        Assert.assertEquals(1, pittsboroLocation);
        Assert.assertEquals(1, nullLocation);
        Assert.assertEquals(0, oldDefaultLocation);

        pittsboroLocation = 0;
        oldDefaultLocation = 0;
        newDefaultLocation = 0;
        nullLocation = 0;

        for(IncludeUrl e : discoveryConfiguration.getIncludeUrlCollection()) {
            if (DiscoveryConfigurationLocationMigratorOffline.NEW_DEFAULT_LOCATION.equals(e.getLocation())) {
                newDefaultLocation++;
            }

            if (DiscoveryConfigurationLocationMigratorOffline.OLD_DEFAULT_LOCATION.equals(e.getLocation())) {
                oldDefaultLocation++;
            }

            if (e.getLocation() == null) {
                nullLocation++;
            }

            if ("pittsboro".equals(e.getLocation())) {
                pittsboroLocation++;
            }
        }

        Assert.assertEquals(1, newDefaultLocation);
        Assert.assertEquals(1, pittsboroLocation);
        Assert.assertEquals(1, nullLocation);
        Assert.assertEquals(0, oldDefaultLocation);
    }
}
