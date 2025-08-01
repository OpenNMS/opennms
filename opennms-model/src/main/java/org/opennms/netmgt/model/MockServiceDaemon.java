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
package org.opennms.netmgt.model;

public class MockServiceDaemon implements MockServiceDaemonMBean {
	
	private boolean startCalled = false;
	private String statusStr = "UNDEFINED";
	private String name;

	public MockServiceDaemon(String name) {
		this.name = name;
	}
        @Override
	public String getStatusText() {
		// TODO Auto-generated method stub
		return statusStr;
	}

        @Override
	public void pause() {
		// TODO Auto-generated method stub

	}

        @Override
	public void resume() {
		// TODO Auto-generated method stub

	}

        @Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

        @Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

        @Override
	public void start() {
		// TODO Auto-generated method stub
		startCalled = true;
		statusStr = "Started";
	}

	public boolean getStartCalled() {
		return startCalled;
	}
	
        @Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}