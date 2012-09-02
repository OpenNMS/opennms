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
import java.io.UnsupportedEncodingException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.GroupManager;

public class MockGroupManager extends GroupManager {
    
    String m_xmlString;
    boolean updateNeeded = false;
    
    public MockGroupManager(String xmlString) throws MarshalException, ValidationException {
        m_xmlString = xmlString;
        parseXML();
    }

    private void parseXML() throws MarshalException, ValidationException {
        try {
            InputStream reader = new ByteArrayInputStream(m_xmlString.getBytes("UTF-8"));
            parseXml(reader);
            updateNeeded = false;
        } catch (UnsupportedEncodingException e) {
            // Can't happen with UTF-8
        }
    }

    @Override
    public void update() throws IOException, MarshalException, ValidationException {
        if (updateNeeded) {
            parseXML();
        }
    }

    @Override
    protected void saveXml(String data) throws IOException {
        m_xmlString = data;
        updateNeeded = true;
    }

}
