/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.opennms.core.utils.TimeoutTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.config.HttpClientConfig;

/**
 * This factory wraps the {@link JestClientFactory} to provide instances of
 * {@link JestClient}.
 */
public class RestClientFactory {

	private static final Logger LOG = LoggerFactory.getLogger(RestClientFactory.class);

	private final JestClientFactory factory;
	private HttpClientConfig config;

	private AtomicInteger m_socketTimeout = new AtomicInteger(0);
	private AtomicInteger m_timeout = new AtomicInteger(0);
	private AtomicInteger m_retries = new AtomicInteger(0);

	private static class OnmsJestClient implements JestClient {

		private static final Logger LOG = LoggerFactory.getLogger(OnmsJestClient.class);

		private final JestClient m_delegate;

		private final int m_retries;

		private final int m_timeout;

		public OnmsJestClient(JestClient delegate, int timeout, int retries) {
			m_delegate = delegate;
			m_timeout = timeout;
			m_retries = retries;
		}

		/**
		 * Perform the REST operation and retry in case of exceptions.
		 */
		@Override
		public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
			// 'strict-timeout' will enforce that the timeout time elapses between subsequent
			// attempts even if the operation returns more quickly than the timeout
			Map<String,Object> params = new HashMap<>();
			params.put("strict-timeout", Boolean.TRUE);

			TimeoutTracker timeoutTracker = new TimeoutTracker(params, m_retries, m_timeout);

			for (timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
				timeoutTracker.startAttempt();
				try {
					return m_delegate.execute(clientRequest);
				} catch (Exception e) {
					LOG.warn("Exception while trying to execute REST operation (attempt {})", timeoutTracker.getAttempt() + 1, e);
				}
			}
			return null;
		}

		@Override
		public <T extends JestResult> void executeAsync(Action<T> clientRequest, JestResultHandler<? super T> jestResultHandler) {
			m_delegate.executeAsync(clientRequest, jestResultHandler);
		}

		@Override
		public void shutdownClient() {
			m_delegate.shutdownClient();
		}

		@Override
		public void setServers(Set<String> servers) {
			m_delegate.setServers(servers);
		}
	}

	/**
	 * Create a RestClientFactory.
	 * 
	 * @param elasticSearchURL Elasticsearch URL, either a single URL or
	 *   multiple URLs that are comma-separated without spaces
	 * @param esusername Optional HTTP username
	 * @param espassword Optional HTTP password
	 */
	public RestClientFactory(String elasticSearchURL, String esusername, String espassword ) throws MalformedURLException {

		// If multiple URLs are specified in a comma-separated string, split them up
		final List<String> targetHosts = Arrays.asList(elasticSearchURL.split(","));
		final HttpClientConfig.Builder configBuilder = new HttpClientConfig.Builder(targetHosts).multiThreaded(true);
		if (!Strings.isNullOrEmpty(esusername) && !Strings.isNullOrEmpty(espassword)) {
			final URL targetUrl = new URL(targetHosts.get(0));

			if (targetHosts.size() > 1) {
				LOG.warn("Credentials have been defined, but multiple target hosts were found. " +
						"Each host will use the same credentials. Preemptive auth is only enabled for host {}", targetHosts.get(0));
			}

			configBuilder.defaultCredentials(esusername, espassword);
			configBuilder.setPreemptiveAuth(new HttpHost(targetUrl.getHost(), targetUrl.getPort(), targetUrl.getProtocol()));
		}
		config = configBuilder.build();

		factory = new JestClientFactory();

		factory.setHttpClientConfig(config);

	}

	public int getRetries() {
		return m_retries.get();
	}

	/**
	 * Set the number of times the REST operation will be retried if
	 * an exception is thrown during the operation.
	 * 
	 * @param retries Number of retries.
	 */
	public void setRetries(int retries) {
		m_retries.set(retries);
	}

	public int getSocketTimeout() {
		return m_socketTimeout.get();
	}

	/**
	 * Set the socket timeout (SO_TIMEOUT) for the REST connections. This is the
	 * maximum period of inactivity while waiting for incoming data.
	 * 
	 * A default value of 3000 is specified in {@link io.searchbox.client.config.ClientConfig.AbstractBuilder<T,K>}.
	 * 
	 * @param timeout Timeout in milliseconds.
	 */
	public void setSocketTimeout(int timeout) {
		m_socketTimeout.set(timeout);
		config = new HttpClientConfig.Builder(config).readTimeout(timeout).build();
		factory.setHttpClientConfig(config);
	}

	public int getTimeout() {
		return m_timeout.get();
	}

	/**
	 * Set the connection timeout for the REST connections. A default value 
	 * of 3000 is specified in {@link io.searchbox.client.config.ClientConfig.AbstractBuilder<T,K>}.
	 * 
	 * This is also used as the minimum interval between successive retries
	 * if the connection is refused in a shorter amount of time.
	 * 
	 * @param timeout Timeout in milliseconds.
	 */
	public void setTimeout(int timeout) {
		m_timeout.set(timeout);
		config = new HttpClientConfig.Builder(config).connTimeout(timeout).build();
		factory.setHttpClientConfig(config);
	}

	public JestClient getJestClient(){
		return new OnmsJestClient(factory.getObject(), getTimeout(), getRetries());
	}

}
