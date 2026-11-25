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
package org.opennms.web.rest.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ResourceTypeDto;
import org.opennms.web.rest.v2.model.ResourceTypeStrategyDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigDto;

/**
 * Field-level validation for the resource types REST service.
 *
 * <p>It runs before the DTOs are mapped onto the config model, whose setters reject null. It also covers what the
 * schema check of the configuration manager does not: the required strategy elements, and values that the JSON
 * conversion would drop because they are empty.</p>
 *
 * <p>Each method returns a human-readable message, or {@code null} when the input is valid.</p>
 */
public abstract class ResourceTypesConfigValidator {

    /** Also safe as a file name, which the download uses. */
    static final Pattern CONFIG_ID = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]*$");

    /** Resource type names become directory names below the RRD repository. */
    static final Pattern TYPE_NAME = Pattern.compile("^[^\\s/\\\\]+$");

    static final Pattern CLASS_NAME = Pattern.compile("^[A-Za-z_$][\\w$]*(\\.[A-Za-z_$][\\w$]*)*$");

    private ResourceTypesConfigValidator() {
    }

    public static String validateConfigId(final String id) {
        if (isBlank(id)) {
            return "The configuration id is required.";
        }
        if (!CONFIG_ID.matcher(id).matches()) {
            return "Invalid configuration id '" + id + "': use letters, digits, '.', '_' and '-', starting with a letter or digit.";
        }
        return null;
    }

    public static String validateConfig(final ResourceTypesConfigDto config) {
        if (config == null) {
            return "The configuration is required.";
        }
        final String idMessage = validateConfigId(config.getId());
        if (idMessage != null) {
            return idMessage;
        }
        final List<ResourceTypeDto> types = config.getResourceTypes();
        if (types == null) {
            return null;
        }
        final Set<String> names = new HashSet<>();
        for (int i = 0; i < types.size(); i++) {
            final ResourceTypeDto type = types.get(i);
            final String message = validateResourceType(type);
            if (message != null) {
                return "Invalid resource type at index " + i + ": " + message;
            }
            if (!names.add(type.getName().trim())) {
                return "Resource type '" + type.getName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    public static String validateResourceType(final ResourceTypeDto type) {
        if (type == null) {
            return "the resource type is empty.";
        }
        if (isBlank(type.getName())) {
            return "the name is required.";
        }
        if (!TYPE_NAME.matcher(type.getName().trim()).matches()) {
            return "the name '" + type.getName() + "' must not contain blanks, '/' or '\\'.";
        }
        if (isBlank(type.getLabel())) {
            return "the label of '" + type.getName().trim() + "' is required.";
        }
        String message = validateStrategy(type.getPersistenceSelectorStrategy());
        if (message != null) {
            return "persistence selector strategy of '" + type.getName().trim() + "': " + message;
        }
        message = validateStrategy(type.getStorageStrategy());
        if (message != null) {
            return "storage strategy of '" + type.getName().trim() + "': " + message;
        }
        return null;
    }

    private static String validateStrategy(final ResourceTypeStrategyDto strategy) {
        if (strategy == null || isBlank(strategy.getClassName())) {
            return "the class is required.";
        }
        if (!CLASS_NAME.matcher(strategy.getClassName().trim()).matches()) {
            return "'" + strategy.getClassName() + "' is not a valid Java class name.";
        }
        if (strategy.getParameters() != null) {
            for (int i = 0; i < strategy.getParameters().size(); i++) {
                final ParameterDto parameter = strategy.getParameters().get(i);
                if (parameter == null || isBlank(parameter.getKey())) {
                    return "the key of parameter " + (i + 1) + " is required.";
                }
                if (parameter.getValue() == null || parameter.getValue().isEmpty()) {
                    return "the value of parameter '" + parameter.getKey().trim() + "' is required.";
                }
            }
        }
        return null;
    }

    private static boolean isBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }
}
