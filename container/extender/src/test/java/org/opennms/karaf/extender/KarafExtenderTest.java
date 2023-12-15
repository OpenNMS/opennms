/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.karaf.extender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class KarafExtenderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private KarafExtender karafExtender;

    @Before
    public void setUp() {
        System.setProperty("karaf.home", tempFolder.getRoot().getAbsolutePath());
        karafExtender = new KarafExtender();
    }

    @Test
    public void canGenerateFeaturesBootList() throws IOException {
        // Create etc/featuresBoot.d
        File featuresBootDotD = tempFolder.newFolder("etc", "featuresBoot.d");
        featuresBootDotD.mkdirs();

        // Add a sub-directory, which should be ignored
        assertTrue("Failed to create subdirectory.", new File(featuresBootDotD, "some-subdirectory").mkdir());

        // Add a file that starts with '.' in the directory, it's contents should be ignored
        Files.write("should-not-be-installed",
                new File(featuresBootDotD, ".ignored.boot"), StandardCharsets.UTF_8);

        // Add a file with features and comments
        Files.write("#this is a comment\n" +
                "  #here's another one that starts with some whitespace\n" +
                "      \t\n" +
                "\t#that was a line that only contained whitespace\n" +
                "feature-1\n" +
                "feature-2/18.0.0\n" +
                "feature-3", new File(featuresBootDotD, "core.boot"), StandardCharsets.UTF_8);

        // Add another file with a name that should be sorted after the previous file
        Files.write("feature-4", new File(featuresBootDotD, "features.boot"), StandardCharsets.UTF_8);

        // Wait for a kar
        Files.write("#plugins!\n" +
                "opennms-oce-plugin wait-for-kar=opennms-oce-plugin", new File(featuresBootDotD, "kar-plugin.boot"), StandardCharsets.UTF_8);

        // Read and verify
        Feature feature1 = Feature.builder().withName("feature-1").build();
        Feature feature2 = Feature.builder().withName("feature-2").withVersion("18.0.0").build();
        Feature feature3 = Feature.builder().withName("feature-3").build();
        Feature feature4 = Feature.builder().withName("feature-4").build();
        Feature ocePluginFeature = Feature.builder().withName("opennms-oce-plugin").withKarDependency("opennms-oce-plugin").build();

        assertEquals(Lists.newArrayList(feature1,
                feature2,
                feature3,
                feature4,
                ocePluginFeature), karafExtender.getFeaturesBoot());
        
        // Now add another file that disables features-1 and feature-2 above
        Files.write("!feature-1\n" +
                "!feature-2/18.0.0\n", new File(featuresBootDotD, "core2.boot"), StandardCharsets.UTF_8);

        // Read and filter
        List<Feature> features = karafExtender.getFeaturesBoot();
        karafExtender.filterFeatures(features);

        // Verify
        assertEquals(Lists.newArrayList(
                feature3,
                feature4,
                ocePluginFeature),
                features);
    }

    @Test
    public void canGenerateRepositoryList() throws IOException, URISyntaxException {
        // Create repositories
        File repositories = tempFolder.newFolder("repositories");
        repositories.mkdirs();

        // Create an empty repository
        File emptyRepository = new File(repositories, "empty");
        emptyRepository.mkdirs();

        // Create a release (non-snapshot) repository
        File releaseRepository = new File(repositories, "release");
        releaseRepository.mkdirs();
        Files.write("mvn:group.id/artifact.id/2.0.0/xml",
                new File(releaseRepository, "features.uri"), StandardCharsets.UTF_8);
        Files.write("  # comment\n" + "released-feature",
                new File(releaseRepository, "features.boot"), StandardCharsets.UTF_8);

        // Create a snapshot repository
        File snapshotRepository = new File(repositories, "snapshot");
        snapshotRepository.mkdirs();
        Files.write("#feature uris\n" +
                "mvn:other.group.id/other.artifact.id/1.0-SNAPSHOT/xml",
                new File(snapshotRepository, "features.uri"), StandardCharsets.UTF_8);
        Files.write("snapshot-feature",
                new File(snapshotRepository, "features.boot"), StandardCharsets.UTF_8);

        // Read and verify
        assertEquals(Lists.newArrayList(
                new Repository(emptyRepository.toPath(), Collections.emptyList(), Collections.emptyList()),
                new Repository(releaseRepository.toPath(),
                        Lists.newArrayList(new URI("mvn:group.id/artifact.id/2.0.0/xml")),
                        Lists.newArrayList(Feature.builder().withName("released-feature").build())),
                new Repository(snapshotRepository.toPath(),
                        Lists.newArrayList(new URI("mvn:other.group.id/other.artifact.id/1.0-SNAPSHOT/xml")),
                        Lists.newArrayList(Feature.builder().withName("snapshot-feature").build()))),
                karafExtender.getRepositories());
    }

    @Test
    public void handlesMissingRepositoryDirectory() throws Exception {
        tempFolder.getRoot().mkdirs();
        assertEquals(Collections.emptyList(), karafExtender.getRepositories());
    }
}
