/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class VmwareAssetsMigratorOfflineIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    @Autowired
    DatabasePopulator databasePopulator;

    @Autowired
    SessionUtils sessionUtils;

    TemporaryDatabase temporaryDatabase;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();
    }

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        this.temporaryDatabase = database;
    }

    @Test
    public void testMigrator() throws Exception {
        // assert that the columns do not exist anymore due to liquibase changeset '27.0.1-remove-vmware-asset-columns'
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagedobjectid')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwarestate')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagedentitytype')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagementserver')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaretopologyinfo')", Boolean.class));

        // add columns that do not exist anymore due to liquibase changeset
        temporaryDatabase.getJdbcTemplate().execute("ALTER TABLE assets ADD COLUMN vmwaremanagedobjectid TEXT");
        temporaryDatabase.getJdbcTemplate().execute("ALTER TABLE assets ADD COLUMN vmwarestate TEXT");
        temporaryDatabase.getJdbcTemplate().execute("ALTER TABLE assets ADD COLUMN vmwaremanagedentitytype TEXT");
        temporaryDatabase.getJdbcTemplate().execute("ALTER TABLE assets ADD COLUMN vmwaremanagementserver TEXT");
        temporaryDatabase.getJdbcTemplate().execute("ALTER TABLE assets ADD COLUMN vmwaretopologyinfo TEXT");

        // assert that columns exists before migrator run
        assertEquals(true, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagedobjectid')", Boolean.class));
        assertEquals(true, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwarestate')", Boolean.class));
        assertEquals(true, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagedentitytype')", Boolean.class));
        assertEquals(true, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagementserver')", Boolean.class));
        assertEquals(true, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaretopologyinfo')", Boolean.class));

        // assert that no metadata is set in the VMware context for node #2
        assertEquals(Optional.empty(), databasePopulator.getNode2().findMetaDataForContextAndKey("VMware", "managedObjectId"));
        assertEquals(Optional.empty(), databasePopulator.getNode2().findMetaDataForContextAndKey("VMware", "state"));
        assertEquals(Optional.empty(), databasePopulator.getNode2().findMetaDataForContextAndKey("VMware", "managedEntityType"));
        assertEquals(Optional.empty(), databasePopulator.getNode2().findMetaDataForContextAndKey("VMware", "managementServer"));
        assertEquals(Optional.empty(), databasePopulator.getNode2().findMetaDataForContextAndKey("VMware", "topologyInfo"));

        // set VMware-related asset data for node #2
        temporaryDatabase.getJdbcTemplate().execute("UPDATE ASSETS SET vmwaremanagedobjectid = 'fooId', vmwarestate = 'fooState', vmwaremanagedentitytype ='fooType', vmwaremanagementserver = 'fooServer', vmwaretopologyinfo = 'fooTopology' WHERE nodeid = " + databasePopulator.getNode2().getId());

        // run the migrator
        final VmwareAssetsMigratorOffline vmwareAssetsMigratorOffline = new VmwareAssetsMigratorOffline();
        vmwareAssetsMigratorOffline.execute();

        sessionUtils.withReadOnlyTransaction(new Runnable() {
            @Override
            public void run() {
                final OnmsNode reloadedNode = databasePopulator.getNodeDao().get(databasePopulator.getNode2().getId());
                // assert that the metadata is correctly set for node #2
                assertEquals("fooId", reloadedNode.findMetaDataForContextAndKey("VMware", "managedObjectId").get().getValue());
                assertEquals("fooState", reloadedNode.findMetaDataForContextAndKey("VMware", "state").get().getValue());
                assertEquals("fooType", reloadedNode.findMetaDataForContextAndKey("VMware", "managedEntityType").get().getValue());
                assertEquals("fooServer", reloadedNode.findMetaDataForContextAndKey("VMware", "managementServer").get().getValue());
                assertEquals("fooTopology", reloadedNode.findMetaDataForContextAndKey("VMware", "topologyInfo").get().getValue());
            }
        });

        // assert that the columns do not exist anymore
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagedobjectid')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwarestate')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagedentitytype')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaremanagementserver')", Boolean.class));
        assertEquals(false, temporaryDatabase.getJdbcTemplate().queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='assets' AND column_name='vmwaretopologyinfo')", Boolean.class));
    }
}
