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

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV2TrapEventForwarder(String ip, int port, String community, SnmpTrapHelper snmpTrapHelper) {
		super(ip, port, community, snmpTrapHelper);
	}

        @Override
	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
		try {
			sendV2EventTrap(event);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		}
		
	}

        @Override
	public void flushSyncEvent(Event event) {
		flushEvent(event);
	}

        @Override
	public void sendStartSync() {
		throw new UnsupportedOperationException();
	}

        @Override
	public void sendEndSync() {
		throw new UnsupportedOperationException();
	}

}
