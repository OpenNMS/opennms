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
package org.opennms.features.scv.api;

/**
 * Callback invoked after credentials in the {@link SecureCredentialsVault} were
 * added, updated or deleted, so that interested parties can discard cached values
 * interpolated from ${scv:...} expressions.
 *
 * Implementations are registered as OSGi services on containers that can act on
 * the change (e.g. OpenNMS core forwards a nodeMetadataUpdated event); on other
 * containers no service is registered and callers simply skip the notification.
 */
public interface CredentialsChangedListener {

    /**
     * @param source a short identifier of what changed the credentials, e.g. the
     *               shell command or REST service name; used as the event source
     */
    void credentialsChanged(String source);
}
