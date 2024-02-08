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
package org.opennms.netmgt.bsm.service.model.functions.map;

import java.util.Optional;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;

@Function(name="Decrease", description = "Decreases the status by one level")
public class Decrease implements MapFunction {
    @Override
    public Optional<Status> map(Status source) {
        if (source == null) {
            return Optional.empty();
        }
        int newId = Math.max(Status.INDETERMINATE.getId(), source.getId() - 1);
        return Optional.of(Status.get(newId));
    }

    @Override
    public <T> T accept(MapFunctionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
