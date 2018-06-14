/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.servicenow;

public class ServiceNowConstants {

    public static final String INCIDENT_NUMBER = "number";
    
    public static final String INCIDENT_ID = "sys_id";
    
    public static final String CALLER_ID = "caller_id";
    
    public static final String SHORT_DESCRIPTION = "short_description";
    
    public static final String DESCRIPTION = "description";

    public static final String STATE = "state";

    public static final String CATEGORY = "category";

    public static final String CLOSE_NOTES = "close_notes";

    public static final String CLOSE_CODE = "close_code";
    
    
    
    
    public static final String CFG_URL = "servicenow.url";
    
    public static final String CFG_USERNAME = "servicenow.username";
    
    public static final String CFG_PASSWORD = "servicenow.password";
    
    public static final String CFG_CALLER_ID = "servicenow.caller_id";
    
    public static final String CFG_CATEGORY = "servicenow.category";
    
    public static final String CFG_CATEGORY_DEFAULT = "network";
    
    public static final String CFG_STATE_OPEN = "servicenow.state.open";
    
    public static final String CFG_STATE_OPEN_DEFAULT = "1";
    
    public static final String CFG_STATE_CLOSE = "servicenow.state.close";
    
    public static final String CFG_STATE_CLOSE_DEFAULT = "6";
    
    public static final String CFG_CLOSE_NOTES = "servicenow.close_notes";
    
    public static final String CFG_CLOSE_NOTES_DEFAULT = "Automatically resolved by OpenNMS ticket integration";
    
    public static final String CFG_CLOSE_CODE = "servicenow.close_code";
    
    public static final String CFG_CLOSE_CODE_DEFAULT = "Resolved by OpenNMS integration";
    
    public static final String CFG_SSL_STRICT = "servicenow.ssl.strict";
    
    public static final String CFG_SSL_STRICT_DEFAULT = "true";
    
    public static final String CFG_CONNECTION_TIMEOUT = "servicenow.connect.timeout";
    
    public static final String CFG_CONNECTION_TIMEOUT_DEFAULT = "3000";
}
