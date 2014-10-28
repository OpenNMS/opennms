/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class FilteringIteratorTest extends TestCase {
    
    public List<Integer> list(Integer...integers) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        Collections.addAll(list, integers);
        return list;
    }

    public Iterator<Integer> createOddOnlyIterator(List<Integer> items) {
        return new FilteringIterator<Integer>(items.iterator()) {

            @Override
            protected boolean matches(Integer item) {
                return (item % 2) != 0;
            }
            
        };
    }
    
    public void testEmptyList() {

        Iterator<Integer> it = createOddOnlyIterator(list());

        assertFalse(it.hasNext());
    }
    
    public void testSingletonMatches() {

        Iterator<Integer> it = createOddOnlyIterator(list(1));

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
    }
    
    public void testSingleDoesntMatch() {

        Iterator<Integer> it = createOddOnlyIterator(list(2));

        assertFalse(it.hasNext());
    }
    
    public void testAllMatchButFirst() {

        Iterator<Integer> it = createOddOnlyIterator(list(2, 3, 5));

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertFalse(it.hasNext());
    }

    public void testNoneMatchButFirst() {

        Iterator<Integer> it = createOddOnlyIterator(list(1, 2, 4));

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertFalse(it.hasNext());
    }

    public void testNoneMatch() {

        Iterator<Integer> it = createOddOnlyIterator(list(6, 2, 4));

        assertFalse(it.hasNext());
    }

    public void testAllMatch() {

        Iterator<Integer> it = createOddOnlyIterator(list(1, 3, 5));

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertFalse(it.hasNext());
    }

    public void testMiddleMatches() {
        
        Iterator<Integer> it = createOddOnlyIterator(list(2, 3, 4));

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }
    
    public void testNextWithoutHasNextReturnsValue() {

        Iterator<Integer> it = createOddOnlyIterator(list(2, 3, 4));

        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
        
    }

    public void testNextWithoutHasNextNoValues() {

        Iterator<Integer> it = createOddOnlyIterator(list(2, 4, 6));
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected this
        }
    }
    
    public void testRemoveFirstElement() {
        
        List<Integer> items = list(1, 2, 4);
        
        Iterator<Integer> it = createOddOnlyIterator(items);

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        it.remove();

        assertEquals(list(2, 4), items);
    }

    public void testRemoveMiddleElement() {
        
        List<Integer> items = list(2, 3, 4);
        
        Iterator<Integer> it = createOddOnlyIterator(items);

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        it.remove();

        assertEquals(list(2, 4), items);
    }

    public void testRemoveLastElement() {
        
        List<Integer> items = list(2, 4, 5);
        
        Iterator<Integer> it = createOddOnlyIterator(items);

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        it.remove();

        assertEquals(list(2, 4), items);
    }

    

}
