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
package org.opennms.netmgt.collection.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.xml.JaxbUtils;

/**
 * Generic code for a {@link ServiceCollector} which is expected to run
 * on both OpenNMS and Minion.
 *
 * This class takes care of performing the necessary marshaling and
 * unmarshaling of parameters with the help of a type map provided
 * by the implementation.
 *
 * @author jwhite
 */
public abstract class AbstractRemoteServiceCollector extends AbstractServiceCollector {

    /**
     * Mapping of parameter names to associated classes, used for unmarshaling
     */
    private final Map<String, Class<?>> parameterTypeMap;

    public AbstractRemoteServiceCollector() {
        this(Collections.emptyMap());
    }

    public AbstractRemoteServiceCollector(Map<String, Class<?>> parameterTypeMap) {
        this.parameterTypeMap = Objects.requireNonNull(parameterTypeMap);
    }

    @Override
    public String getEffectiveLocation(String location) {
        return location;
    }

    @Override
    public Map<String, String> marshalParameters(Map<String, Object> parameters) {
        final Map<String, String> marshaledParams = new HashMap<>();
        parameters.forEach((k,v) -> {
            if (v == null) {
                // Skip
            } else if (v instanceof String) {
                // As-is
                marshaledParams.put(k, (String)v);
            } else {
                // This entry needs to be marshaled, so we should
                // find an corresponding entry in the parameterTypeMap
                final Class<?> clazz = parameterTypeMap.get(k);
                if (clazz == null) {
                    throw new IllegalStateException(String.format("The parameter map for collector %s include a parameters named %s "
                            + "which must be marshaled, but no type mapping was provided. Aborting.",
                            getClass().getCanonicalName(), k));
                }
                if (!clazz.isAssignableFrom(v.getClass())) {
                    throw new IllegalStateException(String.format("The parameter map for collector %s include a parameters named %s "
                            + "which must be marshaled, but type mapping is incorrect (got %s, but expected %s). Aborting.",
                            getClass().getCanonicalName(), k, v.getClass(), clazz));
                }
                // The type mapping matches, let's marshal
                marshaledParams.put(k, JaxbUtils.marshal(v));
            }
        });
        return marshaledParams;
    }

    @Override
    public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
        final Map<String, Object> unmarshaledParams = new HashMap<>();
        parameters.forEach((k,v) -> {
            if (v == null) {
                // Skip
            } else {
                // Find an corresponding entry in the parameterTypeMap
                final Class<?> clazz = parameterTypeMap.get(k);
                if (clazz == null) {
                    // No type mapping found, use the string
                    unmarshaledParams.put(k, v);
                } else {
                    // Unmarshal
                    unmarshaledParams.put(k, JaxbUtils.unmarshal(clazz, v));
                }
            }
        });
        return unmarshaledParams;
    }
}
