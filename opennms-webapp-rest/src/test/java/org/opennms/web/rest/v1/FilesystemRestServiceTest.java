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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class FilesystemRestServiceTest {
    private static final String SRC_FOLDER = "src/test/resources/NMS-15704/";
    private final List<String> requests = new ArrayList<>();
    private File temporarySecretFile;

    @Before
    public void before() throws IOException {
        XmlTest.initOpennmsHome();
        this.temporarySecretFile = File.createTempFile( "secret-file", "");
        temporarySecretFile.deleteOnExit();
        final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temporarySecretFile));
        bufferedWriter.write("secret-content");
        bufferedWriter.close();
    }

    @Test
    public void testXmlValidation() throws IOException {
        final FilesystemRestService filesystemRestService = new FilesystemRestService(null);
        final HttpServer httpServer = HttpServer.create(new InetSocketAddress(1337), 10);
        httpServer.createContext("/", new TestHttpHandler());
        httpServer.setExecutor(null);
        httpServer.start();

        requests.clear();

        try {
            filesystemRestService.maybeValidateXml(new File(SRC_FOLDER + "NMS-15704-1.xml"));
        } catch (Exception e) {
        }

        System.err.println(requests);
        assertEquals(0, requests.size());

        try {
            filesystemRestService.maybeValidateXml(new File(SRC_FOLDER + "NMS-15704-2.xml"));
        } catch (Exception e) {
        }

        System.err.println(requests);
        assertEquals(0, requests.size());

        httpServer.stop(0);
    }

    @Test
    public void testJettyDoctypeIsAccepted() throws IOException {
        final File file = File.createTempFile("jetty", ".xml");
        file.deleteOnExit();
        Files.writeString(file.toPath(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE Configure PUBLIC \"-//Jetty//Configure//EN\" \"http://www.eclipse.org/jetty/configure.dtd\">\n"
                + "<Configure id=\"Server\"/>");
        new FilesystemRestService(null).maybeValidateXml(file);
    }

    @Test
    public void testEntityExpansionIsLimited() throws IOException {
        // 10^10 expansions of entity a0. The parser must stop at its expansion limit.
        final StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root [<!ENTITY a0 \"aaaaaaaaaa\">");
        for (int i = 1; i <= 9; i++) {
            xml.append("<!ENTITY a").append(i).append(" \"");
            for (int j = 0; j < 10; j++) {
                xml.append("&a").append(i - 1).append(';');
            }
            xml.append("\">");
        }
        xml.append("]><root>&a9;</root>");

        final File file = File.createTempFile("expansion", ".xml");
        file.deleteOnExit();
        Files.writeString(file.toPath(), xml.toString());
        try {
            new FilesystemRestService(null).maybeValidateXml(file);
            fail("The validation must reject a file that exceeds the entity expansion limit");
        } catch (BadRequestException e) {
            final String message = String.valueOf(e.getResponse().getEntity());
            assertTrue(message, message.contains("entity expansions"));
        }
    }

    private String loadDtdFile() throws IOException {
        final File dtdFile = new File(SRC_FOLDER + "hostname_exfiltrate.dtd");
        final String content = Files.readString(dtdFile.toPath());
        return content.replace("#SECRET_FILE#", temporarySecretFile.getAbsolutePath());
    }

    class TestHttpHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange httpExchange) throws IOException {
            requests.add(httpExchange.getRequestURI().toString());

            final String response;

            if ("/hostname_exfiltrate.dtd".equals(httpExchange.getRequestURI().toString())) {
                requests.add(httpExchange.getRequestURI().toString());
                response = loadDtdFile();
                httpExchange.sendResponseHeaders(200, response.length());
            } else {
                response = "Not found";
                httpExchange.sendResponseHeaders(404, response.length());
            }

            final OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
