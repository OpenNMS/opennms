/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.stacks;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.opennms.smoketest.containers.MinionContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import com.google.common.io.Resources;

/**
 * All the Minion related settings that need to be tweaked on
 * a per container basis.
 *
 * @author jwhite
 */
public class MinionProfile {

    public static MinionProfile DEFAULT = MinionProfile.newBuilder().build();

    public static final String DEFAULT_LOCATION = "MINION";

    private final String location;
    private final String id;
    private final boolean jvmDebuggingEnabled;
    private final boolean icmpSupportEnabled;
    private final List<OverlayFile> files;

    private final String dominionGrpcScvClientSecret;
    private final Function<MinionContainer, WaitStrategy> waitStrategy;
    private final Map<String, String> legacyConfiguration;

    private MinionProfile(Builder builder) {
        location = builder.location;
        id = builder.id;
        jvmDebuggingEnabled = builder.jvmDebuggingEnabled;
        icmpSupportEnabled = builder.icmpSupportEnabled;
        files = Collections.unmodifiableList(builder.files);
        dominionGrpcScvClientSecret = builder.dominionGrpcScvClientSecret;
        waitStrategy = Objects.requireNonNull(builder.waitStrategy);
        legacyConfiguration = builder.legacyConfiguration; // it is okay for this to be null when config is non-legacy
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String location = DEFAULT_LOCATION;
        private String id = UUID.randomUUID().toString();
        private boolean jvmDebuggingEnabled = false;
        private boolean icmpSupportEnabled = false;
        private List<OverlayFile> files = new LinkedList<>();
        private String dominionGrpcScvClientSecret;
        private Function<MinionContainer, WaitStrategy> waitStrategy = MinionContainer.WaitForMinion::new;
        private Map<String, String> legacyConfiguration = null;

        public Builder withLocation(String location) {
            this.location = Objects.requireNonNull(location);
            return this;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder withJvmDebuggingEnabled(boolean enabled) {
            jvmDebuggingEnabled = enabled;
            return this;
        }

        public Builder withIcmpSupportEnabled(boolean enabled) {
            this.icmpSupportEnabled = enabled;
            return this;
        }

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
         * @param resourceName resource path
         * @param target path the target file related to $OPENNMS_HOME/
         * @return this builder
         */
        public Builder withFile(String resourceName, String target) {
            files.add(new OverlayFile(Resources.getResource(resourceName), target));
            return this;
        }

        public Builder withDominionGrpcScvClientSecret(final String dominionGrpcScvClientSecret) {
            this.dominionGrpcScvClientSecret = dominionGrpcScvClientSecret;
            return this;
        }

        public Builder withWaitStrategy(final Function<MinionContainer, WaitStrategy> waitStrategy) {
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder withLegacyConfiguration(Map<String, String> configuration) {
            this.legacyConfiguration = Collections.unmodifiableMap(configuration);
            return this;
        }

        public MinionProfile build() {
            return new MinionProfile(this);
        }
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public boolean isJvmDebuggingEnabled() {
        return jvmDebuggingEnabled;
    }

    public boolean isIcmpSupportEnabled() {
        return icmpSupportEnabled;
    }

    public List<OverlayFile> getFiles() {
        return files;
    }

    public String getDominionGrpcScvClientSecret() {
        return dominionGrpcScvClientSecret;
    }

    public Function<MinionContainer, WaitStrategy> getWaitStrategy() {
        return waitStrategy;
    }

    public boolean isLegacy() {
        return legacyConfiguration != null;
    }

    public Map<String, String> getLegacyConfiguration() {
        return legacyConfiguration;
    }
}
