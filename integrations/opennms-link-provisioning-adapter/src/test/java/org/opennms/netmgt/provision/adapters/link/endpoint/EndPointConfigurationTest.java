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

package org.opennms.netmgt.provision.adapters.link.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.DefaultEndPointConfigurationDao;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.springframework.core.io.ClassPathResource;

public class EndPointConfigurationTest {

    private EndPointConfigurationDao m_endPointDao;
    
    @Before
    public void setUp() throws Exception {
        DefaultEndPointConfigurationDao dao = new DefaultEndPointConfigurationDao();
        dao.setConfigResource(new ClassPathResource("/test-endpoint-configuration.xml"));
        dao.afterPropertiesSet();
        m_endPointDao = dao;

        Properties props = new Properties();
        props.setProperty("log4j.logger.org.springframework", "WARN");
        props.setProperty("log4j.logger.org.hibernate", "WARN");
        props.setProperty("log4j.logger.org.opennms", "DEBUG");
        props.setProperty("log4j.logger.org.opennms.netmgt.dao.castor", "WARN");
        MockLogAppender.setupLogging(props);
        
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

    }
    
    @Test
    public void testGetFromDao() throws Exception {
        EndPointTypeValidator validator = m_endPointDao.getValidator();
        assertEquals("test config has 2 entries", 2, validator.getConfigs().size());
    }

    @Test
    public void testGetXsd() throws Exception {
        String xsd = m_endPointDao.getXsd();
        System.err.println(xsd);
        assertTrue(xsd.contains("endpoint-type"));
    }
}
