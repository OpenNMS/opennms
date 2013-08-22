/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd.nb;


import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public abstract class Nms0001NetworkBuilder extends LinkdNetworkBuilder {

    protected static final String FROH_IP = "192.168.239.51";
    protected static final String FROH_NAME = "froh";
    static final String FROH_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.29";
    
    protected static final Map<InetAddress,Integer> FROH_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FROH_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FROH_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FROH_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FROH_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FROH_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String OEDIPUS_IP = "192.168.239.62";
    protected static final String OEDIPUS_NAME = "oedipus";
    static final String OEDIPUS_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";
   
    protected static final Map<InetAddress,Integer> OEDIPUS_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> OEDIPUS_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> OEDIPUS_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> OEDIPUS_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> OEDIPUS_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> OEDIPUS_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
        
    protected static final String SIEGFRIE_IP = "192.168.239.54";
    protected static final String SIEGFRIE_NAME = "siegfrie";
    static final String SIEGFRIE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";
   
    protected static final Map<InetAddress,Integer> SIEGFRIE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SIEGFRIE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SIEGFRIE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SIEGFRIE_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SIEGFRIE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SIEGFRIE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

protected static final String FROH_ISIS_SYS_ID     = "000110088500";
protected static final String OEDIPUS_ISIS_SYS_ID  = "000110255062";
protected static final String SIEGFRIE_ISIS_SYS_ID = "000110255054";


}
