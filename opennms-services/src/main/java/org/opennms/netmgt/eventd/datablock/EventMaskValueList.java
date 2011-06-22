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

import java.util.ArrayList;
import java.util.Collection;

/**
 * The maskelement values in a 'EventKey' are stored in this ArrayList subclass
 * This list is pretty much constant once it constructed - so the hashcode is
 * evaluated once at construction and reused(if new values are added or values
 * changed, hashcode is re-evaluated)
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class EventMaskValueList extends ArrayList<String> {
    private static final long serialVersionUID = -7375744883429450497L;

    /**
     * The hash code calculated from the elements
     */
    private int m_hashCode;

    /**
     * Default constructor for this class
     */
    public EventMaskValueList() {
        super();
        m_hashCode = -1111;
    }

    /**
     * constructor for this class
     *
     * @param c a {@link java.util.Collection} object.
     */
    public EventMaskValueList(Collection<String> c) {
        super(c);
        evaluateHashCode();
    }

    /**
     * constructor for this class
     *
     * @param initCapacity a int.
     */
    public EventMaskValueList(int initCapacity) {
        super(initCapacity);
        m_hashCode = -1111;
    }

    /**
     * the constructor for this class
     *
     * @param value
     *            the string to be added to this list.
     */
    public EventMaskValueList(String value) {
        super();
        add(value);
        evaluateHashCode();
    }

    /*
     * Following methods are to ensure hashcode is not out of sync with elements
     */

    /**
     * Override to re-evaluate hashcode
     *
     * @see java.util.ArrayList#add(Object)
     * @param o a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean add(String o) {
        boolean ret = super.add(o);
        evaluateHashCode();
        return ret;
    }

    /**
     * Override to re-evaluate hashcode
     *
     * @see java.util.ArrayList#add(int, Object)
     * @param index a int.
     * @param o a {@link java.lang.String} object.
     */
    public void add(int index, String o) {
        super.add(index, o);
        evaluateHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.ArrayList#addAll(Collection)
     */
    public boolean addAll(Collection<? extends String> o) {
        boolean ret = super.addAll(o);
        evaluateHashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.ArrayList#addAll(int, Collection)
     */
    public boolean addAll(int index, Collection<? extends String> o) {
        boolean ret = super.addAll(index, o);
        evaluateHashCode();
        return ret;
    }

    /**
     * Override to re-evaluate hashcode
     *
     * @see java.util.ArrayList#clear()
     */
    public void clear() {
        super.clear();
        evaluateHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.ArrayList#remove(int)
     */
    public String remove(int index) {
        String obj = super.remove(index);
        evaluateHashCode();

        return obj;
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.ArrayList#removeRange(int,int)
     */
    protected void removeRange(int from, int to) {
        super.removeRange(from, to);
        evaluateHashCode();
    }

    /**
     * Override to re-evaluate hashcode
     *
     * @see java.util.ArrayList#remove(Object)
     * @param o a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean remove(String o) {
        boolean ret = super.remove(o);
        evaluateHashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.ArrayList#removeAll(Collection)
     */
    public boolean removeAll(Collection<?> o) {
        boolean ret = super.removeAll(o);
        evaluateHashCode();
        return ret;
    }

    /**
     * Override to re-evaluate hashcode
     *
     * @see java.util.ArrayList#set(int,Object)
     * @param index a int.
     * @param o a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String set(int index, String o) {
        String old = super.set(index, o);
        evaluateHashCode();
        return old;
    }

    /*
     * End methods to ensure hashcode is not out of sync with elements
     */

    /**
     * <p>
     * Handling the mask values ending with '%' is a pain since the hashcodes
     * will need to work in reverse!
     * 
     * <p>
     * For e.g. consider mask values '.1.3.6.1.4.1.9%' and '.1.3.6.1.4.1.%'
     * normal hashcodes will mean '.1.3.6.1.4.1.9%' will have a larger hashcode.
     * However when we try a match in the eventconf(which uses a treemap of
     * eventkeys to get the event keys in an ordered fashion), we want
     * '.1.3.6.1.4.1.9%' to be a better match than '.1.3.6.1.4.1.%' - i.e.we
     * want '.1.3.6.1.4.1.9%' to have a lesser hashcode than '.1.3.6.1.4.1.%' -
     * hence this method. Only a lot of testing will reveal conflicts etc.
     * though
     * </p>
     */
    private int evaluateHashCode(String value) {
        int h = 0;
        if (value == null) {
            return h;
        }

        int length = value.length();
        if (value.startsWith(".1.")) {
            // eid?
            StringBuffer newValue = new StringBuffer();
            for (int i = 0; i < length; i++) {
                char tc = value.charAt(i);
                if (tc != '.') {
                    newValue.append(tc);
                }
            }

            value = newValue.toString();
            length = value.length();
        }

        if (value.endsWith("%")) {
            char val[] = value.toCharArray();
            int len = value.length();
            int lastIndex = len - 1;

            for (int i = 0; i <= lastIndex; i++) {
                if (i != 0) {
                    h = 31 * h / i - val[i];
                } else {
                    h = 31 * h - val[i];
                }
            }
        } else {
            h = value.hashCode();
        }

        return h;
    }

    /**
     * Evaluate the hash code for this object
     */
    public void evaluateHashCode() {
        int hashCode = 1;
        for (String str : this) {
            m_hashCode = 31 * hashCode;
            if (str != null) {
                m_hashCode += evaluateHashCode(str);
            }
        }
    }

    /**
     * Overrides the 'hashCode()' method in the superclass.
     *
     * @return a hash code for this object
     */
    public int hashCode() {
        if (m_hashCode != -1111) {
            return m_hashCode;
        } else {
            evaluateHashCode();
            return m_hashCode;
        }
    }
}
