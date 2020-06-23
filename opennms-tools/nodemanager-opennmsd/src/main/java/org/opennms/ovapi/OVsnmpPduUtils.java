/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.ovapi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.nnm.swig.NNM;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpVarBind;


public abstract class OVsnmpPduUtils {
    
    private static class StringCreator {
        
        private StringBuffer buf = new StringBuffer();
        private boolean m_first = true;

        public StringCreator(Object o) {
            buf.append(o.getClass().getName());
        }

        public void append(String name, Object val) {
            appendFieldPrefix(name);
            buf.append(val);
        }
        
        public void append(String name, int val) {
            appendFieldPrefix(name);
            buf.append(val);
        }

        private void appendFieldPrefix(String name) {
            if (!m_first) {
                buf.append(", ");
            } else {
                m_first = false;
            }
            buf.append(name);
            buf.append(" = ");
        }
        
        public String toString() {
            return buf.toString();
        }
        
        
        
        
        
    }
    
    public static String toString(OVsnmpPdu pdu) {
        StringCreator buf = new StringCreator(pdu);
        buf.append("address", pdu.getIpAddress());
        buf.append("command", pdu.getCommand());
        buf.append("agentAddr", pdu.getAgentAddress());
        buf.append("enterprise", pdu.getEnterpriseObjectId());
        buf.append("generic", pdu.getGenericType());
        buf.append("specific", pdu.getSpecificType());
        buf.append("time", pdu.getTime()+" cs");
        
        int count = 1;
        OVsnmpVarBind varbind = pdu.getVarBinds();
        while(varbind != null) {
            buf.append(count+": oid", varbind.getObjectId());
            buf.append(count+": type", varbind.getType());
            buf.append(count+": val", getVarbindValue(varbind));
            varbind = varbind.getNextVarBind();
            count++;
        }
        
        return buf.toString();
    }
    
    public static String getVarbindValue(OVsnmpVarBind varBind) {
        int type = varBind.getType();
        if (type == NNM.ASN_BOOLEAN) {
            return (varBind.getValue().getIntValue() == 0 ? "false" : "true");
        } else if (type == NNM.ASN_INTEGER) {
            return Integer.toString(varBind.getValue().getIntValue());
        } else if (type == NNM.ASN_OCTET_STR) {
            byte[] bytes = new byte[varBind.getValLength()];
            varBind.getValue().getOctetString(bytes);
            return new String(bytes);
        } else if (type == NNM.ASN_U_INTEGER) {
            return ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_OBJECT_ID) {
            return varBind.getValue().getObjectId(varBind.getValLength());
        } else if (type == NNM.ASN_TIMETICKS) {
            int centis = varBind.getValue().getIntValue();
            return ""+centis/100+"."+centis%100+" s";
        } else if (type == NNM.ASN_COUNTER32) {
            return ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_COUNTER64) {
            return ""+varBind.getValue().getCounter64Value();
        } else if (type == NNM.ASN_GAUGE) {
            return ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_IPADDRESS) {
            byte[] bytes = new byte[4];
            varBind.getValue().getOctetString(bytes);
            try {
                return InetAddress.getByAddress(bytes).getHostAddress();
            } catch (UnknownHostException e) {
                return "UnknownHost that can't happen";
            }
        } else {
            return "UNKNOWN TYPE: "+type;
        }
    }


}
