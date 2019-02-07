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

package org.opennms.core.rpc.utils.pattern;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TemplateTest {
    @Test
    public void testEmpty() {
        final Template template = Template.parse("");

        assertThat(template.getVariables(), is(empty()));
        assertThat(template.match("").isPresent(), is(true));
        assertThat(template.match("asd").isPresent(), is(false));
    }

    @Test
    public void testStatic() {
        final Template template = Template.parse("HTTP");

        assertThat(template.getVariables(), is(empty()));
        assertThat(template.match("HTTP").isPresent(), is(true));
        assertThat(template.match("HTTPS").isPresent(), is(false));
    }

    @Test
    public void testSimple() {
        final Template template = Template.parse("HTTP-{vhost}-");

        assertThat(template.getVariables(), contains("vhost"));
        assertThat(template.match("").isPresent(), is(false));
        assertThat(template.match("asd").isPresent(), is(false));
        assertThat(template.match("HTTP-www.example.com-").get(), hasEntry(is("vhost"), is("www.example.com")));
    }

    @Test
    public void testMultiple() {
        final Template template = Template.parse("HTTP-{vhost}-{port}-");

        assertThat(template.getVariables(), contains("vhost", "port"));
        assertThat(template.match("").isPresent(), is(false));
        assertThat(template.match("asd").isPresent(), is(false));
        assertThat(template.match("HTTP-www.example.com-8080-").get(), allOf(
                hasEntry(is("vhost"), is("www.example.com")),
                hasEntry(is("port"), is("8080"))
        ));
    }

    @Test
    public void testComplex() {
        final Template template = Template.parse("HTTP-{vhost}-{port:[0-9]+}-");

        assertThat(template.getVariables(), contains("vhost", "port"));
        assertThat(template.match("").isPresent(), is(false));
        assertThat(template.match("asd").isPresent(), is(false));
        assertThat(template.match("HTTP-www.example.com-asd-").isPresent(), is(false));
        assertThat(template.match("HTTP-www.example.com-8080-").get(), allOf(
                hasEntry(is("vhost"), is("www.example.com")),
                hasEntry(is("port"), is("8080"))
        ));
    }
}
