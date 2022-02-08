/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InetAddressXmlAdapter extends XmlAdapter<String, InetAddress> {

    private static Logger LOG = LoggerFactory.getLogger(InetAddressXmlAdapter.class);

    /** {@inheritDoc} */
    @Override
    public String marshal(final InetAddress inetAddr) throws Exception {
        return inetAddr == null? null : new IPAddress(inetAddr).toDbString();
    }

    /** {@inheritDoc} */
    @Override
    public InetAddress unmarshal(final String ipAddr) throws Exception {
        try {
            return (ipAddr == null || ipAddr.isEmpty()) ? null : new IPAddress(ipAddr).toInetAddress();
        }
        catch (Throwable t) {
            LOG.warn(String.format("Invalid IP address <%s>", ipAddr));
            return null;
        }
    }

}
