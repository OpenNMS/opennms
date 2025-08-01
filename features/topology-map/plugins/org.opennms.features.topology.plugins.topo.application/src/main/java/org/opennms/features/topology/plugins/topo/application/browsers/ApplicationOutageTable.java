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
package org.opennms.features.topology.plugins.topo.application.browsers;

import java.util.Map;

import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.features.topology.api.browsers.SelectionAwareTable;

// Shows outages related to the shown applications
public class ApplicationOutageTable extends SelectionAwareTable {
    /**
     *  Leave OnmsVaadinContainer without generics; the Aries blueprint code cannot match up
     *  the arguments if you put the generic types in.
     */
    public ApplicationOutageTable(final String caption, final OnmsVaadinContainer container) {
        super(caption, container);
    }

    public void setColumnGenerators(Map generators) {
        for (Object key : generators.keySet()) {
            addGeneratedColumn(key, (ColumnGenerator)generators.get(key));
        }
    }
}
