package org.opennms.features.elasticsearch.eventforwarder.internal;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.bean.BeanInvocation;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.opennms.core.camel.IndexNameFunction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bean is camel processor that translates the incoming OpenNMS event into a HashMap
 * that Elasticsearch can understand.
 *
 * Note that Elasticsearch will not infer types from the incoming document, but it needs to be taught
 * about each field type by using a mapping template.
 *
 * @see org.opennms.features.elasticsearch.eventforwarder.internal.ElMappingLoader
 *
 * Created:
 * User: unicoletti (at DevJam2015)
 * Date: 11:11 AM 6/24/15
 */
public class ESHeaders {
    private static final Logger LOG = LoggerFactory.getLogger(ESHeaders.class);

    private boolean logEventDescription = false;
    private NodeCache cache;

    private IndexNameFunction idxName = new IndexNameFunction();

    private String remainder = "opennms";

    public void process(Exchange exchange) {
        Message in = exchange.getIn();
        String indexName=null;
        String indexType=null;
        final Map<String,Object> body=new HashMap<>();

        try {
            Object incoming=in.getBody();

            if(incoming instanceof BeanInvocation) {
                Object argument=((BeanInvocation)incoming).getArgs()[0];

                if(argument instanceof Event) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Processing event");
                    }

                    indexType="events";

                    Event event = (Event) argument;

                    populateBodyFromEvent(body, event);

                    if (event.getNodeid() != null) {
                        try {
                            // if the event is a uei.opennms.org/nodes/*updated,changed,deleted then force a refresh
                            maybeRefreshCache(event);

                            // will cache on first access
                            body.putAll(cache.getEntry(event.getNodeid()));

                        } catch (Exception e) {
                            LOG.error("error fetching nodeData categories: ", e);
                        }
                    }
                } else if(argument instanceof NorthboundAlarm) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Processing alarm");
                    }

                    indexType="alarms";

                    NorthboundAlarm alarm = (NorthboundAlarm) argument;

                    populateBodyFromAlarm(body, alarm);

                    if (alarm.getNodeId() != null) {
                        try {

                            // will cache on first access
                            body.putAll(cache.getEntry((long) alarm.getNodeId()));

                        } catch (Exception e) {
                            LOG.error("error fetching nodeData categories: ", e);
                        }
                    }
                }
            } else if(incoming instanceof Map) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Processing a generic map");
                }

                body.putAll((Map) incoming);
            }

            if(body.containsKey("@timestamp")) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Computing indexName from @timestamp: "+body.get("@timestamp"));
                }
                indexName=idxName.apply(remainder, (Date) body.get("@timestamp"));
            } else {
                indexName = idxName.apply(remainder);
            }
        } catch(Exception e) {
            LOG.error("Cannot compute index name, failing back to default: "+e.getMessage());
            indexName = idxName.apply(remainder);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Computing indexName from @timestamp: " + body.get("@timestamp") + " yields " + indexName);
        }
        exchange.getOut().setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME, indexName);
        exchange.getOut().setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE, indexType);
        exchange.getOut().setBody(body);
    }

    private void populateBodyFromAlarm(Map<String,Object> body, NorthboundAlarm alarm) {
        body.put("id",alarm.getId());
        body.put("eventuei", alarm.getUei());
        body.put("@timestamp", alarm.getLastOccurrence());
        body.put("count", alarm.getCount());
        Calendar cal=Calendar.getInstance();
        cal.setTime(alarm.getLastOccurrence());
        body.put("dow", cal.get(Calendar.DAY_OF_WEEK));
        body.put("hour", cal.get(Calendar.HOUR_OF_DAY));
        body.put("dom", cal.get(Calendar.DAY_OF_MONTH)); // this is not present in the original sql-based tool https://github.com/unicolet/opennms-events/blob/master/sql/opennms_events.sql#L26
        body.put("poller", alarm.getPoller());
        body.put("ipaddr", alarm.getIpAddr()!=null ? alarm.getIpAddr() : null );
        body.put("servicename", alarm.getService());
        body.put("eventseverity_text", alarm.getSeverity().getLabel());
        body.put("eventseverity", alarm.getSeverity().getId());

        body.put("nodeid", alarm.getNodeId());
        body.put("ackuser",alarm.getAckUser());
        body.put("acktime",alarm.getAckTime());
        body.put("appdn", alarm.getAppDn());
        body.put("suppressedby",alarm.getSuppressedBy());
        body.put("suppressed",alarm.getSuppressed());
    }

    private void maybeRefreshCache(Event event) {
        String uei=event.getUei();
        if(uei!=null && uei.startsWith("uei.opennms.org/nodes/")) {
            if (
                    uei.endsWith("Added")
                    || uei.endsWith("Deleted")
                    || uei.endsWith("Updated")
                    || uei.endsWith("Changed")
                    ) {
                cache.refreshEntry(event.getNodeid());
            }
        }
    }

    /**
     * utility method to populate a Map with the most import event attributes
     *
     * @param body the map
     * @param event the event object
     */
    private void populateBodyFromEvent(Map<String,Object> body, Event event) {
        body.put("id",event.getDbid());
        body.put("eventuei",event.getUei());
        Date eventTime = event.getTime();
        body.put("@timestamp", eventTime);
        Calendar cal=Calendar.getInstance();
        cal.setTime(eventTime);
        body.put("dow", cal.get(Calendar.DAY_OF_WEEK));
        body.put("hour", cal.get(Calendar.HOUR_OF_DAY));
        body.put("dom", cal.get(Calendar.DAY_OF_MONTH)); // this is not present in the original sql-based tool https://github.com/unicolet/opennms-events/blob/master/sql/opennms_events.sql#L26
        body.put("eventsource", event.getSource());
        body.put("ipaddr", event.getInterfaceAddress()!=null ? InetAddressUtils.str(event.getInterfaceAddress()) : null );
        body.put("servicename", event.getService());
        // params are exported as attributes, see below
        body.put("eventseverity_text", event.getSeverity());
        body.put("eventseverity", OnmsSeverity.get(event.getSeverity()).getId());

        if(isLogEventDescription()) {
            body.put("eventdescr", event.getDescr());
        }
        body.put("nodeid", event.getNodeid());
        body.put("host",event.getHost());
        for(Parm parm : event.getParmCollection()) {
            body.put("p_" + parm.getParmName(), parm.getValue().getContent());
        }
        body.put("interface", event.getInterface());
        body.put("logmsg", ( event.getLogmsg()!=null ? event.getLogmsg().getContent() : null ));
        body.put("logmsgdest", ( event.getLogmsg()!=null ? event.getLogmsg().getDest() : null ));
    }

    // getters and setters
    public String getRemainder() {
        return remainder;
    }

    public void setRemainder(String remainder) {
        this.remainder = remainder;
    }

    public boolean isLogEventDescription() {
        return logEventDescription;
    }

    public void setLogEventDescription(boolean logEventDescription) {
        this.logEventDescription = logEventDescription;
    }

    public void setLogEventDescription(String logEventDescription) {
        this.logEventDescription = Boolean.parseBoolean(logEventDescription);
    }

    public NodeCache getCache() {
        return cache;
    }

    public void setCache(NodeCache cache) {
        this.cache = cache;
    }
}
