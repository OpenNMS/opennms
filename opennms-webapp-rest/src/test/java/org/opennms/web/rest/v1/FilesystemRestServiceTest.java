/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
