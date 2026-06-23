package org.opennms.smoketest.hawtio;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public abstract class AbstractHawtioIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Supplier<SshClient> supplier;

    protected AbstractHawtioIT(final Supplier<SshClient> supplier) {
        this.supplier = supplier;
    }

    @Test
    public void verify() {
        verify(supplier);
    }

    public void verify(final Supplier<SshClient> f) {

        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(2, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = f.get()) {
                        final PrintStream featureListPipe = sshClient.openShell();
                        final String featureListCommand = "feature:list";
                        featureListPipe.println(featureListCommand);
                        featureListPipe.println("logout");

                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        final String featureListOutput = sshClient.getStdout();

                        // check that all four hawtio features are started
                        final boolean featureInstalled = Arrays.stream(featureListOutput.split("\n"))
                                .filter(row -> row.matches("hawtio-\\d.\\d\\d.\\d"))
                                .filter(bundle -> bundle.contains("Started")).count() == 4L;

                        logger.info(featureListCommand);
                        logger.info("{}", featureListOutput);

                        final PrintStream bundleListPipe = sshClient.openShell();
                        final String bundleListCommand = "bundle:list";
                        bundleListPipe.println(bundleListCommand);
                        bundleListPipe.println("logout");

                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        final String bundleListOutput = sshClient.getStdout();

                        // check that hawtio OSGi Web Console bundle is active
                        final boolean bundleActive = Arrays.stream(bundleListOutput.split("\n"))
                                .filter(row -> row.contains("hawtio :: OSGi Web Console"))
                                .findFirst().filter(bundle -> bundle.contains("Active"))
                                .isPresent();

                        logger.info(bundleListCommand);
                        logger.info("{}", bundleListOutput);
                        return bundleActive;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });
    }
}
