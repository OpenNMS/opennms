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
package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract BasicDetector class.</p>
 *
 * @author <a href=mailto:desloge@opennms.com>Donald Desloge</a>
 * @version $Id: $
 */
public abstract class BasicDetector<Request, Response> extends SyncAbstractDetector {
    
    private static final Logger LOG = LoggerFactory.getLogger(BasicDetector.class);
    
    private ClientConversation<Request, Response> m_conversation = new ClientConversation<Request, Response>();
    
    /**
     * <p>Constructor for BasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    protected BasicDetector(String serviceName, int port, int timeout, int retries) {
        super(serviceName, port, timeout, retries);
    }
    
    /**
     * <p>Constructor for BasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected BasicDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isServiceDetected(final InetAddress address) {
    	final String ipAddr = InetAddressUtils.str(address);
    	final int port = getPort();
    	final int retries = getRetries();
        final int timeout = getTimeout();
        LOG.info("isServiceDetected: Checking address: {} for {} capability on port {}", ipAddr, getServiceName(), getPort());

        final Client<Request, Response> client = getClient();
        for (int attempts = 0; attempts <= retries; attempts++) {

            try {
                client.connect(address, port, timeout);
                LOG.info("isServiceDetected: Attempting to connect to address: {}, port: {}, attempt: #{}", ipAddr, port, attempts);
                
                if (attemptConversation(client)) {
                    return true;
                }
                
            } catch (ConnectException e) {
                // Connection refused!! Continue to retry.
                LOG.info("isServiceDetected: {}: Unable to connect to address: {} port {}, attempt #{}",getServiceName(), ipAddr, port, attempts, e);
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                LOG.info("isServiceDetected: {}: No route to address {} was available", getServiceName(), ipAddr, e);
            } catch (final PortUnreachableException e) {
                // Port unreachable
                LOG.info("isServiceDetected: {}: Port unreachable while connecting to address {} port {} within timeout: {} attempt: {}", getServiceName(), ipAddr, port, timeout, attempts, e);
            } catch (InterruptedIOException e) {
                // Expected exception
                LOG.info("isServiceDetected: {}: Did not connect to address {} port {} within timeout: {} attempt: {}", getServiceName(), ipAddr, port, timeout, attempts, e);
            } catch (IOException e) {
                LOG.error("isServiceDetected: {}: An unexpected I/O exception occured contacting address {} port {}",getServiceName(), ipAddr, port, e);
            } catch (Throwable t) {
                LOG.error("isServiceDetected: {}: Unexpected error trying to detect {} on address {} port {}", getServiceName(), getServiceName(), ipAddr, port, t);
            } finally {
                client.close();
            }
        }
        return false;
    }
    
    /**
     * <p>dispose</p>
     */
    @Override
    public void dispose(){
        // Do nothing by default
    }

    /**
     * <p>getClient</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.Client} object.
     */
    abstract protected Client<Request, Response> getClient();

    private boolean attemptConversation(Client<Request, Response> client) throws IOException, Exception {
        return getConversation().attemptConversation(client);
    }
    
    /**
     * <p>expectBanner</p>
     *
     * @param bannerValidator a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    protected final void expectBanner(ResponseValidator<Response> bannerValidator) {
        getConversation().expectBanner(bannerValidator);
    }
    
    /**
     * <p>send</p>
     *
     * @param requestBuilder a {@link org.opennms.netmgt.provision.support.RequestBuilder} object.
     * @param responseValidator a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    protected final void send(RequestBuilder<Request> requestBuilder, ResponseValidator<Response> responseValidator) {
        getConversation().addExchange(requestBuilder, responseValidator);
    }
    /**
     * <p>send</p>
     *
     * @param request a Request object.
     * @param responseValidator a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    protected void send(Request request, ResponseValidator<Response> responseValidator) {
        getConversation().addExchange(request, responseValidator);
    }
    
    /**
     * <p>getConversation</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.ClientConversation} object.
     */
    protected final ClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }
    
}
