//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 Blast Consulting Co.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Consulting Co.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.blast.com/
//
//
// Tab Size = 8
//
//
//
package org.opennms.core.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides set functionality for ints.
 */
public class IntSet {
    
    Set set = new HashSet();

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(int n) {
        return set.add(new Integer(n));
    }
    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(IntSet s) {
        return set.addAll(s.set);
    }
    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        set.clear();
    }
    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(int n) {
        return set.contains(new Integer(n));
    }
    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(IntSet s) {
        return set.containsAll(s.set);
    }
    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return set.isEmpty();
    }
    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        return set.iterator();
    }
    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(int n) {
        return set.remove(new Integer(n));
    }
    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(IntSet s) {
        return set.remove(s.set);
    }
    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(IntSet s) {
        return set.retainAll(s.set);
    }
    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        return set.size();
    }
    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public int[] toArray() {
        int[] array = new int[size()];
        
        int i = 0;
        for (Iterator it = set.iterator(); it.hasNext(); i++) {
            Integer element = (Integer) it.next();
            array[i] = element.intValue();
        }
        return array;
    }
}
