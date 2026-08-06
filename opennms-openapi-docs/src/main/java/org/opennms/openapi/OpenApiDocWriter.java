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
package org.opennms.openapi;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writes the OpenAPI documents into the build output, driven by the
 * exec-maven-plugin execution in this module's pom.
 */
public final class OpenApiDocWriter {

    private OpenApiDocWriter() {
    }

    /**
     * Run inside Maven, by exec:java rather than exec:exec, this class is loaded by a
     * plugin realm and part of the project classpath is shadowed. The documents still
     * generate and every assertion about them still passes, they are just quietly
     * wrong, so refuse to produce them at all.
     */
    private static void requireForkedJvm() {
        if (OpenApiDocWriter.class.getClassLoader() != ClassLoader.getSystemClassLoader()) {
            throw new IllegalStateException("loaded by " + OpenApiDocWriter.class.getClassLoader()
                    + " rather than the system class loader, so this is not a forked JVM."
                    + " The exec-maven-plugin execution must use the exec goal, not java.");
        }
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: OpenApiDocWriter <output-directory>");
        }

        requireForkedJvm();

        final Path outputDir = Paths.get(args[0]);
        Files.createDirectories(outputDir);

        for (final OpenApiDocGenerator.Api api : OpenApiDocGenerator.Api.values()) {
            final String document = OpenApiDocGenerator.generate(api);
            final Path target = outputDir.resolve(api.getFileName());

            Files.write(target, (document.endsWith("\n") ? document : document + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            System.out.println("wrote " + target.toAbsolutePath());
        }
    }
}
