package org.opennms.web.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

public class AlarmRestServiceBase extends OnmsRestService {

    protected static final Pattern m_severityPattern;

    static {
        final String severities = StringUtils.join(OnmsSeverity.names(), "|");
        m_severityPattern = Pattern.compile("\\s+(\\{alias\\}.)?severity\\s*(\\!\\=|\\<\\>|\\<\\=|\\>\\=|\\=|\\<|\\>)\\s*'?(" + severities + ")'?");
    }

    protected Criteria getCriteria(final MultivaluedMap<String,String> params, final boolean stripOrdering) {
    	final CriteriaBuilder cb = getCriteriaBuilder(params, stripOrdering);

    	final Criteria criteria = cb.toCriteria();
    	LogUtils.debugf(this, "criteria = %s", criteria);
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
