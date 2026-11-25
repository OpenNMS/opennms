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
package org.opennms.web.rest.v2.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The body of {@code GET /nodes/{nodeCriteria}/notificationStatus}.
 */
@Schema(name = "NodeNotificationStatus", description = "How many notifications one node has: the caller's "
        + "outstanding ones, everyone's outstanding ones, and all of them. Counted on the server.")
public class NodeNotificationStatusDto {

    @Schema(description = "The user that `yourOutstanding` counts for: the caller.", example = "admin")
    private String user;

    @Schema(description = "Outstanding notifications sent to the caller. A notification sent to the caller by "
            + "more than one method is counted once.", example = "2")
    private long yourOutstanding;

    @Schema(description = "Outstanding notifications, whoever they were sent to.", example = "5")
    private long allOutstanding;

    @Schema(description = "All notifications, outstanding and acknowledged.", example = "40")
    private long total;

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public long getYourOutstanding() {
        return yourOutstanding;
    }

    public void setYourOutstanding(final long yourOutstanding) {
        this.yourOutstanding = yourOutstanding;
    }

    public long getAllOutstanding() {
        return allOutstanding;
    }

    public void setAllOutstanding(final long allOutstanding) {
        this.allOutstanding = allOutstanding;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(final long total) {
        this.total = total;
    }
}
