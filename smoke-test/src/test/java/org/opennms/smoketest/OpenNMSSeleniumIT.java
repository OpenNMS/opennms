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
package org.opennms.smoketest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.DefaultRecordingFileFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.lifecycle.TestDescription;

/**
 * Base class for Selenium based testing of the OpenNMS web application.
 */
public class OpenNMSSeleniumIT extends AbstractOpenNMSSeleniumHelper {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSSeleniumIT.class);

    /**
     * When to save web browser video recordings.
     * The default, RECORD_FAILING will only save recordings when there are failed tests.
     * Other options: SKIP or RECORD_ALL.
     *
     * Historical warning: This container can fail and cause test instability - for example if retrieving the
     * recording from the container fails, an exception will be thrown causing the test to fail.
     *
     * Although RECORD_FAILING is the default in BrowserWebDriverContainer, the constructor we use resets
     * it to SKIP. :( So we always explicitly set it.
     */
    private static final BrowserWebDriverContainer.VncRecordingMode RECORDING_MODE =
            BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING; // RECORD_ALL, RECORD_FAILING, or SKIP

    // Use mp4 since everything supports it
    private static final VncRecordingContainer.VncRecordingFormat RECORDING_FORMAT =
            VncRecordingContainer.VncRecordingFormat.MP4;

    private static final File RECORDING_DIRECTORY = new File("target");

    private static final CachingRecordingFileFactory RECORDING_FILE_FACTORY = new CachingRecordingFileFactory();

    public static final WorkaroundBrowserWebDriverContainer firefox =
            new WorkaroundBrowserWebDriverContainer()
            .withCapabilities(getFirefoxOptions())
            .withRecordingMode(RECORDING_MODE, RECORDING_DIRECTORY, RECORDING_FORMAT)
            .withRecordingFileFactory(RECORDING_FILE_FACTORY)
            .withNetwork(Network.SHARED)
            // Increase the containers shared memory to 2GB to help prevent Firefox from crashing
            .withSharedMemorySize(2147483648L)
            // Non-blocking entropy
            .withEnv("JAVA_OPTS", "-Djava.security.egd=file:/dev/./urandom")
            .withEnv("SCREEN_WIDTH", "2048")
            .withEnv("SCREEN_HEIGHT", "1400")
            .withCreateContainerCmdModifier(createCmd -> {
                TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);

                // Use this hook to ensure that the downloads directory exists
                final File downloads = AbstractOpenNMSSeleniumHelper.DOWNLOADS_FOLDER;
                downloads.mkdirs();
                try {
                    // Make the folder world readable/writable
                    Files.setPosixFilePermissions(downloads.toPath(), PosixFilePermissions.fromString("rwxrwxrwx"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .withFileSystemBind("target/downloads", "/tmp/firefox-downloads");

    public static FirefoxOptions getFirefoxOptions() {
        final FirefoxOptions options = new FirefoxOptions();
        options.setProfile(new FirefoxProfile());
        // Disable browser notifications
        options.addPreference("dom.webnotifications.enabled", Boolean.FALSE);
        // Increase the browser resolution on startup
        options.addArguments("--width=2048");
        options.addArguments("--height=1400");
        // Configure FireFox to download PDFs to disk
        options.addPreference("browser.download.folderList", 2); // Use for the default download directory the last folder specified for a download
        options.addPreference("browser.download.dir", "/tmp/firefox-downloads"); // Set the last directory used for saving a file from the "What should (browser) do with this file?" dialog.
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf"); // List of MIME types to save to disk without asking what to use to open the file
        options.addPreference("pdfjs.disabled", true);  // Disable the built-in PDF viewer
        // Debug Selenium <-> Firefox
        //options.setLogLevel(FirefoxDriverLogLevel.TRACE);
        return options;
    }

    protected static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(stack)
            // Start the Selenium container *after* OpenNMS has started so that the recording doesn't start
            // until OpenNMS is actually up and running
            .around(firefox);

    public static RemoteWebDriver driver;

    @BeforeClass
    public static void setUpClass() {
        driver = firefox.getWebDriver();
    }

    public static Optional<Throwable> failed = Optional.empty();

    /**
     * {@code @ClassRule} does not seem to know if tests failed, but afterTest needs
     * to know if a screen recording should be saved. Store the latest failure
     * exception here. The specific failure and exception doesn't matter for
     * this, we just need to know if there are >0 exceptions for afterTest.
     */
    @Rule(order = Integer.MIN_VALUE)
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            failed = Optional.of(e);
        }

        @Override
        protected void succeeded(Description description) {
        }
    };

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public String getBaseUrlInternal() {
        return stack.opennms().getBaseUrlInternal().toString();
    }

    @Override
    public String getBaseUrlExternal() {
        return stack.opennms().getBaseUrlExternal().toString();
    }

    /**
     * We work around some bugs and shortcomings in org.testcontainers' BrowserWebDriverContainer here until we can
     * submit issues/PRs upstream and the fixes are released.
     * First, when a test is complete, nothing is done to tell the VNC recorder Docker image to stop recording.
     * Transcoding the recording is CPU-intensive and single-threaded and happens while new data is coming in if the
     * VNC recorder process is still running. Unluckily for us, because of our large screen size, the transcoder can't
     * keep up with the incoming data from the VNC recorder, so it never gets to EOF on its input file and will run
     * forever. We work around that by sending a SIGSTOP to the VNC recorder process inside its Docker container (we
     * can't just stop the container because we need it around and running because that's where the transcoder runs).
     * Lastly, we provide an alternate RecordingFileFactory that caches the recording file name and we output the
     * full path as a URI to make it easier to view the recordings.
     */
    public static class WorkaroundBrowserWebDriverContainer
            extends BrowserWebDriverContainer<WorkaroundBrowserWebDriverContainer> {
        @Override
        public void beforeTest(TestDescription description) {
            // These are static and reused across all test classes, so reset before every test class run
            RECORDING_FILE_FACTORY.recordingFile = null;
            OpenNMSSeleniumIT.failed = Optional.empty();

            super.beforeTest(description);
        }

        @Override
        public void afterTest(TestDescription description, Optional<Throwable> throwable) {
            try {
                Field f = firefox.getClass().getSuperclass().getDeclaredField("vncRecordingContainer");
                f.setAccessible(true);
                VncRecordingContainer vncRecordingContainer = (VncRecordingContainer) f.get(firefox);
                if (vncRecordingContainer != null && vncRecordingContainer.isRunning()) {
                    var command = "kill -STOP $(grep -l -F flvrec.py /proc/*/cmdline | sed 's,^/proc/,,;s,/.*,,' "
                            + "| grep '^[0-9]*$' | grep -v '^1$' | sort -n | head  -1)";
                    var results = vncRecordingContainer.execInContainer("sh", "-c", command);
                    if (results.getExitCode() != 0) {
                        throw new RuntimeException("Got non-zero exit code " + results.getExitCode() + " when attempting to "
                                + "send a SIGSTOP to the VNC recorder process in the container.\n"
                                + "Command: " + command + "\n"
                                + "Stdout: " + results.getStdout() + "\n"
                                + "Stderr: " + results.getStderr());
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            // This is where the recording is trans-coded and extracted to the local system.
            super.afterTest(description, OpenNMSSeleniumIT.failed);

            // I really wanted a better message with a clickable URI at the end of the test.
            if (RECORDING_FILE_FACTORY.recordingFile != null &&
                    RECORDING_FILE_FACTORY.recordingFile.exists()) {
                LOG.info("Recording: {}", RECORDING_FILE_FACTORY.recordingFile.toURI());
            }
        }
    }

    public static class CachingRecordingFileFactory extends DefaultRecordingFileFactory {
        public File recordingFile = null;

        @Override
        public File recordingFileForTest(
                File vncRecordingDirectory,
                String prefix,
                boolean succeeded,
                VncRecordingContainer.VncRecordingFormat recordingFormat
        ) {
            recordingFile = super.recordingFileForTest(vncRecordingDirectory, prefix, succeeded, recordingFormat);
            return recordingFile;
        }
    }
}
