/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.xml.bind;

import java.net.InetAddress;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.core.utils.InetAddressUtils;

/**
 * InetAddresssXmlAdapter
 *
 * @author brozow
 * @version $Id: $
 */
public class InetAddressXmlAdapter extends XmlAdapter<String, InetAddress> {

    /** {@inheritDoc} */
    @Override
    public String marshal(final InetAddress inetAddr) throws Exception {
        return InetAddressUtils.toIpAddrString(inetAddr);
    }

    /** {@inheritDoc} */
    @Override
    public InetAddress unmarshal(final String ipAddr) throws Exception {
        return (ipAddr == null || ipAddr.isEmpty())? null : InetAddressUtils.addr(ipAddr);
    }

}
