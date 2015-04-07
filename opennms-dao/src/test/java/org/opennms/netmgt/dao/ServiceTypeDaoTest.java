/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
        
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class ServiceTypeDaoTest implements InitializingBean {
	@Autowired
	private ServiceTypeDao m_serviceTypeDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
