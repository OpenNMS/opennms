/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.discovery;

import java.net.InetAddress;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>
 * This class is used to represent the polling information needed by the
 * discovery process. Each instance encapsulates an internet address, timeout in
 * milliseconds, and a retry count.
 * </p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 */
public class IPPollAddress {
    /**
     * The dotted decimal IPv4 address for the poll.
     */
    private InetAddress m_address; // dotted IP m_address

    /**
     * The timeout for the poller in 1/1000th of a second.
     */
    private long m_timeout;

    /**
     * The number of times to attempt to contact the remote.
     */
    private int m_retries;

    /**
     * <P>
     * Constructs an IPPollAddress object with the specified parameters.
     * </P>
     * 
     * @param ipAddress
     *            The Dotted Decimal IPv4 Address.
     * @param timeout
     *            The timeout between retries in 1/1000th of a second.
     * @param retries
     *            The number of times to attempt to contact the address.
     * 
     */
    public IPPollAddress(final InetAddress ipAddress, final long timeout, final int retries) {
        m_address = ipAddress;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <P>
     * Returns the timeout in 1/1000th of a second increments.
     * </P>
     *
     * @return The timeout associated with the host in 1/1000th of a second.
     */
    public long getTimeout() {
        return m_timeout;
    }

    /**
     * <P>
     * Returns the current number of retries set for this address.
     * </P>
     *
     * @return The retry count for the instance.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * Returns the internet address encapsulated in the object.
     *
     * @return The encapsulated internet address.
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <P>
     * Returns true if the passed object is equal to self. The objects must be
     * equal in address, timeout, and the number of retries.
     * </P>
     *
     * @return True if the objects are logically equal. False is returned otherwise.
     * @param pollAddr a {@link org.opennms.netmgt.model.discovery.IPPollAddress} object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof IPPollAddress) {
            IPPollAddress pollAddr = (IPPollAddress)object;
            if (pollAddr != null) {
                if (pollAddr == this) {
                    return true;
                } else if (pollAddr.getAddress().equals(m_address) && pollAddr.getRetries() == m_retries && pollAddr.getTimeout() == m_timeout) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("address", m_address)
    		.append("retries", m_retries)
    		.append("timeout", m_timeout)
    		.toString();
    }
}
