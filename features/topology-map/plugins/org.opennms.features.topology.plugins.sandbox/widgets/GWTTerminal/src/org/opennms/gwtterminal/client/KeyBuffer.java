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

package org.opennms.gwtterminal.client;

public class KeyBuffer {

	private Key head;
	private Key tail;
	private int size;

	public KeyBuffer () {
		this.head = null;
		this.tail = null;
		this.size = 0;
	}

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
	
	public String pop(){
		if (head != null){
			String headValue = head.getValue();
			if (head.getNext() != null){
				head = head.getNext();
			} else {
				head = null;
				tail = null;
			}
			size--;
			return headValue;
		} else return null;
	}

	public int size() {
		return this.size;
	}
	
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

}
