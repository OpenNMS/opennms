/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.StringTokenizer;

public class SnmpInstId extends SnmpObjId {
    
    public static final SnmpInstId INST_ZERO = new SnmpInstId(0);

    public SnmpInstId(int[] instanceIds) {
        super(instanceIds);
    }

    public SnmpInstId(String instance) {
        super(instance);
    }
    
    public SnmpInstId(SnmpObjId instance) {
        super(instance);
    }

    public SnmpInstId(int instance) {
        super(new int[] { instance }, false);
    }
    
    @Override
    protected boolean addPrefixDotInToString() {
        return false;
    }

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

    public int toInt() {
        if (this.length() != 1)
            throw new IllegalArgumentException("Cannot convert "+this+" to an int");
        
        return getLastSubId();
    }
    
    
}
