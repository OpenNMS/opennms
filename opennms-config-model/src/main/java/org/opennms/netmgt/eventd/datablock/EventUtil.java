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

package org.opennms.netmgt.eventd.datablock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.opennms.core.utils.Base64;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Value;

/**
 * EventUtil is used primarily for the event parm expansion - has methods used
 * by all the event components to send in the event and the element to expanded
 * and have the 'expanded' value sent back
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="mailto:weave@oculan.com">Brain Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class EventUtil {
    /**
     * Converts the value of a parm ('Value') of the instance to a string
     *
     * @param pvalue a {@link org.opennms.netmgt.xml.event.Value} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getValueAsString(Value pvalue) {
        if (pvalue == null)
            return null;

        if (pvalue.getContent() == null)
            return null;

        String result = "";
        String encoding = pvalue.getEncoding();
        if (encoding.equals(EventConstants.XML_ENCODING_TEXT)) {
            result = pvalue.getContent();
        } else if (encoding.equals(EventConstants.XML_ENCODING_BASE64)) {
            byte[] bytes = Base64.decodeBase64(pvalue.getContent().toCharArray());
            result = "0x"+toHexString(bytes);
        } else if (encoding.equals(EventConstants.XML_ENCODING_MAC_ADDRESS)) {
            result = pvalue.getContent();
        } else {
            throw new IllegalStateException("Unknown encoding for parm value: " + encoding);
        }

        return result.trim();
    }

    public static String toHexString(byte[] data) {
        final StringBuffer b = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            final int x = (int) data[i] & 0xff;
            if (x < 16) b.append("0");
            b.append(Integer.toString(x, 16).toLowerCase());
        }
        return b.toString();
    }

    /**
     * <p>cloneEvent</p>
     *
     * @param orig a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event cloneEvent(Event orig) {
        Event copy = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            copy = (Event)in.readObject();
        }
        catch(IOException e) {
            ThreadCategory.getInstance(EventUtil.class).error("Exception cloning event", e);
        }
        catch(ClassNotFoundException cnfe) {
            ThreadCategory.getInstance(EventUtil.class).error("Exception cloning event", cnfe);
        }
        return copy;
    }

    /**
     * Expand the value if it has parms in one of the following formats -
     * %element% values are expanded to have the value of the element where
     * 'element' is an element in the event DTD - %parm[values-all]% is expanded
     * to a delimited list of all parmblock values - %parm[names-all]% is
     * expanded to a list of all parm names - %parm[all]% is expanded to a full
     * dump of all parmblocks - %parm[name]% is expanded to the value of the
     * parameter named 'name' - %parm[ <name>]% is replaced by the value of the
     * parameter named 'name', if present - %parm[# <num>]% is replaced by the
     * value of the parameter number 'num', if present - %parm[##]% is replaced
     * by the number of parameters
     *
     * @param inp
     *            the input string in which parm values are to be expanded
     * @param decode
     *            the varbind decode for this
     * @return expanded value if the value had any parameter to expand, null
     *         otherwise
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static String expandParms(String inp, Event event, Map<String, Map<String, String>> decode) {
        int index1 = -1;
        int index2 = -1;
    
        if (inp == null) {
            return null;
        }
    
        StringBuffer ret = new StringBuffer();
    
        String tempInp = inp;
        int inpLen = inp.length();
    
        // check input string to see if it has any %xxx% substring
        while ((tempInp != null) && ((index1 = tempInp.indexOf(EventUtil.PERCENT)) != -1)) {
            // copy till first %
            ret.append(tempInp.substring(0, index1));
            tempInp = tempInp.substring(index1);
    
            index2 = tempInp.indexOf(EventUtil.PERCENT, 1);
            if (index2 != -1) {
                // Get the value between the %s
                String parm = tempInp.substring(1, index2);
                // m_logger.debug("parm: " + parm + " found in value");
    
                // If there's any whitespace in between the % signs, then do not try to 
                // expand it with a parameter value
                if (parm.matches(".*\\s.*")) {
                    ret.append(EventUtil.PERCENT);
                    tempInp = tempInp.substring(1);
                    continue;
                }
    
                String parmVal = EventUtil.getValueOfParm(parm, event);
                // m_logger.debug("value of parm: " + parmVal);
    
                if (parmVal != null) {
                    if (decode != null && decode.containsKey(parm) && decode.get(parm).containsKey(parmVal)) {
                        ret.append(decode.get(parm).get(parmVal));
                        ret.append("(");
                        ret.append(parmVal);
                        ret.append(")");
                    } else {
                        ret.append(parmVal);
                    }
                }
    
                if (index2 < (inpLen - 1)) {
                    tempInp = tempInp.substring(index2 + 1);
                } else {
                    tempInp = null;
                }
            }
            else {
                break;
            }
        }
    
        if ((index1 == -1 || index2 == -1) && (tempInp != null)) {
            ret.append(tempInp);
        }
    
        String retStr = ret.toString();
        if (retStr != null && !retStr.equals(inp)) {
            return retStr;
        } else {
            return null;
        }
    }

    /**
     * The '%' sign used to indicate parms to be expanded
     */
    public final static char PERCENT = '%';   
}
