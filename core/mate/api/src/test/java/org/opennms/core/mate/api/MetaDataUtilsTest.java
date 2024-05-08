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
package org.opennms.core.mate.api;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.pagesequence.Parameter;

public class MetaDataUtilsTest {
    final Map<ContextKey, String> metaData = new HashMap<>();

    @Before
    public void setUp() {
        metaData.put(new ContextKey("ctx1", "key1"), "val1");
        metaData.put(new ContextKey("ctx1", "key2"), "val2");
        metaData.put(new ContextKey("ctx2", "key3"), "val3");
        metaData.put(new ContextKey("ctx2", "key4"), "val4");
        metaData.put(new ContextKey("ctx3", "port"), "1234");
        metaData.put(new ContextKey("ctx3", "user"), "papapape");
        metaData.put(new ContextKey("ctx4", "inner"), "outer");
        metaData.put(new ContextKey("ctx4", "outer:example"), "working");
        metaData.put(new ContextKey("ctx5", "key1"), "${ctx5:key2}");
        metaData.put(new ContextKey("ctx5", "key2"), "working");
        metaData.put(new ContextKey("ctx6", "key1"), "${ctx6:key1}a");
        metaData.put(new ContextKey("ctx7", "key1"), "42");
    }

    @Test
    public void testToBeInterpolated() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attribute1", "aaa${ctx1:key1|default}bbb");
        attributes.put("attribute2", Interpolator.pleaseInterpolate("aaa${ctx1:key1|default}bbb"));

        Map<String, Object> interpolatedAttributes = Interpolator.interpolateAttributes(attributes, new MapScope(Scope.ScopeName.NODE, this.metaData));

