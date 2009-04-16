//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.filter.InterfaceFilter;
import org.opennms.web.notification.filter.NodeFilter;
import org.opennms.web.notification.filter.ResponderFilter;
import org.opennms.web.notification.filter.ServiceFilter;
import org.opennms.web.notification.filter.UserFilter;

public abstract class NoticeUtil extends Object {
    protected static final Map<String, SortStyle> sortStylesString;
    protected static final Map<SortStyle, String> sortStyles;

    protected static final Map<String, AcknowledgeType> ackTypesString;
    protected static final Map<AcknowledgeType, String> ackTypes;

    static {
        sortStylesString = new HashMap<String, SortStyle>();
        sortStylesString.put("user", SortStyle.USER);
        sortStylesString.put("responder", SortStyle.RESPONDER);
        sortStylesString.put("pagetime", SortStyle.PAGETIME);
        sortStylesString.put("respondtime", SortStyle.RESPONDTIME);
        sortStylesString.put("node", SortStyle.NODE);
        sortStylesString.put("interface", SortStyle.INTERFACE);
        sortStylesString.put("service", SortStyle.SERVICE);
        sortStylesString.put("id", SortStyle.ID);
        sortStylesString.put("rev_user", SortStyle.REVERSE_USER);
        sortStylesString.put("rev_responder", SortStyle.REVERSE_RESPONDER);
        sortStylesString.put("rev_pagetime", SortStyle.REVERSE_PAGETIME);
        sortStylesString.put("rev_respondtime", SortStyle.REVERSE_RESPONDTIME);
        sortStylesString.put("rev_node", SortStyle.REVERSE_NODE);
        sortStylesString.put("rev_interface", SortStyle.REVERSE_INTERFACE);
        sortStylesString.put("rev_service", SortStyle.REVERSE_SERVICE);
        sortStylesString.put("rev_id", SortStyle.REVERSE_ID);
        
        sortStyles = new HashMap<SortStyle, String>();
        sortStyles.put(SortStyle.USER, "user");
        sortStyles.put(SortStyle.RESPONDER, "responder");
        sortStyles.put(SortStyle.PAGETIME, "pagetime");
        sortStyles.put(SortStyle.RESPONDTIME, "respondtime");
        sortStyles.put(SortStyle.NODE, "node");
        sortStyles.put(SortStyle.INTERFACE, "interface");
        sortStyles.put(SortStyle.SERVICE, "service");
        sortStyles.put(SortStyle.ID, "id");
        sortStyles.put(SortStyle.REVERSE_USER, "rev_user");
        sortStyles.put(SortStyle.REVERSE_RESPONDER, "rev_responder");
        sortStyles.put(SortStyle.REVERSE_PAGETIME, "rev_pagetime");
        sortStyles.put(SortStyle.REVERSE_RESPONDTIME, "rev_respondtime");
        sortStyles.put(SortStyle.REVERSE_NODE, "rev_node");
        sortStyles.put(SortStyle.REVERSE_INTERFACE, "rev_interface");
        sortStyles.put(SortStyle.REVERSE_SERVICE, "rev_service");
        sortStyles.put(SortStyle.REVERSE_ID, "rev_id");

        ackTypesString = new HashMap<String, AcknowledgeType>();
        ackTypesString.put("ack", AcknowledgeType.ACKNOWLEDGED);
        ackTypesString.put("unack", AcknowledgeType.UNACKNOWLEDGED);

        ackTypes = new HashMap<AcknowledgeType, String>();
        ackTypes.put(AcknowledgeType.ACKNOWLEDGED, "ack");
        ackTypes.put(AcknowledgeType.UNACKNOWLEDGED, "unack");
    }

    public static org.opennms.web.filter.Filter getFilter(String filterString) {
        Filter filter = null;

        StringTokenizer tokens = new StringTokenizer(filterString, "=");
        String type = tokens.nextToken();
        String value = tokens.nextToken();

        if (type.equals(UserFilter.TYPE)) {
            filter = new UserFilter(value);
        } else if (type.equals(ResponderFilter.TYPE)) {
            filter = new ResponderFilter(value);
        } else if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
        } else if (type.equals(ServiceFilter.TYPE)) {
            filter = new ServiceFilter(WebSecurityUtils.safeParseInt(value));
        }

        return filter;
    }

}
