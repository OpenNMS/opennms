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

/**
 * SurveillanceStatus
 *
 * @author brozow
 */
public interface SurveillanceStatus {

    /**
     * The number of nodes that do not have at least one service up
     */
    Integer getDownEntityCount();

    /**
     * The total number of nodes represented by this status
     */
    Integer getTotalEntityCount();

    /**
     * A string presenting the status of the associated set of nodes
     * Possible values are:
     * <ul>
     * <li>'Normal' representing that there are no ouages for active services on the set of associated nodes<li>
     * <li>'Warning' representing exactly one service from set of all active services on the associated nodes has an outage
     * <li>'Critical' representing that more than one service on the from the set of all active services on the assocuate nodes has an outage
     * </ul> 
     */
    String getStatus();

}
