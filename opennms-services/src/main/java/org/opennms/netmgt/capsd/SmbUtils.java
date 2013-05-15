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

import jcifs.netbios.NbtAddress;

import org.opennms.netmgt.config.capsd.SmbAuth;

/**
 * This class contains several static convience methods utilized by Capsd while
 * doing data collection via jCIFS and the SMB (Server Message Block) protocol.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class SmbUtils {
    // NetBIOS Node Name Suffix Codes
    //
    /** Constant <code>WORKSTATION_SERVICE=0x00</code> */
    public static final int WORKSTATION_SERVICE = 0x00; // <computername>

    /** Constant <code>MESSENGER_SERVICE_A=0x01</code> */
    public static final int MESSENGER_SERVICE_A = 0x01; // <computername>

    /** Constant <code>MASTER_BROWSER_G=0x01</code> */
    public static final int MASTER_BROWSER_G = 0x01; // \\--__MSBROWSE__

    /** Constant <code>MESSENGER_SERVICE_B=0x03</code> */
    public static final int MESSENGER_SERVICE_B = 0x03; // <computername>

    /** Constant <code>RAS_SERVER_SERVICE=0x06</code> */
    public static final int RAS_SERVER_SERVICE = 0x06; // <computername>

    /** Constant <code>NETDDE_SERVICE=0x1F</code> */
    public static final int NETDDE_SERVICE = 0x1F; // <computername>

    /** Constant <code>FILE_SERVER_SERVICE=0x20</code> */
    public static final int FILE_SERVER_SERVICE = 0x20; // <computername>

    /** Constant <code>RAS_CLIENT_SERVICE=0x21</code> */
    public static final int RAS_CLIENT_SERVICE = 0x21; // <computername>

    /** Constant <code>MS_EXCHANGE_INTERCHANGE=0x22</code> */
    public static final int MS_EXCHANGE_INTERCHANGE = 0x22; // <computername>

    /** Constant <code>MS_EXCHANGE_STORE=0x23</code> */
    public static final int MS_EXCHANGE_STORE = 0x23; // <computername>

    /** Constant <code>MS_EXCHANGE_DIRECTORY=0x24</code> */
    public static final int MS_EXCHANGE_DIRECTORY = 0x24; // <computername>

    /** Constant <code>MODEM_SHARING_SERVER_SERVICE=0x30</code> */
    public static final int MODEM_SHARING_SERVER_SERVICE = 0x30; // <computername>

    /** Constant <code>MODEM_SHARING_CLIENT_SERVICE=0x31</code> */
    public static final int MODEM_SHARING_CLIENT_SERVICE = 0x31; // <computername>

    /** Constant <code>SMS_CLIENT_REMOTE_CONTROL=0x43</code> */
    public static final int SMS_CLIENT_REMOTE_CONTROL = 0x43; // <computername>

    /** Constant <code>SMS_ADMIN_REMOTE_CONTROL_TOOL=0x44</code> */
    public static final int SMS_ADMIN_REMOTE_CONTROL_TOOL = 0x44; // <computername>

    /** Constant <code>SMS_CLIENTS_REMOTE_CHAT=0x45</code> */
    public static final int SMS_CLIENTS_REMOTE_CHAT = 0x45; // <computername>

    /** Constant <code>SMS_CLIENTS_REMOTE_TRANSFER=0x46</code> */
    public static final int SMS_CLIENTS_REMOTE_TRANSFER = 0x46; // <computername>

    /** Constant <code>DEC_PATHWORKS_TCPIP_SERVICE_A=0x4C</code> */
    public static final int DEC_PATHWORKS_TCPIP_SERVICE_A = 0x4C; // <computername>

    /** Constant <code>DEC_PATHWORKS_TCPIP_SERVICE_B=0x52</code> */
    public static final int DEC_PATHWORKS_TCPIP_SERVICE_B = 0x52; // <computername>

    /** Constant <code>MS_EXCHANGE_MTA=0x87</code> */
    public static final int MS_EXCHANGE_MTA = 0x87; // <computername>

    /** Constant <code>MS_EXCHANGE_IMC=0x6A</code> */
    public static final int MS_EXCHANGE_IMC = 0x6A; // <computername>

    /** Constant <code>NETWORK_MONITOR_AGENT=0xBE</code> */
    public static final int NETWORK_MONITOR_AGENT = 0xBE; // <computername>

    /** Constant <code>NETWORK_MONITOR_APPLICATION=0xBF</code> */
    public static final int NETWORK_MONITOR_APPLICATION = 0xBF; // <computername>

    /** Constant <code>MESSENGER_SERVICE=0x03</code> */
    public static final int MESSENGER_SERVICE = 0x03; // <username>

    /** Constant <code>DOMAIN_NAME=0x00</code> */
    public static final int DOMAIN_NAME = 0x00; // <domain>

    /** Constant <code>DOMAIN_MASTER_BROWSER=0x1B</code> */
    public static final int DOMAIN_MASTER_BROWSER = 0x1B; // <domain>

    /** Constant <code>DOMAIN_CONTROLLERS=0x1C</code> */
    public static final int DOMAIN_CONTROLLERS = 0x1C; // <domain>

    /** Constant <code>MASTER_BROWSER_U=0x1D</code> */
    public static final int MASTER_BROWSER_U = 0x1D; // <domain>

    /** Constant <code>BROWSER_SERVICE_ELECTIONS=0x1E</code> */
    public static final int BROWSER_SERVICE_ELECTIONS = 0x1E; // <domain>

    /** Constant <code>INTERNET_INFORMATION_SERVER_G=0x1C</code> */
    public static final int INTERNET_INFORMATION_SERVER_G = 0x1C; // INET~SERVICES
                                                                    // (GROUP)

    /** Constant <code>INTERNET_INFORMATION_SERVER_U=0x00</code> */
    public static final int INTERNET_INFORMATION_SERVER_U = 0x00; // IS~<computername>
                                                                    // (UNIQUE)

    /** Constant <code>LOTUS_NOTES_SERVER_SERVICE=0x2B</code> */
    public static final int LOTUS_NOTES_SERVER_SERVICE = 0x2B; // <computername>

    /** Constant <code>LOTUS_NOTES_IRIS_MULTICAST=0x2F</code> */
    public static final int LOTUS_NOTES_IRIS_MULTICAST = 0x2F; // IRISMULTICAST

    /** Constant <code>LOTUS_NOTES_IRIS_NAME_SERVER=0x33</code> */
    public static final int LOTUS_NOTES_IRIS_NAME_SERVER = 0x33; // IRISNAMESERVER

    /** Constant <code>DCA_IRMALAN_GATEWAY_SERVER_SERVICE=0x20</code> */
    public static final int DCA_IRMALAN_GATEWAY_SERVER_SERVICE = 0x20; // FORTE_$ND800ZA

    /**
     * This method attempts to determine the authentication domain for a remote
     * host. The list of NbtAddress objects is processed in order to find an
     * entry with a DOMAIN_NAME (0x00) suffix. WORKSTATION_SERVICE and
     * INTERNET_INFORMATION_SERVER share the same 0x00 suffix so these entries
     * must be ignored while processing the address list.
     * 
     * @param addresses
     *            List of NbtAddress objects associated with the remote host.
     * @param cname
     *            NetBIOS name of the remote host.
     * 
     * @return remote host's authentication domain or null if unavailable.
     */
    static String getAuthenticationDomainName(NbtAddress[] addresses, String cname) {
        String domain = null;
        if (addresses != null) {
            for (int i = 0; i < addresses.length && domain == null; i++) {
                NbtAddress addr = addresses[i];

                // look at the domain name type and search for
                // anything that returns 0x00. Then check other
                // criteria to eliminate IIS and other nondesired
                // elements from the list
                //
                if (addr.getNameType() == DOMAIN_NAME) {
                    // the 0x00 suffic can be one of the following types:
                    //
                    // WORKSTATION_SERVICE
                    // INTERNET_INFORMATION_SERVER
                    // DOMAIN_NAME
                    //
                    // Eliminate the Workstation service by verifying that
                    // the NetBIOS name does not equal the current
                    // computer name.
                    //
                    // Eliminate the IIS servers hits by checking for the
                    // string prefix IS~ in the NetBIOS name.
                    //
                    if (!addr.getHostName().equals(cname) && !addr.getHostName().startsWith("IS~")) {
                        domain = addr.getHostName();
                    }
                }
            }
        }
        return domain;
    }

    /**
     * Returns the operating system label to be associated with a node in 'node'
     * table in the databse. This call should be made after an attempt to
     * determine if the interface supports Microsoft Exchange. This is
     * determined by the {@link MSExchangePlugin MSExchangePlugin}class.
     * 
     * @param nativeOS
     *            OS string returned by jCIFS following SMB session
     *            establishment with the remote host.
     * @param addresses
     *            array of NbtAddress objects associated with the remote host
     *            being tested.
     * @param isSamba
     *            <em>true</em> if it has been derived that the remote system
     *            is running Samba.
     * @param hasExchange
     *            <em>true</em> if the service supports microsoft exhange.
     * 
     * @return The Operating system label
     */
    static String getOsLabel(String nativeOS, NbtAddress[] addresses, boolean isSamba, boolean hasExchange) {
        String osLabel = null;

        // Given the operating system value returned by
        // jCIFS via SMB (nativeOS) now see if we can derive
        // anything else from the SMB data we've collected
        // in order to be more precise with our OS label.
        //
        if (nativeOS == null) {
            // HACK: nativeOS will be null if the share enumeration
            // failed for the remote host. If however the box is
            // running Samba we can safely assume the OS is either
            // Linux or UNIX.
            //
            if (isSamba) {
                osLabel = "Linux/UNIX";
            } else if (hasExchange) {
                osLabel = "Windows Server"; // Don't know if we have Win 2000 or
                                            // NT 4.0
            }
        } else if (nativeOS.length() == 0) {
            // HACK: Windows 95/98 boxes don't appear to give us the operating
            // system so if we have successfully enumerate the shares on a
            // server
            // but the operating system return is an emtpy string then we will
            // assume
            // it is a Win 95/98 box.
            //
            osLabel = "Windows 95/98";
        } else if (nativeOS.equalsIgnoreCase("Unix")) {
            // jCIFS reports "Unix" but the remote OS may actually
            // be Linux.
            //
            osLabel = "Linux/UNIX";
        } else if (nativeOS.equalsIgnoreCase("Windows 5.0")) {
            // jCIFS reports "Windows 5.0"...switch this to "Windows 2000"
            //
            nativeOS = "Windows 2000";

            if (hasExchange || isNTServer(addresses))
                osLabel = nativeOS.concat(" Server");
            else
                osLabel = nativeOS;
        } else if (nativeOS.startsWith("Windows NT")) {
            // Windows NT
            //
            if (hasExchange || isNTServer(addresses))
                osLabel = nativeOS.concat(" Server");
            else
                osLabel = nativeOS;
        } else {
            osLabel = nativeOS;
        }

        return osLabel;
    }

    /**
     * This method is responsible for taking an array of jCIFS NbtAddress
     * objects associated with a particular node and determining if that node is
     * an NT server versus an NT workstation based on the services it has
     * registered.
     * 
     * If the remote host is registered as a DOMAIN_CONTROLLERS or a
     * MS_EXCHANGE_MTA we return 'true'; otherwise, 'false' is returned.
     * 
     * @param addresses
     *            Array of NbtAddress objects associated with the remote host
     *            being tested.
     * 
     * @return <em>true</em> if NT Server, <em>false</em> otherwise.
     */
    static boolean isNTServer(NbtAddress[] addresses) {
        boolean isNTServer = false;

        if (addresses != null) {
            for (int i = 0; i < addresses.length; i++) {
                NbtAddress nbtAddr = addresses[i];

                // Domain Controller
                //
                // If we find an address with a name sufix equal to
                // 0x1C (DOMAIN_CONTROLLER or INTERNET_INFORMATION_SERVER_G)
                // and the hostnamethis box is an NT server.
                //
                if (nbtAddr.getNameType() == DOMAIN_CONTROLLERS && !nbtAddr.getHostName().equals("INET~SERVICES")) {
                    isNTServer = true;
                    break;
                }

                // Exchange Server
                //
                // Looks like a Windows 2000 box running full-blown
                // MS Outlook will appear in the NetBIOS registry
                // as MS_EXCHANGE_IMC and MS_EXCHANGE_MTA so we
                // can't use those suffixes to identify a server
                //
                // So...if we detect MICROSOFT_EXCHANGE_STORE or
                // MICROSOFT_EXCHANGE_DIRECTORY we will assume we
                // have a server.
                // 
                // ??? Can we really assume we have an exchange server
                // based on this and therefore NT Server???
                //
                if (nbtAddr.getNameType() == MS_EXCHANGE_STORE || nbtAddr.getNameType() == MS_EXCHANGE_DIRECTORY) {
                    isNTServer = true;
                    break;
                }

                // Internet Information Server
                //
                // If we find an address with a name suffix equal to
                // 0x1C (INTERNET_INFORMATION_SERVER_G) we are done, we've
                // found an NT server.
                //
                // If we find an address with a name suffix equal to
                // 0x00 (INTERNET_INFORMATION_SERVER_U) we must go on to
                // verify that its NetBIOS name begins with "IS~". If so
                // we've found an NT server.
                //
                // WARNING: Unfortunately IIS on an NT Server looks no different
                // than Personal Web Server on a Windows 2000 Professional
                // box from the standpoint of the NetBIOS name suffixes.
                // So we can't use it to distinguish between server and
                // workstation.
                /*
                 * if (nbtAddr.getNameType() == INTERNET_INFORMATION_SERVER_G) {
                 * isNTServer = true; break; } else if (nbtAddr.getNameType() ==
                 * INTERNET_INFORMATION_SERVER_U) { if
                 * (nbtAddr.getHostName().startsWith("IS~")) { isNTServer =
                 * true; break; } }
                 */
            }
        }

        return isNTServer;
    }

    /**
     * Convenience method which takes an SmbAuth object with userid and password
     * information and the NetBIOS name for a remote server and builds the
     * appropriate SMB url string which can be used to enumerate the server's
     * shares.
     * 
     * @param smbAuth
     *            SMB Authentication object w/ userid/password info
     * @param cname
     *            NetBIOS address of remote server
     * 
     * @return URL string which can be used in a subsequent SmbFile() call.
     */
    static String getSmbURL(SmbAuth smbAuth, String cname) {
        // Build SMB server url
        //
        // If we don't have a valid SmbAuth object url has format:
        // smb://<netbios_name>
        // 
        // For server authentication url has format:
        // smb://<userid>:<password>@<netbios_name>
        //
        // For domain authentication url has format:
        // smb://<domain>;<userid>:<password>@<netbios_name>

        // Set url parameters
        String domainParm = null;
        String useridParm = null;
        String passwordParm = null;

        if (smbAuth != null) {
            useridParm = smbAuth.getUser();
            passwordParm = smbAuth.getPassword();
            if (smbAuth.getType().equalsIgnoreCase("domain"))
                domainParm = smbAuth.getContent();
        }

        // Build url from parms
        String url = "smb://";
        if (domainParm != null)
            url = url.concat(domainParm + ";");

        if (useridParm != null)
            url = url.concat(useridParm);

        if (passwordParm != null)
            url = url.concat(":" + passwordParm);

        if (useridParm != null)
            url = url.concat("@");

        url = url.concat(cname);

        return url;
    }
}
