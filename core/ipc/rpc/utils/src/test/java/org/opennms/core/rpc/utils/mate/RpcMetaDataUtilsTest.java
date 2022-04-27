/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.rpc.utils.mate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.pagesequence.Parameter;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;

public class RpcMetaDataUtilsTest {
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

        Assert.assertEquals(attributes.size(), interpolatedAttributes.size());
        Assert.assertEquals("aaaval1bbb", interpolatedAttributes.get("attribute1"));
        Assert.assertEquals("aaaval3bbb", interpolatedAttributes.get("attribute2"));
        Assert.assertEquals("aaadefaultbbbaaaval4bbb", interpolatedAttributes.get("attribute3"));
        Assert.assertEquals("aaabbb", interpolatedAttributes.get("attribute4"));
        Assert.assertEquals("aaabbb", interpolatedAttributes.get("attribute5"));
        Assert.assertEquals("aaadefaultbbb", interpolatedAttributes.get("attribute6"));
        Assert.assertTrue(interpolatedAttributes.get("attribute7") instanceof Integer);
        Assert.assertTrue(interpolatedAttributes.get("attribute8") instanceof Long);
        Assert.assertEquals(42, interpolatedAttributes.get("attribute7"));
        Assert.assertEquals(42L, interpolatedAttributes.get("attribute8"));
        Assert.assertEquals("aaa${abc}bbb", interpolatedAttributes.get("attribute9"));
        Assert.assertEquals("aaaworkingbbb", interpolatedAttributes.get("attribute10"));
        Assert.assertEquals("aaaworkingbbb", interpolatedAttributes.get("attribute11"));
        Assert.assertEquals("xx${ctx6:key1}aaaaaaaayy", interpolatedAttributes.get("attribute12"));
    }

    @Test
    public void testPageSequence() {
        final PageSequence pageSequence = JaxbUtils.unmarshal(PageSequence.class, getClass().getClassLoader().getResourceAsStream("page-sequence.xml"));
        System.err.println(pageSequence);
        final Map<String, Object> input = new HashMap<>();
        input.put("page-sequence", pageSequence);
        final Map<String, Object> output = Interpolator.interpolateObjects(input, new MapScope(Scope.ScopeName.NODE, this.metaData));
        Assert.assertEquals("8980", ((PageSequence)output.get("page-sequence")).getPages().get(0).getPort());
        Assert.assertEquals("1234", ((PageSequence)output.get("page-sequence")).getPages().get(1).getPort());
        Assert.assertEquals("8980", ((PageSequence)output.get("page-sequence")).getPages().get(2).getPort());
        Assert.assertThat(((PageSequence)output.get("page-sequence")).getPages().get(1).getParameters(), hasItem(new Parameter("j_username", "papapape")));
    }

    @Test
    public void testNewRegExpPattern() {
        final Interpolator.Result result = Interpolator.interpolate("foo-${aaa}-bar-${ctx1:key1|down}-bla-${ctx2:key4|down}-blupp-${bbb}", new MapScope(Scope.ScopeName.NODE, this.metaData));
        Assert.assertEquals(2, result.parts.size());
        Assert.assertEquals("val1", result.parts.get(0).value.value);
        Assert.assertEquals("val4", result.parts.get(1).value.value);
        Assert.assertEquals("foo-${aaa}-bar-val1-bla-val4-blupp-${bbb}", result.output);
    }
}
