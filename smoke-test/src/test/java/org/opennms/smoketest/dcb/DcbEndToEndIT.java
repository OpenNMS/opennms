package org.opennms.smoketest.dcb;

import static com.jayway.awaitility.Awaitility.await;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonArray;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonBoolean;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonInt;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonNull;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.deviceconfig.persistence.impl.DeviceConfigDaoImpl;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.IpInterfaceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.google.common.collect.Iterables;

// TODO: Running two backups on the same target is broken. Use filenameSuffix after NMS-14144 to avoid race in backup script
public class DcbEndToEndIT {
    private static final String DCB_CONFIG_TYPE = "testcfg";
    private static final String DCB_USERNAME = "dcbuser";
    private static final String DCB_PASSWORD = "dcbpass";
    private static final String DCB_SCRIPT_NAME = "test";
    private static final String DCB_SVC_NAME = "DeviceConfig";

    private static final int SSH_PORT = 2222;

    private static final String FOREIGN_SOURCE = "SmokeTests";

    private static final OpenNMSProfile OPENNMS_PROFILE = OpenNMSProfile.newBuilder()
                                                                        .withFile("device-config/test.dcb", "etc/device-config/" + DCB_SCRIPT_NAME + ".dcb")
                                                                        .build();

    @ClassRule
    public static final OpenNMSStack STACK = OpenNMSStack.withModel(StackModel.newBuilder()
                                                                              .withOpenNMS(OPENNMS_PROFILE)
                                                                              .withMinion()
                                                                              .build());

