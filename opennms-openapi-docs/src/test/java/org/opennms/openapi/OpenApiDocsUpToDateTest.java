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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Fails if the committed documents have drifted from what the ReST annotations
 * now describe. To refresh them:
 *
 *   mvn -pl opennms-openapi-docs test -Dopenapi.regenerate=true
 */
public class OpenApiDocsUpToDateTest {

    private static final String REGENERATE_PROPERTY = "openapi.regenerate";

    private static final Path RESOURCE_DIR = Paths.get("src", "main", "resources", "openapi");

    @Test
    public void v1DocumentIsUpToDate() throws Exception {
        check(OpenApiDocGenerator.Api.V1);
    }

    @Test
    public void v2DocumentIsUpToDate() throws Exception {
        check(OpenApiDocGenerator.Api.V2);
    }

    private void check(final OpenApiDocGenerator.Api api) throws Exception {
        final String generated = OpenApiDocGenerator.generate(api);
        final Path target = RESOURCE_DIR.resolve(api.getFileName());

        if (Boolean.getBoolean(REGENERATE_PROPERTY)) {
            Files.createDirectories(target.getParent());
            Files.write(target, withTrailingNewline(generated).getBytes(StandardCharsets.UTF_8));
            System.out.println("regenerated " + target.toAbsolutePath());
            return;
        }

        final String committed = read(target);
        assertEquals(api.getTitle() + " is out of date; refresh it with"
                        + " 'mvn -pl opennms-openapi-docs test -D" + REGENERATE_PROPERTY + "=true'"
                        + " and commit " + target,
                committed, withTrailingNewline(generated));
    }

    private static String read(final Path target) throws IOException {
        if (!Files.exists(target)) {
            throw new IOException(target.toAbsolutePath() + " is missing; generate it with"
                    + " 'mvn -pl opennms-openapi-docs test -D" + REGENERATE_PROPERTY + "=true'");
        }
        return new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
    }

    private static String withTrailingNewline(final String document) {
        return document.endsWith("\n") ? document : document + "\n";
    }
}
