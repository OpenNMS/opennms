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

import org.opennms.core.utils.Base64;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
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
}
