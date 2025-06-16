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
package org.opennms.netmgt.scriptd.helper;

import java.util.regex.Pattern;

import org.opennms.netmgt.xml.event.Event;

public class UeiEventMatch implements EventMatch {

	private String ueimatch;
	
	public UeiEventMatch() {
		super();
		this.ueimatch = null;
	}
	
	
	public UeiEventMatch(String ueimatch) {
		super();
		this.ueimatch = ueimatch;
	}

	
        @Override
	public boolean match(Event event) {
		if (event == null) return false;
		if (event.getUei() == null ) return false;
		if (this.ueimatch == null) return false;
		if (this.ueimatch.startsWith("~"))
			return rematch(event.getUei(), this.ueimatch.substring(1));
		else
			return (event.getUei().equals(this.ueimatch));
		
	}

	private boolean rematch(String text, String regex) {
		Pattern p = Pattern.compile(regex);
		return p.matcher(text).matches();
	}
}
