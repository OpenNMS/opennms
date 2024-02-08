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
package org.opennms.distributed.core.api;

/**
 * REST API client for OpenNMS.
 *
 * @author jwhite
 */
public interface RestClient {

    /**
     * Retrieves the OpenNMS version.
     *
     * @throws Exception on failure
     */
    public String getVersion() throws Exception;

    /**
     * Used to verify connectivity with the REST endpoint.
     *
     * @throws Exception on failure
     */
    public void ping() throws Exception;
    
    /**
     * Used to fetch snmpV3users from REST endpoint.
     * 
     *  @throws Exception on failure
     */
    public String getSnmpV3Users() throws Exception;

}
