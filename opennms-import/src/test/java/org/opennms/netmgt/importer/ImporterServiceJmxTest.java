//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 10: Created a separate test case for JMX tests using default configs. - dj@opennms.org
// 2007 Aug 25: Use AbstractTransactionalTemporaryDatabaseSpringContextTests
//              and new Spring context files. - dj@opennms.org
// 2007 Jun 24: Organize imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.importer;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.importer.jmx.ImporterService;
import org.opennms.netmgt.importer.jmx.ImporterServiceMBean;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ImporterServiceJmxTest {
    
    @Before
    public void setUp() throws Exception {
        
        MockDatabase db = new MockDatabase();
        DataSourceFactory.setInstance(db);

        MockLogAppender.setupLogging();
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
        
        
        BeanFactoryReference ref = BeanUtils.getBeanFactory("daemonContext");
        ApplicationContext daemonContext = (ApplicationContext) ref.getFactory();
        
        new ClassPathXmlApplicationContext(new String[] { "classpath:META-INF/opennms/mockEventIpcManager.xml" }, daemonContext);
        
        
    }
    
    @Test
    public void testStartStop() throws InterruptedException {
        ImporterServiceMBean mbean = new ImporterService();
        mbean.init();
        mbean.start();
        
        Thread.sleep(30000);
        
        mbean.stop();
    }
}
