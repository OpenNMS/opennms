package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.bean.BeanInvocation;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    Logger logger = LoggerFactory.getLogger(ESHeaders.class);

    private boolean logEventDescription=false;
    private NodeCache cache;

    IndexNameFunction idxName = new IndexNameFunction();

    String remainder="opennms";

    public void process(Exchange exchange) {
        Message in = exchange.getIn();
        String indexName=null;
        final Map body=new HashMap();
        Event event;

        try {
            Object incoming=in.getBody();

            if(incoming instanceof BeanInvocation) {

                event = (Event) ((BeanInvocation)incoming).getArgs()[0];

                populateBodyFromEvent(body, event);

                if(event.getNodeid()!=null) {
                    try {
                        // if the event is a uei.opennms.org/nodes/*updated,changed,deleted then force a refresh
                        maybeRefreshCache(event);

                        // will cache on first access
                        body.putAll(cache.getEntry(event.getNodeid()));

                    } catch(Exception e) {
                        logger.error("error fetching nodeData categories: ", e);
                    }
                }
            } else if(incoming instanceof Map) {
                body.putAll((Map) incoming);
            }

            if(body.containsKey("@timestamp")) {
                logger.trace("Computing indexName from @timestamp: "+body.get("@timestamp"));
                indexName=idxName.apply(remainder, (Date) body.get("@timestamp"));
            } else {
                indexName = idxName.apply(remainder);
            }
        } catch(Exception e) {
            logger.error("Cannot compute index name, failing back to default: "+e.getMessage());
            indexName = idxName.apply(remainder);
        }
        logger.trace("Computing indexName from @timestamp: "+body.get("@timestamp")+" yelds "+indexName);
        in.setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME, indexName);
        exchange.getOut().setBody(body);
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
    private void populateBodyFromEvent(Map body, Event event) {
        body.put("id",event.getDbid());
        body.put("eventuei",event.getUei());
        body.put("@timestamp", event.getCreationTime());
        Calendar cal=Calendar.getInstance();
        cal.setTime(event.getCreationTime());
        body.put("dow", cal.get(Calendar.DAY_OF_WEEK));
        body.put("hour", cal.get(Calendar.HOUR_OF_DAY));
        body.put("dom", cal.get(Calendar.DAY_OF_MONTH)); // this is not present in the original sql-based tool https://github.com/unicolet/opennms-events/blob/master/sql/opennms_events.sql#L26
        body.put("eventsource", event.getSource());
        body.put("ipaddr", event.getInterfaceAddress()!=null ? event.getInterfaceAddress().toString() : null );
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
