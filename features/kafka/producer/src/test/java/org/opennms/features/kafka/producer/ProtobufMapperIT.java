/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAlias;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.opennms.features.kafka.producer.KafkaForwarderIT.getHwEntityChassis;
import static org.opennms.features.kafka.producer.KafkaForwarderIT.getHwEntityContainer;
import static org.opennms.features.kafka.producer.KafkaForwarderIT.getHwEntityModule;
import static org.opennms.features.kafka.producer.KafkaForwarderIT.getHwEntityPort;
import static org.opennms.features.kafka.producer.KafkaForwarderIT.getHwEntityPowerSupply;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, reuseDatabase = false)
public class ProtobufMapperIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private HwEntityDao hwEntityDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private EventConfDao eventConfDao;

    @Autowired
    private SessionUtils sessionUtils;


    private ProtobufMapper protobufMapper;

    @Before
    public void setup() {
        protobufMapper = new ProtobufMapper(eventConfDao, hwEntityDao, sessionUtils, nodeDao, 10);
        databasePopulator.addExtension(new DatabasePopulator.Extension<HwEntityDao>() {

            @Override
            public DatabasePopulator.DaoSupport<HwEntityDao> getDaoSupport() {
                return new DatabasePopulator.DaoSupport<HwEntityDao>(HwEntityDao.class, hwEntityDao);
            }

            @Override
            public void onPopulate(DatabasePopulator populator, HwEntityDao dao) {
                OnmsNode node = new OnmsNode();
                node.setId(1);
                OnmsHwEntity port = getHwEntityPort(node);
                dao.save(port);
                OnmsHwEntity container = getHwEntityContainer(node);
                container.addChildEntity(port);
                dao.save(container);
                OnmsHwEntity module = getHwEntityModule(node);
                module.addChildEntity(container);
                dao.save(module);
                OnmsHwEntity powerSupply = getHwEntityPowerSupply(node);
                dao.save(powerSupply);
                OnmsHwEntity chassis = getHwEntityChassis(node);
                chassis.addChildEntity(module);
                chassis.addChildEntity(powerSupply);
                dao.save(chassis);
                dao.flush();
            }

            @Override
            public void onShutdown(DatabasePopulator populator, HwEntityDao dao) {
                for (OnmsHwEntity entity : dao.findAll()) {
                    dao.delete(entity);
                }
            }
        });

        databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    public void testLoadingHwTree() {
        OnmsNode onmsNode = nodeDao.get(1);
        // Save hwentity alias.
        OnmsHwEntity onmsHwEntity = hwEntityDao.findEntityByIndex(1, 35);
        OnmsHwEntityAlias onmsHwEntityAlias = new OnmsHwEntityAlias(0, ".1.3.6.1.2.1.2.2.1.1.10104");
        onmsHwEntityAlias.setHwEntity(onmsHwEntity);
        onmsHwEntity.addEntAliases(new TreeSet<>(Arrays.asList(onmsHwEntityAlias)));
        hwEntityDao.save(onmsHwEntity);
        hwEntityDao.flush();
        onmsHwEntity = hwEntityDao.findEntityByIndex(1, 35);
        SortedSet<OnmsHwEntityAlias> set = onmsHwEntity.getEntAliases();
        assertThat(set, Matchers.hasSize(1));
        OpennmsModelProtos.Node node = protobufMapper.toNode(onmsNode).build();
        Assert.assertThat(node, Matchers.notNullValue());
        assertThat(node.getHwInventory(), not(nullValue()));
        assertThat(node.getHwInventory().getChildrenList().size(), equalTo(2));
        List<OpennmsModelProtos.HwEntity> hwEntityList = node.getHwInventory().getChildrenList();
        OpennmsModelProtos.HwEntity hwEntity = hwEntityList.stream().filter(entity -> entity.getChildrenCount() > 0).findFirst().get();
        assertThat(hwEntity.getChildren(0).getChildren(0).getEntPhysicalClass(), equalTo("port"));
        assertThat(hwEntity.getChildren(0).getChildren(0).getEntHwAliasCount(), equalTo(1));
        assertThat(hwEntity.getChildren(0).getChildren(0).getEntHwAlias(0).getIndex(), equalTo(0));
        assertThat(hwEntity.getChildren(0).getChildren(0).getEntHwAlias(0).getOid(), equalTo(".1.3.6.1.2.1.2.2.1.1.10104"));
    }
}
