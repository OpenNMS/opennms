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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.poller.PollerParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.common.collect.Maps;

public class RpcMetaDataUtilsTest {
    final Map<ContextKey, String> metaData = new HashMap<>();

    @Before
    public void setUp() {
        metaData.put(new ContextKey("ctx1", "key1"), "val1");
        metaData.put(new ContextKey("ctx1", "key2"), "val2");
        metaData.put(new ContextKey("ctx2", "key3"), "val3");
        metaData.put(new ContextKey("ctx2", "key4"), "val4");
        metaData.put(new ContextKey("ctx3", "key5"), "12345");
    }

    @Test
    public void testMetaDataInterpolation() {
        final Map<String, String> attributes = new TreeMap<>();

        attributes.put("attribute1", "aaa${ctx1:key1|ctx2:key2|default}bbb");
        attributes.put("attribute2", "aaa${ctx1:key3|ctx2:key3|default}bbb");
        attributes.put("attribute3", "aaa${ctx1:key4|ctx2:key1|default}bbbaaa${ctx1:key4|ctx2:key4|default}bbb");
        attributes.put("attribute4", "aaa${ctx1:key4}bbb");
        attributes.put("attribute5", "aaa${ctx1:key4|}bbb");
        attributes.put("attribute6", "aaa${ctx1:key4|default}bbb");
        attributes.put("attribute7", "aaa${ctx1:key4|${nodeLabel}}bbb");
        attributes.put("attribute8", "aaa${abc}bbb");

        final Map<String, String> interpolatedAttributes = Maps.transformValues(attributes, value -> Interpolator.interpolate(value, new MapScope(this.metaData)));

        Assert.assertEquals(attributes.size(), interpolatedAttributes.size());
        Assert.assertEquals("aaaval1bbb", interpolatedAttributes.get("attribute1"));
        Assert.assertEquals("aaaval3bbb", interpolatedAttributes.get("attribute2"));
        Assert.assertEquals("aaadefaultbbbaaaval4bbb", interpolatedAttributes.get("attribute3"));
        Assert.assertEquals("aaabbb", interpolatedAttributes.get("attribute4"));
        Assert.assertEquals("aaabbb", interpolatedAttributes.get("attribute5"));
        Assert.assertEquals("aaadefaultbbb", interpolatedAttributes.get("attribute6"));
        Assert.assertEquals("aaa${nodeLabel}bbb", interpolatedAttributes.get("attribute7"));
        Assert.assertEquals("aaa${abc}bbb", interpolatedAttributes.get("attribute8"));
    }

    @Test
    public void testComplexPollerParameterInterpolation() throws Exception {
        // TODO fooker: This is not working right now...
        final Element element = JAXB.unmarshal("" +
                "<page-sequence>" +
                "</page-sequence>" +
                "", Element.class);

        final PollerParameter interpolated = Interpolator.interpolate(PollerParameter.complex(element), new MapScope(this.metaData));
        final PageSequence pageSequence = interpolated.asComplex().get().getInstance(PageSequence.class);

        assertThat(pageSequence.getPages(), hasSize(1));
        assertThat(pageSequence.getPages().get(0).getVirtualHost(), is("chaos.val1.example.com"));
        assertThat(pageSequence.getPages().get(0).getPort(), is(12345));
    }
}
