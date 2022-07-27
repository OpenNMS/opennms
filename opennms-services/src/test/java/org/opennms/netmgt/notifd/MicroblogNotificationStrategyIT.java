/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.jaxb.DefaultMicroblogConfigurationDao;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.core.io.Resource;

/**
 * TODO: Make this unit test work
 * 
 * @author <a href="mailto:jeffg@opennms.org>Jeff Gehlbach</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 *
 */
@JUnitTemporaryDatabase
public class MicroblogNotificationStrategyIT {

    protected DefaultMicroblogConfigurationDao m_dao;
    protected Resource m_daoConfigResource;

    @Before
    public void setUpConfigDao() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        m_daoConfigResource = ConfigurationTestUtils.getSpringResourceForResource(this, "/microblog-configuration.xml");
        Assert.assertTrue(m_daoConfigResource.exists());
    }

    @Ignore
    @Test
    public void postNotice() {
        NotificationStrategy ns = new MicroblogNotificationStrategy(m_daoConfigResource);
        List<Argument> arguments = configureArgs();

        Assert.assertEquals("NotificationStrategy should return 0 on success", 0, ns.send(arguments));
    }
    
    public List<Argument> configureArgs() {
        List<Argument> arguments = new ArrayList<>();
        Argument arg = null;
        arg = new Argument("-tm", null, "text message for " + getClass().getSimpleName() + " at " + new Date(), false);
        arguments.add(arg);        

        return arguments;
    }
}
