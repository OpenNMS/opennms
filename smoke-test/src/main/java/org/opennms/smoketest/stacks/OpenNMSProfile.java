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
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * All the OpenNMS related settings that need to be tweaked on
 * a per container basis.
 *
 * @author jwhite
 */
public class OpenNMSProfile {

    public static OpenNMSProfile DEFAULT = OpenNMSProfile.newBuilder().build();

    private final boolean kafkaProducerEnabled;
    private final Map<URL, String> files;

    private OpenNMSProfile(Builder builder) {
        kafkaProducerEnabled = builder.kafkaProducerEnabled;
        files = Collections.unmodifiableMap(builder.files);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean kafkaProducerEnabled = false;
        private Map<URL, String> files = new LinkedHashMap<>();

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
         * @param target path the target file related to $OPENNMS_HOME/
         * @return this builder
         */
        public Builder withFile(Path source, String target) {
            try {
                files.put(source.toUri().toURL(), target);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return this;
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

    public boolean isKafkaProducerEnabled() {
        return kafkaProducerEnabled;
    }

    public Map<URL, String> getFiles() {
        return files;
    }

}
