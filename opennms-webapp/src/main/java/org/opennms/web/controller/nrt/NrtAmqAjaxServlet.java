/*========================================================================
 *Copyright 2006 Mort Bay Consulting Pty. Ltd.
 *------------------------------------------------------------------------
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at 
 *http://www.apache.org/licenses/LICENSE-2.0
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 *
 * Small changes for Error / Status Codes
 */
//========================================================================

package org.opennms.web.controller.nrt;

import org.apache.activemq.web.MessageListenerServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Small changes for status codes
 *
 * @author Markus Neumann
 */
public class NrtAmqAjaxServlet extends MessageListenerServlet {

    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtAmqAjaxServlet.class);

    private static final long serialVersionUID = -3875280764356406142L;
    private Map<String, byte[]> jsCache = new HashMap<String, byte[]>();
    private long jsLastModified = 1000 * (System.currentTimeMillis() / 1000);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo() != null && request.getPathInfo().endsWith(".js")) {
            logger.debug("A-Request: '{}'", request);
            doJavaScript(request, response);
            logger.debug("B-Response: '{}'", response);
        } else {
            super.doGet(request, response);
        }
    }

    protected void doJavaScript(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Look for a local resource first.
        String js = request.getServletPath() + request.getPathInfo();
        URL url = getServletContext().getResource(js);
        if (url != null) {
            getServletContext().getNamedDispatcher("default").forward(request, response);
            return;
        }

        // Serve from the classpath resources
        String resource = "org/apache/activemq/web" + request.getPathInfo();
        synchronized (jsCache) {

            byte[] data = jsCache.get(resource);
            if (data == null) {
                InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                if (in != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int len = in.read(buf);
                    while (len >= 0) {
                        out.write(buf, 0, len);
                        len = in.read(buf);
                    }
                    in.close();
                    out.close();
                    data = out.toByteArray();
                    jsCache.put(resource, data);
                }
            }

            if (data != null) {

                long ifModified = request.getDateHeader("If-Modified-Since");

                if (ifModified == jsLastModified) {
                    //response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    //response.setStatus(242, "there is no new data");
                    logger.debug("there is no new data");
                    response.setStatus(242);
                } else {
                    response.setContentType("application/x-javascript");
                    response.setContentLength(data.length);
                    response.setDateHeader("Last-Modified", jsLastModified);
                    response.getOutputStream().write(data);
                }
            } else {
                //response.sendError(HttpServletResponse.SC_NOT_FOUND);
                //response.setStatus(200, "there was no data");
                logger.debug("there was no data");
                response.setStatus(223);
            }
        }
    }
}

