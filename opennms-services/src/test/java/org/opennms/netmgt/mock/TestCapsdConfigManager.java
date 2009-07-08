/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.mock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CapsdConfigManager;

public class TestCapsdConfigManager extends CapsdConfigManager {
    private String m_xml;

    public TestCapsdConfigManager(String xml) throws MarshalException, ValidationException, IOException {
        super(new StringReader(xml));
        save();
    }

    @Override
    protected void saveXml(String xml) throws IOException {
        m_xml = xml;
    }

    @Override
    protected void update() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        loadXml(new StringReader(m_xml));
    }
    
    public String getXml() {
        return m_xml;
    }
    
}