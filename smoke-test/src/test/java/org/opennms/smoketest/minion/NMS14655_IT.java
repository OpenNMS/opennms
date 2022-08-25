package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.SshClient;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

@Category(MinionTests.class)
public class NMS14655_IT {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    @ClassRule
    public static final MinionContainer MINION_CONTAINER = new MinionContainer(StackModel.newBuilder().build(), MinionProfile.newBuilder()
            .withId("00000000-0000-0000-0000-000000ddba11")
            .withLocation("Fulda")
            .withDominionGrpcScvClientSecret("foobar")
            .withWaitStrategy(c -> new AbstractWaitStrategy() {
                @Override
                protected void waitUntilReady() {
                }
            })
            .build());

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
