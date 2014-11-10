/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmRestServiceBase extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AlarmRestServiceBase.class);


    protected static final Pattern m_severityPattern;

    static {
        final String severities = StringUtils.join(OnmsSeverity.names(), "|");
        m_severityPattern = Pattern.compile("\\s+(\\{alias\\}.)?severity\\s*(\\!\\=|\\<\\>|\\<\\=|\\>\\=|\\=|\\<|\\>)\\s*'?(" + severities + ")'?");
    }

    protected Criteria getCriteria(final MultivaluedMap<String,String> params, final boolean stripOrdering) {
    	final CriteriaBuilder cb = getCriteriaBuilder(params, stripOrdering);

    	final Criteria criteria = cb.toCriteria();
    	LOG.debug("criteria = {}", criteria);
		return criteria;
    }

	protected CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params, final boolean stripOrdering) {
		translateParameters(params);

    	final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);

    	cb.fetch("firstEvent", FetchType.EAGER);
        cb.fetch("lastEvent", FetchType.EAGER);
        
        cb.alias("node", "node", JoinType.LEFT_JOIN);
        cb.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        cb.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);

        if (params.containsKey("alarmId")) {
        	if (params.containsKey("id")) {
        		throw new IllegalArgumentException("Form parameters contain both the 'alarmId' and 'id' properties!  Pick a side!");
        	}
        	params.put("id", params.remove("alarmId"));
        }
    	applyQueryFilters(params, cb);
    	if (stripOrdering) {
    		cb.clearOrder();
    		cb.limit(DEFAULT_LIMIT);
    		cb.offset(0);
    	} else {
    	    cb.orderBy("lastEventTime").desc();
    	}
    	cb.distinct();

    	return cb;
	}

    protected void translateParameters(final MultivaluedMap<String, String> params) {
    	// this is handled by a @QueryParam annotation, ignore it from the UriInfo object
    	params.remove("severities");

    	if (params.containsKey("nodeId")) {
    		final String nodeId = params.getFirst("nodeId");
    		params.remove("nodeId");
    		params.add("node.id", nodeId);
    	}

    	if (params.containsKey("nodeLabel")) {
    		final String nodeLabel = params.getFirst("nodeLabel");
    		params.remove("nodeLabel");
    		params.add("node.label", nodeLabel);
    	}

    	final String query = params.getFirst("query");
        // System.err.println("tranlateSeverity: query = " + query + ", pattern = " + p);
        if (query != null) {
            final Matcher m = m_severityPattern.matcher(query);
            if (m.find()) {
                // System.err.println("translateSeverity: group(1) = '" + m.group(1) + "', group(2) = '" + m.group(2) + "', group(3) = '" + m.group(3) + "'");
                final String alias = m.group(1);
                final String comparator = m.group(2);
                final String severity = m.group(3);
                final OnmsSeverity onmsSeverity = OnmsSeverity.get(severity);
                // System.err.println("translateSeverity: " + severity + " = " + onmsSeverity);
                
                final String newQuery = m.replaceFirst(" " + (alias == null? "" : alias) + "severity " + comparator + " " + onmsSeverity.getId());
                params.remove("query");
                params.add("query", newQuery);
                // System.err.println("translateSeverity: newQuery = '" + newQuery + "'");
            } else {
                // System.err.println("translateSeverity: failed to find pattern");
            }
        }
    }

}
