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
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the feature repositories the container boots with from
 * {@code etc/org.apache.karaf.features.cfg}. Features served from one of these
 * repositories belong to the product itself and are never plugins.
 */
final class BootRepositories {
    private static final Logger LOG = LoggerFactory.getLogger(BootRepositories.class);

    static final String CFG_FILE_NAME = "org.apache.karaf.features.cfg";
    static final String KEY = "featuresRepositories";

    /** Repository coordinates that are always the product's own, whatever the cfg says. */
    static final List<String> CORE_REPOSITORY_PREFIXES = Collections.unmodifiableList(java.util.Arrays.asList(
            "mvn:org.opennms.karaf/",
            "mvn:org.opennms.integration.api/"));

    private BootRepositories() {
    }

    /** Never throws: a missing or unreadable file yields an empty set. */
    static Set<String> load(final Path cfg) {
        if (cfg == null || !Files.isRegularFile(cfg)) {
            LOG.debug("Karaf features configuration {} is not readable; no boot repositories known", cfg);
            return Collections.emptySet();
        }
        try {
            return parse(Files.readAllLines(cfg, StandardCharsets.UTF_8));
        } catch (final IOException | RuntimeException e) {
            LOG.warn("Failed to read the boot feature repositories from {}: {}", cfg, e.toString());
            return Collections.emptySet();
        }
    }

    static Set<String> parse(final List<String> rawLines) {
        final List<String> logical = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean continued = false;
        for (final String raw : rawLines) {
            final String line = raw.trim();
            if (!continued && (line.isEmpty() || line.startsWith("#") || line.startsWith("!"))) {
                continue;
            }
            if (line.endsWith("\\")) {
                current.append(line, 0, line.length() - 1);
                continued = true;
                continue;
            }
            current.append(line);
            logical.add(current.toString());
            current.setLength(0);
            continued = false;
        }
        if (current.length() > 0) {
            logical.add(current.toString());
        }

        final Set<String> repositories = new LinkedHashSet<>();
        for (final String entry : logical) {
            final int separator = separatorIndex(entry);
            if (separator < 0 || !KEY.equals(entry.substring(0, separator).trim())) {
                continue;
            }
            for (final String value : entry.substring(separator + 1).split(",")) {
                final String url = value.trim();
                if (!url.isEmpty()) {
                    repositories.add(url);
                }
            }
        }
        return repositories;
    }

    /** True when the repository is one the product ships, by cfg listing or by coordinates. */
    static boolean isCore(final String repositoryUrl, final Set<String> bootRepositories) {
        if (repositoryUrl == null) {
            return false;
        }
        final String url = repositoryUrl.trim();
        if (bootRepositories.contains(url)) {
            return true;
        }
        for (final String prefix : CORE_REPOSITORY_PREFIXES) {
            if (url.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static int separatorIndex(final String entry) {
        final int equals = entry.indexOf('=');
        final int colon = entry.indexOf(':');
        if (equals < 0) {
            return colon;
        }
        return colon < 0 ? equals : Math.min(equals, colon);
    }
}
