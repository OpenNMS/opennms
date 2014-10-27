/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

/**
 * A TrapdIpMgr that doesn't talk to the database.  If we want something
 * there for our test, we'll populate it.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class MockTrapdIpMgr extends JdbcTrapdIpMgr {
    @Override
    public void afterPropertiesSet() throws Exception {
        // Don't check for the dataSource property being set
    }

    @Override
    public synchronized void dataSourceSync() {
        // Don't do anything... don't want to have to mess with the DB here
    }

    public synchronized void clearKnownIpsMap() {
        m_knownips.clear();
    }
}