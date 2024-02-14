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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.simple.SimpleMetaTopologyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LinkdMetaTopologyProvider extends SimpleMetaTopologyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdMetaTopologyProvider.class);

    private final List<GraphProvider> m_graphProviders = new ArrayList<>();

    public LinkdMetaTopologyProvider(LinkdTopologyProvider defaultlinkdTopologyProvider, List<LinkdTopologyProvider> linkdTopologyProviders) {
        super(Objects.requireNonNull(defaultlinkdTopologyProvider));
        m_graphProviders.add(defaultlinkdTopologyProvider);
        m_graphProviders.addAll(Objects.requireNonNull(linkdTopologyProviders));
    }

    @Override
    public List<GraphProvider> getGraphProviders() {
        return m_graphProviders;
    }

}