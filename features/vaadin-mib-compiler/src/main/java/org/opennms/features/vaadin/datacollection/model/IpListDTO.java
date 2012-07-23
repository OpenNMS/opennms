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

package org.opennms.features.vaadin.datacollection.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class IpListDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class IpListDTO implements Serializable {

    /** List of IP addresses. */
    private List<String> m_ipAddresses;

    /** List of IP address masks. */
    private List<String> m_ipAddressMasks;

    /**
     * Instantiates a new IP list DTO.
     */
    public IpListDTO() {
        super();
        m_ipAddresses = new ArrayList<String>();
        m_ipAddressMasks = new ArrayList<String>();
    }

    /**
     * Gets the IP address collection.
     *
     * @return the IP address collection
     */
    public List<String> getIpAddrCollection() {
        return m_ipAddresses;
    }

    /**
     * Gets the IP address mask collection.
     *
     * @return the IP address mask collection
     */
    public List<String> getIpAddrMaskCollection() {
        return m_ipAddressMasks;
    }

    /**
     * Sets the IP address collection.
     *
     * @param ipAddrs the new IP address collection
     */
    public void setIpAddrCollection(final List<String> ipAddrs) {
        this.m_ipAddresses = ipAddrs;
    }

    /**
     * Sets the IP address mask collection.
     *
     * @param ipAddrMasks the new IP address mask collection
     */
    public void setIpAddrMaskCollection(final List<String> ipAddrMasks) {
        this.m_ipAddressMasks = ipAddrMasks;
    }

}
