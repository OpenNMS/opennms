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
package org.opennms.container.web.bridge.api;

import java.util.List;

/**
 * The Rest Endpoints are provided by the jax-rs-connector, which already provides access to those.
 * As those need to be made available to the Jetty Classpath, it would either mean copy over
 * some jax-rs-connector jars to the ${OPENNMS_HOME}/lib directory and hack the custom.properties, or provide a wrapper implementation.
 * As the last was easier, this is what was used.
 *
 * @author mvrueden
 */
public interface RestEndpointRegistry {
    List<String> getRestEndpoints();
}
