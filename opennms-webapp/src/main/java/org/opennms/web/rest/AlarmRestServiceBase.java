package org.opennms.web.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;

public class AlarmRestServiceBase extends OnmsRestService {

    protected static final Pattern m_severityPattern;

    static {
        final String severities = StringUtils.join(OnmsSeverity.names(), "|");
        m_severityPattern = Pattern.compile("\\s+(\\{alias\\}.)?severity\\s*(\\!\\=|\\<\\>|\\<\\=|\\>\\=|\\=|\\<|\\>)\\s*'?(" + severities + ")'?");
    }

    protected OnmsCriteria getQueryFilters(final MultivaluedMap<String,String> params) {
        return getQueryFilters(params, false);
    }

    protected OnmsCriteria getQueryFilters(final MultivaluedMap<String,String> params, boolean stripOrdering) {
        translateSeverity(params);

        final OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);

        if (!stripOrdering) {
            setLimitOffset(params, criteria, DEFAULT_LIMIT, false);
            addOrdering(params, criteria, false);
            // Set default ordering
            addOrdering(
                new MultivaluedMapImpl(
                    new String[][] { 
                        new String[] { "orderBy", "lastEventTime" }, 
                        new String[] { "order", "desc" } 
                    }
                ), criteria, false
            );
        }

        addFiltersToCriteria(params, criteria, OnmsAlarm.class);


        criteria.setFetchMode("firstEvent", FetchMode.JOIN);
        criteria.setFetchMode("lastEvent", FetchMode.JOIN);
        
        criteria.createAlias("node", "node", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);

        return getDistinctIdCriteria(OnmsAlarm.class, criteria);
    }

    protected void translateSeverity(final MultivaluedMap<String, String> params) {
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
