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
package org.opennms.netmgt.graph.api.validation;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.graph.api.validation.exception.InvalidGraphContainerIdException;

import com.google.common.base.Strings;

public class GraphContainerIdValidator {

    private static final Pattern PATTERN = Pattern.compile(NamespaceValidator.REG_EXP);

    public void validate(String containerId) {
        Objects.requireNonNull(containerId);
        if (Strings.isNullOrEmpty(containerId)) {
            throw new InvalidGraphContainerIdException("Id of container must nut be empty or null");
        }
        final Matcher matcher = PATTERN.matcher(containerId);
        if (!matcher.matches()) {
            throw new InvalidGraphContainerIdException(NamespaceValidator.REG_EXP, containerId);
        }
    }
}
