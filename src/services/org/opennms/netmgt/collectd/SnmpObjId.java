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
package org.opennms.netmgt.collectd;

import java.util.StringTokenizer;

public class SnmpObjId implements Comparable {

    private int[] m_ids;
    
    private SnmpObjId() {
        m_ids = null;
    }

    public SnmpObjId(int[] ids) {
        m_ids = cloneIds(ids);
    }

    public SnmpObjId(String oid) {
        m_ids = convertStringToInts(oid);
    }
    
    public int[] getIds() {
        return cloneIds(m_ids);
    }

    private int[] cloneIds(int[] ids) {
        if (ids == null) return null;
        int[] newIds = new int[ids.length];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        return newIds;
    }

    private int[] convertStringToInts(String oid) {
        if (oid.startsWith(".")) {
            oid = oid.substring(1);
        }
        
        StringTokenizer tokenizer = new StringTokenizer(oid, ".");
        int[] ids = new int[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            try {
                String tok = tokenizer.nextToken();
                ids[index] = Integer.parseInt(tok);
                if (ids[index] < 0)
                    throw new IllegalArgumentException("String "+oid+" could not be converted to a SnmpObjId. It has a negative for subId "+index);
                index++;
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("String "+oid+" could not be converted to a SnmpObjId at subId "+index);
            }
        }
        return ids;
    }
    
    

    public boolean equals(Object obj) {
        if (obj instanceof SnmpObjId)
            return compareTo(obj) == 0;
        else
            return false;
    }

    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(m_ids.length*2+10); // a guess at the str len
        for(int i = 0; i < m_ids.length; i++) {
            buf.append('.');
            buf.append(m_ids[i]);
        }
        return buf.toString();
    }

    public int compareTo(Object o) {
        if (o == null) throw new NullPointerException("o is null");
        SnmpObjId other = (SnmpObjId)o;

        // compare each element in order for as much length as they have in common
        // which is the entire length of one or both oids
        int minLen = Math.min(m_ids.length, other.m_ids.length);
        for(int i = 0; i < minLen; i++) {
            int diff = m_ids[i] - other.m_ids[i];
            // the first one that is not equal indicates which is bigger
            if (diff != 0)
                return diff;
        }
        
        // if they get to hear then both are identifical for their common length
        // so which ever is longer is then greater
        return m_ids.length - other.m_ids.length;
    }

    public SnmpObjId append(String inst) {
        return append(convertStringToInts(inst));
    }

    public SnmpObjId append(int[] instIds) {
        int[] ids = new int[m_ids.length+instIds.length];
        System.arraycopy(m_ids, 0, ids, 0, m_ids.length);
        System.arraycopy(instIds, 0, ids, m_ids.length, instIds.length);
        SnmpObjId result = new SnmpObjId();
        result.m_ids = ids;
        return result;
    }

    public boolean isPrefixOf(SnmpObjId other) {
        if (m_ids.length > other.m_ids.length)
            return false;
        
        for(int i = 0; i < m_ids.length; i++) {
            if (m_ids[i] != other.m_ids[i])
                return false;
        }
        
        return true;
    }


}
