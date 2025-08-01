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
package org.opennms.netmgt.dao.hibernate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class UserDefinedLinkDaoHibernateIT {

    @Autowired
    private UserDefinedLinkDao userDefinedLinkDao;

    @Autowired
    private DatabasePopulator populator;

    @BeforeTransaction
    public void setUp() {
        populator.populateDatabase();
    }

    @AfterTransaction
    public void tearDown() {
        populator.resetDatabase();
    }

    @Test
    @Transactional
    public void canCrudLinks() {
        // No links initially
        assertThat(userDefinedLinkDao.findAll(), hasSize(0));

        OnmsNode node1 = populator.getNode1();
        OnmsNode node2 = populator.getNode2();

        // Create
        UserDefinedLink link = new UserDefinedLink();
        link.setNodeIdA(node1.getId());
        link.setNodeIdZ(node2.getId());
        link.setOwner("test");
        link.setLinkId("my link id");
        link.setLinkLabel("my link");
        userDefinedLinkDao.save(link);

        assertThat(userDefinedLinkDao.findAll(), hasSize(1));

        // In
        assertThat(userDefinedLinkDao.getInLinks(node1.getId()), hasSize(0));
        assertThat(userDefinedLinkDao.getInLinks(node2.getId()), hasSize(1));

        // Out
        assertThat(userDefinedLinkDao.getOutLinks(node1.getId()), hasSize(1));
        assertThat(userDefinedLinkDao.getOutLinks(node2.getId()), hasSize(0));

        // Lookup by label
        assertThat(userDefinedLinkDao.getLinksWithLabel("my link"), hasSize(1));

        // Update
        link.setLinkLabel("not your link");
        userDefinedLinkDao.update(link);

        assertThat(userDefinedLinkDao.getLinksWithLabel("my link"), hasSize(0));
        assertThat(userDefinedLinkDao.getLinksWithLabel("not your link"), hasSize(1));

        // Delete
        userDefinedLinkDao.delete(link);

        // Back to empty
        assertThat(userDefinedLinkDao.findAll(), hasSize(0));
    }
}
