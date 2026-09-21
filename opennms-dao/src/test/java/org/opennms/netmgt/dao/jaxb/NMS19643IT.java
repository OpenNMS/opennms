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
package org.opennms.netmgt.dao.jaxb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.config.api.TextEncryptor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.config.snmp.SnmpProfiles;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NMS19643IT implements InitializingBean {

    @Autowired
    private SnmpConfigDao snmpConfigDao;

    @Autowired
    private TextEncryptor textEncryptor;

    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testPostConstruct() {
        assertNotNull(snmpConfigDao);
        assertNotNull(textEncryptor);

        final SnmpConfig snmpConfig = snmpConfigDao.getConfig();
        final String encryptedSecret = textEncryptor.encrypt("snmp-config", "secret");

        final Definition def = new Definition();
        def.setEncrypted(true);
        def.setSpecifics(List.of("10.1.2.3"));
        def.setWriteCommunity(encryptedSecret);
        snmpConfig.addDefinition(def);

        final SnmpProfile snmpProfile = new SnmpProfile();
        snmpProfile.setEncrypted(true);
        snmpProfile.setWriteCommunity(encryptedSecret);
        final SnmpProfiles snmpProfiles = new SnmpProfiles();
        snmpProfiles.addSnmpProfile(snmpProfile);
        snmpConfig.setSnmpProfiles(snmpProfiles);

        snmpConfig.setEncrypted(true);
        snmpConfig.setWriteCommunity(encryptedSecret);

        snmpConfigDao.updateConfig(snmpConfig);

        assertEquals(encryptedSecret, snmpConfigDao.getConfig().getWriteCommunity());
        assertEquals(encryptedSecret, snmpConfigDao.getConfig().getSnmpProfiles().getSnmpProfiles().getFirst().getWriteCommunity());
        assertEquals(encryptedSecret, snmpConfigDao.getConfig().getDefinitions().getFirst().getWriteCommunity());

        ((DefaultSnmpConfigDao) snmpConfigDao).decryptConfig();

        assertEquals("secret", snmpConfigDao.getConfig().getWriteCommunity());
        assertEquals("secret", snmpConfigDao.getConfig().getSnmpProfiles().getSnmpProfiles().getFirst().getWriteCommunity());
        assertEquals("secret", snmpConfigDao.getConfig().getDefinitions().getFirst().getWriteCommunity());
    }
}
