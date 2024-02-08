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
package org.opennms.netmgt.poller;

import static org.awaitility.Awaitility.await;
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
