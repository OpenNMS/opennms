package org.opennms.smoketest.minion;

import static org.opennms.smoketest.utils.KarafShellUtils.awaitHealthCheckSucceeded;

import java.net.InetSocketAddress;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;

@Category(MinionTests.class)
public class NMS14441_IT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;
    private final InetSocketAddress karafSsh = stack.minion().getSshAddress();
    private final KarafShell karafShell = new KarafShell(karafSsh);

    @Test
    public void verifyStartup() {
        karafShell.runCommand(
                "config:edit org.opennms.features.minion.dominion.grpc\n" +
                "config:property-set clientSecret foobar\n" +
                "config:update"
        );

        karafShell.runCommand("feature:uninstall scv-jceks-impl");
        karafShell.runCommand("feature:install dominion-secure-credentials-vault");

        awaitHealthCheckSucceeded(karafSsh, 3, "Minion");
    }
}
