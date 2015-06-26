package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.bean.BeanInvocation;
import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Created:
 * User: unicoletti
 * Date: 11:11 AM 6/24/15
 */
public class ESHeaders {
    Logger logger = LoggerFactory.getLogger(ESHeaders.class);

    private volatile NodeDao nodeDao;
    private volatile TransactionOperations transactionOperations;
    private boolean logEventDescription;

    IndexNameFunction idxName = new IndexNameFunction();

    String remainder="opennms";

    public void process(Exchange exchange) {
        Message in = exchange.getIn();
        String indexName=null;
        Map body=new HashMap<String,Object>();
        Event event;

        try {
            Object incoming=in.getBody();

            if(incoming instanceof BeanInvocation) {

                event = (Event) ((BeanInvocation)incoming).getArgs()[0];

                body.put("@timestamp", event.getCreationTime());
                Calendar cal=Calendar.getInstance();
                cal.setTime(event.getCreationTime());
                body.put("dow", cal.get(cal.DAY_OF_WEEK));
                body.put("hour", cal.get(cal.HOUR_OF_DAY));
                body.put("dom", cal.get(cal.DAY_OF_MONTH));
                body.put("uei",event.getUei());
                body.put("id",event.getDbid());
                body.put("eventseverity_text", event.getSeverity());
                body.put("severity", OnmsSeverity.get(event.getSeverity()).getId());

                body.put("service", event.getService());
                body.put("ipaddr", event.getInterfaceAddress()!=null ? event.getInterfaceAddress().toString() : null );
                if(isLogEventDescription()) {
                    body.put("description", event.getDescr());
                }
                body.put("nodeid", event.getNodeid());
                body.put("host",event.getHost());
                StringBuffer params=new StringBuffer();
                for(Parm parm : event.getParmCollection()) {
                    params.append(parm.getParmName()+"="+parm.getValue().getContent()+" ");
                }
                body.put("eventparms", params.toString());
                body.put("source", event.getSource());
                body.put("interface", event.getInterface());
                body.put("logmsg", ( event.getLogmsg()!=null ? event.getLogmsg().getContent() : null ));
                body.put("logmsgdest", ( event.getLogmsg()!=null ? event.getLogmsg().getDest() : null ));

                if(event.getNodeid()!=null) {
                    OnmsNode node = nodeDao.get(event.getNodeid().toString());
                    if (node != null) {
                        body.put("nodelbl", node.getLabel());
                        body.put("nodesysname", node.getSysName());
                        body.put("nodesyslocation", node.getSysLocation());
                        body.put("foreignsource", node.getForeignSource());
                        body.put("operatingsystem", node.getOperatingSystem());
                        StringBuffer categories=new StringBuffer();
                        try {
                            transactionOperations.execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                    OnmsNode node = nodeDao.get(event.getNodeid().toString());
                                    for (OnmsCategory cat : node.getCategories()) {
                                        categories.append(cat.getName() + " ");
                                    }
                                }
                            });
                        } catch(Exception e) {
                            logger.error("error fetching node categories: ", e);
                        }
                        body.put("categories", categories.toString());
                    }
                }
            } else if(incoming instanceof Map) {
                body = (Map) incoming;
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
        // attention: this will probably log a LOT of lines!
        if(body!=null) {
            logger.trace("Computing indexName from @timestamp: "+body.get("@timestamp")+" yelds "+indexName);
        }
        in.setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME, indexName);
        exchange.getOut().setBody(body);
    }

    public String getRemainder() {
        return remainder;
    }

    public void setRemainder(String remainder) {
        this.remainder = remainder;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public TransactionOperations getTransactionOperations() {
        return transactionOperations;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    public boolean isLogEventDescription() {
        return logEventDescription;
    }

    public void setLogEventDescription(boolean logEventDescription) {
        this.logEventDescription = logEventDescription;
    }
}
