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
import java.nio.charset.StandardCharsets;

import org.opennms.netmgt.config.DestinationPathManager;
/**
 * @author david hustace <david@opennms.org>
 */

public class MockDestinationPathManager extends DestinationPathManager {
    
    public MockDestinationPathManager(String xmlString) throws IOException {
        InputStream reader = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
        parseXML(reader);
    }

    @Override
    protected void saveXML(String writerString) throws IOException {

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.DestinationPathManager#update()
     */
    @Override
    public void update() throws IOException, FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
