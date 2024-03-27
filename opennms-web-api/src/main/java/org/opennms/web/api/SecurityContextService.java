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
package org.opennms.web.api;

public interface SecurityContextService {

	/**
	 * Get the user name about the currently logged in user
	 * 
	 * @return user name from security context otherwise null
	 */
	public String getUsername();

	/**
	 * Get the user password about the currently logged in user
	 * 
	 * @return user password from security context otherwise null
	 */
	public String getPassword();

	/**
	 * Check if the currently logged in user has the required role.
	 * 
	 * @param role
	 *            - required role
	 * @return true if role is assigned, otherwise false
	 */
	public boolean hasRole(String role);

	/**
	 * Check if the currently logged in user is authenticated.
	 * 
	 * @return true is authenticated, otherwise false
	 */
	public boolean isAuthenticated();
}
