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
package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.DefaultForeignSourceService;
import org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;


public class DefaultForeignSourceServiceTest {
    private FilesystemForeignSourceRepository m_deployed;
    private FilesystemForeignSourceRepository m_pending;
    private ForeignSourceService m_service;

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(new File("target/foreign-sources"));
        FileUtils.deleteQuietly(new File("target/imports"));

        m_deployed  = new FilesystemForeignSourceRepository();
        m_deployed.setForeignSourcePath("target/foreign-sources/deployed");
        m_deployed.setRequisitionPath("target/imports/deployed");

        m_pending = new FilesystemForeignSourceRepository();
        m_pending.setForeignSourcePath("target/foreign-sources");
        m_pending.setRequisitionPath("target/imports");
        
        m_service = new DefaultForeignSourceService();
        m_service.setDeployedForeignSourceRepository(m_deployed);
        m_service.setPendingForeignSourceRepository(m_pending);
    }

    @Test
    public void integrationTest() throws Exception {
        assertTrue(m_deployed.getForeignSources().isEmpty());
        assertTrue(m_pending.getForeignSources().isEmpty());

        assertEquals(0, m_service.getAllForeignSources().size());

        // create a new foreign source
        ForeignSource fs = m_service.getForeignSource("test");

        // test doesn't exist, so it should tell us that it's based on the default foreign source
        assertTrue(fs.isDefault());

        // modify it and save
        fs.setDetectors(new ArrayList<PluginConfig>());
        m_service.saveForeignSource("test", fs);
        
        // now it shouln't be marked as default, since we've saved a modified version
        fs = m_service.getForeignSource("test");
        assertFalse(fs.isDefault());

        // we like it so much, let's make it the default!
        m_service.saveForeignSource("default", fs);
        fs = m_service.getForeignSource("monkey");
        assertTrue(fs.isDefault());
    }
}
