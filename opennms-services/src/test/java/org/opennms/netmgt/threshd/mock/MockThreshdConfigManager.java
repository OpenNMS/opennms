/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.threshd.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.ThreshdConfigManager;

public class MockThreshdConfigManager extends ThreshdConfigManager {

    @SuppressWarnings("deprecation")
    public MockThreshdConfigManager(String xml, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
        super(new StringReader(xml), localServer, verifyServer);
    }

    @SuppressWarnings("deprecation")
    public MockThreshdConfigManager(Reader rdr, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
        super(rdr, localServer, verifyServer);
    }

    public MockThreshdConfigManager(InputStream stream, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
        super(stream, localServer, verifyServer);
    }

    public void reloadXML() throws IOException, MarshalException, ValidationException {
        // TODO Auto-generated method stub

    }

    protected void saveXML(String xmlString) throws IOException {
        // TODO Auto-generated method stub

    }

}