    @ClassRule
    public static final GenericContainer<?> TARGET_CONTAINER = new GenericContainer<>(
            new ImageFromDockerfile().withDockerfileFromBuilder(builder -> builder.from("linuxserver/openssh-server")
                                                                 .run("apk add --update tftp-hpa")
                                                                 .build()))
            .withEnv("PASSWORD_ACCESS", "true")
            .withEnv("SUDO_ACCESS", "true")
            .withEnv("USER_NAME", DCB_USERNAME)
            .withEnv("USER_PASSWORD", DCB_PASSWORD)
            .withExposedPorts(SSH_PORT)
            .withNetwork(Network.SHARED)
            .withNetworkAliases("target")
            .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);

    private static String targetAddress;

    private static OnmsNode localNode;
    private static OnmsNode remoteNode;
    private static OnmsIpInterface localInterface;
    private static OnmsIpInterface remoteInterface;
    private static OnmsMonitoredService localService;
    private static OnmsMonitoredService remoteService;

    private static RestClient restClient;

    @BeforeClass
    public static void setupClass() throws Exception {
        targetAddress = getContainerInternalIpAddress(TARGET_CONTAINER);

        restClient = new RestClient(STACK.opennms().getWebAddress());

        final Requisition requisition = new Requisition();
        requisition.setForeignSource(FOREIGN_SOURCE);

        // Node in default location for triggering backup from core
        {
            final List<RequisitionMonitoredService> monitoredServiceList = new ArrayList<>();
            monitoredServiceList.add(new RequisitionMonitoredService(DCB_SVC_NAME));

            final RequisitionInterface requisitionInterface = new RequisitionInterface();
            requisitionInterface.setIpAddr(targetAddress);
            requisitionInterface.setManaged(true);
            requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
            requisitionInterface.setMonitoredServices(monitoredServiceList);

            final List<RequisitionMetaData> metaDataList = new ArrayList<>();
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:config-type", DCB_CONFIG_TYPE));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:username", DCB_USERNAME));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:password", DCB_PASSWORD));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:script-file", DCB_SCRIPT_NAME));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:ssh-port", Integer.toString((SSH_PORT))));

            final RequisitionNode requisitionNode = new RequisitionNode();
            requisitionNode.setForeignId("local");
            requisitionNode.setNodeLabel("local");
            requisitionNode.setLocation(null);
            requisitionNode.setMetaData(metaDataList);
            requisitionNode.putInterface(requisitionInterface);

            requisition.insertNode(requisitionNode);
        }

        // Node in remote location for triggering backup through minion
        {
            final List<RequisitionMonitoredService> monitoredServiceList = new ArrayList<>();
            monitoredServiceList.add(new RequisitionMonitoredService(DCB_SVC_NAME));

            final RequisitionInterface requisitionInterface = new RequisitionInterface();
            requisitionInterface.setIpAddr(targetAddress);
            requisitionInterface.setManaged(true);
            requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
            requisitionInterface.setMonitoredServices(monitoredServiceList);

            final List<RequisitionMetaData> metaDataList = new ArrayList<>();
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:config-type", DCB_CONFIG_TYPE));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:username", DCB_USERNAME));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:password", DCB_PASSWORD));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:script-file", DCB_SCRIPT_NAME));
            metaDataList.add(new RequisitionMetaData("requisition", "dcb:ssh-port", Integer.toString((SSH_PORT))));

            final RequisitionNode requisitionNode = new RequisitionNode();
            requisitionNode.setForeignId("remote");
            requisitionNode.setNodeLabel("remote");
            requisitionNode.setLocation(STACK.minion().getLocation());
            requisitionNode.setMetaData(metaDataList);
            requisitionNode.putInterface(requisitionInterface);

            requisition.insertNode(requisitionNode);
        }

        restClient.addOrReplaceRequisition(requisition);
        restClient.importRequisition(FOREIGN_SOURCE);

        final NodeDao nodeDao = STACK.postgres().dao(NodeDaoHibernate.class);
        final IpInterfaceDao ipInterfaceDao = STACK.postgres().dao(IpInterfaceDaoHibernate.class);
        final MonitoredServiceDao monitoredServiceDao = STACK.postgres().dao(MonitoredServiceDaoHibernate.class);

        localNode = await()
                .atMost(3, MINUTES)
                .pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class).eq("foreignId", "local").toCriteria()), notNullValue());

        remoteNode = await()
                .atMost(3, MINUTES)
                .pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class).eq("foreignId", "remote").toCriteria()), notNullValue());

        localInterface = ipInterfaceDao.findPrimaryInterfaceByNodeId(localNode.getId());
        remoteInterface = ipInterfaceDao.findPrimaryInterfaceByNodeId(remoteNode.getId());

        localService = monitoredServiceDao.getPrimaryService(localNode.getId(), "DeviceConfig");
        remoteService = monitoredServiceDao.getPrimaryService(remoteNode.getId(), "DeviceConfig");
    }

    @Before
    public void setup() throws Exception {
        // Ensure there is no backup history for every test
        final var deviceConfigDao = STACK.postgres().dao(DeviceConfigDaoImpl.class);
        deviceConfigDao.deleteAll(deviceConfigDao.findAll());
        deviceConfigDao.flush();
    }

    private static String getContainerInternalIpAddress(final GenericContainer<?> container) {
        return Iterables.getOnlyElement(container.getContainerInfo()
                                                 .getNetworkSettings()
                                                 .getNetworks()
                                                 .entrySet())
                        .getValue()
                        .getIpAddress();
    }

    @Test
    public void testTriggerBackupWithREST() throws Exception {
        await().atMost(2, MINUTES)
               .until(restClient::getBackups, is(nullValue()));

        // Trigger local backup
        restClient.triggerBackup(new JSONArray(List.of(new JSONObject()
                                                               .put("ipAddress", targetAddress)
                                                               .put("location", STACK.minion().getLocation())
                                                               .put("serviceName", DCB_SVC_NAME)))
                                         .toString());

        // TODO: Remove after NMS-14143
        await().atMost(2, MINUTES)
                       .until(restClient::getBackups, jsonArray(hasSize(1)));

        // Trigger remote backup
        restClient.triggerBackup(new JSONArray(List.of(new JSONObject()
                                                               .put("ipAddress", targetAddress)
                                                               .put("location", MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID)
                                                               .put("serviceName", DCB_SVC_NAME)))
                                         .toString());

        // TODO: Remove after NMS-14143
        await().atMost(2, MINUTES)
               .until(restClient::getBackups, jsonArray(hasSize(2)));

        assertThat(restClient.getBackups(), jsonArray(containsInAnyOrder(
                jsonObject()
                        .where("nodeId", is(jsonInt(localNode.getId())))
                        .where("ipInterfaceId", is(jsonInt(localInterface.getId())))
                        .where("serviceName", is(jsonText(localService.getServiceName())))
                        .where("ipAddress", is(jsonText(targetAddress)))
                        .where("configType", is(jsonText(DCB_CONFIG_TYPE)))
                        .where("fileName", is(jsonText("test")))
                        .where("failureReason", is(jsonNull()))
                        .where("isSuccessfulBackup", is(jsonBoolean(true)))
                        .where("backupStatus", is(jsonText("success")))
                        .where("scheduledInterval", is(jsonObject().where("DeviceConfig", is(jsonText("At 12:00 am")))))
                        .where("config", is(jsonText(containsString(String.format("%s %s\n", getContainerInternalIpAddress(STACK.opennms()), 6969))))),
                jsonObject()
                        .where("nodeId", is(jsonInt(remoteNode.getId())))
                        .where("ipInterfaceId", is(jsonInt(remoteInterface.getId())))
                        .where("serviceName", is(jsonText(remoteService.getServiceName())))
                        .where("ipAddress", is(jsonText(targetAddress)))
                        .where("configType", is(jsonText(DCB_CONFIG_TYPE)))
                        .where("fileName", is(jsonText("test")))
                        .where("failureReason", is(jsonNull()))
                        .where("isSuccessfulBackup", is(jsonBoolean(true)))
                        .where("backupStatus", is(jsonText("success")))
                        .where("scheduledInterval", is(jsonObject().where("DeviceConfig", is(jsonText("At 12:00 am")))))
                        .where("config", is(jsonText(containsString(String.format("%s %s\n", getContainerInternalIpAddress(STACK.minion()), 6969))))))));
        //                                                                                                                                               ^-- Yes, 8 closing braces
    }

    @Test
    public void testTriggerBackupWithCLI() throws Exception {
        await().atMost(2, MINUTES)
               .until(restClient::getBackups, is(nullValue()));

        // Trigger local backup
        try (final SshClient sshClient = STACK.opennms().ssh(); final PrintStream pipe = sshClient.openShell()) {
            pipe.printf("opennms:device-config-trigger-backup -s %s -l %s %s%n", DCB_SVC_NAME, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, targetAddress);
            pipe.printf("logout%n");

            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        await().atMost(2, MINUTES)
               .until(restClient::getBackups, jsonArray(hasSize(1)));

        // Trigger remote backup
        try (final SshClient sshClient = STACK.opennms().ssh(); final PrintStream pipe = sshClient.openShell()) {
            pipe.printf("opennms:device-config-trigger-backup -s %s -l %s %s%n", DCB_SVC_NAME, STACK.minion().getLocation(), targetAddress);
            pipe.printf("logout%n");

            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        await().atMost(2, MINUTES)
               .until(restClient::getBackups, jsonArray(hasSize(2)));

        assertThat(restClient.getBackups(), jsonArray(containsInAnyOrder(
                jsonObject()
                        .where("nodeId", is(jsonInt(localNode.getId())))
                        .where("ipInterfaceId", is(jsonInt(localInterface.getId())))
                        .where("serviceName", is(jsonText(localService.getServiceName())))
                        .where("ipAddress", is(jsonText(targetAddress)))
                        .where("configType", is(jsonText(DCB_CONFIG_TYPE)))
                        .where("fileName", is(jsonText("test")))
                        .where("failureReason", is(jsonNull()))
                        .where("isSuccessfulBackup", is(jsonBoolean(true)))
                        .where("backupStatus", is(jsonText("success")))
                        .where("scheduledInterval", is(jsonObject().where("DeviceConfig", is(jsonText("At 12:00 am")))))
                        .where("config", is(jsonText(containsString(String.format("%s %s\n", getContainerInternalIpAddress(STACK.opennms()), 6969))))),
                jsonObject()
                        .where("nodeId", is(jsonInt(remoteNode.getId())))
                        .where("ipInterfaceId", is(jsonInt(remoteInterface.getId())))
                        .where("serviceName", is(jsonText(remoteService.getServiceName())))
                        .where("ipAddress", is(jsonText(targetAddress)))
                        .where("configType", is(jsonText(DCB_CONFIG_TYPE)))
                        .where("fileName", is(jsonText("test")))
                        .where("failureReason", is(jsonNull()))
                        .where("isSuccessfulBackup", is(jsonBoolean(true)))
                        .where("backupStatus", is(jsonText("success")))
                        .where("scheduledInterval", is(jsonObject().where("DeviceConfig", is(jsonText("At 12:00 am")))))
                        .where("config", is(jsonText(containsString(String.format("%s %s\n", getContainerInternalIpAddress(STACK.minion()), 6969))))))));
        //                                                                                                                                               ^-- Yes, 8 closing braces
    }

    @Test
    public void testGetConfigWithCLI() throws Exception {
        await().atMost(2, MINUTES)
               .until(restClient::getBackups, is(nullValue()));

        // Trigger local backup
        try (final SshClient sshClient = STACK.opennms().ssh(); final PrintStream pipe = sshClient.openShell()) {
            pipe.printf("opennms:device-config-get -s %s -l %s %s%n", DCB_SVC_NAME, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, targetAddress);
            pipe.printf("logout%n");

            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            assertThat(sshClient.getStdout(), containsString(String.format("%s %s", getContainerInternalIpAddress(STACK.opennms()), 6969)));
        }

        // Trigger remote backup
        try (final SshClient sshClient = STACK.opennms().ssh(); final PrintStream pipe = sshClient.openShell()) {
            pipe.printf("opennms:device-config-get -s %s -l %s %s%n", DCB_SVC_NAME, STACK.minion().getLocation(), targetAddress);
            pipe.printf("logout%n");

            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            assertThat(sshClient.getStdout(), containsString(String.format("%s %s", getContainerInternalIpAddress(STACK.minion()), 6969)));
        }

        // Ensure backup was not stored
//        await().atMost(2, MINUTES)
//               .until(restClient::getBackups, is(nullValue()));
        // TODO: remove with NMS-14176
        await().atMost(2, MINUTES)
               .until(restClient::getBackups, jsonArray(hasSize(2)));
    }

    @Test
    @Ignore
    public void testTriggerBackupBySchedule() throws Exception {
    }

    @Test
    @Ignore
    public void testPushedBackup() throws Exception {
    }

    // TODO: Add a test for running on multiple targets in parallel
}
