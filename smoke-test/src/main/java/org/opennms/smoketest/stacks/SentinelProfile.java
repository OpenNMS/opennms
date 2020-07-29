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
import java.util.Objects;
import java.util.UUID;

/**
 * All the Sentinel related settings that need to be tweaked on
 * a per container basis.
 *
 * @author jwhite
 */
public class SentinelProfile {

    public static final SentinelProfile DEFAULT = SentinelProfile.newBuilder().build();

    private final String id;
    private final boolean jvmDebuggingEnabled;

    private final List<OverlayFile> files;

    private SentinelProfile(Builder builder) {
        id = builder.id;
        jvmDebuggingEnabled = builder.jvmDebuggingEnabled;
        files = Collections.unmodifiableList(builder.files);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String id = UUID.randomUUID().toString();
        private boolean jvmDebuggingEnabled = false;
        private List<OverlayFile> files = new LinkedList<>();

        public Builder withFile(Path source, String target) {
            try {
                files.add(new OverlayFile(source.toUri().toURL(), target));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
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

        public SentinelProfile build() {
            return new SentinelProfile(this);
        }
    }

    public String getId() {
        return id;
    }

    public boolean isJvmDebuggingEnabled() {
        return jvmDebuggingEnabled;
    }

    public List<OverlayFile> getFiles() {
        return files;
    }

}
