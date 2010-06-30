//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.util.StringTokenizer;

/**
 * <p>SnmpInstId class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpInstId extends SnmpObjId {
    
    /** Constant <code>INST_ZERO</code> */
    public static final SnmpInstId INST_ZERO = new SnmpInstId(0);

    /**
     * <p>Constructor for SnmpInstId.</p>
     *
     * @param instanceIds an array of int.
     */
    public SnmpInstId(int[] instanceIds) {
        super(instanceIds);
    }

    /**
     * <p>Constructor for SnmpInstId.</p>
     *
     * @param instance a {@link java.lang.String} object.
     */
    public SnmpInstId(String instance) {
        super(instance);
    }
    
    /**
     * <p>Constructor for SnmpInstId.</p>
     *
     * @param instance a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpInstId(SnmpObjId instance) {
        super(instance);
    }

    /**
     * <p>Constructor for SnmpInstId.</p>
     *
     * @param instance a int.
     */
    public SnmpInstId(int instance) {
        super(new int[] { instance }, false);
    }

    /**
     * <p>addPrefixDotInToString</p>
     *
     * @return a boolean.
     */
    protected boolean addPrefixDotInToString() {
        return false;
    }

    /**
     * <p>convertToSnmpInstIds</p>
     *
     * @param instances a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpInstId} objects.
     */
    public static SnmpInstId[] convertToSnmpInstIds(String instances) {
        StringTokenizer tokenizer = new StringTokenizer(instances, ",");
        SnmpInstId[] insts = new SnmpInstId[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            SnmpInstId inst = new SnmpInstId(token);
            insts[index] = inst;
            index++;
        }
        return insts;
    }

    /**
     * <p>toInt</p>
     *
     * @return a int.
     */
    public int toInt() {
        if (this.length() != 1)
            throw new IllegalArgumentException("Cannot convert "+this+" to an int");
        
        return getLastSubId();
    }
    
    
}
