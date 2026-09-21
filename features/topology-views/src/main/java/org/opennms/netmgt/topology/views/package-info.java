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

/**
 * Persistence model and DAO for custom (user-composed) topology views.
 *
 * <p>A topology view is a named document describing a free-form canvas
 * of nodes, edges, and free-standing text labels assembled by an
 * operator. Views are stored in the shared JSON key-value store
 * ({@code kvstore_jsonb}, context {@code topology-views}); the canvas
 * model itself lives as a JSON document column so the schema evolves
 * with the front-end without database migrations.
 */
package org.opennms.netmgt.topology.views;
