/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.core.wsman.WSManEndpoint;
import org.springframework.core.io.FileSystemResource;

public class WSManConfigDaoJaxbTest {
    @Test
    public void canBuildEndpointForSpecific() throws UnknownHostException {
        WSManConfigDaoJaxb configDao = new WSManConfigDaoJaxb();
        configDao.setConfigResource(new FileSystemResource("src/test/resources/wsman-config.xml"));
        configDao.afterPropertiesSet();
        WSManEndpoint endpoint = configDao.getEndpoint(InetAddress.getByName("172.23.1.2"));
        assertEquals("http://172.23.1.2:5985/ws-man", endpoint.getUrl().toString());
    }
}
