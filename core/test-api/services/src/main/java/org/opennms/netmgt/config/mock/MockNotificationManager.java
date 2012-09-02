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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationManager;
/**
 * @author david
 */
public class MockNotificationManager extends NotificationManager {

    @SuppressWarnings("deprecation")
    public MockNotificationManager(NotifdConfigManager configManager, DataSource db, String mgrString) throws MarshalException, ValidationException {
        super(configManager, db);
        Reader reader = new StringReader(mgrString);
        parseXML(reader);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#saveXML(java.lang.String)
     */
    @Override
    protected void saveXML(String xmlString) throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#update()
     */
    @Override
    public void update() throws IOException, MarshalException, ValidationException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#getInterfaceFilter(java.lang.String)
     */
    protected String getInterfaceFilter(String rule) {
        return "SELECT DISTINCT ipaddr, servicename, nodeid FROM ifservices, service WHERE ifservices.serviceid = service.serviceid";
    }
}
