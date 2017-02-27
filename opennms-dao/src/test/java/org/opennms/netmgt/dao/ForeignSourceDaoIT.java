/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.ForeignSourceDao;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class ForeignSourceDaoIT {

    @Autowired
    private ForeignSourceDao foreignSourceDao;

    @Test
    @Transactional
    public void createForeignSource() {
        Date date = new Date();
        ForeignSourceEntity foreignSource = new ForeignSourceEntity();
        foreignSource.setDefault(true);
        foreignSource.setName("custom foreign source");
        foreignSource.setScanInterval(1234);
        foreignSource.setDate(date);

        foreignSource.addPlugin(createPluginConfig(foreignSource, PluginConfigType.Detector, "name", "class", null));
        foreignSource.addPlugin(createPluginConfig(foreignSource, PluginConfigType.Policy, "name", "class", null));
        foreignSourceDao.save(foreignSource);
        foreignSourceDao.flush();

        ForeignSourceEntity received = foreignSourceDao.get(foreignSource.getName());
        Assert.assertNotNull(received);
        Assert.assertEquals(Boolean.TRUE, received.isDefault());
        Assert.assertEquals("custom foreign source", received.getName());
        Assert.assertEquals(1234, received.getScanInterval());
        Assert.assertEquals(date, received.getDate());
        Assert.assertEquals(1, received.getDetectors().size());
        Assert.assertEquals(1, received.getPolicies().size());

        // TODO MVR update foreign source
        // - change values in existing hierarchy
        // - add new elements in existing hierarchy
        // - remove elements in existing hierarchy
    }

    private static PluginConfigEntity createPluginConfig(ForeignSourceEntity parent, PluginConfigType pluginConfigType, String name, String pluginClass, Map<String, String> parameters) {
        PluginConfigEntity pc = pluginConfigType.newInstance();
        pc.setForeignSource(parent);
        pc.setName(name);
        pc.setPluginClass(pluginClass);
        if (parameters != null) {
            pc.setParameters(new HashMap<>(parameters));
        }
        return pc;
    }
}
