/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest.minion;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

/**
 * Verifies that certificates listed in minion-server-certs.env are added to
 * a copy of the JDK truststore, not to a new truststore that holds only those
 * certificates (NMS-20625).
 */
@Category(MinionTests.class)
public class MinionServerCertsIT {

    private static final String CERT_ALIAS = "nms20625-test-ca";
    private static final String STORE_PASSWORD = "changeit";
    // ${MINION_HOME}/cacerts in the container entrypoint
    private static final String MINION_TRUSTSTORE = "/opt/minion/cacerts";
    private static final Pattern ENTRY_COUNT = Pattern.compile("contains (\\d+) entr");

    private static final Path WORK_DIR = Paths.get("target", "minion-server-certs").toAbsolutePath();
    private static final Path CERT_DIR = generateCertificate();

    @ClassRule
    public static final MinionContainer MINION_CONTAINER = new MinionContainer(StackModel.newBuilder().build(), MinionProfile.newBuilder()
            // the Minion overlay is applied to $MINION_HOME/etc
            .withFile(WORK_DIR.resolve("minion-server-certs.env"), "minion-server-certs.env")
            // no OpenNMS in this stack, so the health check would never pass
            .withWaitStrategy(c -> new AbstractWaitStrategy() {
                @Override
                protected void waitUntilReady() {
                }
            })
            .build());

    static {
        // mounted the way a deployment provides the certificates
        MINION_CONTAINER.withFileSystemBind(CERT_DIR.toString(), "/opt/minion/server-certs", BindMode.READ_ONLY);
    }

    private static Path generateCertificate() {
        final Path certDir = WORK_DIR.resolve("server-certs");
        final Path keystore = WORK_DIR.resolve("server-cert.p12");
        try {
            Files.createDirectories(certDir);
            Files.deleteIfExists(keystore);
            Files.deleteIfExists(certDir.resolve(CERT_ALIAS));
            hostKeytool("-genkeypair", "-alias", CERT_ALIAS, "-keyalg", "RSA", "-keysize", "2048",
                    "-validity", "3650", "-dname", "CN=" + CERT_ALIAS, "-storetype", "PKCS12",
                    "-keystore", keystore.toString(), "-storepass", STORE_PASSWORD);
            hostKeytool("-exportcert", "-rfc", "-alias", CERT_ALIAS, "-keystore", keystore.toString(),
                    "-storepass", STORE_PASSWORD, "-file", certDir.resolve(CERT_ALIAS).toString());
            Files.write(WORK_DIR.resolve("minion-server-certs.env"), (CERT_ALIAS + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Could not generate the test certificate", e);
        }
        return certDir;
    }

    private static void hostKeytool(final String... args) throws IOException, InterruptedException {
        final String[] command = new String[args.length + 1];
        command[0] = Paths.get(System.getProperty("java.home"), "bin", "keytool").toString();
        System.arraycopy(args, 0, command, 1, args.length);
        final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(1, TimeUnit.MINUTES) || process.exitValue() != 0) {
            throw new IOException("keytool " + String.join(" ", args) + " failed: " + output);
        }
    }

    @Test
    public void serverCertsAreAddedToJdkTruststore() throws Exception {
        // the entrypoint imports the certificate before it starts Karaf
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> keytool("-list", "-alias", CERT_ALIAS, "-keystore", MINION_TRUSTSTORE).getExitCode() == 0);

        final int jdkEntries = countEntries("$JAVA_HOME/lib/security/cacerts");
        final int minionEntries = countEntries(MINION_TRUSTSTORE);

        assertThat("JDK truststore has no CA certificates", jdkEntries, greaterThan(0));
        assertThat("Minion truststore should hold every JDK CA plus the configured certificate",
                minionEntries, equalTo(jdkEntries + 1));
    }

    private static Container.ExecResult keytool(final String... args) throws Exception {
        if (!MINION_CONTAINER.isRunning()) {
            throw new AssertionError("Minion container stopped:\n" + MINION_CONTAINER.getLogs());
        }
        final String[] command = new String[args.length + 3];
        command[0] = "keytool";
        System.arraycopy(args, 0, command, 1, args.length);
        command[args.length + 1] = "-storepass";
        command[args.length + 2] = STORE_PASSWORD;
        return MINION_CONTAINER.execInContainer(command);
    }

    private static int countEntries(final String keystore) throws Exception {
        // run through a shell so $JAVA_HOME is expanded in the container
        final Container.ExecResult result = MINION_CONTAINER.execInContainer("sh", "-c",
                "keytool -list -keystore \"" + keystore + "\" -storepass " + STORE_PASSWORD);
        final Matcher matcher = ENTRY_COUNT.matcher(result.getStdout());
        if (result.getExitCode() != 0 || !matcher.find()) {
            throw new AssertionError("Could not list " + keystore + ": " + result.getStdout() + result.getStderr());
        }
        return Integer.parseInt(matcher.group(1));
    }
}
