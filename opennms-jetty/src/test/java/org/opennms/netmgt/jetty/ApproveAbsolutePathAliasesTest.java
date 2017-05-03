/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
