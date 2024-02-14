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
package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Sets;

public class ReductionKeyVertex extends AbstractBusinessServiceVertex {

    public static final int MAX_LABEL_LENGTH = 27;
    private static final Pattern REDUCTION_KEY_LABEL_PATTERN = Pattern.compile("^.*\\/(.+?):.*:(.+)$");

    private final String reductionKey;

    public ReductionKeyVertex(GraphVertex graphVertex) {
        this(graphVertex.getReductionKey(), graphVertex.getLevel());
    }

    public ReductionKeyVertex(String reductionKey, int level) {
        super(Type.ReductionKey + ":" + reductionKey, getLabelFromReductionKey(reductionKey), level);
        this.reductionKey = reductionKey;
        setTooltipText(String.format("Reduction Key '%s'", reductionKey));
        setIconKey("bsm.reduction-key");
    }

    protected static String getLabelFromReductionKey(String reductionKey) {
        String label;
        Matcher m = REDUCTION_KEY_LABEL_PATTERN.matcher(reductionKey);
        if (m.matches()) {
            label = String.format("%s:%s", m.group(1), m.group(2));
        } else {
            label = reductionKey;
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            return label.substring(0, MAX_LABEL_LENGTH - "...".length()) + "...";
        }
        return label;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    @Override
    public Type getType() {
        return Type.ReductionKey;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet(getReductionKey());
    }

    @Override
    public <T> T accept(BusinessServiceVertexVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
