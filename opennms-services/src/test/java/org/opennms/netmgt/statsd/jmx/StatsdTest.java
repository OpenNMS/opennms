/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Aug 24: Use mockEventIpcManager.xml and eventIpcManager-factoryInit
 *              Spring contexts instead of doing the work directly. - dj@opennms.org
 * 2007 Apr 16: Re-enable tests now that the problem has been found with cruisecontrol. - dj@opennms.org
 * 2007 Apr 06: Use DaoTestConfigBean for system properties. - dj@opennms.org
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.statsd.jmx;

import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.DaoTestConfigBean;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class StatsdTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    public StatsdTest() {
        super();

        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/eventIpcManager-factoryInit.xml"
        };
    }
    
    public void testInitStartStop() throws Exception {
        Statsd mbean = new Statsd();
        
        mbean.init();
        mbean.start();
        
        Thread.sleep(3000);
        
        mbean.stop();
    }
    
    // due to bugs introduced by the refreshing behavior of appContexts...
    // this will have to this well have to wait until we implement osgi
    public void FIXMEtestInitStartStopTwice() throws Exception {
        Statsd mbean = new Statsd();
        
        mbean.init();
        mbean.start();
        mbean.stop();

        mbean.init();
        mbean.start();
        mbean.stop();
    }
}
