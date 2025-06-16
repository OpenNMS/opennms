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
package org.opennms.netmgt.flows.classification.internal.value;

import org.opennms.netmgt.flows.classification.internal.decision.Bound;

public interface RuleValue<S extends Comparable<S>, T extends RuleValue<S, T>> {

    /**
     * Shrinks this rule value by removing those parts that are already covered by the given bound.
     * <p>
     * The given bounds result from thresholds along paths in the decision tree. During
     * classification those parts that are covered by these threshold need not to be checked again.
     *
     * @return Returns a shrunk rule value or {@code null} if this rule value is completely covered by the given bound.
     */
    T shrink(Bound<S> bound);
}
