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
package org.opennms.netmgt.notifd.api;

import java.util.List;

/**
 * Simplified interface for retrieving notification related configuration items.
 *
 * We use this interface to make these accessible via OSGi since the current configuration
 * model and DAOs for notifications are not available in the container.
 *
 * @author jwhite
 */
public interface NotificationConfigProvider {

    List<String> getDestinationPathNames();

    List<String> getTargetNames(String destinationPathName, boolean includeEscalations);

    List<String> getCommands(String destinationPathName, String targetName, boolean includeEscalations);
}
