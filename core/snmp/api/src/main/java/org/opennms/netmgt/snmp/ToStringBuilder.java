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
package org.opennms.netmgt.snmp;


class ToStringBuilder {
	
	final StringBuilder buf;
	boolean first = true;
	boolean finished = false;

	public ToStringBuilder(Object o) {
		buf = new StringBuilder(512);
		
		buf.append(o.getClass().getSimpleName());
		buf.append('@');
		buf.append(String.format("%x", System.identityHashCode(o)));
		buf.append('[');
		
	}
	
	public ToStringBuilder append(String label, String value) {
		assertNotFinished();
		if (!first) {
			buf.append(", ");
		} else {
			first = false;
		}
		buf.append(label).append('=').append(value);
		return this;
	}
	
	public ToStringBuilder append(String label, Object value) {
		return append(label, value == null ? null : value.toString());
	}
	
        @Override
	public String toString() {
		if (!finished) {
			buf.append(']');
			finished = true;
		}
		
		return buf.toString();
	}
	
	private void assertNotFinished() {
		if (finished) {
			throw new IllegalStateException("This builder has already been completed by calling toString");
		}
	}
	
}
