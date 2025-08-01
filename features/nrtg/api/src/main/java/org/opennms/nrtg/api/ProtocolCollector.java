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
package org.opennms.nrtg.api;

import org.opennms.nrtg.api.model.CollectionJob;

/**
 * Interface for a protocol specific collector.
 * <p>
 * a protocol collector does the actual work of a collection job, by collecting
 * the data-points from the node via the protocol.
 * </p>
 * <p>
 * There should only be one collector per protocol.
 * </p>
 *
 * @author Simon Walter
 */
public interface ProtocolCollector {
    String getProtcol();

    CollectionJob collect(CollectionJob job);
}
