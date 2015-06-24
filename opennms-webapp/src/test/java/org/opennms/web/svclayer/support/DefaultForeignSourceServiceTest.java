/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
