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
package org.opennms.netmgt.telemetry.protocols.collection;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;

import java.util.Objects;

/**
 * Used to group both a collection agent and an associated collection set.
 */
public class CollectionSetWithAgent {
    private final CollectionAgent agent;
    private final CollectionSet collectionSet;

    public CollectionSetWithAgent(CollectionAgent agent, CollectionSet collectionSet) {
        this.agent = Objects.requireNonNull(agent);
        this.collectionSet = Objects.requireNonNull(collectionSet);
    }

    public CollectionAgent getAgent() {
        return agent;
    }

    public CollectionSet getCollectionSet() {
        return collectionSet;
    }
}
