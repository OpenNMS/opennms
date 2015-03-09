/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero
 * General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/ For more information contact: OpenNMS(R)
 * Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.omg.PortableInterceptor.INACTIVE;
import org.opennms.core.network.IPAddress;

public abstract class LldpUtils {

    public enum LldpChassisIdSubType {
        /*
         * LldpChassisIdSubtype ::= TEXTUAL-CONVENTION STATUS current
         * DESCRIPTION "This TC describes the source of a chassis identifier.
         * 
         * The enumeration 'chassisComponent(1)' represents a chassis
         * identifier based on the value of entPhysicalAlias object (defined
         * in IETF RFC 2737) for a chassis component (i.e., an
         * entPhysicalClass value of 'chassis(3)').
         * 
         * The enumeration 'interfaceAlias(2)' represents a chassis identifier
         * based on the value of ifAlias object (defined in IETF RFC 2863) for
         * an interface on the containing chassis.
         * 
         * The enumeration 'portComponent(3)' represents a chassis identifier
         * based on the value of entPhysicalAlias object (defined in IETF RFC
         * 2737) for a port or backplane component (i.e., entPhysicalClass
         * value of 'port(10)' or 'backplane(4)'), within the containing
         * chassis.
         * 
         * The enumeration 'macAddress(4)' represents a chassis identifier
         * based on the value of a unicast source address (encoded in network
         * byte order and IEEE 802.3 canonical bit order), of a port on the
         * containing chassis as defined in IEEE Std 802-2001.
         * 
         * The enumeration 'networkAddress(5)' represents a chassis identifier
         * based on a network address, associated with a particular chassis.
         * The encoded address is actually composed of two fields. The first
         * field is a single octet, representing the IANA AddressFamilyNumbers
         * value for the specific address type, and the second field is the
         * network address value.
         * 
         * The enumeration 'interfaceName(6)' represents a chassis identifier
         * based on the value of ifName object (defined in IETF RFC 2863) for
         * an interface on the containing chassis.
         * 
         * The enumeration 'local(7)' represents a chassis identifier based on
         * a locally defined value." SYNTAX INTEGER { chassisComponent(1),
         * interfaceAlias(2), portComponent(3), macAddress(4),
         * networkAddress(5), interfaceName(6), local(7) }
         */
        LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT(1), LLDP_CHASSISID_SUBTYPE_INTERFACEALIAS(
                2), LLDP_CHASSISID_SUBTYPE_PORTCOMPONENT(3), LLDP_CHASSISID_SUBTYPE_MACADDRESS(
                4), LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS(5), LLDP_CHASSISID_SUBTYPE_INTERFACENAME(
                6), LLDP_CHASSISID_SUBTYPE_LOCAL(7);

        private int m_type;

        LldpChassisIdSubType(int type) {
            m_type = type;
        }

        protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
            s_typeMap.put(1, "chassisComponent");
            s_typeMap.put(2, "interfaceAlias");
            s_typeMap.put(3, "portComponent");
            s_typeMap.put(4, "macAddress");
            s_typeMap.put(5, "networkAddress");
            s_typeMap.put(6, "interfaceName");
            s_typeMap.put(7, "local");
        }

