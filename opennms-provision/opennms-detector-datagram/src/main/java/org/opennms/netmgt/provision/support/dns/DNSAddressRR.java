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

package org.opennms.netmgt.provision.support.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;

/**
 * <P>
 * Holds a DNS resource record which is a DNS response that gives the IP address
 * of a particular hostname. A resource record typically has:
 * </P>
 *
 * <TABLE BORDER=0>
 * <TH>
 * <TD>Element</TD>
 * <TD>Description</TD>
 * </TH>
 * <TR>
 * <TD>Name</TD>
 * <TD>Domain name that the resource record describes.</TD>
 * </TR>
 * <TR>
 * <TD>Type</TD>
 * <TD>Type of RR.</TD>
 * </TR>
 * <TR>
 * <TD>Class</TD>
 * <TD>RR Class.</TD>
 * </TR>
 * <TR>
 * <TD>TTL</TD>
 * <TD>Time-To-Live for the RR.</TD>
 * </TR>
 * <TR>
 * <TD>RDLEN</TD>
 * <TD>Length of the following data.</TD>
 * </TR>
 * <TR>
 * <TD>Data</TD>
 * <TD>Actual data of this RR.</TD>
 * </TR>
 * </TABLE>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 */
public final class DNSAddressRR {
    /**
     * <P>
     * Name of this RR.
     * </P>
     */
    private String m_name;

    /**
     * <P>
     * Type of this RR.
     * </P>
     */
    private int m_type;

    /**
     * <P>
     * Class of this RR.
     * </P>
     */
    private int m_class;

    /**
     * <P>
     * Time to live for this RR.
     * </P>
     */
    private long m_TTL;

    /**
     * <P>
     * Time at which this RR was created.
     * </P>
     */
    private long m_created;

    /**
     * <P>
     * The IP Address for the Route Record.
     * </P>
     */
    private int[] ipAddress;

    /**
     * <P>
     * Returns the address in the dotted decimal format.
     * </P>
     * 
     * @return The address in the dotted decimal format.
     */
    private String addressToByteString() {
        return ipAddress[0] + "." + ipAddress[1] + "." + ipAddress[2] + "." + ipAddress[3];
    }

    /**
     * <P>
     * Constructs an new DNS Address Resource Record with the specified
     * information.
     * </P>
     *
     * @param name
     *            name of the RR
     * @param type
     *            type of the RR
     * @param clas
     *            class of the RR
     * @param ttl
     *            time for which this RR is valid
     * @param dnsIn
     *            inputstream for this RR
     * @exception java.io.IOException
     *                Thrown if an error occurs decoding data from the passed
     *                DNSInputStream.
     * @throws java.io.IOException if any.
     */
    public DNSAddressRR(final String name, final int type, final int clas, final long ttl, final DNSInputStream dnsIn) throws IOException {
        m_name = name;
        m_type = type;
        m_class = clas;
        m_TTL = ttl;
        m_created = System.currentTimeMillis();

        // decode
        ipAddress = new int[4];
        for (int i = 0; i < 4; ++i) {
            ipAddress[i] = dnsIn.readByte();
        }
    }

    /**
     * <P>
     * Returns the address from the address record as a byte array.
     * </P>
     *
     * @return The address as a byte array.
     */
    public byte[] getAddress() {
        final byte[] ip = new byte[4];
        for (int j = 0; j < 4; j++)
            ip[j] = (byte) (ipAddress[j]);
        return ip;
    }

    /**
     * <P>
     * the InetAddress of the address contained for the record.
     * </P>
     *
     * @return The InetAddress of the address
     * @exception java.net.UnknownHostException
     *                Thrown if the InetAddress object cannot be constructed.
     * @throws java.net.UnknownHostException if any.
     */
    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddressUtils.addr(addressToByteString());
    }

    /**
     * <P>
     * Converts the object to a textual string that describes the resource
     * record.
     * </P>
     *
     * @return The string describing the object.
     */
    @Override
    public String toString() {
        return getRRName() + "\tInternet Address = " + addressToByteString();
    }

    /**
     * <P>
     * Returns the name of this RR.
     * </P>
     *
     * @return The name of this RR.
     */
    public String getRRName() {
        return m_name;
    }

    /**
     * <P>
     * Returns the type of this RR.
     * </P>
     *
     * @return The type of this RR.
     */
    public int getRRType() {
        return m_type;
    }

    /**
     * <P>
     * Returns the class of this RR.
     * </P>
     *
     * @return The class of this RR.
     */
    public int getRRClass() {
        return m_class;
    }

    /**
     * <P>
     * Returns the TTL of this RR.
     * </P>
     *
     * @return the TTL of this RR
     */
    public long getRRTTL() {
        return m_TTL;
    }

    /**
     * <P>
     * Returns true if still valid i.e. TTL has not expired.
     * </P>
     *
     * @return True if valid, false if not.
     */
    public boolean isValid() {
        return m_TTL * 1000 > System.currentTimeMillis() - m_created;
    }
}
