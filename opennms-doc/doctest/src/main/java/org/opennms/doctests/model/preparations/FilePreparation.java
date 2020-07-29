/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.doctests.model.preparations;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;

import org.opennms.doctests.model.Preparation;
import org.opennms.doctests.model.Sequence;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OverlayFile;
import org.opennms.smoketest.stacks.StackModel;

import com.google.common.base.MoreObjects;
import com.google.common.io.Files;

public class FilePreparation extends Preparation {
    public static final Path OPENNMS_HOME = Paths.get("${OPENNMS_HOME}");

    private final Path path;

    private FilePreparation(final Builder builder) {
        super(builder);
        this.path = Objects.requireNonNull(builder.path);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Path getPath() {
        return this.path;
    }

    public void prepare(final Sequence sequence,
                        final OpenNMSProfile.Builder profile) throws Exception {
        // Assume `${OPENNMS_HOME}` prefix
        if (!this.path.startsWith(OPENNMS_HOME)) {
            throw new AssertionError("path must start with '${OPENNMS_HOME}'");
        }

        final Path path = OPENNMS_HOME.relativize(this.path);

        // Copy snippet content to temporary file
        final File temp = File.createTempFile(String.format("%s-%s", sequence.getId(), path.getFileName().toString()), null);
        temp.deleteOnExit();
        Files.write(this.getContent().getBytes(), temp);

        // Add the temporary file to the overlay
        profile.withFile(temp.toURI().toURL(), path.toString());
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                    .add("path", this.path);
    }

    @Override
    public String toString() {
        return this.toStringHelper().toString();
    }

    public static class Builder extends Preparation.Builder<Builder> {
        private Path path;

        private Builder() {
        }

        @Override
        protected Builder adapt() {
            return this;
        }

        public Builder withPath(final Path path) {
            this.path = path;
            return this;
        }

        @Override
        public FilePreparation build() {
            return new FilePreparation(this);
        }
    }
}
