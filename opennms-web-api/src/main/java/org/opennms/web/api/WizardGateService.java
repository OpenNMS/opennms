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

/**
 * Determines whether the Vue first-sign-in wizard needs to be shown.
 * Published as an OSGi service by the datachoices feature.
 */
public interface WizardGateService {

    /**
     * Returns true if any wizard step (Usage Statistics notice or Product Update Enrollment notice)
     * is still pending acknowledgment for a user with the given roles.
     *
     * @param isAdmin       true when the user has ROLE_ADMIN
     * @param isAdminOrRest true when the user has ROLE_ADMIN or ROLE_REST
     */
    boolean hasUnacknowledgedNotices(boolean isAdmin, boolean isAdminOrRest);
}
