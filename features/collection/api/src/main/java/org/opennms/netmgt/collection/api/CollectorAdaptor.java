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

import java.util.Map;

/**
 * Cross-cutting hook for a {@link ServiceCollector} dispatch. Adaptors
 * run on the OpenNMS controller (never on the Minion) and wrap a
 * collection with pre- and post-RPC behavior without modifying the
 * collector itself. Modeled on {@code ServiceMonitorAdaptor}.
 */
public interface CollectorAdaptor {

    /** Set on the parameter map when the cause chain carried an "auth failure:" marker. */
    String AUTH_FAILURE_PARAM = "_collectorAdaptor.authFailure";

    /**
     * Invoked after runtime attributes are fetched but before Mate's
     * {@code Interpolator} walks them. Adaptors that own a custom
     * {@code ${prefix:...}} placeholder must substitute here, because
     * Mate's standard pass strips unknown prefixes to empty.
     */
    default Map<String, Object> beforeRuntimeInterpolation(final CollectionAgent agent,
                                                           final Map<String, Object> runtimeAttributes) {
        return runtimeAttributes;
    }

    /** Invoked after the collection future resolves; may inspect or replace the result. */
    default CollectionSet handleCollectionResult(final CollectionAgent agent,
                                                 final Map<String, Object> parameters,
                                                 final CollectionSet result) {
        return result;
    }
}
