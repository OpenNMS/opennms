/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.outages.api.WriteablePollOutagesDao;
import org.opennms.netmgt.config.dao.outages.impl.AbstractPollOutagesDao;
import org.opennms.netmgt.config.dao.outages.impl.OnmsPollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class)
public class PollOutagesConfigPersistenceIT {
    private WriteablePollOutagesDao pollOutagesDao;

    @Autowired
    private JsonStore jsonStore;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private static final Resource BASE_CONFIG = new ClassPathResource("etc/poll-outages.xml");
    private File testDir;

    @Before
    public void setPollOutagesDao() throws IOException {
        testDir = tempFolder.newFolder("etc");
        Files.copy(Paths.get(BASE_CONFIG.getURI()), Paths.get(testDir.getAbsolutePath(), "poll-outages.xml"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());
        pollOutagesDao = new OnmsPollOutagesDao(jsonStore);
    }
    
    @Test
    public void canStoreInitialConfig() {
        waitForInitialConfig();
    }
    
    @Test
    public void configChangeIsPropogated() throws IOException {
        waitForInitialConfig();
        
        // Verify the initial config we see in the DB matches the one on the FS
        Path resource = Paths.get(testDir.getAbsolutePath(), "/poll-outages.xml");
        Outages fromFile = JaxbUtils.unmarshal(Outages.class, resource.toFile());
        Outages fromDB = pollOutagesDao.getReadOnlyConfig();
        assertThat(fromFile, equalTo(fromDB));
        
        // Write a new config to the FS then reload, now expect the DB copy to match the new FS copy
        String newOutageConfig = "<?xml version=\"1.0\"?>\n" +
                "<outages xmlns=\"http://xmlns.opennms.org/xsd/config/poller/outages\"></outages>";

        Files.write(resource, newOutageConfig.getBytes());
        fromFile = JaxbUtils.unmarshal(Outages.class, resource.toFile());
        assertThat(fromFile.getOutages().size(), equalTo(0));
        pollOutagesDao.reload();
        fromDB = pollOutagesDao.getReadOnlyConfig();
        assertThat(fromFile, equalTo(fromDB));
    }
    
    private void waitForInitialConfig() {
        await().atMost(10, TimeUnit.SECONDS).until(() ->
                jsonStore.get(AbstractPollOutagesDao.JSON_STORE_KEY,
                        ConfigDaoConstants.JSON_KEY_STORE_CONTEXT).isPresent());
    }
}
