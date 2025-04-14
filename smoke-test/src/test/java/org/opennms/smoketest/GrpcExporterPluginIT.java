package org.opennms.smoketest;

import java.io.IOException;
import java.time.Duration;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;

@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class GrpcExporterPluginIT {
    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    protected KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Make sure the Karaf shell is healthy before we start
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
    }

    @Test
    public void everythingHappy() throws Exception {
        karafShell.runCommandOnce("feature:install opennms-grpc-exporter" ,
                output -> !output.toLowerCase().contains("error"), false);
        karafShell.checkFeature("opennms-grpc-exporter", "Started", Duration.ofSeconds(30));
    }
}
