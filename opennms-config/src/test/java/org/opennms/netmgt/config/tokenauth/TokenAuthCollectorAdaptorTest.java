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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;

public class TokenAuthCollectorAdaptorTest {

    private TokenProvider tokenProvider;
    private TokenAuthCollectorAdaptor adaptor;
    private CollectionAgent agent;

    @Before
    public void setUp() throws Exception {
        tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.hasAnyAuths()).thenReturn(true);
        adaptor = new TokenAuthCollectorAdaptor(tokenProvider);

        agent = mock(CollectionAgent.class);
        when(agent.getNodeId()).thenReturn(42);
        when(agent.getAddress()).thenReturn(InetAddress.getByName("10.0.0.1"));
    }

    @Test
    public void handleCollectionResultInvalidatesEachCommaSeparatedName() {
        final Map<String, Object> params = new HashMap<>();
        params.put(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM, "vault,f5");
        params.put(TokenAuthCollectorAdaptor.AUTH_FAILURE_PARAM, Boolean.TRUE);
        final CollectionSet failed = mock(CollectionSet.class);
        when(failed.getStatus()).thenReturn(CollectionStatus.FAILED);

        final CollectionSet returned = adaptor.handleCollectionResult(agent, params, failed);

        assertSame(failed, returned);
        verify(tokenProvider).invalidate("vault");
        verify(tokenProvider).invalidate("f5");
    }

    @Test
    public void handleCollectionResultDoesNotInvalidateWhenFailedWithoutAuthMarker() {
        final Map<String, Object> params = new HashMap<>();
        params.put(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM, "vault");
        // No AUTH_FAILURE_PARAM: e.g. transport error, 5xx, broker outage.
        final CollectionSet failed = mock(CollectionSet.class);
        when(failed.getStatus()).thenReturn(CollectionStatus.FAILED);

        adaptor.handleCollectionResult(agent, params, failed);

        verify(tokenProvider, never()).invalidate(anyString());
    }

    @Test
    public void handleCollectionResultDoesNotInvalidateOnSuccess() {
        final Map<String, Object> params = new HashMap<>();
        params.put(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM, "vault");
        final CollectionSet ok = mock(CollectionSet.class);
        when(ok.getStatus()).thenReturn(CollectionStatus.SUCCEEDED);

        adaptor.handleCollectionResult(agent, params, ok);

        verify(tokenProvider, never()).invalidate(anyString());
    }

    @Test
    public void handleCollectionResultDoesNotInvalidateWhenNoAuthsParticipated() {
        final Map<String, Object> params = new HashMap<>();
        final CollectionSet failed = mock(CollectionSet.class);
        when(failed.getStatus()).thenReturn(CollectionStatus.FAILED);

        adaptor.handleCollectionResult(agent, params, failed);

        verify(tokenProvider, never()).invalidate(anyString());
    }

    @Test
    public void handleCollectionResultIgnoresNullParameters() {
        final CollectionSet failed = mock(CollectionSet.class);
        when(failed.getStatus()).thenReturn(CollectionStatus.FAILED);

        final CollectionSet returned = adaptor.handleCollectionResult(agent, null, failed);

        assertSame(failed, returned);
        verify(tokenProvider, never()).invalidate(anyString());
    }

    // ------------------------------------------------------------------
    // beforeRuntimeInterpolation
    // ------------------------------------------------------------------

    @Test
    public void beforeRuntimeInterpolationShortCircuitsWhenNoAuthsConfigured() {
        when(tokenProvider.hasAnyAuths()).thenReturn(false);
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("Authorization", "Bearer ${token:vault}");

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        assertSame(attrs, out);
        verify(tokenProvider, never()).getToken(anyString(), any(Scope.class));
    }

    @Test
    public void beforeRuntimeInterpolationReturnsInputUnchangedWhenEmpty() {
        final Map<String, Object> empty = new HashMap<>();
        assertSame(empty, adaptor.beforeRuntimeInterpolation(agent, empty));
        assertNull(adaptor.beforeRuntimeInterpolation(agent, null));
    }

    @Test
    public void beforeRuntimeInterpolationReturnsInputUnchangedWhenNoPlaceholder() {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("url", "https://example.com/data");
        attrs.put("collection", buildSampleCollection("X-Token", "static-value"));

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        assertSame("no allocation when nothing to substitute", attrs, out);
        verify(tokenProvider, never()).getToken(anyString(), any(Scope.class));
    }

    @Test
    public void beforeRuntimeInterpolationSubstitutesInStringValue() {
        when(tokenProvider.getToken(eq("vault"), any(Scope.class))).thenReturn(Optional.of("tok-xyz"));

        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("Authorization", "Bearer ${token:vault}");

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        assertEquals("Bearer tok-xyz", out.get("Authorization"));
        assertEquals("vault", out.get(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM));
    }

    @Test
    public void beforeRuntimeInterpolationSubstitutesInsideJaxbValue() {
        when(tokenProvider.getToken(eq("vault"), any(Scope.class))).thenReturn(Optional.of("tok-xyz"));

        final SampleCollection collection = buildSampleCollection("X-Vault-Token", "${token:vault}");
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("xmlCollection", collection);

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        final Object substituted = out.get("xmlCollection");
        assertNotSame("JAXB value should have been replaced after substitution", collection, substituted);
        assertTrue(substituted instanceof SampleCollection);
        assertEquals("tok-xyz", ((SampleCollection) substituted).getRequest().getHeaderValue());
        assertEquals("vault", out.get(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM));
    }

    @Test
    public void beforeRuntimeInterpolationUnwrapsAndRewrapsToBeInterpolated() throws Exception {
        when(tokenProvider.getToken(eq("vault"), any(Scope.class))).thenReturn(Optional.of("tok-xyz"));

        // XmlCollector wraps its runtime-attribute values with
        // Interpolator.pleaseInterpolate(...) so Mate's standard pass
        // knows to recurse into them. The adaptor must unwrap, substitute,
        // and rewrap so Mate continues to process the value normally.
        final SampleCollection collection = buildSampleCollection("X-Vault-Token", "${token:vault}");
        final Object wrapped = Interpolator.pleaseInterpolate(collection);
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("xmlCollection", wrapped);

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        final Object substitutedWrapped = out.get("xmlCollection");
        assertNotSame("wrapper should be a fresh ToBeInterpolated", wrapped, substitutedWrapped);
        assertTrue("wrapped should be a ToBeInterpolated", Interpolator.isPleaseInterpolate(substitutedWrapped));
        final SampleCollection substitutedInner = (SampleCollection) Interpolator.unwrap(substitutedWrapped);
        assertEquals("tok-xyz", substitutedInner.getRequest().getHeaderValue());
        assertEquals("vault", out.get(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM));
    }

    @Test
    public void beforeRuntimeInterpolationUsesInlineFallbackWhenTokenAbsent() {
        when(tokenProvider.getToken(eq("missing"), any(Scope.class))).thenReturn(Optional.empty());

        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("h", "X: ${token:missing|fallback-value}");

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        assertEquals("X: fallback-value", out.get("h"));
    }

    @Test
    public void beforeRuntimeInterpolationSubstitutesEmptyWhenTokenAbsentAndNoFallback() {
        when(tokenProvider.getToken(eq("missing"), any(Scope.class))).thenReturn(Optional.empty());

        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("h", "X: ${token:missing}");

        final Map<String, Object> out = adaptor.beforeRuntimeInterpolation(agent, attrs);

        assertEquals("X: ", out.get("h"));
    }

    private static SampleCollection buildSampleCollection(final String headerName, final String headerValue) {
        final SampleCollection collection = new SampleCollection();
        final SampleRequest request = new SampleRequest();
        request.setHeaderName(headerName);
        request.setHeaderValue(headerValue);
        collection.setRequest(request);
        return collection;
    }

    /**
     * Minimal JAXB-annotated stand-in for the kind of collection-config
     * snippet (e.g. {@code XmlDataCollection}) that the XmlCollector
     * places into the runtime-attribute map. Carries a single header
     * value so we can verify the adaptor's marshal/substitute/unmarshal
     * round-trip preserves shape.
     */
    @XmlRootElement(name = "sample-collection")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SampleCollection {
        @XmlElement(name = "request")
        private SampleRequest request;

        public SampleRequest getRequest() { return request; }
        public void setRequest(final SampleRequest request) { this.request = request; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SampleRequest {
        @XmlElement(name = "header-name")
        private String headerName;
        @XmlElement(name = "header-value")
        private String headerValue;

        public String getHeaderName() { return headerName; }
        public void setHeaderName(final String headerName) { this.headerName = headerName; }
        public String getHeaderValue() { return headerValue; }
        public void setHeaderValue(final String headerValue) { this.headerValue = headerValue; }
    }

    @Test
    public void handleCollectionResultIgnoresEmptyNameList() {
        final Map<String, Object> params = new HashMap<>();
        params.put(TokenAuthCollectorAdaptor.AUTH_NAMES_USED_PARAM, "");
        params.put(TokenAuthCollectorAdaptor.AUTH_FAILURE_PARAM, Boolean.TRUE);
        final CollectionSet failed = mock(CollectionSet.class);
        when(failed.getStatus()).thenReturn(CollectionStatus.FAILED);

        adaptor.handleCollectionResult(agent, params, failed);

        verify(tokenProvider, never()).invalidate(anyString());
    }
}
