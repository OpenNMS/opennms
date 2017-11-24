package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

public class NxosTelemetryIT {

    private static final Logger LOG = LoggerFactory.getLogger(JtiTelemetryIT.class);
    public static final String SENDER_IP = "192.168.1.1";

    private static TestEnvironment m_testEnvironment;
    private static Executor executor;
    private static InetSocketAddress opennmsHttp;

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            builder.withOpenNMSEnvironment().addFile(
                    JtiTelemetryIT.class.getResource("/telemetry/nxos-telemetryd-configuration.xml"),
                    "etc/telemetryd-configuration.xml");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDockerAndLoadExecutor() {
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
    public void verifyNxosTelemetryOnOpenNMS() throws Exception {

        Date startOfTest = new Date();

        OnmsNode onmsNode = JtiTelemetryIT.sendnewSuspectEvent(executor, opennmsHttp, m_testEnvironment, false,
                startOfTest);

        final InetSocketAddress opennmsUdp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 50000, "udp");

        sendNxosTelemetryMessage(opennmsUdp);

        await().atMost(30, SECONDS).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(matchRrdFileFromNodeResource(onmsNode.getId()));

    }

    @Test
    public void verifyNxosTelemetryOnMinion() throws Exception {

        Date startOfTest = new Date();

        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // Modify minion configuration for telemetry
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.telemetry.listeners-udp-50000");
            pipe.println("config:property-set name NXOS");
            pipe.println("config:property-set class-name org.opennms.netmgt.telemetry.listeners.udp.UdpListener");
            pipe.println("config:property-set listener.port 50000");
            pipe.println("config:update");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        OnmsNode onmsNode = JtiTelemetryIT.sendnewSuspectEvent(executor, opennmsHttp, m_testEnvironment, true,
                startOfTest);

        final InetSocketAddress minionUdp = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 50000, "udp");

        sendNxosTelemetryMessage(minionUdp);

        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS)
                .until(matchRrdFileFromNodeResource(onmsNode.getId()));
    }

    private void sendNxosTelemetryMessage(InetSocketAddress udpAddress) throws IOException {

        byte[] nxosOutBytes = Resources.toByteArray(Resources.getResource("telemetry/cisco-nxos-proto.raw"));
        DatagramPacket packet = new DatagramPacket(nxosOutBytes, nxosOutBytes.length, udpAddress);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        } catch (IOException e) {
            LOG.error("Exception while sending nxos packets", e);
        }
    }

    public static Callable<Boolean> matchRrdFileFromNodeResource(Integer id)
            throws ClientProtocolException, IOException {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                HttpResponse response = executor
                        .execute(Request.Get(String.format("http://%s:%d/opennms/rest/resources/fornode/%d",
                                opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort(), id)))
                        .returnResponse();

                String message = EntityUtils.toString(response.getEntity());
                LOG.info(message);
                if (message.contains("rrdFile=\"loadavg")) {
                    return true;
                } else {
                    return false;
                }

            }
        };
    }

}
