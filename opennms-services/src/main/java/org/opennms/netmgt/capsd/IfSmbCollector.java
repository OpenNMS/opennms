/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.netbios.NbtAddress;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.capsd.SmbAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SMB information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:mike@opennms.org">Mike </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * 
 */
final class IfSmbCollector implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(IfSmbCollector.class);
    
    /**
     * The MAC address that is returned from a Samba server
     */
    private static final String SAMBA_MAC = "00:00:00:00:00:00";

    /**
     * The target internet address to test for SMB support
     */
    private final InetAddress m_target;

    /**
     * This value is set of the node supports SMB.
     */
    private boolean m_isSamba;

    /**
     * The collected media access control address.
     */
    private String m_mac;

    /**
     * The domain name.
     */
    private String m_domain;

    /**
     * The primary NetBIOS address.
     */
    private NbtAddress m_addr;

    /**
     * The list of all the NetBIOS addresses for the target IP.
     */
    private NbtAddress[] m_allAddrs;

    /**
     * The list of available shares on the SMB box, if any
     */
    private String[] m_shares;

    /**
     * True if the box has MS Exchange running. This is set in the constructor.
     */
    private final boolean m_hasExchange;

    /**
     * This method is used to convert a 6 byte MAC address into a colon
     * separated string.
     * 
     * @param mac
     *            The 6 byte MAC address
     * 
     * @return The formatted MAC address.
     */
    private String toMacString(byte[] mac) {
        StringBuffer mbuf = new StringBuffer();
        for (int i = 0; i < mac.length; i++) {
            mbuf.append((int) (mac[i] >> 4) & 0x0f).append((int) mac[i] & 0x0f);
            if (i != 5)
                mbuf.append(':');
        }
        return mbuf.toString();
    }

    /**
     * Constructs a new SMB collector targeted at the passed address. The
     * presence of an Exchange server is set to false.
     * 
     * @param target
     *            The target IP address.
     * 
     */
    IfSmbCollector(InetAddress target) {
        m_target = target;
        m_isSamba = false;
        m_mac = null;
        m_addr = null;
        m_domain = null;
        m_allAddrs = null;
        m_shares = null;
        m_hasExchange = false;
    }

    /**
     * Constructs a new SMB collector targeted at the passed address. The
     * presence of an Exchange server is set to passed value.
     * 
     * @param target
     *            The target IP address.
     * @param hasExchange
     *            Sets the presence or absence of an exchange server.
     * 
     */
    IfSmbCollector(InetAddress target, boolean hasExchange) {
        m_target = target;
        m_isSamba = false;
        m_mac = null;
        m_addr = null;
        m_domain = null;
        m_allAddrs = null;
        m_shares = null;
        m_hasExchange = hasExchange;
    }

    /**
     * Returns the current target of this collection.
     */
    InetAddress getTarget() {
        return m_target;
    }

    /**
     * Returns true if the target is a SAMBA server.
     */
    boolean isSamba() {
        return m_isSamba;
    }

    /**
     * Returns the MAC address, if present.
     */
    String getMAC() {
        return m_mac;
    }

    /**
     * Returns the primary NetBIOS address for the target if it was recovered.
     */
    NbtAddress getNbtAddress() {
        return m_addr;
    }

    /**
     * Retrns the NetBIOS name associated with the primary NetBIOS address.
     */
    String getNbtName() {
        if (m_addr != null)
            return m_addr.getHostName().trim();
        else
            return null;
    }

    /**
     * Returns the domain name associated with this NetBIOS address
     */
    String getDomainName() {
        return m_domain;
    }

    /**
     * Returns the list of all NetBIOS names recovered from the target node.
     */
    NbtAddress[] getAllNbtAddresses() {
        return m_allAddrs;
    }

    /**
     * Returns the list of all available shares on the target if the call
     * succeeded. If the call failed then a null or empty list is return.
     */
    String[] getShares() {
        return m_shares;
    }

    /**
     * Returns the presence of an exchange server. This is the same value as set
     * in the class' constructor.
     */
    boolean hasExchange() {
        return m_hasExchange;
    }

    /**
     * This method inspects the provided netBIOS name for control characters
     * (chars w/ decimal value less than 20/ <SPACE>
     * 
     * @param nbName
     *            NetBIOS name to check
     * 
     * @return true if string contains control chars, false otherwise.
     */
    boolean containsCtrlChars(String nbName) {
        byte[] bytes = nbName.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] < 20)
                return true;
        }

        return false;
    }

    /**
     * The main execution method used to collect the SMB information for the
     * collector.
     */
    @Override
    public void run() {
        try {
            m_addr = NbtAddress.getByName(InetAddressUtils.str(m_target));

            // If the retrieved SMB name is equal to the IP address
            // of the host, the it is safe to assume that the interface
            // does not support SMB
            //
            if (m_addr.getHostName().equals(InetAddressUtils.str(m_target))) {
                LOG.debug("IfSmbCollector: failed to retrieve SMB name for {}", InetAddressUtils.str(m_target));
                m_addr = null;
            }
        } catch (UnknownHostException e) {
            LOG.debug("IfSmbCollector: UnknownHostException: failed to retrieve SMB name, reason:", e.getMessage());
            m_addr = null;
        }

        if (m_addr != null && containsCtrlChars(m_addr.getHostName())) {
            LOG.warn("IfSmbCollector: Retrieved SMB name for address {} contains control chars: '{}', discarding.", InetAddressUtils.str(m_target), m_addr.getHostName());
            m_addr = null;
        }

        if (m_addr != null) {
            LOG.debug("IfSmbCollector: SMB name of {} is: {}", InetAddressUtils.str(m_target), m_addr.getHostName());
            try {
                // Attempt to resolve the Media Access Control Address
                //
                byte[] mac = m_addr.getMacAddress();
                m_mac = toMacString(mac);
                if (m_mac.equals(SAMBA_MAC)) {
                    m_isSamba = true;
                    m_mac = null;
                }
            } catch (UnknownHostException e) {
                LOG.debug("IfSmbCollector: failed to get MAC for {} due to address failure", InetAddressUtils.str(m_target), e);
            }

            // Domain name
            //
            try {
                // next collect all the NetBIOS names from the target system
                //
                m_allAddrs = NbtAddress.getAllByAddress(m_addr);
                m_domain = SmbUtils.getAuthenticationDomainName(m_allAddrs, m_addr.getHostName());
                LOG.debug("IfSmbCollector: domain name: '{}'", m_domain);
            } catch (UnknownHostException e) {
                LOG.debug("IfSmbCollector: failed to get all the addresses for the interface {}", InetAddressUtils.str(m_target), e);
            }

            // get the SMB authentication object
            //
            SmbAuth authentication = null;
            if (m_domain != null)
                authentication = CapsdConfigFactory.getInstance().getSmbAuth(m_domain);

            if (authentication == null)
                authentication = CapsdConfigFactory.getInstance().getSmbAuth(m_addr.getHostName());

            LOG.debug("IfSmbCollector: SMB authenticator: {}", authentication);

            // If SMB is not set in capsd-configuration, authentication could be
            // null. Then stop
            // SMB collectio.
            if (authentication == null)
                return;

            /*
             * --------------------------------------------------------------------- /*
             * Commenting the share enumeration code out for now...saw a
             * scenario /* where a thread blocked indefinitely waiting for the
             * jCIFS code to /* return. Will be doing additional testing to try
             * and figure out /* what the problem is but for now will be
             * commenting this code out. /* THe only thing we lose is
             * potentially the OS of the remote box /
             */

            // Try to enumerate all the shares on the
            // remote target.
            //
            try {
                String smbUrl = SmbUtils.getSmbURL(authentication, m_addr.getHostName());
                LOG.debug("IfSmbCollector: smbUrl = {}", smbUrl);
                SmbFile sfile = new SmbFile(smbUrl);
                LOG.debug("IfSmbCollector: got SmbFile object, retrieving share list...");
                m_shares = sfile.list();
                LOG.debug("IfSmbCollector: shares list retrieved...");

            } catch (MalformedURLException e) {
                LOG.debug("IfSmbCollector: failed to get SMB resource and OS name for host {}", InetAddressUtils.str(m_target), e);
            } catch (SmbAuthException e) {
                LOG.debug("IfSmbCollector: unable to list SMB shares, authentication failed, reason: {}", e.getMessage());
            } catch (SmbException e) {
                LOG.debug("IfSmbCollector: unable to list SMB shares, reason: {}", e.getMessage());
            }
            /*---------------------------------------------------------------------*/

        } // end if(addr != null)

    } // end run
}
