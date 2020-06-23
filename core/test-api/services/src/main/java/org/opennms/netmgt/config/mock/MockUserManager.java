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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;

public class MockUserManager extends UserManager {

    String m_xmlString;
    boolean updateNeeded = true;
    private long m_lastModified;
    private long m_fileSize;
    
    public MockUserManager(GroupManager groupManager, String xmlString) {
        super(groupManager);
        m_xmlString = xmlString;
        parseXML();
    }

    private void parseXML() {
        InputStream in = new ByteArrayInputStream(m_xmlString.getBytes());
        parseXML(in);
        updateNeeded = false;
        m_lastModified = System.currentTimeMillis();
        m_fileSize = m_xmlString.getBytes().length;
    }

    @Override
    protected void saveXML(String writerString) throws IOException {
        m_xmlString = writerString;
        updateNeeded = true;
    }

    @Override
    protected void doUpdate() throws IOException, FileNotFoundException {
        if (updateNeeded) {
            parseXML();
        }
    }

    @Override
    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    @Override
    public long getLastModified() {
        return m_lastModified;
    }

    @Override
    public long getFileSize() {
        return m_fileSize;
    }

    @Override
    public void reload() throws IOException, FileNotFoundException {
        parseXML();
    }

}
