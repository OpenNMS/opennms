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
package org.opennms.web.mail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.EmailValidator;
import org.opennms.core.utils.StreamUtils;
import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;
import org.opennms.web.servlet.MissingParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MailerServlet class.</p>
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class MailerServlet extends HttpServlet {
    /**
     * 
     */
    private static final Logger LOG = LoggerFactory.getLogger(MailerServlet.class);
    private static final long serialVersionUID = -6241742874510146572L;

    /** Constant <code>REQUIRED_FIELDS="new String[] { sendto, subject, usernam"{trunked}</code> */
    protected static final String[] REQUIRED_FIELDS = new String[] { "sendto", "subject", "msg" };

    protected String redirectSuccess;

    protected EmailValidator emailValidator = EmailValidator.getInstance();

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter("redirect.success");

        if (this.redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sendto = request.getParameter("sendto");
        String subject = request.getParameter("subject");
        String msg = request.getParameter("msg");
        String username = request.getRemoteUser();

        LOG.debug("To: {}, Subject: {}, message: {}, username: {}", sendto, subject, msg, username);

        if (sendto == null) {
            throw new MissingParameterException("sendto", REQUIRED_FIELDS);
        }
        else if (!emailValidator.isValid(sendto)) {
            throw new IllegalArgumentException("sendto is an invalid email address.");
        }

        if (subject == null) {
            throw new MissingParameterException("subject", REQUIRED_FIELDS);
        }

        if (msg == null) {
            throw new MissingParameterException("msg", REQUIRED_FIELDS);
        }

        try {
            // All other settings are handled internal to JavaMailer via
            // javamail-configuration.properties
            JavaMailer mailer = new JavaMailer();
            mailer.setTo(sendto);
            mailer.setSubject(subject);
            mailer.setMessageText(msg);
            mailer.mailSend();
            response.sendRedirect(this.redirectSuccess);
        }
        catch (JavaMailerException jme) {
            LOG.warn("Issue encountered when sending email", jme);
            PrintWriter out = response.getWriter();
            StreamUtils.streamToStream(new StringReader(jme.getMessage()), out);
            out.close();
        }
    }
}
