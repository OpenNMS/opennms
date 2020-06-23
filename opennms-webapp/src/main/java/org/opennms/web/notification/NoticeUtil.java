/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
