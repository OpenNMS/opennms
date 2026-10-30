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
package org.opennms.karaf.extender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;

import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.apache.karaf.kar.KarService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;
import org.opennms.core.test.MockLogAppender;

public class KarDependencyHandlerTest {
    private static final String KAR = "my-plugin";
    private static final URI FEATURE_URI = URI.create("mvn:org.opennms.plugins/my-plugin/1.0.0/xml/features");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private KarService karService;
    private FeaturesService featuresService;
    private KarafExtender.ExtenderStatus extenderStatus;
    private Path karStorage;
    private Path deployFolder;
    private long jvmStartTime;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        karService = mock(KarService.class);
        featuresService = mock(FeaturesService.class);
        extenderStatus = mock(KarafExtender.ExtenderStatus.class);
        karStorage = tempFolder.newFolder("data", "kar").toPath();
        deployFolder = tempFolder.newFolder("deploy").toPath();
        jvmStartTime = System.currentTimeMillis();

        when(karService.list()).thenReturn(List.of(KAR));
        when(featuresService.listRepositories()).thenReturn(new Repository[0]);
    }

    @After
    public void tearDown() {
        // Clear the flag in case a test interrupted the thread
        Thread.interrupted();
    }

    @Test
    public void installsKarAgainAfterCacheClean() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        final Path karFile = writeKar(KAR);

        newHandler().run();

        verify(karService).install(karFile.toUri());
        verify(featuresService, never()).addRepository(any(URI.class));
        verify(extenderStatus).karsDone("");
    }

    @Test
    public void doesNothingWhenRepositoryIsRegistered() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        writeKar(KAR);
        final Repository registered = mock(Repository.class);
        when(registered.getURI()).thenReturn(FEATURE_URI);
        when(featuresService.listRepositories()).thenReturn(new Repository[]{registered});

        newHandler().run();

        verify(karService, never()).install(any(URI.class));
        verify(featuresService, never()).addRepository(any(URI.class));
    }

    @Test
    public void skipsKarInstalledDuringThisStart() throws Exception {
        writeFeatureConfig(jvmStartTime + 60_000);
        writeKar(KAR);

        newHandler().run();

        verify(karService, never()).install(any(URI.class));
        verify(featuresService, never()).addRepository(any(URI.class));
    }

    @Test
    public void doesNothingWhenCacheIsNotCleaned() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        writeKar(KAR);

        newHandler(List.of(deployFolder), false).run();

        verify(karService, never()).install(any(URI.class));
        verify(featuresService, never()).addRepository(any(URI.class));
        verify(extenderStatus).karsDone("");
    }

    @Test
    public void onlyAddsRepositoryWhenKarIsNotInDeployFolder() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);

        newHandler().run();

        verify(featuresService).addRepository(FEATURE_URI);
        verify(karService, never()).install(any(URI.class));
    }

    @Test
    public void doesNotFailWhenRepositoryOfKarNotInDeployFolderCannotBeAdded() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        doThrow(new Exception("boom")).when(featuresService).addRepository(FEATURE_URI);

        newHandler().run();

        verify(extenderStatus).karsDone("");
        verify(extenderStatus, never()).karsFailed(anyString());
    }

    @Test
    public void findsKarInAnyDeployFolder() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        final Path karFile = writeKar(KAR);

        newHandler(List.of(tempFolder.newFolder("other-deploy").toPath(), deployFolder), true).run();

        verify(karService).install(karFile.toUri());
    }

    @Test
    public void addsRepositoriesOfMissingKarsBeforeInstallingKars() throws Exception {
        // "another-kar" is listed last and isn't in the deploy folder, the features of KAR may depend on it
        final String otherKar = "another-kar";
        final URI otherFeatureUri = URI.create("mvn:org.opennms.plugins/another-kar/1.0.0/xml/features");
        when(karService.list()).thenReturn(List.of(KAR, otherKar));
        writeFeatureConfig(jvmStartTime - 60_000);
        writeFeatureConfig(otherKar, otherFeatureUri, jvmStartTime - 60_000);
        final Path karFile = writeKar(KAR);

        newHandler().run();

        final InOrder inOrder = inOrder(featuresService, karService);
        inOrder.verify(featuresService).addRepository(otherFeatureUri);
        inOrder.verify(karService).install(karFile.toUri());
    }

    @Test
    public void reportsFailureWhenKarCannotBeInstalled() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        final Path karFile = writeKar(KAR);
        doThrow(new Exception("boom")).when(karService).install(karFile.toUri());

        newHandler().run();

        verify(extenderStatus).karsFailed("Failed to restore KAR(s) [my-plugin]");
        verify(extenderStatus, never()).karsDone(anyString());
    }

    @Test
    public void stopsWhenInterrupted() throws Exception {
        writeFeatureConfig(jvmStartTime - 60_000);
        writeKar(KAR);

        Thread.currentThread().interrupt();
        newHandler().run();

        verify(karService, never()).install(any(URI.class));
        verify(extenderStatus, never()).karsDone(anyString());
    }

    private KarDependencyHandler newHandler() {
        return newHandler(List.of(deployFolder), true);
    }

    private KarDependencyHandler newHandler(List<Path> deployFolders, boolean cacheCleanedAtStart) {
        return new KarDependencyHandler(Collections.emptyList(), karService, featuresService, extenderStatus,
                karStorage, deployFolders, cacheCleanedAtStart, jvmStartTime);
    }

    private void writeFeatureConfig(long lastModified) throws IOException {
        writeFeatureConfig(KAR, FEATURE_URI, lastModified);
    }

    private void writeFeatureConfig(String kar, URI featureUri, long lastModified) throws IOException {
        final Path featureConfig = Files.createDirectories(karStorage.resolve(kar)).resolve("features.cfg");
        Files.write(featureConfig, List.of(featureUri.toString()), StandardCharsets.UTF_8);
        Files.setLastModifiedTime(featureConfig, FileTime.fromMillis(lastModified));
    }

    private Path writeKar(String kar) throws IOException {
        // The handler only needs the file to exist, Karaf reads it
        return Files.write(deployFolder.resolve(kar + ".kar"), new byte[0]);
    }
}
