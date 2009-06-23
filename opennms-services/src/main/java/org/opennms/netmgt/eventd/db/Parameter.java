//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd.db;

import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * This is an utility class used to format the event parameters - to be inserted
 * into the 'events' table
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 */
public final class Parameter {
    /**
     * Format the list of event parameters
     * 
     * @param parms
     *            the list
     * 
     * @return the formatted event parameters string
     */
    public static String format(Parms parms) {
        boolean first = true;
        StringBuffer parmbuf = new StringBuffer();

        for (Parm parm : parms.getParmCollection()) {
            if (!first)
                parmbuf.append(Constants.MULTIPLE_VAL_DELIM);
            parmbuf.append(format(parm));
            first = false;
        }

        return parmbuf.toString();
    }

    /**
     * Format each parameter
     * 
     * @param parm
     *            the parameter
     * 
     * @return the formatted event parameter string
     */
    public static String format(Parm parm) {
        Value pValue = parm.getValue();

        String type = pValue.getType();
        String encoding = pValue.getEncoding();

        String tmp = Constants.escape(parm.getParmName(), Constants.NAME_VAL_DELIM);
        String name = Constants.escape(tmp, Constants.MULTIPLE_VAL_DELIM);
        tmp = Constants.escape(pValue.getContent(), Constants.NAME_VAL_DELIM);
        String value = Constants.escape(tmp, Constants.MULTIPLE_VAL_DELIM);

        String empty = "";
        name = (name != null ? name.trim() : empty);
        value = (value != null ? value.trim() : empty);
        type = (type != null ? type.trim() : empty);
        encoding = (encoding != null ? encoding.trim() : empty);

        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append(Constants.NAME_VAL_DELIM);
        buf.append(value);
        buf.append('(');
        buf.append(type);
        buf.append(Constants.DB_ATTRIB_DELIM);
        buf.append(encoding);
        buf.append(')');

        return buf.toString();
        // return name + Constants.NAME_VAL_DELIM + value + "(" + type +
        // Constants.DB_ATTRIB_DELIM + encoding + ")";
    }
    
    public static Parms decode(String eventparms) {
        if (eventparms == null ) return null;
        Parms parms = new Parms();
  
        String[] paramslistString = eventparms.split(Character.toString(Constants.MULTIPLE_VAL_DELIM));
        if (paramslistString != null) {
                for (int i =0; i< paramslistString.length;i++) {
                    String[] paramEncoded = paramslistString[i].split(Character.toString(Constants.NAME_VAL_DELIM));
                    if (paramEncoded != null && paramEncoded.length == 2) {
                                Parm parm = new Parm();
                                parm.setParmName(paramEncoded[0]);
                                Value value = new Value();
                                String[] valueEncoded = paramEncoded[1].split("\\(");
                                boolean isParmCorrect = false;
                                if (valueEncoded != null && valueEncoded.length == 2) {
                                        value.setContent(valueEncoded[0]);
                                        String[] typeAndEncode = valueEncoded[1].split(Character.toString(Constants.DB_ATTRIB_DELIM));
                                        if (typeAndEncode != null && typeAndEncode.length == 2) {
                                                value.setType(typeAndEncode[0]);
                                                value.setEncoding(typeAndEncode[1].split("\\)")[0]);
                                                isParmCorrect = true;
                                        }
                                }
                                if (isParmCorrect) {
                                        parm.setValue(value);
                                parms.addParm(parm);
                                }
                    }
                }
        }
        return parms;

    }

}
