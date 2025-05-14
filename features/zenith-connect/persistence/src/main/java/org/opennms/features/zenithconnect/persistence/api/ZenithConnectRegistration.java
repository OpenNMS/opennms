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
package org.opennms.features.zenithconnect.persistence.api;

public class ZenithConnectRegistration {
    /** Registration id, a uuid */
    public String id;

    /** Creation time of this registration in ms UTC */
    public Long createTimeMs;

    /**
     * System ID of the NMS registering with Zenith.
     * For Meridian this is the 'monitoringsystems.id'.
     */
    public String systemId;

    /** Display name or friendly name of the NMS system. */
    public String displayName;

    /** Zenith host address, including protocol. E.g. 'https://zenith.mysystem.com' */
    public String zenithHost;

    /**
     * Relative URL of the Zenith Connect endpoint that processes registrations. E.g. '/zenith-connect'.
     * The combination of zenithHost + zenithRelativeUrl is the full URL to the Zenith Connect endpoint.
     */
    public String zenithRelativeUrl;

    /**
     * Keycloak access token received from Zenith after registration.
     */
    public String accessToken;

    /**
     * Keycloak refresh token received from Zenith after registration.
     */
    public String refreshToken;

    /** Denotes whether this system has been successfully registered with Zenith. */
    public Boolean registered;

    /** Denotes whether this system is actively sending data to Zenith. */
    public Boolean active;
}
