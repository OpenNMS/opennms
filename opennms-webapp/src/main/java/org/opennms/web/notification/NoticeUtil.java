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
package org.opennms.web.notification;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.filter.AcknowledgedByFilter;
import org.opennms.web.notification.filter.InterfaceFilter;
import org.opennms.web.notification.filter.LocationFilter;
import org.opennms.web.notification.filter.NegativeLocationFilter;
import org.opennms.web.notification.filter.NegativeNodeFilter;
import org.opennms.web.notification.filter.NegativeNodeLocationFilter;
import org.opennms.web.notification.filter.NodeFilter;
import org.opennms.web.notification.filter.NodeLocationFilter;
import org.opennms.web.notification.filter.NotificationIdFilter;
import org.opennms.web.notification.filter.ResponderFilter;
import org.opennms.web.notification.filter.ServiceFilter;
import org.opennms.web.notification.filter.SeverityFilter;
import org.opennms.web.notification.filter.UserFilter;

/**
 * <p>Abstract NoticeUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class NoticeUtil extends Object {

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a org$opennms$web$filter$Filter object.
     */
    public static org.opennms.web.filter.Filter getFilter(String filterString, ServletContext servletContext) {
        Filter filter = null;

        StringTokenizer tokens = new StringTokenizer(filterString, "=");
        String type = tokens.nextToken();
        String value;
        try {
            value = tokens.nextToken();
        } catch (NoSuchElementException e) {
            // No value was specified, return null for this filter
            return null;
        }

        if (type.equals(AcknowledgedByFilter.TYPE)) {
            filter = new AcknowledgedByFilter(value);
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value));
        } else if(type.equals(NegativeNodeFilter.TYPE)) {
        	filter = new NegativeNodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NotificationIdFilter.TYPE)) {
            filter = new NotificationIdFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(ResponderFilter.TYPE)) {
            filter = new ResponderFilter(value);
        } else if (type.equals(ServiceFilter.TYPE)) {
            filter = new ServiceFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(UserFilter.TYPE)) {
            filter = new UserFilter(value);
        } else if (type.equals(SeverityFilter.TYPE)) {
            filter = new SeverityFilter(OnmsSeverity.get(value));
        } else if (type.equals(LocationFilter.TYPE)) {
            filter = new LocationFilter(WebSecurityUtils.sanitizeString(value));
        } else if (type.equals(NegativeLocationFilter.TYPE)) {
            filter = new NegativeLocationFilter(WebSecurityUtils.sanitizeString(value));
        } else if (type.equals(NodeLocationFilter.TYPE)) {
            filter = new NodeLocationFilter(WebSecurityUtils.sanitizeString(value));
        } else if (type.equals(NegativeNodeLocationFilter.TYPE)) {
            filter = new NegativeNodeLocationFilter(WebSecurityUtils.sanitizeString(value));
        }

        return filter;
    }

}
