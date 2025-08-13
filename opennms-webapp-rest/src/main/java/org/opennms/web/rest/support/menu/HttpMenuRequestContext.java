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

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.core.time.ExtendedDateOnlyFormat;
import org.opennms.core.time.ExtendedTimeOnlyFormat;
import org.opennms.netmgt.config.NotifdConfigFactory;

public class HttpMenuRequestContext implements MenuRequestContext {
    final private HttpServletRequest request;
    final private CentralizedDateTimeFormat dateTimeFormatter = new CentralizedDateTimeFormat();

    final private ExtendedDateOnlyFormat dateOnlyFormatter = new ExtendedDateOnlyFormat();

    final private ExtendedTimeOnlyFormat timeOnlyFormatter = new ExtendedTimeOnlyFormat();

    public HttpMenuRequestContext(HttpServletRequest request) {
        this.request = request;
    }

    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    public String calculateUrlBase() {
        return org.opennms.web.api.Util.calculateUrlBase(this.request);
    }

    public boolean isUserInRole(final String role) {
        return this.request.isUserInRole(role);
    }

    public boolean isUserInAnyRole(List<String> roles) {
       return roles.stream().anyMatch(this.request::isUserInRole);
    }

    public String getFormattedDateTime() {
        return this.dateTimeFormatter.format(Instant.now(), extractUserTimeZone());
    }

    public String getFormattedDate() {
        return this.dateOnlyFormatter.format(Instant.now(), extractUserTimeZone());
    }

    public String getFormattedTime() {
        return this.timeOnlyFormatter.format(Instant.now(), extractUserTimeZone());
    }

    public String getNoticeStatus() {
        try {
            return NotifdConfigFactory.getPrettyStatus();
        } catch (final Throwable ignored) {
        }

        return "Unknown";
    }

    public String getSystemProperty(String name, String def) {
        return System.getProperty(name, def);
    }

    private ZoneId extractUserTimeZone() {
        ZoneId timeZoneId = (ZoneId) this.request.getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);

        if (timeZoneId == null) {
            timeZoneId = ZoneId.systemDefault();
        }

        return timeZoneId;
    }
}
