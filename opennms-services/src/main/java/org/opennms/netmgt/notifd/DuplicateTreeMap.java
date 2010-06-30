//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.notifd;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>DuplicateTreeMap class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DuplicateTreeMap<K, V> extends TreeMap<K, List<V>> {
    /**
     * 
     */
    private static final long serialVersionUID = 8020472612288161254L;

    /**
     * <p>putItem</p>
     *
     * @param key a K object.
     * @param value a V object.
     * @param <K> a K object.
     * @param <V> a V object.
     * @return a V object.
     */
    public V putItem(K key, V value) {
        List<V> l;
        if (super.containsKey(key)) {
            l = super.get(key);
        } else {
            l = new LinkedList<V>();
            put(key, l);
        }
        
        if (l.contains(value)) {
            return value;
        } else {
            l.add(value);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        for (List<V> list : values()) {
            for (V item : list) {
                buffer.append(item.toString() + System.getProperty("line.separator"));
            }
        }

        return buffer.toString();
    }
}

