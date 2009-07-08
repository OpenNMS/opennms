/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2004-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.notifd.mock;

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
    protected void saveXML(String xmlString) throws IOException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#update()
     */
    protected void update() throws IOException, MarshalException,
            ValidationException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#getInterfaceFilter(java.lang.String)
     */
    protected String getInterfaceFilter(String rule) {
        
        return "SELECT DISTINCT ipaddr, servicename, nodeid FROM ifservices, service WHERE ifservices.serviceid = service.serviceid";
    }
}
