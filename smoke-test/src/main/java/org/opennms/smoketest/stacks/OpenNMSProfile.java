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
package org.opennms.smoketest.stacks;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.opennms.smoketest.containers.OpenNMSContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import com.google.common.io.Resources;

/**
 * All the OpenNMS related settings that need to be tweaked on
 * a per container basis.
 *
 * @author jwhite
 */
public class OpenNMSProfile {

    public static OpenNMSProfile DEFAULT = OpenNMSProfile.newBuilder().build();

    private final boolean jvmDebuggingEnabled;
    private final boolean kafkaProducerEnabled;
    private final List<OverlayFile> files;
    private final Function<OpenNMSContainer, WaitStrategy> waitStrategy;
    private final HashMap<String, Path> installFeatures;

    private OpenNMSProfile(Builder builder) {
        jvmDebuggingEnabled = builder.jvmDebuggingEnabled;
        kafkaProducerEnabled = builder.kafkaProducerEnabled;
        files = Collections.unmodifiableList(builder.files);
        waitStrategy = Objects.requireNonNull(builder.waitStrategy);
        installFeatures = Objects.requireNonNull(builder.installFeatures);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean jvmDebuggingEnabled = false;
        private boolean kafkaProducerEnabled = false;
        private List<OverlayFile> files = new LinkedList<>();
        private Function<OpenNMSContainer, WaitStrategy> waitStrategy = OpenNMSContainer.WaitForOpenNMS::new;
        private HashMap<String, Path> installFeatures = new LinkedHashMap<>();

        /**
         * Enable/disable JVM debugging.
         *
         * @param enabled true if enabled, false otherwise
         * @return this builder
         */
        public Builder withJvmDebuggingEnabled(boolean enabled) {
            jvmDebuggingEnabled = enabled;
            return this;
        }

        /**
         * Enable/disable the Kafka producer feature.
         *
         * This will automatically enable Kafka on the stack if it is not already enabled.
         *
         * @param enabled true if enabled, false otherwise
         * @return this builder
         */
        public Builder withKafkaProducerEnabled(boolean enabled) {
            kafkaProducerEnabled = enabled;
            return this;
        }

        /**
         * Add files to the container over
         *
         * @param source path to the source file on disk
         * @param target path the target file relative to $OPENNMS_HOME/
         * @return this builder
         */
        public Builder withFile(Path source, String target) {
            try {
                files.add(new OverlayFile(source.toUri().toURL(), target));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        /**
         * Add files to the container over
         *
         * @param source source URL
         * @param target path the target file related to $OPENNMS_HOME/
         * @return this builder
         */
        public Builder withFile(URL source, String target) {
            files.add(new OverlayFile(source, target));
            return this;
        }

        /**
         * Add files to the container over
         *
         * @param resourceName resource path
         * @param target path the target file related to $OPENNMS_HOME/
         * @return this builder
         */
        public Builder withFile(String resourceName, String target) {
            files.add(new OverlayFile(Resources.getResource(resourceName), target));
            return this;
        }

        /**
         * Add files to the container over
         *
         * @param source source URL
         * @param target path the target file related to $OPENNMS_HOME/
         * @param permissions file permissions to set
         * @return this builder
         */
        public Builder withFile(URL source, String target, Set<PosixFilePermission> permissions) {
            files.add(new OverlayFile(source, target, permissions));
            return this;
        }

        /**
         * Add files to the container over
         *
         * @param resourceName resource path
         * @param target path the target file related to $OPENNMS_HOME/
         * @param permissions file permissions to set
         * @return this builder
         */
        public Builder withFile(String resourceName, String target, Set<PosixFilePermission> permissions) {
            files.add(new OverlayFile(Resources.getResource(resourceName), target, permissions));
            return this;
        }

        public Builder withWaitStrategy(final Function<OpenNMSContainer, WaitStrategy> waitStrategy) {
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder withInstallFeature(final String feature) {
            return withInstallFeature(feature, null, null);
        }
        public Builder withInstallFeature(final String feature, final String waitForKar) {
            return withInstallFeature(feature, waitForKar, null);
        }
        public Builder withInstallFeature(final String feature, final String waitForKar, final Path karFile) {
            if (waitForKar != null) {
                installFeatures.put(String.format("%s wait-for-kar=%s", feature, waitForKar), karFile);
            } else {
                installFeatures.put(feature, karFile);
            }
            return this;
        }

        public Builder withDisableFeature(final String feature) {
            return withInstallFeature(String.format("!%s", feature), null, null);
        }

        /**
         * Build the profile.
         *
         * @return an immutable profile
         */
        public OpenNMSProfile build() {
            return new OpenNMSProfile(this);
        }

    }

    public boolean isJvmDebuggingEnabled() {
        return jvmDebuggingEnabled;
    }

    public boolean isKafkaProducerEnabled() {
        return kafkaProducerEnabled;
    }

    public List<OverlayFile> getFiles() {
        return files;
    }

    public Function<OpenNMSContainer, WaitStrategy> getWaitStrategy() {
        return waitStrategy;
    }

    public HashMap<String, Path> getInstallFeatures() {
        return installFeatures;
    }
}
