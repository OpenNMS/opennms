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
package org.opennms.netmgt.provision.detector.ssh.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.provision.detector.ssh.request.NullRequest;
import org.opennms.netmgt.provision.detector.ssh.response.SshResponse;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ssh.InsufficientParametersException;
import org.opennms.netmgt.provision.support.ssh.Ssh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SshClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SshClient implements Client<NullRequest, SshResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SshClient.class);
    private boolean m_isAvailable = false;

    private String m_banner = null;
    private String m_match = null;
    private String m_clientBanner = Ssh.DEFAULT_CLIENT_BANNER;

    public static final int DEFAULT_RETRY = 0;

    /**
     * <p>close</p>
     */
    @Override
    public void close() {

    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws Exception {
        Map<String,?> emptyMap = Collections.emptyMap();
        TimeoutTracker tracker = new TimeoutTracker(emptyMap, SshClient.DEFAULT_RETRY, timeout);

        String banner = m_banner;
        String match = m_match;
        String clientBanner = m_clientBanner;
        PollStatus ps = PollStatus.unavailable();

        Ssh ssh = new Ssh(address, port, tracker.getConnectionTimeout());
        ssh.setClientBanner(clientBanner);

        Pattern regex = null;
        if (match == null && (banner == null || banner.equals("*"))) {
            regex = null;
        } else if (match != null) {
            regex = Pattern.compile(match);
        } else if (banner != null) {
            regex = Pattern.compile(banner);
        }

        for (tracker.reset(); tracker.shouldRetry() && !ps.isAvailable(); tracker.nextAttempt()) {
            try {
                ps = ssh.poll(tracker);
            } catch (InsufficientParametersException e) {
                LOG.error("Caught InsufficientParametersException: {}", e.getMessage(), e);
                break;
            }

        }

        // If banner matching string is null or wildcard ("*") then we
        // only need to test connectivity and we've got that!

        if (regex != null && ps.isAvailable()) {
            String response = ssh.getServerBanner();

            if (response == null) {
                ps = PollStatus.unavailable("server closed connection before banner was recieved.");
            }

            if (!regex.matcher(response).find()) {
                // Got a response but it didn't match... no need to attempt
                // retries
                LOG.debug("isServer: NON-matching response='{}'", response);
                ps = PollStatus.unavailable("server responded, but banner did not match '" + banner + "'");
            } else {
                LOG.debug("isServer: matching response='{}'", response);
            }
        }
        PollStatus result = ps;

        m_isAvailable = result.isAvailable();
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.ssh.response.SshResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public SshResponse receiveBanner() throws IOException, Exception {
        SshResponse response = new SshResponse();
        response.receive(m_isAvailable);
        return response;
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.ssh.request.NullRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.ssh.response.SshResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public SshResponse sendRequest(NullRequest request) throws IOException, Exception {
        return null;
    }

    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(String banner) {
        m_banner = banner;
    }

    /**
     * <p>setMatch</p>
     *
     * @param match a {@link java.lang.String} object.
     */
    public void setMatch(String match) {
        m_match = match;
    }

    /**
     * <p>setClientBanner</p>
     *
     * @param clientBanner a {@link java.lang.String} object.
     */
    public void setClientBanner(String clientBanner) {
        m_clientBanner = clientBanner;
    }

}
