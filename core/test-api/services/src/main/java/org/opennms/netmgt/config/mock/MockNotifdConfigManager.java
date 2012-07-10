/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.NotifdConfigManager;

/**
 * @author David Hustace <david@opennms.org>
 */
public class MockNotifdConfigManager extends NotifdConfigManager {
        
    private String m_nextNotifIdSql;
    private String m_nextUserNotifIdSql;

    /*
     * init the mock config
     */
    
    
    /**
     * @param configString
     * @throws IOException
     * @throws ValidationException
     * @throws MarshalException
     */
    public MockNotifdConfigManager(String configString) throws MarshalException, ValidationException, IOException {
        InputStream reader = new ByteArrayInputStream(configString.getBytes("UTF-8"));
        parseXml(reader);
        reader.close();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#update()
     */
    @Override
    protected void update() throws IOException, MarshalException, ValidationException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#saveXml(java.lang.String)
     */
    @Override
    protected void saveXml(String xml) throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#getNextNotifIdSql()
     */
    @Override
    public String getNextNotifIdSql() throws IOException, MarshalException,
            ValidationException {
        return m_nextNotifIdSql;
    }
    
    public void setNextNotifIdSql(String sql) {
        m_nextNotifIdSql = sql;
    }

    @Override
    public String getNextUserNotifIdSql() throws IOException, MarshalException, ValidationException {
        // TODO Auto-generated method stub
        return m_nextUserNotifIdSql;
    }

    public void setNextUserNotifIdSql(String sql) {
        m_nextUserNotifIdSql = sql;
    }
    
}