        /**
         * <p>
         * ElementIdentifierTypeString
         * </p>
         * 
         * @return a {@link java.lang.String} object.
         */
        /**
 */
        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                return s_typeMap.get(code);
            return null;
        }

        public Integer getValue() {
            return m_type;
        }

        public static LldpChassisIdSubType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException(
                                                   "Cannot create LldpChassisIdSubType from null code");
            switch (code) {
            case 1:
                return LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT;
            case 2:
                return LLDP_CHASSISID_SUBTYPE_INTERFACEALIAS;
            case 3:
                return LLDP_CHASSISID_SUBTYPE_PORTCOMPONENT;
            case 4:
                return LLDP_CHASSISID_SUBTYPE_MACADDRESS;
            case 5:
                return LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS;
            case 6:
                return LLDP_CHASSISID_SUBTYPE_INTERFACENAME;
            case 7:
                return LLDP_CHASSISID_SUBTYPE_LOCAL;
            default:
                throw new IllegalArgumentException(
                                                   "Cannot create LldpChassisIdSubType from code "
                                                           + code);
            }
        }

    }

    public enum LldpPortIdSubType {
        LLDP_PORTID_SUBTYPE_INTERFACEALIAS(1), LLDP_PORTID_SUBTYPE_PORTCOMPONENT(
                2), LLDP_PORTID_SUBTYPE_MACADDRESS(3), LLDP_PORTID_SUBTYPE_NETWORKADDRESS(
                4), LLDP_PORTID_SUBTYPE_INTERFACENAME(5), LLDP_PORTID_SUBTYPE_AGENTCIRCUITID(
                6), LLDP_PORTID_SUBTYPE_LOCAL(7);
        /*
         * LldpPortIdSubtype ::= TEXTUAL-CONVENTION STATUS current DESCRIPTION
         * "This TC describes the source of a particular type of port
         * identifier used in the LLDP MIB.
         * 
         * The enumeration 'interfaceAlias(1)' represents a port identifier
         * based on the ifAlias MIB object, defined in IETF RFC 2863.
         * 
         * The enumeration 'portComponent(2)' represents a port identifier
         * based on the value of entPhysicalAlias (defined in IETF RFC 2737)
         * for a port component (i.e., entPhysicalClass value of 'port(10)'),
         * within the containing chassis.
         * 
         * The enumeration 'macAddress(3)' represents a port identifier based
         * on a unicast source address (encoded in network byte order and IEEE
         * 802.3 canonical bit order), which has been detected by the agent
         * and associated with a particular port (IEEE Std 802-2001).
         * 
         * The enumeration 'networkAddress(4)' represents a port identifier
         * based on a network address, detected by the agent and associated
         * with a particular port.
         * 
         * The enumeration 'interfaceName(5)' represents a port identifier
         * based on the ifName MIB object, defined in IETF RFC 2863.
         * 
         * The enumeration 'agentCircuitId(6)' represents a port identifier
         * based on the agent-local identifier of the circuit (defined in RFC
         * 3046), detected by the agent and associated with a particular port.
         * 
         * The enumeration 'local(7)' represents a port identifier based on a
         * value locally assigned."
         * 
         * SYNTAX INTEGER { interfaceAlias(1), portComponent(2),
         * macAddress(3), networkAddress(4), interfaceName(5),
         * agentCircuitId(6), local(7) }
         */
        private int m_type;

        LldpPortIdSubType(Integer chassisIdsubtype) {
            m_type = chassisIdsubtype;
        }

        protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
            s_typeMap.put(1, "interfaceAlias");
            s_typeMap.put(2, "portComponent");
            s_typeMap.put(3, "macAddress");
            s_typeMap.put(4, "networkAddress");
            s_typeMap.put(5, "interfaceName");
            s_typeMap.put(6, "agentCircuitId");
            s_typeMap.put(7, "local");
        }

        /**
         * <p>
         * ElementIdentifierTypeString
         * </p>
         * 
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                return s_typeMap.get(code);
            return null;
        }

        public static LldpPortIdSubType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException(
                                                   "Cannot create LldpPortIdSubType from null code");
            switch (code) {
            case 1:
                return LLDP_PORTID_SUBTYPE_INTERFACEALIAS;
            case 2:
                return LLDP_PORTID_SUBTYPE_PORTCOMPONENT;
            case 3:
                return LLDP_PORTID_SUBTYPE_MACADDRESS;
            case 4:
                return LLDP_PORTID_SUBTYPE_NETWORKADDRESS;
            case 5:
                return LLDP_PORTID_SUBTYPE_INTERFACENAME;
            case 6:
                return LLDP_PORTID_SUBTYPE_AGENTCIRCUITID;
            case 7:
                return LLDP_PORTID_SUBTYPE_LOCAL;
            default:
                throw new IllegalArgumentException(
                                                   "Cannot create LldpPortIdSubType from code "
                                                           + code);
            }
        }

        public Integer getValue() {
            return m_type;
        }

    }
    
    public enum  IanaAddressFamilyNumber {
    /*
     * 0       Reserved                
     * 1       IP (IP version 4)               
     * 2       IP6 (IP version 6)              
     * 3       NSAP            
     * 4       HDLC (8-bit multidrop)          
     * 5       BBN 1822                
     * 6       802 (includes all 802 media plus Ethernet "canonical format")           
     * 7       E.163           
     * 8       E.164 (SMDS, Frame Relay, ATM)          
     * 9       F.69 (Telex)            
     * 10      X.121 (X.25, Frame Relay)               
     * 11      IPX             
     * 12      Appletalk               
     * 13      Decnet IV               
     * 14      Banyan Vines            
     * 15      E.164 with NSAP format subaddress       [ATM Forum UNI 3.1. October 1995.][Andy_Malis]  
     * 16      DNS (Domain Name System)                
     * 17      Distinguished Name      [Charles_Lynn]  
     * 18      AS Number       [Charles_Lynn]  
     * 19      XTP over IP version 4   [Mike_Saul]     
     * 20      XTP over IP version 6   [Mike_Saul]     
     * 21      XTP native mode XTP     [Mike_Saul]     
     * 22      Fibre Channel World-Wide Port Name      [Mark_Bakke]    
     * 23      Fibre Channel World-Wide Node Name      [Mark_Bakke]    
     * 24      GWID    [Subra_Hegde]   
     * 25      AFI for L2VPN information       [RFC4761][RFC6074]      
     * 26      MPLS-TP Section Endpoint Identifier     [RFC7212]       
     * 27      MPLS-TP LSP Endpoint Identifier [RFC7212]       
     * 28      MPLS-TP Pseudowire Endpoint Identifier  [RFC7212]       
     * 29      MT IP: Multi-Topology IP version 4      [RFC7307]       
     * 30      MT IPv6: Multi-Topology IP version 6
     * 31-16383        Unassigned              
     * 16384   EIGRP Common Service Family     [Donnie_Savage] 2008-05-13
     * 16385   EIGRP IPv4 Service Family       [Donnie_Savage] 2008-05-13
     * 16386   EIGRP IPv6 Service Family       [Donnie_Savage] 2008-05-13
     * 16387   LISP Canonical Address Format (LCAF)    [David_Meyer]   2009-11-12
     * 16388   BGP-LS  [draft-ietf-idr-ls-distribution]        2013-03-20
     * 16389   48-bit MAC      [RFC7042]       2013-05-06
     * 16390   64-bit MAC      [RFC7042]       2013-05-06
     * 16391   OUI     [draft-eastlake-trill-ia-appsubtlv]     2013-09-25
     * 16392   MAC/24  [draft-eastlake-trill-ia-appsubtlv]     2013-09-25
     * 16393   MAC/40  [draft-eastlake-trill-ia-appsubtlv]     2013-09-25
     * 16394   IPv6/64 [draft-eastlake-trill-ia-appsubtlv]     2013-09-25
     * 16395   RBridge Port ID [draft-eastlake-trill-ia-appsubtlv]     2013-09-25
     * 16396   TRILL Nickname  [RFC-ietf-trill-oam-fm-11]      2014-09-02
     * 16397-32767     Unassigned              
     * 32768-65534     Unassigned              
     * 65535   Reserved             
     *  
     */
        IANA_ADDRESS_FAMILY_Reserved(0),
        IANA_ADDRESS_FAMILY_IP(1),
        IANA_ADDRESS_FAMILY_IP6(2),
        IANA_ADDRESS_FAMILY_NSAP(3),
        IANA_ADDRESS_FAMILY_HDLC(4),
        IANA_ADDRESS_FAMILY_BBN(5),
        IANA_ADDRESS_FAMILY_802(6),
        IANA_ADDRESS_FAMILY_E_163(7),
        IANA_ADDRESS_FAMILY_E_164(8),
        IANA_ADDRESS_FAMILY_F_69(9),
        IANA_ADDRESS_FAMILY_X_121(10),
        IANA_ADDRESS_FAMILY_IPX(11),
        IANA_ADDRESS_FAMILY_Appletalk(12),
        IANA_ADDRESS_FAMILY_Decnet(13),
        IANA_ADDRESS_FAMILY_Banyan(14),
        IANA_ADDRESS_FAMILY_E_164_NSAP(15),
        IANA_ADDRESS_FAMILY_DNS(16),
        IANA_ADDRESS_FAMILY_Distinguished(17),
        IANA_ADDRESS_FAMILY_AS(18),
        IANA_ADDRESS_FAMILY_XTP_IP(19),
        IANA_ADDRESS_FAMILY_XTP_IPv6(20),
        IANA_ADDRESS_FAMILY_XTP(21),
        IANA_ADDRESS_FAMILY_FibreChannel_World_Wide_Port_Name(22),
        IANA_ADDRESS_FAMILY_FibreChannel_World_Wide_Node_Name(23),
        IANA_ADDRESS_FAMILY_GWID(24),
        IANA_ADDRESS_FAMILY_AFI(25),
        IANA_ADDRESS_FAMILY_MPLS_TP_Section_Endpoint_Identifier(26),
        IANA_ADDRESS_FAMILY_MPLS_TP_LSP_Endpoint_Identifier(27),
        IANA_ADDRESS_FAMILY_MPLS_Pseudowire_Section_Endpoint_Identifier(28),
        IANA_ADDRESS_FAMILY_MT_IP(29),
        IANA_ADDRESS_FAMILY_MT_IPv6(30),
        IANA_ADDRESS_FAMILY_Unassigned(31);
        
        private int m_type;

        public Integer getValue() {
            return m_type;
        }

        IanaAddressFamilyNumber(Integer ianaAddressFamilyNumber) {
            m_type = ianaAddressFamilyNumber;
        }

        protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
            s_typeMap.put(0, "Reserved");
            s_typeMap.put(1, "IP (IP version 4)");
            s_typeMap.put(2, "IP6 (IP version 6)");
            s_typeMap.put(3, "NSAP");
            s_typeMap.put(4, "HDLC (8-bit multidrop)");
            s_typeMap.put(5, "BBN 1822");
            s_typeMap.put(6, "802 (includes all 802 media plus Ethernet \"canonical format\")");
            s_typeMap.put(7, "E.163");
            s_typeMap.put(8, "E.164 (SMDS, Frame Relay, ATM)");
            s_typeMap.put(9, "F.69 (Telex)");
            s_typeMap.put(10, "X.121 (X.25, Frame Relay)");
            s_typeMap.put(11, "IPX");
            s_typeMap.put(12, "Appletalk");
            s_typeMap.put(13, "Decnet IV");
            s_typeMap.put(14, "Banyan Vines");
            s_typeMap.put(15, "E.164 with NSAP format subaddress");
            s_typeMap.put(16, "DNS (Domain Name System)");
            s_typeMap.put(17, "Distinguished Name");
            s_typeMap.put(18, "AS Number");
            s_typeMap.put(19, "XTP over IP version 4");
            s_typeMap.put(20, "XTP over IP version 6");
            s_typeMap.put(21, "XTP native mode XTP");
            s_typeMap.put(22, "Fibre Channel World-Wide Port Name");
            s_typeMap.put(23, "Fibre Channel World-Wide Node Name");
            s_typeMap.put(24, "GWID");
            s_typeMap.put(25, "AFI for L2VPN information");
            s_typeMap.put(26, "MPLS-TP Section Endpoint Identifier");
            s_typeMap.put(27, "MPLS-TP LSP Endpoint Identifier");
            s_typeMap.put(28, "MPLS-TP Pseudowire Endpoint Identifier");
            s_typeMap.put(29, "MT IP: Multi-Topology IP version 4");
            s_typeMap.put(30, "MT IPv6: Multi-Topology IP version 6");
            s_typeMap.put(31, "Unassigned");
        }
        
        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                return s_typeMap.get(code);
            return null;
        }

        public static IanaAddressFamilyNumber get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException(
                                                   "Cannot create IanaAddressFamilyNumber from null code");
            switch (code) {
            case 0:
               return IANA_ADDRESS_FAMILY_Reserved;
            case 1:
                return IANA_ADDRESS_FAMILY_IP;
            case 2:
                return IANA_ADDRESS_FAMILY_IP6;
            case 3:
                return IANA_ADDRESS_FAMILY_NSAP;
            case 4:
                return IANA_ADDRESS_FAMILY_HDLC;
            case 5:
                return IANA_ADDRESS_FAMILY_BBN;
            case 6:
                return IANA_ADDRESS_FAMILY_802;
            case 7:
                return IANA_ADDRESS_FAMILY_E_163;
            case 8:
                return IANA_ADDRESS_FAMILY_E_164;
            case 9:
                return IANA_ADDRESS_FAMILY_F_69;
            case 10:
                return IANA_ADDRESS_FAMILY_X_121;
            case 11:
                return IANA_ADDRESS_FAMILY_IPX;
            case 12:
                return IANA_ADDRESS_FAMILY_Appletalk;
            case 13:
                return IANA_ADDRESS_FAMILY_Decnet;
            case 14:
                return IANA_ADDRESS_FAMILY_Banyan;
            case 15:
                return IANA_ADDRESS_FAMILY_E_164_NSAP;
            case 16:
                return IANA_ADDRESS_FAMILY_DNS;
            case 17:
                return IANA_ADDRESS_FAMILY_Distinguished;
            case 18:
                return IANA_ADDRESS_FAMILY_AS;
            case 19:
                return IANA_ADDRESS_FAMILY_XTP_IP;
            case 20:
                return IANA_ADDRESS_FAMILY_XTP_IPv6;
            case 21:
                return IANA_ADDRESS_FAMILY_XTP;
            case 22:
                return IANA_ADDRESS_FAMILY_FibreChannel_World_Wide_Port_Name;
            case 23:
                return IANA_ADDRESS_FAMILY_FibreChannel_World_Wide_Node_Name;
            case 24:
                return IANA_ADDRESS_FAMILY_GWID;
            case 25:
                return IANA_ADDRESS_FAMILY_AFI;
            case 26:
                return IANA_ADDRESS_FAMILY_MPLS_TP_Section_Endpoint_Identifier;
            case 27:
                return IANA_ADDRESS_FAMILY_MPLS_TP_LSP_Endpoint_Identifier;
            case 28:
                return IANA_ADDRESS_FAMILY_MPLS_Pseudowire_Section_Endpoint_Identifier;
            case 29:
                return IANA_ADDRESS_FAMILY_MT_IP;
            case 30:
                return IANA_ADDRESS_FAMILY_MT_IPv6;
            case 31:
                return IANA_ADDRESS_FAMILY_Unassigned;
            default:
                throw new IllegalArgumentException(
                                                   "Cannot create IanaAddressFamilyNumber from code "
                                                           + code);
            }
    
        }
        

        
     }
    
   /*
    * 
    * If the associated LldpChassisIdSubtype object has a value of
    * 'networkAddress(5)', then this string identifies a particular
    * network address, encoded in network byte order, associated
    * with one or more ports on the containing chassis. The first
    * octet contains the IANA Address Family Numbers enumeration
    * value for the specific address type, and octets 2 through
    * N contain the network address value in network byte order.   
    * 
    */

    public static String decodeNetworkAddress(String networkAddress) {
        IanaAddressFamilyNumber type = IanaAddressFamilyNumber.get(IanaFamilyAddressStringToType(networkAddress));
        switch (type) {
            case IANA_ADDRESS_FAMILY_IP:
            case IANA_ADDRESS_FAMILY_IP6:
                return (new IPAddress(IanaFamilyAddressStringToBytes(networkAddress))).toDbString();
            default:
                return IanaAddressFamilyNumber.getTypeString(type.getValue()) + ": " + "type=" +type.getValue() + " address=" + networkAddress.substring(networkAddress.indexOf(":"));
        }
    }

    public static Integer IanaFamilyAddressStringToType(String networkAddress) {
        if (networkAddress == null) {
            throw new IllegalArgumentException("Cannot decode null IANA Family address");
        }
        return Integer.decode("0x" + networkAddress.split(":")[0]);
    }
    
    public static byte[] IanaFamilyAddressStringToBytes(String networkAddress) {
        if (networkAddress == null) {
            throw new IllegalArgumentException("Cannot decode null IANA Family address");
        }
        String[] digits = networkAddress.split(":");

        byte[] contents = new byte[digits.length-1];
        // Decode each MAC address digit into a hexadecimal byte value
        for (int i = 1; i < digits.length; i++) {
            // Prefix the value with "0x" so that Integer.decode() knows which base to use
            contents[i-1] = Integer.decode("0x" + digits[i]).byteValue();
        }
        return contents;
    }

}
