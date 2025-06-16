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
package org.opennms.web.tags;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.opennms.core.time.CentralizedDateTimeFormat;

/**
 * This class replaces the &lt;fmt:formatDate /&gt; tag.
 * Why do we need a new tag?
 * => fmt can't be configured via a System Property (without side effects)
 * => we want to support the new java.time classes
 *
 * It will output datetimes as ISO_8601 type style unless otherwise defined in opennms.properties.
 * See also:
 *   https://en.wikipedia.org/wiki/ISO_8601 and
 *   https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 */
public class DateTimeTag extends SimpleTagSupport {

    private Instant instant;

    @Override
    public void doTag() throws IOException {
        // Output an empty string for null values. I believe fmt:formatDate does the same
        String output = Optional.ofNullable(new CentralizedDateTimeFormat().format(instant, getZoneId())).orElse("");
        getJspContext().getOut().write(output);
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Deprecated // please try to use the new Java Date API when possible: setInstant(Instant instant)
    public void setDate(Date date) {
        if (date != null) {
            this.instant = date.toInstant();
        }
    }

    private ZoneId getZoneId(){
        ZoneId timeZoneId = (ZoneId) this.getJspContext().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID, PageContext.SESSION_SCOPE);
        if(timeZoneId == null){
            timeZoneId = ZoneId.systemDefault();
        }
        return timeZoneId;
    }
}
