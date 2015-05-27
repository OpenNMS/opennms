package org.opennms.web.rest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.MediaType;
import java.util.Date;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventProxy.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class OutageRestServiceTest extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private DatabasePopulator populator;

    @Before
    @Override
    public void setUp() throws Throwable {
        super.setUp();
        Assert.assertNotNull(populator);
        Assert.assertNotNull(applicationDao);

        populator.addExtension(new DatabasePopulator.Extension<ApplicationDao>() {

            private OnmsOutage unresolvedOutage;

            private OnmsEvent outageEvent;

            private OnmsApplication application;

            @Override
            public DatabasePopulator.DaoSupport<ApplicationDao> getDaoSupport() {
                return new DatabasePopulator.DaoSupport<>(ApplicationDao.class, applicationDao);
            }

            @Override
            public void onPopulate(DatabasePopulator populator, ApplicationDao dao) {
                OnmsDistPoller distPoller = populator.getDistPoller("localhost", "127.0.0.1");
                outageEvent = populator.buildEvent(distPoller);
                populator.getEventDao().save(outageEvent);
                populator.getEventDao().flush();

                // create the application
                application = new OnmsApplication();
                application.setName("Awesome Application");
                dao.save(application);

                // get the SNMP service from node 1 and assign the application to it
                final OnmsMonitoredService svc = populator.getMonitoredServiceDao().get(populator.getNode1().getId(), InetAddressUtils.addr("192.168.1.2"), "HTTP");
                svc.addApplication(application);
                application.addMonitoredService(svc);
                populator.getMonitoredServiceDao().saveOrUpdate(svc);
                populator.getMonitoredServiceDao().flush();

                // create a unresolved outage
                unresolvedOutage = new OnmsOutage(new Date(), outageEvent, svc);
                populator.getOutageDao().save(unresolvedOutage);
                populator.getOutageDao().flush();
            }

            @Override
            public void onShutdown(DatabasePopulator populator, ApplicationDao dao) {
                // All other tables have been already deleted,
                // Delete OnmsApplications
                for (OnmsApplication application : dao.findAll()) {
                    dao.delete(application);
                }
            }
        });

        populator.populateDatabase();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        populator.resetDatabase();
    }

    @Test
    public void testGetAllOutages() throws Exception {
        String xml = sendRequest(GET, "/outages", 200);

        MockHttpServletRequest jsonRequest = createRequest(getServletContext(), GET, "/outages");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        Assert.assertNotNull(xml);
        Assert.assertNotNull(json);
    }
}
