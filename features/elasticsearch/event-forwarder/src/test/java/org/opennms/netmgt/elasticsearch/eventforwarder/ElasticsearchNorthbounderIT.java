/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.elasticsearch.eventforwarder;

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.util.KeyValueHolder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.camel.IndexNameFunction;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.test.elasticsearch.JUnitElasticsearchServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.jayway.awaitility.core.ConditionTimeoutException;

@Ignore("Flapping. Camel context does consistently startup in time.")
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
public class ElasticsearchNorthbounderIT extends CamelBlueprintTest {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchNorthbounderIT.class);

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @ClassRule
    public static final JUnitElasticsearchServer ELASTICSEARCH = new JUnitElasticsearchServer();

    /**
     */
    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup( Map<String, KeyValueHolder<Object, Dictionary>> services ) {
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "blueprint-empty-camel-context.xml";
        //return "file:src/main/resources/OSGI-INF/blueprint/blueprint-event-forwarder.xml";
    }

    @Test(timeout=120000)
    public void testReceiveElasticsearchEvent() throws Exception {

        // Make sure that only the single Elasticsearch event listener is registered
        with().pollInterval(1, SECONDS).await().atMost(60, SECONDS).until(() -> m_eventIpcManager.getEventListenerCount(), equalTo(1));

        // Do a very pendantic check to make sure that the Camel context has started up.
        try {
            with().pollInterval(1, SECONDS).await().atMost(60, SECONDS).until(() -> {
                // Get all Camel contexts
                ServiceReference<?>[] references = getBundleContext().getAllServiceReferences(CamelContext.class.getName(), null);
                for (ServiceReference<?> reference : references) {
                    CamelContext context = (CamelContext)getBundleContext().getService(reference);
                    if (
                         // If the context has started and contains the endpoints from
                         // blueprint-event-forwarder.xml, then we've found the correct
                         // context so return true.
                         context.getStatus().isStarted() && 
                         context.hasEndpoint("seda:elasticsearchForwardEvent?concurrentConsumers=4&size=1000") != null &&
                         context.hasEndpoint("seda:elasticsearchForwardAlarm?concurrentConsumers=4&size=1000") != null &&
                         context.hasEndpoint("seda:ES_PRE?concurrentConsumers=4&size=1000") != null &&
                         context.hasEndpoint("seda:ES?concurrentConsumers=4&size=1000") != null
                    ) {
                        return true;
                    }
                }
                return false;
            });
        } catch (ConditionTimeoutException e) {
            LOG.error("Camel never started up. Test cannot continue.");
            throw e;
        }

        EventAnticipator anticipator = m_eventIpcManager.getEventAnticipator();

        final String ipAddress = "4.2.2.2";
        final String foreignSource = "testDiscover";

        // This date will create an ES index with name "opennms-2011.01"
        final Date date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.US).parse("Jan 03, 2011 11:43:00 AM EST");

        final EventBuilder eb = new EventBuilder( EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery" );
        eb.setTime(date);
        eb.setInterface( InetAddress.getByName( ipAddress ) );
        eb.setHost( InetAddressUtils.getLocalHostName() );
        eb.addParam("RTT", 0);
        eb.addParam("foreignSource", foreignSource);

        anticipator.anticipateEvent(eb.getEvent());

        m_eventIpcManager.send(eb.getEvent());

        anticipator.verifyAnticipated();

        with().pollInterval(1, SECONDS).await().atMost(60, SECONDS).until(() -> {
            try {
                // Refresh the "opennms-2011.01" index
                ELASTICSEARCH.getClient().admin().indices().prepareRefresh(new IndexNameFunction().apply("opennms", date)).execute().actionGet();

                // Search for all entries in the index
                SearchResponse response = ELASTICSEARCH.getClient()
                    // Search the index that the event above created
                    .prepareSearch(new IndexNameFunction().apply("opennms", date)) // opennms-2011.01
                    .setQuery(QueryBuilders.matchAllQuery())
                    .execute()
                    .actionGet();

                LOG.debug("RESPONSE: {}", response.toString());

                assertEquals("ES search hits was not equal to 1", 1, response.getHits().totalHits());
                assertEquals("Event UEI did not match", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, response.getHits().getAt(0).getSource().get("eventuei"));
                assertEquals("Event IP address did not match", "4.2.2.2", response.getHits().getAt(0).getSource().get("ipaddr"));
            } catch (Throwable e) {
                LOG.warn(e.getMessage(), e);
                return false;
            }
            return true;
        });
    }
}
