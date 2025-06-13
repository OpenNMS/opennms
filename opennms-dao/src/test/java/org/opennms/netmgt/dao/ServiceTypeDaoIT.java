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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
        
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class ServiceTypeDaoIT implements InitializingBean {
	@Autowired
	private ServiceTypeDao m_serviceTypeDao;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

	@Test
	@Transactional
    public void testLazyLoad() {
    	OnmsServiceType t = new OnmsServiceType("ICMP");
    	m_serviceTypeDao.save(t);
    	
    	
    	OnmsServiceType type = m_serviceTypeDao.get(1);
    	assertEquals("ICMP", type.getName());
    }

	@Test
	@Transactional
    public void testSave() {
        String name = "ICMP";
        tweakSvcType(name);
        tweakSvcType(name);
        tweakSvcType(name);
        tweakSvcType(name);
    }

    private void tweakSvcType(String name) {
        OnmsServiceType svcType = m_serviceTypeDao.findByName(name);
        if (svcType == null)
            m_serviceTypeDao.save(new OnmsServiceType(name));
        else {
            svcType.setName(svcType.getName()+'-'+svcType.getId());
            m_serviceTypeDao.update(svcType);
        }
        m_serviceTypeDao.clear();
    }

}
