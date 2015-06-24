/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.ssh.internal.gwt.client.ui;

/**
 * The KeyBuffer class is a two-way linked list that keeps track
 * of keys entered by the user in order of when they typed them.
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class KeyBuffer {

	private Key head; //Key at the beginning of the list
	private Key tail; //Key at the end of the list
	private int size; //Size of the list

	/**
	 * The KeyBuffer() constructor creates a linked list and initalizes the head
	 * and tail to null, and the size to 0
	 */
	public KeyBuffer () {
		this.head = null;
		this.tail = null;
		this.size = 0;
	}

	/**
	 * The add method takes a passed in String and creates a Key object. The new Key
	 * is then added to the tail end of the list. If the list is empty, the new Key is set as
	 * the head of the list.
	 * @param s Value of the Key
	 */
	public void add(String s){
		Key newKey = new Key(s);
		if (head == null){
			head = newKey;
			tail = newKey;
		} else {
			tail.setNext(newKey);
			newKey.setPrev(tail);
			tail = newKey;
		}
		size++;
	}

	/**
	 * The size method returns the current number of Keys in the list
	 * @return Number of Keys in List
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * The toString method appends all of the Keys into one String
	 */
	@Override
	public String toString() {
		String s = "";
		Key current = head;
		while (current != null) {
			s += current.getValue();
			current = current.getNext(); 
		}
		return s;
	}
	
	/**
	 * The drain method returns the toString representation of the List
	 * and then empties the list of all keys
	 * @return toString represention of the List
	 */
	public String drain() {
		String keybufContents = toString();
		free();
		return keybufContents;
	}
	
	/**
	 * The free method releases all objects and resources used in
	 * the List so that any unused memory can be garbage collected.
	 */
	public void free() {
		Key current = head;
		Key next = null;
		while (current != null) {
			if (current.getNext() != null) {
				next = current.getNext();
			}
			current.setNext(null);
			current.setPrev(null);
			current.setValue(null);
			current = next;
			next = null;
		}
		head = tail = null;
		size = 0;
	}

}
