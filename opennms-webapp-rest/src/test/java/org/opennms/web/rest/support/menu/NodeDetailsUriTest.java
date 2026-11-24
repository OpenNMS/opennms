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
package org.opennms.web.rest.support.menu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.dao.support.NodeResourceType;
import org.opennms.web.api.ElementConstants;

/**
 * opennms-dao cannot depend on opennms-web-api, so the node details URI is defined in both
 * places. This module sees both, so it checks they stay equal.
 */
public class NodeDetailsUriTest {

    @Test
    public void resourceTypeUriMatchesElementConstants() {
        assertEquals("NodeResourceType.NODE_DETAILS_URI differs from ElementConstants.NODE_DETAILS_URI; "
                + "node resource links would break",
                ElementConstants.NODE_DETAILS_URI, NodeResourceType.NODE_DETAILS_URI);
    }
}
