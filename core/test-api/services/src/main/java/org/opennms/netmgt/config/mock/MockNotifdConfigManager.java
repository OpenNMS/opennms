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

package org.opennms.netmgt.config.mock;

import java.io.IOException;

import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotifdConfigManager;

/**
 * @author David Hustace <david@opennms.org>
 */
public class MockNotifdConfigManager extends NotifdConfigManager {
        
    private String m_nextNotifIdSql;
    private String m_nextUserNotifIdSql;

    private NotifdConfigFactory notifdConfigFactory;

    public MockNotifdConfigManager(NotifdConfigFactory notifdConfigFactory) throws IOException {
        this.notifdConfigFactory = notifdConfigFactory;
        this.configuration = notifdConfigFactory.getConfiguration();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#update()
     */
    @Override
    protected void update() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#getNextNotifIdSql()
     */
    @Override
    public String getNextNotifIdSql() throws IOException {
        return m_nextNotifIdSql;
    }
    
    public void setNextNotifIdSql(String sql) {
        m_nextNotifIdSql = sql;
    }

    @Override
    public String getNextUserNotifIdSql() throws IOException {
        // TODO Auto-generated method stub
        return m_nextUserNotifIdSql;
    }

    public void setNextUserNotifIdSql(String sql) {
        m_nextUserNotifIdSql = sql;
    }

    @Override
    protected String getConfigName() {
        return NotifdConfigFactory.CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return NotifdConfigFactory.DEFAULT_CONFIG_ID;
    }
}
