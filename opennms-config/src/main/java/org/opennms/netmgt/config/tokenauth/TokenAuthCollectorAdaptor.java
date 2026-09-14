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
package org.opennms.netmgt.config.tokenauth;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.CollectorAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Resolves {@code ${token:<name>}} placeholders on the controller before
 * Mate's {@code Interpolator} runs, and invalidates the token cache for
 * any auth name used in a FAILED collection.
 */
public class TokenAuthCollectorAdaptor implements CollectorAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthCollectorAdaptor.class);

    /** {@code ${token:<name>}} or {@code ${token:<name>|<fallback>}}. */
    private static final Pattern TOKEN_PLACEHOLDER =
            Pattern.compile("\\$\\{token:([^|}]+)(?:\\|([^}]*))?\\}");

    /**
     * Comma-joined list of auth names resolved during substitution.
     * Stored as a String for marshal safety across the RPC boundary.
     */
    static final String AUTH_NAMES_USED_PARAM = "__opennms.tokenauth.names";

    private final TokenProvider tokenProvider;
    private volatile EntityScopeProvider entityScopeProvider;

    public TokenAuthCollectorAdaptor(final TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
    }

    @Autowired(required = false)
    public void setEntityScopeProvider(final EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }

    @Override
    public Map<String, Object> beforeRuntimeInterpolation(final CollectionAgent agent,
                                                          final Map<String, Object> runtimeAttributes) {
        if (runtimeAttributes == null || runtimeAttributes.isEmpty()) {
            return runtimeAttributes;
        }
        // Skip the JAXB-marshal placeholder scan when no auths exist.
        if (!tokenProvider.hasAnyAuths()) {
            return runtimeAttributes;
        }
        if (!containsTokenPlaceholder(runtimeAttributes.values())) {
            return runtimeAttributes;
        }

        final Scope scope = buildScope(agent);
        final Set<String> namesUsed = new LinkedHashSet<>();
        final Map<String, Object> resolved = new HashMap<>(runtimeAttributes);
        for (final Map.Entry<String, Object> entry : runtimeAttributes.entrySet()) {
            final Object value = entry.getValue();
            final Object substituted = substituteInValue(value, scope, namesUsed);
            if (substituted != value) {
                resolved.put(entry.getKey(), substituted);
            }
        }

        if (!namesUsed.isEmpty()) {
            resolved.put(AUTH_NAMES_USED_PARAM, String.join(",", namesUsed));
        }
        return resolved;
    }

    @Override
    public CollectionSet handleCollectionResult(final CollectionAgent agent,
                                                final Map<String, Object> parameters,
                                                final CollectionSet result) {
        if (result == null
                || result.getStatus() == null
                || result.getStatus() != CollectionStatus.FAILED) {
            return result;
        }
        if (parameters == null) {
            return result;
        }
        // Only invalidate on flagged auth failures; let transport/5xx FAILEDs pass.
        if (!Boolean.TRUE.equals(parameters.get(AUTH_FAILURE_PARAM))) {
            return result;
        }
        final Object raw = parameters.get(AUTH_NAMES_USED_PARAM);
        if (!(raw instanceof String) || ((String) raw).isEmpty()) {
            return result;
        }
        for (final String name : ((String) raw).split(",")) {
            final String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            tokenProvider.invalidate(trimmed);
            LOG.debug("Invalidated token cache for auth '{}' after auth failure (node {})",
                    trimmed, agent != null ? agent.getNodeId() : null);
        }
        return result;
    }

    private Scope buildScope(final CollectionAgent agent) {
        if (entityScopeProvider == null || agent == null) {
            return EmptyScope.EMPTY;
        }
        final Integer nodeId = agent.getNodeId();
        final String address = agent.getAddress() != null
                ? InetAddressUtils.toIpAddrString(agent.getAddress())
                : null;
        if (nodeId == null) {
            return EmptyScope.EMPTY;
        }
        if (address == null) {
            return entityScopeProvider.getScopeForNode(nodeId);
        }
        return new FallbackScope(
                entityScopeProvider.getScopeForNode(nodeId),
                entityScopeProvider.getScopeForInterface(nodeId, address));
    }

    private static boolean containsTokenPlaceholder(final Collection<?> values) {
        for (final Object v : values) {
            if (mayContainTokenPlaceholder(v)) {
                return true;
            }
        }
        return false;
    }

    private static boolean mayContainTokenPlaceholder(final Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof String) {
            return ((String) v).contains("${token:");
        }
        if (Interpolator.isPleaseInterpolate(v)) {
            return mayContainTokenPlaceholder(Interpolator.unwrap(v));
        }
        if (v.getClass().isAnnotationPresent(XmlRootElement.class)) {
            final String marshaled = JaxbUtils.marshal(v);
            return marshaled != null && marshaled.contains("${token:");
        }
        return false;
    }

    private Object substituteInValue(final Object value, final Scope scope, final Set<String> namesUsed) {
        if (value instanceof String) {
            final String stringValue = (String) value;
            if (!stringValue.contains("${token:")) {
                return value;
            }
            return substitute(stringValue, scope, namesUsed);
        }
        if (Interpolator.isPleaseInterpolate(value)) {
            final Object inner = Interpolator.unwrap(value);
            if (inner == null) {
                return value;
            }
            final Object substituted = substituteInValue(inner, scope, namesUsed);
            if (substituted == inner) {
                return value;
            }
            return Interpolator.pleaseInterpolate(substituted);
        }
        if (value != null && value.getClass().isAnnotationPresent(XmlRootElement.class)) {
            final String marshaled = JaxbUtils.marshal(value);
            if (marshaled == null || !marshaled.contains("${token:")) {
                return value;
            }
            final String substituted = substitute(marshaled, scope, namesUsed);
            return JaxbUtils.unmarshal(value.getClass(), substituted, false);
        }
        return value;
    }

    private String substitute(final String value, final Scope scope, final Set<String> namesUsed) {
        final Matcher m = TOKEN_PLACEHOLDER.matcher(value);
        final StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            final String authName = m.group(1).trim();
            final String fallback = m.group(2);
            out.append(value, last, m.start());
            final String resolved = tokenProvider.getToken(authName, scope)
                    .orElse(fallback != null ? fallback : "");
            out.append(resolved);
            namesUsed.add(authName);
            last = m.end();
        }
        out.append(value, last, value.length());
        return out.toString();
    }
}