        assertEquals("aaa${ctx1:key1|default}bbb", interpolatedAttributes.get("attribute1"));
        assertEquals("aaaval1bbb", interpolatedAttributes.get("attribute2"));
    }

    @Test
    public void testMetaDataInterpolation() {
        final Map<String, Object> attributes = new TreeMap<>();

        attributes.put("attribute1", "aaa${ctx1:key1|ctx2:key2|default}bbb");
        attributes.put("attribute2", "aaa${ctx1:key3|ctx2:key3|default}bbb");
        attributes.put("attribute3", "aaa${ctx1:key4|ctx2:key1|default}bbbaaa${ctx1:key4|ctx2:key4|default}bbb");
        attributes.put("attribute4", "aaa${ctx1:key4}bbb");
        attributes.put("attribute5", "aaa${ctx1:key4|}bbb");
        attributes.put("attribute6", "aaa${ctx1:key4|default}bbb");
        attributes.put("attribute7", Integer.valueOf(42));
        attributes.put("attribute8", Long.valueOf(42L));
        attributes.put("attribute9", "aaa${abc}bbb");
        attributes.put("attribute10", "aaa${ctx4:${ctx4:inner}:example}bbb");
        attributes.put("attribute11", "aaa${ctx5:key1}bbb");
        attributes.put("attribute12", "xx${ctx6:key1}yy");

        final Map<String, Object> interpolatedAttributes = Interpolator.interpolateObjects(attributes, new MapScope(Scope.ScopeName.NODE, this.metaData));

        assertEquals(attributes.size(), interpolatedAttributes.size());
        assertEquals("aaaval1bbb", interpolatedAttributes.get("attribute1"));
        assertEquals("aaaval3bbb", interpolatedAttributes.get("attribute2"));
        assertEquals("aaadefaultbbbaaaval4bbb", interpolatedAttributes.get("attribute3"));
        assertEquals("aaabbb", interpolatedAttributes.get("attribute4"));
        assertEquals("aaabbb", interpolatedAttributes.get("attribute5"));
        assertEquals("aaadefaultbbb", interpolatedAttributes.get("attribute6"));
        assertTrue(interpolatedAttributes.get("attribute7") instanceof Integer);
        assertTrue(interpolatedAttributes.get("attribute8") instanceof Long);
        assertEquals(42, interpolatedAttributes.get("attribute7"));
        assertEquals(42L, interpolatedAttributes.get("attribute8"));
        assertEquals("aaa${abc}bbb", interpolatedAttributes.get("attribute9"));
        assertEquals("aaaworkingbbb", interpolatedAttributes.get("attribute10"));
        assertEquals("aaaworkingbbb", interpolatedAttributes.get("attribute11"));
        assertEquals("xx${ctx6:key1}aaaaaaaayy", interpolatedAttributes.get("attribute12"));
    }

    @Test
    public void testPageSequence() {
        final PageSequence pageSequence = JaxbUtils.unmarshal(PageSequence.class, getClass().getClassLoader().getResourceAsStream("page-sequence.xml"));
        final Map<String, Object> input = new HashMap<>();
        input.put("page-sequence", pageSequence);
        final Map<String, Object> output = Interpolator.interpolateObjects(input, new MapScope(Scope.ScopeName.NODE, this.metaData));
        assertEquals("8980", ((PageSequence)output.get("page-sequence")).getPages().get(0).getPort());
        assertEquals("1234", ((PageSequence)output.get("page-sequence")).getPages().get(1).getPort());
        assertEquals("8980", ((PageSequence)output.get("page-sequence")).getPages().get(2).getPort());
        assertThat(((PageSequence)output.get("page-sequence")).getPages().get(1).getParameters(), hasItem(new Parameter("j_username", "papapape")));
    }

    @Test
    public void testNewRegExpPattern() {
        final Interpolator.Result result = Interpolator.interpolate("foo-${aaa}-bar-${ctx1:key1|down}-bla-${ctx2:key4|down}-blupp-${bbb}", new MapScope(Scope.ScopeName.NODE, this.metaData));
        assertEquals(2, result.parts.size());
        assertEquals("val1", result.parts.get(0).value.value);
        assertEquals("val4", result.parts.get(1).value.value);
        assertEquals("foo-${aaa}-bar-val1-bla-val4-blupp-${bbb}", result.output);
    }

    @Test
    public void testFallBackScopeProvider() {
        final Map<ContextKey, String> map1 = new TreeMap<>();
        map1.put(new ContextKey("foobar", "key1"), "value1");
        map1.put(new ContextKey("foobar", "key2"), "value2");
        map1.put(new ContextKey("foobar", "key3"), "value3");

        final Map<ContextKey, String> map2 = new TreeMap<>();
        map2.put(new ContextKey("foobar", "key3"), "new3");

        final FallBackScopeProvider fallBackScopeProvider = new FallBackScopeProvider(
                () -> new MapScope(Scope.ScopeName.NODE, map1),
                () -> new MapScope(Scope.ScopeName.INTERFACE, map2)
        );

        assertEquals("value1", Interpolator.interpolate("${foobar:key1}", fallBackScopeProvider.getScope()).output);
        assertEquals("value2", Interpolator.interpolate("${foobar:key2}", fallBackScopeProvider.getScope()).output);
        assertEquals("new3", Interpolator.interpolate("${foobar:key3}", fallBackScopeProvider.getScope()).output);
    }

    @Test
    public void testParts() {
        final Interpolator.Result result = Interpolator.interpolate("foo-${aaa}-bar-${ctx1:key1|down}-bla-${ctx2:key4|down}-blupp-${bbb}", new MapScope(Scope.ScopeName.NODE, this.metaData));
        assertEquals(2, result.parts.size());
        assertEquals("val1", result.parts.get(0).value.value);
        assertEquals("${ctx1:key1|down}", result.parts.get(0).input);
        assertEquals("ctx1:key1", result.parts.get(0).match);
        assertEquals("val4", result.parts.get(1).value.value);
        assertEquals("${ctx2:key4|down}", result.parts.get(1).input);
        assertEquals("ctx2:key4", result.parts.get(1).match);
        assertEquals("foo-${aaa}-bar-val1-bla-val4-blupp-${bbb}", result.output);
    }

    @Test
    public void testNMS16374() {
        assertResultOutput("${ctx1:key1|aaa:bbb|ccc:ddd}", "val1");
        assertResultOutput("${aaa:bbb|ctx1:key1|ccc:ddd}","val1");
        assertResultOutput("${aaa:bbb|ccc:ddd|ctx1:key1}","val1");
        assertResultOutput("${aaa:bbb|ccc:ddd|eee:fff}","");
        assertResultOutput("${aaa:bbb}","");
        assertResultOutput("${ctx1:key1}", "val1");
        assertResultOutput("${aaa:bbb|ccc:ddd|\"eee:fff\"}","eee:fff");
        assertResultOutput("${aaa:bbb|ccc:ddd|\"ctx1:key1\"}","ctx1:key1");
        assertResultOutput("${aaa:bbb|ccc:ddd|'eee:fff'}","eee:fff");
        assertResultOutput("${aaa:bbb|ccc:ddd|'ctx1:key1'}","ctx1:key1");
        assertResultOutput("${requisition:url|detector:url|'jdbc:postgresql://OPENNMS_JDBC_HOSTNAME:5432/opennms'}", "jdbc:postgresql://OPENNMS_JDBC_HOSTNAME:5432/opennms");
        assertResultOutput("${requisition:url|\"http://example.org\"}", "http://example.org");
    }

    @Test
    public void testEscaping() {
        assertResultOutput("xxx\"$$aaa'$bbb$ccc${ctx1:key1|aaa:bbb|ccc:ddd}", "xxx\"$$aaa'$bbb$cccval1");
        assertResultOutput("xxx\"$$\"aaa'$$'bbb$$ccc${ctx1:key1|aaa:bbb|ccc:ddd}", "xxx\"$$\"aaa'$$'bbb$$cccval1");
        assertResultOutput("${aaa:bbb|ccc:ddd|\"eee:fff\"}","eee:fff");
        assertResultOutput("${aaa:bbb|ccc:ddd|\"ctx1:key1\"}","ctx1:key1");
        assertResultOutput("${aaa:bbb|ccc:ddd|'eee:fff'}","eee:fff");
        assertResultOutput("${aaa:bbb|ccc:ddd|'ctx1:key1'}","ctx1:key1");
    }

    private void assertResultOutput(final String expression, final String expected) {
        final Interpolator.Result result = Interpolator.interpolate(expression, new MapScope(Scope.ScopeName.NODE, this.metaData));
        assertEquals(expected, result.output);
    }
}
