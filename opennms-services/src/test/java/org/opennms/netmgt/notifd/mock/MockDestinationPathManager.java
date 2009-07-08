/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2004-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.DestinationPathManager;
/**
 * @author david hustace <david@opennms.org>
 */

public class MockDestinationPathManager extends DestinationPathManager {
    
    public MockDestinationPathManager(String xmlString) throws MarshalException, ValidationException {
        Reader reader = new StringReader(xmlString);
        parseXML(reader);
    }

    protected void saveXML(String writerString) throws IOException {

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.DestinationPathManager#update()
     */
    public void update() throws IOException, MarshalException,
            ValidationException, FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
