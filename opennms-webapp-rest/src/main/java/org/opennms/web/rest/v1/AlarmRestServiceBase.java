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
package org.opennms.web.rest.v1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

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
        cb.alias("node.location", "location", JoinType.LEFT_JOIN);

        if (params.containsKey("alarmId")) {
        	if (params.containsKey("id")) {
        	    throw getException(Status.BAD_REQUEST, "Form parameters contain both the 'alarmId' and 'id' properties!  Pick a side!");
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
