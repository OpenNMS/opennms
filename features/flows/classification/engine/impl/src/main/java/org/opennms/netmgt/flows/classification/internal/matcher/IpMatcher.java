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
package org.opennms.netmgt.flows.classification.internal.matcher;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.internal.value.IpValue;

import com.google.common.base.Function;

class IpMatcher implements Matcher {

    // Extracts the value from the ClassificationRequest. Allows to easily distinguish between srcAddress and dstAddress
    private final Function<ClassificationRequest, IpAddr> valueExtractor;
    private final IpValue value;

    protected IpMatcher(IpValue input, Function<ClassificationRequest, IpAddr> valueExtractor) {
        this.value = input;
        this.valueExtractor = Objects.requireNonNull(valueExtractor);
    }

    @Override
    public boolean matches(ClassificationRequest request) {
        var addr = valueExtractor.apply(request);
        final boolean matches = value.isInRange(addr);
        return matches;
    }
}
