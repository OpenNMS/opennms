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
package org.opennms.netmgt.vmmgr;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;

public class TestDaemon extends AbstractServiceDaemon {

	public TestDaemon() {
		super("test-daemon");
		System.err.println("Creating: "+getName());
	}

        @Override
	protected void onPause() {
		System.err.println("Pausing: "+getName());
	}

        @Override
	public String status() {
		String status = super.getStatusText();
		System.err.println("Status: "+getName()+" = "+status);
		return status;
	}

        @Override
	protected void onResume() {
		System.err.println("Resuming: "+getName());
	}

        @Override
	protected void onStart() {
		System.err.println("Starting: "+getName());
	}

        @Override
	protected void onStop() {
		System.err.println("Stopping: "+getName());
	}

        @Override
    protected void onInit() {
    }

}
