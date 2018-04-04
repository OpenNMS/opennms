package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;

public class KafkaProducerIT {

    private static TestEnvironment m_testEnvironment;
    private static Executor executor;
    private static InetSocketAddress opennmsHttp;
    public static final String NODE_IP = "192.168.1.1";

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all().kafka();
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
        if (m_testEnvironment == null) {
            return;
        }
        opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        final HttpHost opennmsHttpHost = new HttpHost(opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort());
        // Ignore 302 response to the POST
        HttpClient instance = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        executor = Executor.newInstance(instance).auth(opennmsHttpHost, "admin", "admin")
                .authPreemptive(opennmsHttpHost);
    }

    @Test
    public void testKafkaAlarmStoreData() throws Exception {
        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        String kafkaHost = m_testEnvironment.getContainerInfo(ContainerAlias.KAFKA).networkSettings().ipAddress();
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.kafka.producer.client");
            pipe.println("config:property-set bootstrap.servers " + kafkaHost + ":9092");
            pipe.println("config:update");
            pipe.println("feature:install opennms-kafka-producer");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        await().atMost(10, MINUTES).pollInterval(90, SECONDS).pollDelay(0, SECONDS).until(() -> checkAlarms(sshAddr),
                containsString("uei.opennms.org/alarms/trigger"));

    }

    private String checkAlarms(InetSocketAddress sshAddr) throws Exception {

        generateAlarmEvent();
        String shellOutput;
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {

            PrintStream pipe = sshClient.openShell();
            pipe.println("kafka:alarms");
            pipe.println("logout");
            await().atMost(65, SECONDS).until(sshClient.isShellClosedCallable());
            shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "kafka:alarms");
            return shellOutput;
        }

    }

    private static void generateAlarmEvent() throws ClientProtocolException, IOException {
        Date startOfTest = new Date();
        Event alarmEvent = new Event();
        alarmEvent.setUei("uei.opennms.org/alarms/trigger");
        alarmEvent.setSeverity("7");
        List<Parm> parms = new ArrayList<>();

        Parm parm = new Parm("service", "kafka");
        parms.add(parm);
        alarmEvent.setParmCollection(parms);

        String alarmXmlString = JaxbUtils.marshal(alarmEvent);
        InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        executor.execute(Request.Post(String.format("http://%s:%d/opennms/rest/events",
                opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort()))
                .bodyString(alarmXmlString, ContentType.APPLICATION_XML)).returnContent();

        AlarmDao alarmDao = daoFactory.getDao(AlarmDaoHibernate.class);

        Criteria alarmCriteria = new CriteriaBuilder(OnmsAlarm.class).eq("uei", "uei.opennms.org/alarms/trigger")
                .ge("lastEventTime", startOfTest).eq("reductionKey", "uei.opennms.org/alarms/trigger:::kafka")
                .toCriteria();

        await().atMost(30, SECONDS).pollInterval(10, SECONDS)
                .until(DaoUtils.countMatchingCallable(alarmDao, alarmCriteria), greaterThan(0));
    }

}
