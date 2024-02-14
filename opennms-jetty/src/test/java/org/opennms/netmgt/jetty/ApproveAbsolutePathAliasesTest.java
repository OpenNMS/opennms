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
package org.opennms.netmgt.jetty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.util.resource.PathResource;
import org.junit.Test;

public class ApproveAbsolutePathAliasesTest {

    @Test
    public void canApproveDoubleSlash() throws URISyntaxException, IOException {
        ApproveAbsolutePathAliases aliasCheck = new ApproveAbsolutePathAliases();

        // If the alias and file only differ by a double slash, it should be approved
        String path = "/WEB-INF/jsp//support/index.jsp";
        String uri = "file:///opt/opennms/jetty-webapps/opennms/WEB-INF/jsp//support/index.jsp";
        assertThat(aliasCheck.check(path, new PathResource(new URI(uri)) {
            @Override
            public URI getAlias() {
                try {
                    return new URI("file:///opt/opennms/jetty-webapps/opennms/WEB-INF/jsp/support/index.jsp");
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }), equalTo(true));

        // Other differences should not be approved
        uri = "file:///opt/opennms/jetty-webapps/opennms/WEB-INF/jsp/support/index.jsp";
        assertThat(aliasCheck.check(path, new PathResource(new URI(uri)) {
            @Override
            public URI getAlias() {
                try {
                    return new URI("file:///opt/opennms/jetty-webapps/opennms/WEB-INF/jsp/.support/index.jsp");
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }), equalTo(false));
    }
}
