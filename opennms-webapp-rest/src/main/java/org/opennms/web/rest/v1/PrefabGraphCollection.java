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
package org.opennms.web.rest.v1;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.model.PrefabGraph;

@XmlRootElement(name = "prefab-graphs")
public final class PrefabGraphCollection extends JaxbListWrapper<PrefabGraph> {
    private static final long serialVersionUID = 1L;
    public PrefabGraphCollection() {
        super();
    }
    public PrefabGraphCollection(Collection<? extends PrefabGraph> graphs) {
        super(graphs);
    }
    @XmlElement(name = "prefab-graph")
    public List<PrefabGraph> getObjects() {
        return super.getObjects();
    }
}