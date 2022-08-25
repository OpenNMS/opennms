package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.nio.file.Files.createTempDirectory;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.opennms.smoketest.utils.OverlayUtils.jsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.OverlayUtils;
import org.opennms.smoketest.utils.SshClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.utility.MountableFile;

@Category(MinionTests.class)
public class NMS14655_IT {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static class UnhealthyMinionContainer extends MinionContainer {
        private final static String LOCATION = "Fulda";
        private final static String ID = "00000000-0000-0000-0000-000000ddba11";

        private UnhealthyMinionContainer() {
            super(StackModel.newBuilder().build(), ID, LOCATION, c -> new AbstractWaitStrategy() {
                @Override
                protected void waitUntilReady() {
                }
            });

            final Path minionConfig;

            try {
                minionConfig = createTempDirectory("minion").toAbsolutePath().resolve("minion-config.yaml");

                FileUtils.copyFile(new File(MountableFile.forClasspathResource("minion-config/minion-config.yaml").getFilesystemPath()), minionConfig.toFile());
                OverlayUtils.setOverlayPermissions(minionConfig);

                final String basicConfig = "{\"location\": \"" + LOCATION + "\",\"id\": \"" + ID + "\",\"broker-url\": \"failover:tcp://opennms:61616\"}";
                OverlayUtils.writeYaml(minionConfig, jsonMapper.readValue(basicConfig, Map.class));

                final String scvConfig = "{\"scv\": {\"provider\": \"dominion\"}}";
                OverlayUtils.writeYaml(minionConfig, jsonMapper.readValue(scvConfig, Map.class));

                final String gprcConfig = "{\"dominion\": { \"grpc\": { \"client-secret\":\"foobar\"}}}";
                OverlayUtils.writeYaml(minionConfig, jsonMapper.readValue(gprcConfig, Map.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            container.addFileSystemBind(minionConfig.toString(), "/opt/minion/minion-config.yaml", BindMode.READ_ONLY, SelinuxContext.SINGLE);
        }
    }

    @ClassRule
    public static final MinionContainer MINION_CONTAINER = new UnhealthyMinionContainer();

    @Test
    public void verifyStartup() {
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    final String bundleCount = ssh(MINION_CONTAINER.getSshAddress(), "bundle:list | grep SCV | wc -l");
                    final String scvBundles = ssh(MINION_CONTAINER.getSshAddress(), "bundle:list | grep SCV");
                    return bundleCount.contains("3") && scvBundles.contains("Dominion gRPC Impl") && !scvBundles.contains("JCEKS Impl");
                });
    }

    private String ssh(final InetSocketAddress sshAddress, final String command) {
        try (final SshClient sshClient = new SshClient(sshAddress, USERNAME, PASSWORD)) {
            final PrintStream pipe = sshClient.openShell();
            if (command != null) {
                pipe.println(command);
            }
            pipe.println("logout");
            await().atMost(30, SECONDS).until(sshClient.isShellClosedCallable());
            // Retrieve output and strip some ANSI sequences
            String output = sshClient.getStdout().replaceAll("\u001B\\[[;\\d]*m", "");
            // Extract command output
            final Pattern pattern = Pattern.compile(".*admin@minion\\(\\)>(.*)admin@minion\\(\\)>.*", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(output);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
