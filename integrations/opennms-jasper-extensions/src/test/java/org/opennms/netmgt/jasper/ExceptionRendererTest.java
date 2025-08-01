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
package org.opennms.netmgt.jasper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;
import org.opennms.netmgt.jasper.grafana.ExceptionToPngRenderer;

public class ExceptionRendererTest {

    /**
     * Validate that we get some non-empty byte array when rendering the exception.
     */
    @Test
    public void canRenderExceptionToPng() throws IOException {
        Exception ex = new Exception("Oops");
        byte[] bytes = ExceptionToPngRenderer.renderExceptionToPng(ex);
        assertThat(bytes.length, greaterThanOrEqualTo(10));
        // Can be used for manually viewing the PNG
        //writePngToDisk(bytes);
    }

    @SuppressWarnings("unused")
    private static void writePngToDisk(byte[] bytes) {
        try {
            Files.write(Paths.get("/tmp", "oops.png"), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
