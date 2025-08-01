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
