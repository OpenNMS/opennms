/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
