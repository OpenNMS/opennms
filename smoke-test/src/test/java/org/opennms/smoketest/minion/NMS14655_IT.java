package org.opennms.smoketest.minion;

import java.net.InetSocketAddress;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShell;

@Category(MinionTests.class)
public class NMS14655_IT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinions(MinionProfile.newBuilder()
                    .withScvProvider("dominion")
                    .build())
            .build());
    private final InetSocketAddress karafSsh = stack.minion().getSshAddress();
    private final KarafShell karafShell = new KarafShell(karafSsh);

    @Test
    public void verifyStartup() {
        karafShell.runCommand(
                "config:edit org.opennms.features.minion.dominion.grpc\n" +
                        "config:property-set clientSecret foobar\n" +
                        "config:update"
        );

        karafShell.runCommand("bundle:list | grep SCV | wc -l", output -> "3".equals(output.trim()));
        karafShell.runCommand("bundle:list | grep SCV", output -> !output.contains("SCV :: JCEKS Impl"));
        karafShell.runCommand("bundle:list | grep SCV", output -> output.contains("SCV :: Dominion gRPC Impl"));
    }
}
