/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model.discovery;

import java.io.Serializable;
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
public class IPPollAddress implements Serializable {

    private static final long serialVersionUID = -4162816651553193934L;

    /**
     * Foreign source where this address should be persisted.
     */
    private final String m_foreignSource;

    /**
     * Network location of this address.
     */
    private final String m_location;

    /**
     * The dotted decimal IPv4 address for the poll.
     */
    private final InetAddress m_address; // dotted IP m_address

    /**
     * The timeout for the poller in 1/1000th of a second.
     */
    private final long m_timeout;

    /**
     * The number of times to attempt to contact the remote.
     */
    private final int m_retries;

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
    public IPPollAddress(final String foreignSource, final String location, final InetAddress ipAddress, final long timeout, final int retries) {
        m_foreignSource = foreignSource;
        m_location = location;
        m_address = ipAddress;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * Foreign source where this address should be persisted.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    /**
     * Network location of this address.
     */
    public String getLocation() {
        return m_location;
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
