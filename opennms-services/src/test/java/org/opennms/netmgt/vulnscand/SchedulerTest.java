/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 26, 2008
 *
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
package org.opennms.netmgt.vulnscand;

import junit.framework.TestCase;

import org.opennms.netmgt.config.VulnscandConfigFactory;
import org.opennms.test.DaoTestConfigBean;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SchedulerTest extends TestCase {

    public SchedulerTest() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.setRelativeHomeDirectory("src/test/test-configurations/vulnscand");
        bean.afterPropertiesSet();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        VulnscandConfigFactory.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreate() throws Exception {
//      FifoQueue q = new FifoQueueImpl();
//      Scheduler scheduler = new Scheduler(q);
//      Vulnscand vulnscand = null;
//      vulnscand.initialize();
    }
}
