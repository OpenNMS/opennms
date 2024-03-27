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
package org.opennms.features.jest.client.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
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
