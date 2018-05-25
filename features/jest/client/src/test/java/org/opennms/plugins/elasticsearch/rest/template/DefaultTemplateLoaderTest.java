/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;

public class DefaultTemplateLoaderTest {

    @Test
    public void canLoadTemplateForVersion() throws IOException {
        DefaultTemplateLoader loader = mock(DefaultTemplateLoader.class);
        when(loader.load(any(), any())).thenCallRealMethod();
        when(loader.getResource("/template.es6.json")).thenReturn("ES6!");
        when(loader.getResource("/template.json")).thenReturn("ES!");

        // ES 6 template
        String template = loader.load(new Version(6,2,3), "/template");
        assertThat(template, equalTo("ES6!"));

        // Fallback to next major when no specific match is made
        template = loader.load(new Version(7,1,0), "/template");
        assertThat(template, equalTo("ES6!"));

        // Use the default otherwise
        template = loader.load(new Version(2,1,1), "/template");
        assertThat(template, equalTo("ES!"));
    }

    @Test(expected = NullPointerException.class)
    public void failsWithNPEIfNoMatchIsMade() throws IOException {
        DefaultTemplateLoader loader = new DefaultTemplateLoader();
        loader.load(new Version(6,2,3), "/non-existent-template");
    }
}
