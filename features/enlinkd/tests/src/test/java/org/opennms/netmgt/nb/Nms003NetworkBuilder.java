/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.nb;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 */

public class Nms003NetworkBuilder extends NmsNetworkBuilder {
    static {
        SWITCH1_IP_IF_MAP.put(InetAddressUtils.addr("172.16.40.1"), 40);
        SWITCH1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.100.246"), 10101);
        SWITCH1_IP_IF_MAP.put(InetAddressUtils.addr("172.16.10.1"), 10);
        SWITCH1_IP_IF_MAP.put(InetAddressUtils.addr("172.16.30.1"), 30);
        SWITCH1_IP_IF_MAP.put(InetAddressUtils.addr("172.16.20.1"), 20);
        SWITCH1_IF_IFNAME_MAP.put(10128, "Gi0/28");
        SWITCH1_IF_IFDESCR_MAP.put(10128, "GigabitEthernet0/28");
        SWITCH1_IF_MAC_MAP.put(10128, "0016c8bd4d9c");
        SWITCH1_IF_IFNAME_MAP.put(10113, "Gi0/13");
        SWITCH1_IF_IFDESCR_MAP.put(10113, "GigabitEthernet0/13");
        SWITCH1_IF_MAC_MAP.put(10113, "0016c8bd4d8d");
        SWITCH1_IF_IFNAME_MAP.put(5001, "Po1");
        SWITCH1_IF_IFDESCR_MAP.put(5001, "Port-channel1");
        SWITCH1_IF_MAC_MAP.put(5001, "0016c8bd4d8c");
        SWITCH1_IF_IFNAME_MAP.put(10117, "Gi0/17");
        SWITCH1_IF_IFDESCR_MAP.put(10117, "GigabitEthernet0/17");
        SWITCH1_IF_MAC_MAP.put(10117, "0016c8bd4d91");
        SWITCH1_IF_IFNAME_MAP.put(10106, "Gi0/6");
        SWITCH1_IF_IFDESCR_MAP.put(10106, "GigabitEthernet0/6");
        SWITCH1_IF_MAC_MAP.put(10106, "0016c8bd4d86");
        SWITCH1_IF_IFNAME_MAP.put(10122, "Gi0/22");
        SWITCH1_IF_IFDESCR_MAP.put(10122, "GigabitEthernet0/22");
        SWITCH1_IF_MAC_MAP.put(10122, "0016c8bd4d96");
        SWITCH1_IF_IFNAME_MAP.put(30, "Vl30");
        SWITCH1_IF_IFDESCR_MAP.put(30, "Vlan30");
        SWITCH1_IF_MAC_MAP.put(30, "0016c8bd4dc4");
        SWITCH1_IF_IFNAME_MAP.put(10, "Vl10");
        SWITCH1_IF_IFDESCR_MAP.put(10, "Vlan10");
        SWITCH1_IF_MAC_MAP.put(10, "0016c8bd4dc2");
        SWITCH1_IF_IFNAME_MAP.put(10102, "Gi0/2");
        SWITCH1_IF_IFDESCR_MAP.put(10102, "GigabitEthernet0/2");
        SWITCH1_IF_MAC_MAP.put(10102, "0016c8bd4d82");
        SWITCH1_IF_IFNAME_MAP.put(10111, "Gi0/11");
        SWITCH1_IF_IFDESCR_MAP.put(10111, "GigabitEthernet0/11");
        SWITCH1_IF_MAC_MAP.put(10111, "0016c8bd4d8b");
        SWITCH1_IF_IFNAME_MAP.put(10109, "Gi0/9");
        SWITCH1_IF_IFDESCR_MAP.put(10109, "GigabitEthernet0/9");
        SWITCH1_IF_MAC_MAP.put(10109, "0016c8bd4d89");
        SWITCH1_IF_IFNAME_MAP.put(10126, "Gi0/26");
        SWITCH1_IF_IFDESCR_MAP.put(10126, "GigabitEthernet0/26");
        SWITCH1_IF_MAC_MAP.put(10126, "0016c8bd4d9a");
        SWITCH1_IF_IFNAME_MAP.put(10105, "Gi0/5");
        SWITCH1_IF_IFDESCR_MAP.put(10105, "GigabitEthernet0/5");
        SWITCH1_IF_MAC_MAP.put(10105, "0016c8bd4d85");
        SWITCH1_IF_IFNAME_MAP.put(10116, "Gi0/16");
        SWITCH1_IF_IFDESCR_MAP.put(10116, "GigabitEthernet0/16");
        SWITCH1_IF_MAC_MAP.put(10116, "0016c8bd4d90");
        SWITCH1_IF_IFNAME_MAP.put(10123, "Gi0/23");
        SWITCH1_IF_IFDESCR_MAP.put(10123, "GigabitEthernet0/23");
        SWITCH1_IF_MAC_MAP.put(10123, "0016c8bd4d97");
        SWITCH1_IF_IFNAME_MAP.put(10127, "Gi0/27");
        SWITCH1_IF_IFDESCR_MAP.put(10127, "GigabitEthernet0/27");
        SWITCH1_IF_MAC_MAP.put(10127, "0016c8bd4d9b");
        SWITCH1_IF_IFNAME_MAP.put(10110, "Gi0/10");
        SWITCH1_IF_IFDESCR_MAP.put(10110, "GigabitEthernet0/10");
        SWITCH1_IF_MAC_MAP.put(10110, "0016c8bd4d8a");
        SWITCH1_IF_IFNAME_MAP.put(20, "Vl20");
        SWITCH1_IF_IFDESCR_MAP.put(20, "Vlan20");
        SWITCH1_IF_MAC_MAP.put(20, "0016c8bd4dc3");
        SWITCH1_IF_IFNAME_MAP.put(1, "Vl1");
        SWITCH1_IF_IFDESCR_MAP.put(1, "Vlan1");
        SWITCH1_IF_MAC_MAP.put(1, "0016c8bd4dc0");
        SWITCH1_IF_IFNAME_MAP.put(40, "Vl40");
        SWITCH1_IF_IFDESCR_MAP.put(40, "Vlan40");
        SWITCH1_IF_MAC_MAP.put(40, "0016c8bd4dc5");
        SWITCH1_IF_IFNAME_MAP.put(10104, "Gi0/4");
        SWITCH1_IF_IFDESCR_MAP.put(10104, "GigabitEthernet0/4");
        SWITCH1_IF_MAC_MAP.put(10104, "0016c8bd4d84");
        SWITCH1_IF_IFNAME_MAP.put(10119, "Gi0/19");
        SWITCH1_IF_IFDESCR_MAP.put(10119, "GigabitEthernet0/19");
        SWITCH1_IF_MAC_MAP.put(10119, "0016c8bd4d93");
        SWITCH1_IF_IFNAME_MAP.put(10501, "Nu0");
        SWITCH1_IF_IFDESCR_MAP.put(10501, "Null0");
        SWITCH1_IF_IFNAME_MAP.put(10121, "Gi0/21");
        SWITCH1_IF_IFDESCR_MAP.put(10121, "GigabitEthernet0/21");
        SWITCH1_IF_MAC_MAP.put(10121, "0016c8bd4d95");
        SWITCH1_IF_IFNAME_MAP.put(10115, "Gi0/15");
        SWITCH1_IF_IFDESCR_MAP.put(10115, "GigabitEthernet0/15");
        SWITCH1_IF_MAC_MAP.put(10115, "0016c8bd4d8f");
        SWITCH1_IF_IFNAME_MAP.put(10101, "Gi0/1");
        SWITCH1_IF_IFDESCR_MAP.put(10101, "GigabitEthernet0/1");
        SWITCH1_IF_MAC_MAP.put(10101, "0016c8bd4dc1");
        SWITCH1_IF_IFNAME_MAP.put(10112, "Gi0/12");
        SWITCH1_IF_IFDESCR_MAP.put(10112, "GigabitEthernet0/12");
        SWITCH1_IF_MAC_MAP.put(10112, "0016c8bd4d8c");
        SWITCH1_IF_IFNAME_MAP.put(10108, "Gi0/8");
        SWITCH1_IF_IFDESCR_MAP.put(10108, "GigabitEthernet0/8");
        SWITCH1_IF_MAC_MAP.put(10108, "0016c8bd4d88");
        SWITCH1_IF_IFNAME_MAP.put(10107, "Gi0/7");
        SWITCH1_IF_IFDESCR_MAP.put(10107, "GigabitEthernet0/7");
        SWITCH1_IF_MAC_MAP.put(10107, "0016c8bd4d87");
        SWITCH1_IF_IFNAME_MAP.put(10118, "Gi0/18");
        SWITCH1_IF_IFDESCR_MAP.put(10118, "GigabitEthernet0/18");
        SWITCH1_IF_MAC_MAP.put(10118, "0016c8bd4d92");
        SWITCH1_IF_IFNAME_MAP.put(10120, "Gi0/20");
        SWITCH1_IF_IFDESCR_MAP.put(10120, "GigabitEthernet0/20");
        SWITCH1_IF_MAC_MAP.put(10120, "0016c8bd4d94");
        SWITCH1_IF_IFNAME_MAP.put(10125, "Gi0/25");
        SWITCH1_IF_IFDESCR_MAP.put(10125, "GigabitEthernet0/25");
        SWITCH1_IF_MAC_MAP.put(10125, "0016c8bd4d99");
        SWITCH1_IF_IFNAME_MAP.put(10103, "Gi0/3");
        SWITCH1_IF_IFDESCR_MAP.put(10103, "GigabitEthernet0/3");
        SWITCH1_IF_MAC_MAP.put(10103, "0016c8bd4d83");
        SWITCH1_IF_IFNAME_MAP.put(10124, "Gi0/24");
        SWITCH1_IF_IFDESCR_MAP.put(10124, "GigabitEthernet0/24");
        SWITCH1_IF_MAC_MAP.put(10124, "0016c8bd4d98");
        SWITCH1_IF_IFNAME_MAP.put(10114, "Gi0/14");
        SWITCH1_IF_IFDESCR_MAP.put(10114, "GigabitEthernet0/14");
        SWITCH1_IF_MAC_MAP.put(10114, "0016c8bd4d8e");
        SWITCH2_IP_IF_MAP.put(InetAddressUtils.addr("172.16.10.2"), 10);
        SWITCH2_IF_IFNAME_MAP.put(10103, "Gi0/3");
        SWITCH2_IF_IFDESCR_MAP.put(10103, "GigabitEthernet0/3");
        SWITCH2_IF_MAC_MAP.put(10103, "0016c894aa83");
        SWITCH2_IF_IFNAME_MAP.put(10104, "Gi0/4");
        SWITCH2_IF_IFDESCR_MAP.put(10104, "GigabitEthernet0/4");
        SWITCH2_IF_MAC_MAP.put(10104, "0016c894aa84");
        SWITCH2_IF_IFNAME_MAP.put(10111, "Gi0/11");
        SWITCH2_IF_IFDESCR_MAP.put(10111, "GigabitEthernet0/11");
        SWITCH2_IF_MAC_MAP.put(10111, "0016c894aa8b");
        SWITCH2_IF_IFNAME_MAP.put(10, "Vl10");
        SWITCH2_IF_IFDESCR_MAP.put(10, "Vlan10");
        SWITCH2_IF_MAC_MAP.put(10, "0016c894aac1");
        SWITCH2_IF_IFNAME_MAP.put(10115, "Gi0/15");
        SWITCH2_IF_IFDESCR_MAP.put(10115, "GigabitEthernet0/15");
        SWITCH2_IF_MAC_MAP.put(10115, "0016c894aa8f");
        SWITCH2_IF_IFNAME_MAP.put(10109, "Gi0/9");
        SWITCH2_IF_IFDESCR_MAP.put(10109, "GigabitEthernet0/9");
        SWITCH2_IF_MAC_MAP.put(10109, "0016c894aa89");
        SWITCH2_IF_IFNAME_MAP.put(5002, "Po2");
        SWITCH2_IF_IFDESCR_MAP.put(5002, "Port-channel2");
        SWITCH2_IF_MAC_MAP.put(5002, "0016c894aa94");
        SWITCH2_IF_IFNAME_MAP.put(5001, "Po1");
        SWITCH2_IF_IFDESCR_MAP.put(5001, "Port-channel1");
        SWITCH2_IF_MAC_MAP.put(5001, "0016c894aa81");
        SWITCH2_IF_IFNAME_MAP.put(10118, "Gi0/18");
        SWITCH2_IF_IFDESCR_MAP.put(10118, "GigabitEthernet0/18");
        SWITCH2_IF_MAC_MAP.put(10118, "0016c894aa92");
        SWITCH2_IF_IFNAME_MAP.put(10101, "Gi0/1");
        SWITCH2_IF_IFDESCR_MAP.put(10101, "GigabitEthernet0/1");
        SWITCH2_IF_MAC_MAP.put(10101, "0016c894aa81");
        SWITCH2_IF_IFNAME_MAP.put(10117, "Gi0/17");
        SWITCH2_IF_IFDESCR_MAP.put(10117, "GigabitEthernet0/17");
        SWITCH2_IF_MAC_MAP.put(10117, "0016c894aa91");
        SWITCH2_IF_IFNAME_MAP.put(10108, "Gi0/8");
        SWITCH2_IF_IFDESCR_MAP.put(10108, "GigabitEthernet0/8");
        SWITCH2_IF_MAC_MAP.put(10108, "0016c894aa88");
        SWITCH2_IF_IFNAME_MAP.put(10112, "Gi0/12");
        SWITCH2_IF_IFDESCR_MAP.put(10112, "GigabitEthernet0/12");
        SWITCH2_IF_MAC_MAP.put(10112, "0016c894aa8c");
        SWITCH2_IF_IFNAME_MAP.put(10123, "Gi0/23");
        SWITCH2_IF_IFDESCR_MAP.put(10123, "GigabitEthernet0/23");
        SWITCH2_IF_MAC_MAP.put(10123, "0016c894aa97");
        SWITCH2_IF_IFNAME_MAP.put(10122, "Gi0/22");
        SWITCH2_IF_IFDESCR_MAP.put(10122, "GigabitEthernet0/22");
        SWITCH2_IF_MAC_MAP.put(10122, "0016c894aa96");
        SWITCH2_IF_IFNAME_MAP.put(10102, "Gi0/2");
        SWITCH2_IF_IFDESCR_MAP.put(10102, "GigabitEthernet0/2");
        SWITCH2_IF_MAC_MAP.put(10102, "0016c894aa82");
        SWITCH2_IF_IFNAME_MAP.put(10113, "Gi0/13");
        SWITCH2_IF_IFDESCR_MAP.put(10113, "GigabitEthernet0/13");
        SWITCH2_IF_MAC_MAP.put(10113, "0016c894aa8d");
        SWITCH2_IF_IFNAME_MAP.put(10501, "Nu0");
        SWITCH2_IF_IFDESCR_MAP.put(10501, "Null0");
        SWITCH2_IF_IFNAME_MAP.put(10110, "Gi0/10");
        SWITCH2_IF_IFDESCR_MAP.put(10110, "GigabitEthernet0/10");
        SWITCH2_IF_MAC_MAP.put(10110, "0016c894aa8a");
        SWITCH2_IF_IFNAME_MAP.put(10106, "Gi0/6");
        SWITCH2_IF_IFDESCR_MAP.put(10106, "GigabitEthernet0/6");
        SWITCH2_IF_MAC_MAP.put(10106, "0016c894aa86");
        SWITCH2_IF_IFNAME_MAP.put(10120, "Gi0/20");
        SWITCH2_IF_IFDESCR_MAP.put(10120, "GigabitEthernet0/20");
        SWITCH2_IF_MAC_MAP.put(10120, "0016c894aa94");
        SWITCH2_IF_IFNAME_MAP.put(10124, "Gi0/24");
        SWITCH2_IF_IFDESCR_MAP.put(10124, "GigabitEthernet0/24");
        SWITCH2_IF_MAC_MAP.put(10124, "0016c894aa98");
        SWITCH2_IF_IFNAME_MAP.put(10107, "Gi0/7");
        SWITCH2_IF_IFDESCR_MAP.put(10107, "GigabitEthernet0/7");
        SWITCH2_IF_MAC_MAP.put(10107, "0016c894aa87");
        SWITCH2_IF_IFNAME_MAP.put(1, "Vl1");
        SWITCH2_IF_IFDESCR_MAP.put(1, "Vlan1");
        SWITCH2_IF_MAC_MAP.put(1, "0016c894aac0");
        SWITCH2_IF_IFNAME_MAP.put(10114, "Gi0/14");
        SWITCH2_IF_IFDESCR_MAP.put(10114, "GigabitEthernet0/14");
        SWITCH2_IF_MAC_MAP.put(10114, "0016c894aa8e");
        SWITCH2_IF_IFNAME_MAP.put(10119, "Gi0/19");
        SWITCH2_IF_IFDESCR_MAP.put(10119, "GigabitEthernet0/19");
        SWITCH2_IF_MAC_MAP.put(10119, "0016c894aa93");
        SWITCH2_IF_IFNAME_MAP.put(10105, "Gi0/5");
        SWITCH2_IF_IFDESCR_MAP.put(10105, "GigabitEthernet0/5");
        SWITCH2_IF_MAC_MAP.put(10105, "0016c894aa85");
        SWITCH2_IF_IFNAME_MAP.put(10121, "Gi0/21");
        SWITCH2_IF_IFDESCR_MAP.put(10121, "GigabitEthernet0/21");
        SWITCH2_IF_MAC_MAP.put(10121, "0016c894aa95");
        SWITCH2_IF_IFNAME_MAP.put(10116, "Gi0/16");
        SWITCH2_IF_IFDESCR_MAP.put(10116, "GigabitEthernet0/16");
        SWITCH2_IF_MAC_MAP.put(10116, "0016c894aa90");
        SWITCH3_IP_IF_MAP.put(InetAddressUtils.addr("172.16.10.3"), 10);
        SWITCH3_IF_IFNAME_MAP.put(10008, "Fa0/8");
        SWITCH3_IF_IFDESCR_MAP.put(10008, "FastEthernet0/8");
        SWITCH3_IF_MAC_MAP.put(10008, "f4ea67ebdc08");
        SWITCH3_IF_IFNAME_MAP.put(10, "Vl10");
        SWITCH3_IF_IFDESCR_MAP.put(10, "Vlan10");
        SWITCH3_IF_MAC_MAP.put(10, "f4ea67ebdc41");
        SWITCH3_IF_IFNAME_MAP.put(10003, "Fa0/3");
        SWITCH3_IF_IFDESCR_MAP.put(10003, "FastEthernet0/3");
        SWITCH3_IF_MAC_MAP.put(10003, "f4ea67ebdc03");
        SWITCH3_IF_IFNAME_MAP.put(10021, "Fa0/21");
        SWITCH3_IF_IFDESCR_MAP.put(10021, "FastEthernet0/21");
        SWITCH3_IF_MAC_MAP.put(10021, "f4ea67ebdc15");
        SWITCH3_IF_IFNAME_MAP.put(10024, "Fa0/24");
        SWITCH3_IF_IFDESCR_MAP.put(10024, "FastEthernet0/24");
        SWITCH3_IF_MAC_MAP.put(10024, "f4ea67ebdc18");
        SWITCH3_IF_IFNAME_MAP.put(10001, "Fa0/1");
        SWITCH3_IF_IFDESCR_MAP.put(10001, "FastEthernet0/1");
        SWITCH3_IF_MAC_MAP.put(10001, "f4ea67ebdc01");
        SWITCH3_IF_IFNAME_MAP.put(10014, "Fa0/14");
        SWITCH3_IF_IFDESCR_MAP.put(10014, "FastEthernet0/14");
        SWITCH3_IF_MAC_MAP.put(10014, "f4ea67ebdc0e");
        SWITCH3_IF_IFNAME_MAP.put(10019, "Fa0/19");
        SWITCH3_IF_IFDESCR_MAP.put(10019, "FastEthernet0/19");
        SWITCH3_IF_MAC_MAP.put(10019, "f4ea67ebdc13");
        SWITCH3_IF_IFNAME_MAP.put(5001, "Po1");
        SWITCH3_IF_IFDESCR_MAP.put(5001, "Port-channel1");
        SWITCH3_IF_MAC_MAP.put(5001, "f4ea67ebdc13");
        SWITCH3_IF_IFNAME_MAP.put(10023, "Fa0/23");
        SWITCH3_IF_IFDESCR_MAP.put(10023, "FastEthernet0/23");
        SWITCH3_IF_MAC_MAP.put(10023, "f4ea67ebdc17");
        SWITCH3_IF_IFNAME_MAP.put(10012, "Fa0/12");
        SWITCH3_IF_IFDESCR_MAP.put(10012, "FastEthernet0/12");
        SWITCH3_IF_MAC_MAP.put(10012, "f4ea67ebdc0c");
        SWITCH3_IF_IFNAME_MAP.put(10018, "Fa0/18");
        SWITCH3_IF_IFDESCR_MAP.put(10018, "FastEthernet0/18");
        SWITCH3_IF_MAC_MAP.put(10018, "f4ea67ebdc12");
        SWITCH3_IF_IFNAME_MAP.put(10013, "Fa0/13");
        SWITCH3_IF_IFDESCR_MAP.put(10013, "FastEthernet0/13");
        SWITCH3_IF_MAC_MAP.put(10013, "f4ea67ebdc0d");
        SWITCH3_IF_IFNAME_MAP.put(10102, "Gi0/2");
        SWITCH3_IF_IFDESCR_MAP.put(10102, "GigabitEthernet0/2");
        SWITCH3_IF_MAC_MAP.put(10102, "f4ea67ebdc1a");
        SWITCH3_IF_IFNAME_MAP.put(1, "Vl1");
        SWITCH3_IF_IFDESCR_MAP.put(1, "Vlan1");
        SWITCH3_IF_MAC_MAP.put(1, "f4ea67ebdc40");
        SWITCH3_IF_IFNAME_MAP.put(10005, "Fa0/5");
        SWITCH3_IF_IFDESCR_MAP.put(10005, "FastEthernet0/5");
        SWITCH3_IF_MAC_MAP.put(10005, "f4ea67ebdc05");
        SWITCH3_IF_IFNAME_MAP.put(10016, "Fa0/16");
        SWITCH3_IF_IFDESCR_MAP.put(10016, "FastEthernet0/16");
        SWITCH3_IF_MAC_MAP.put(10016, "f4ea67ebdc10");
        SWITCH3_IF_IFNAME_MAP.put(10010, "Fa0/10");
        SWITCH3_IF_IFDESCR_MAP.put(10010, "FastEthernet0/10");
        SWITCH3_IF_MAC_MAP.put(10010, "f4ea67ebdc0a");
        SWITCH3_IF_IFNAME_MAP.put(10002, "Fa0/2");
        SWITCH3_IF_IFDESCR_MAP.put(10002, "FastEthernet0/2");
        SWITCH3_IF_MAC_MAP.put(10002, "f4ea67ebdc02");
        SWITCH3_IF_IFNAME_MAP.put(10022, "Fa0/22");
        SWITCH3_IF_IFDESCR_MAP.put(10022, "FastEthernet0/22");
        SWITCH3_IF_MAC_MAP.put(10022, "f4ea67ebdc16");
        SWITCH3_IF_IFNAME_MAP.put(10011, "Fa0/11");
        SWITCH3_IF_IFDESCR_MAP.put(10011, "FastEthernet0/11");
        SWITCH3_IF_MAC_MAP.put(10011, "f4ea67ebdc0b");
        SWITCH3_IF_IFNAME_MAP.put(10101, "Gi0/1");
        SWITCH3_IF_IFDESCR_MAP.put(10101, "GigabitEthernet0/1");
        SWITCH3_IF_MAC_MAP.put(10101, "f4ea67ebdc19");
        SWITCH3_IF_IFNAME_MAP.put(10020, "Fa0/20");
        SWITCH3_IF_IFDESCR_MAP.put(10020, "FastEthernet0/20");
        SWITCH3_IF_MAC_MAP.put(10020, "f4ea67ebdc14");
        SWITCH3_IF_IFNAME_MAP.put(10006, "Fa0/6");
        SWITCH3_IF_IFDESCR_MAP.put(10006, "FastEthernet0/6");
        SWITCH3_IF_MAC_MAP.put(10006, "f4ea67ebdc06");
        SWITCH3_IF_IFNAME_MAP.put(10501, "Nu0");
        SWITCH3_IF_IFDESCR_MAP.put(10501, "Null0");
        SWITCH3_IF_IFNAME_MAP.put(10009, "Fa0/9");
        SWITCH3_IF_IFDESCR_MAP.put(10009, "FastEthernet0/9");
        SWITCH3_IF_MAC_MAP.put(10009, "f4ea67ebdc09");
        SWITCH3_IF_IFNAME_MAP.put(10015, "Fa0/15");
        SWITCH3_IF_IFDESCR_MAP.put(10015, "FastEthernet0/15");
        SWITCH3_IF_MAC_MAP.put(10015, "f4ea67ebdc0f");
        SWITCH3_IF_IFNAME_MAP.put(10017, "Fa0/17");
        SWITCH3_IF_IFDESCR_MAP.put(10017, "FastEthernet0/17");
        SWITCH3_IF_MAC_MAP.put(10017, "f4ea67ebdc11");
        SWITCH3_IF_IFNAME_MAP.put(10007, "Fa0/7");
        SWITCH3_IF_IFDESCR_MAP.put(10007, "FastEthernet0/7");
        SWITCH3_IF_MAC_MAP.put(10007, "f4ea67ebdc07");
        SWITCH3_IF_IFNAME_MAP.put(10004, "Fa0/4");
        SWITCH3_IF_IFDESCR_MAP.put(10004, "FastEthernet0/4");
        SWITCH3_IF_MAC_MAP.put(10004, "f4ea67ebdc04");
    }

    public OnmsNode getSwitch1() {
        return getNode(SWITCH1_NAME,SWITCH1_SYSOID,SWITCH1_IP,SWITCH1_IP_IF_MAP,SWITCH1_IF_IFNAME_MAP,SWITCH1_IF_MAC_MAP,SWITCH1_IF_IFDESCR_MAP,SWITCH1_IF_IFALIAS_MAP);
    }    

    public OnmsNode getSwitch2() {
        return getNode(SWITCH2_NAME,SWITCH2_SYSOID,SWITCH2_IP,SWITCH2_IP_IF_MAP,SWITCH2_IF_IFNAME_MAP,SWITCH2_IF_MAC_MAP,SWITCH2_IF_IFDESCR_MAP,SWITCH2_IF_IFALIAS_MAP);
    }    

    public OnmsNode getSwitch3() {
       return getNode(SWITCH3_NAME,SWITCH3_SYSOID,SWITCH3_IP,SWITCH3_IP_IF_MAP,SWITCH3_IF_IFNAME_MAP,SWITCH3_IF_MAC_MAP,SWITCH3_IF_IFDESCR_MAP,SWITCH3_IF_IFALIAS_MAP);
    }
}    

