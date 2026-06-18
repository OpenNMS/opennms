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
                        final PrintStream pipe = sshClient.openShell();
                        final String command ="bundle:list";
                        pipe.println(command);
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final boolean bundleActive = Arrays.stream(shellOutput.split("\n"))
                                .filter(row -> row.contains("hawtio :: OSGi Web Console"))
                                .findFirst().filter(bundle -> bundle.contains("Active"))
                                .isPresent();
                        logger.info(command);
                        logger.info("{}", shellOutput);
                        return bundleActive;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });
    }
}
