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
package org.opennms.netmgt.snmp;

import java.util.StringTokenizer;

/**
 * <p>SnmpObjId class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpObjId implements Comparable {
    
    /* FIXME: Change the implementation of this to cache oids and share common prefixes
     * This should enhance the amount of garbage we generate a great deal at least for
     * this class.
     */

    private int[] m_ids;
    
    /**
     * These constructors are private.  The get method should be called to create a new oid
     *
     * @param ids an array of int.
     * @param clone a boolean.
     */
    protected SnmpObjId(int[] ids, boolean clone) {
        m_ids = (clone ? cloneIds(ids) : ids);
    }
    
    /**
     * These constructors are private.  The get method should be called to create a new oid
     *
     * @param ids an array of int.
     */
    protected SnmpObjId(int[] ids) {
        this(ids, true);
    }

    /**
     * These constructors are private.  The get method should be called to create a new oid
     *
     * @param oid a {@link java.lang.String} object.
     */
    protected SnmpObjId(String oid) {
        this(convertStringToInts(oid), false);
    }
    
    /**
     * These constructors are private.  The get method should be called to create a new oid
     *
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    protected SnmpObjId(SnmpObjId oid) {
        this(oid.m_ids);
    }
    
    /**
     * These constructors are private.  The get method should be called to create a new oid
     */ 
    private SnmpObjId(String objId, String instance) {
        this(appendArrays(convertStringToInts(objId), convertStringToInts(instance)), false);
    }
    
    /**
     * These constructors are private.  The get method should be called to create a new oid
     */ 
    private SnmpObjId(SnmpObjId objId, String instance) {
        this(appendArrays(objId.m_ids, convertStringToInts(instance)), false);
    }
    
    /**
     * These constructors are private.  The get method should be called to create a new oid
     */ 
    private SnmpObjId(SnmpObjId objId, SnmpObjId instance) {
        this(appendArrays(objId.m_ids, instance.m_ids), false);
    }

    /**
     * <p>getIds</p>
     *
     * @return an array of int.
     */
    public int[] getIds() {
        return cloneIds(m_ids);
    }
    
    private static int[] cloneIds(int[] ids) {
        return cloneIds(ids, ids.length);
    }
    
    private static int[] cloneIds(int[] ids, int lengthToClone) {
        int len = Math.min(lengthToClone, ids.length);
        int[] newIds = new int[len];
        System.arraycopy(ids, 0, newIds, 0, len);
        return newIds;
    }
    
    private static int[] convertStringToInts(String oid) {
    	    oid = oid.trim();
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
    
    

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof SnmpObjId)
            return compareTo(obj) == 0;
        else
            return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return 0;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(length()*2+10); // a guess at the str len
        for(int i = 0; i < length(); i++) {
            if (i != 0 || addPrefixDotInToString()) {
                buf.append('.');  
            }
            buf.append(m_ids[i]);
        }
        return buf.toString();
    }

    /**
     * <p>addPrefixDotInToString</p>
     *
     * @return a boolean.
     */
    protected boolean addPrefixDotInToString() {
        return true;
    }

    /** {@inheritDoc} */
    public int compareTo(Object o) {
        if (o == null) throw new NullPointerException("o is null");
        SnmpObjId other = (SnmpObjId)o;

        // compare each element in order for as much length as they have in common
        // which is the entire length of one or both oids
        int minLen = Math.min(length(), other.length());
        for(int i = 0; i < minLen; i++) {
            int diff = m_ids[i] - other.m_ids[i];
            // the first one that is not equal indicates which is bigger
            if (diff != 0)
                return diff;
        }
        
        // if they get to hear then both are identifical for their common length
        // so which ever is longer is then greater
        return length() - other.length();
    }

    /**
     * <p>append</p>
     *
     * @param inst a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId append(String inst) {
        return append(convertStringToInts(inst));
    }
    
    /**
     * <p>append</p>
     *
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId append(SnmpObjId inst) {
        return append(inst.m_ids);
    }

    /**
     * <p>append</p>
     *
     * @param instIds an array of int.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId append(int[] instIds) {
        int[] ids = appendArrays(m_ids, instIds);
        return new SnmpObjId(ids, false);
    }

    private static int[] appendArrays(int[] objIds, int[] instIds) {
        int[] ids = new int[objIds.length+instIds.length];
        System.arraycopy(objIds, 0, ids, 0, objIds.length);
        System.arraycopy(instIds, 0, ids, objIds.length, instIds.length);
        return ids;
    }

    /**
     * <p>get</p>
     *
     * @param oid a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public static SnmpObjId get(String oid) {
        return new SnmpObjId(oid);
    }

    /**
     * <p>get</p>
     *
     * @param ids an array of int.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public static SnmpObjId get(int[] ids) {
        return new SnmpObjId(ids);
    }

    /**
     * <p>get</p>
     *
     * @param oid a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public static SnmpObjId get(SnmpObjId oid) {
        return new SnmpObjId(oid);
    }

    /**
     * <p>get</p>
     *
     * @param objId a {@link java.lang.String} object.
     * @param instance a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public static SnmpObjId get(String objId, String instance) {
        return new SnmpObjId(objId, instance);
    }

    /**
     * <p>get</p>
     *
     * @param objId a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param instance a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public static SnmpObjId get(SnmpObjId objId, String instance) {
        return new SnmpObjId(objId, instance);
    }

    /**
     * <p>get</p>
     *
     * @param objId a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param instance a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public static SnmpObjId get(SnmpObjId objId, SnmpObjId instance) {
        return new SnmpObjId(objId, instance);
    }

    /**
     * <p>isPrefixOf</p>
     *
     * @param other a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a boolean.
     */
    public boolean isPrefixOf(SnmpObjId other) {
        if (length() > other.length())
            return false;
        
        for(int i = 0; i < m_ids.length; i++) {
            if (m_ids[i] != other.m_ids[i])
                return false;
        }
        
        return true;
    }

    /**
     * <p>getInstance</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
    public SnmpInstId getInstance(SnmpObjId base) {
        if (!base.isPrefixOf(this)) return null;
        
        int[] instanceIds = new int[length() - base.length()];
        System.arraycopy(m_ids, base.length(), instanceIds, 0, instanceIds.length);
        return new SnmpInstId(instanceIds);
    }

    /**
     * <p>length</p>
     *
     * @return a int.
     */
    public int length() {
        return m_ids.length;
    }
    
    /**
     * <p>getSubIdAt</p>
     *
     * @param index a int.
     * @return a int.
     */
    public int getSubIdAt(int index) {
        return m_ids[index];
    }
    
    /**
     * <p>getLastSubId</p>
     *
     * @return a int.
     */
    public int getLastSubId() {
        return getSubIdAt(length()-1);
    }

    /**
     * <p>decrement</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId decrement() {
        if (getLastSubId() == 0) {
            return new SnmpObjId(cloneIds(m_ids, length() - 1), false);
        }
        else {
            int[] newIds = cloneIds(m_ids, length());
            newIds[newIds.length-1] -= 1;
            return new SnmpObjId(newIds, false);
        }
    }


}
