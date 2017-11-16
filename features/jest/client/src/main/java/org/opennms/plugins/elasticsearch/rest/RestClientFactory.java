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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.aries.blueprint.compendium.cm.CmPropertyPlaceholder;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opennms.plugins.elasticsearch.rest.credentials.CredentialsDTO;
import org.opennms.plugins.elasticsearch.rest.credentials.CredentialsParser;
import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.searchbox.client.AbstractJestClient;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 * This factory wraps the {@link JestClientFactory} to provide instances of
 * {@link JestClient}.
 */
public class RestClientFactory {
	private static final Logger LOG = LoggerFactory.getLogger(RestClientFactory.class);

	/**
	 * Use the same formula used to compute the number of threads used in the Sink API.
	 */
	private static final int DEFAULT_MAX_TOTAL_CONNECTION_PER_ROUTE = Runtime.getRuntime().availableProcessors() * 2;

	/**
	 * Scale according to the number of connections per route.
	 */
	private static final int DEFAULT_MAX_TOTAL_CONNECTION = DEFAULT_MAX_TOTAL_CONNECTION_PER_ROUTE * 3;

	private HttpClientConfig.Builder clientConfigBuilder;
	private int m_timeout = 0;
	private int m_retries = 0;
	private JestClient client;

	/**
	 * Create a RestClientFactory.
	 *
	 * @param elasticSearchURL Elasticsearch URL, either a single URL or
	 *   multiple URLs that are comma-separated without spaces
	 * @param globalElasticUser Optional HTTP username
	 * @param globalElasticPassword Optional HTTP password
	 */
	public RestClientFactory(final String elasticSearchURL, final String globalElasticUser, final String globalElasticPassword ) throws MalformedURLException {
		final List<String> urls = parseUrl(elasticSearchURL);
		final String user = globalElasticUser != null && !globalElasticUser.isEmpty() ? globalElasticUser : null;
		final String password = globalElasticPassword != null && !globalElasticPassword.isEmpty() ? globalElasticPassword : null;

		// Ensure urls is set
		if (urls.isEmpty()) {
			throw new IllegalArgumentException("No urls have been provided");
		}

		// TODO MVR Fix the following hack:
		// This is a hack, due to our osgi-jetty-bridge: The annotation at the NetflowDocument
		// living in the api module (on the jetty side) are not accessible directly (only as proxy) by the Gson lirary
		// living in the osgi container (osgi-jest-complete module) and therefore @SerializedName annotations don't work.
		// So we instantiate a custom gson with a custom field name policy.
		final Gson gson = new GsonBuilder()
				.setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.create();

		// If multiple URLs are specified in a comma-separated string, split them up
		clientConfigBuilder = new HttpClientConfig.Builder(urls)
					.multiThreaded(true)
					.defaultMaxTotalConnectionPerRoute(DEFAULT_MAX_TOTAL_CONNECTION_PER_ROUTE)
					.maxTotalConnection(DEFAULT_MAX_TOTAL_CONNECTION)
					.gson(gson);

		// Apply optional credentials
		if (user != null && password != null) {
			final URL targetUrl = new URL(urls.get(0));
			if (urls.size() > 1) {
				LOG.warn("Credentials have been defined, but multiple target hosts were found. " +
						"Each host will use the same credentials. Preemptive auth is only enabled for host {}", urls.get(0));
			}

			clientConfigBuilder.defaultCredentials(user, password);
			clientConfigBuilder.setPreemptiveAuth(new HttpHost(targetUrl.getHost(), targetUrl.getPort(), targetUrl.getProtocol()));
		}
	}

	/**
	 * Set the number of times the REST operation will be retried if
	 * an exception is thrown during the operation.
	 * 
	 * @param retries Number of retries.
	 */
	public void setRetries(int retries) {
		m_retries = retries;
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
		setReadTimeout(timeout);
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
		setConnTimeout(timeout);
	}

	public void setConnTimeout(int timeout) {
		m_timeout = timeout;
		clientConfigBuilder.connTimeout(timeout);
	}

	public void setReadTimeout(int timeout) {
		clientConfigBuilder.readTimeout(timeout);
	}

	public void setMultiThreaded(boolean multiThreaded) {
		clientConfigBuilder.multiThreaded(multiThreaded);
	}

	/**
	 * Set the default max connections per route.
	 * By default, we use the number of available processors * 2.
	 *
	 * If a negative value is given, the set is ignored.
	 * This allows us to use -1 as the default in the Blueprint in order
	 * to avoid having to caculate the default again there.
	 *
	 * @param connections default max connections per route
	 */
	public void setDefaultMaxTotalConnectionPerRoute(int connections) {
		if (connections < 0) {
			// Ignore
			return;
		}
		clientConfigBuilder.defaultMaxTotalConnectionPerRoute(connections);
	}

	/**
	 * Set the default max total connections.
	 * By default, we use the default max connections per route * 3.
	 *
	 * If a negative value is given, the set is ignored.
	 * This allows us to use -1 as the default in the Blueprint in order
	 * to avoid having to caculate the default again there.
	 *
	 * @param connections default max connections per route
	 */
	public void setMaxTotalConnection(int connections) {
		if (connections < 0) {
			// Ignore
			return;
		}
		clientConfigBuilder.maxTotalConnection(connections);
	}

	/**
	 * Defines if discovery/sniffing of nodes in the cluster is enabled.
	 *
	 * @param discovery true if discovery should be enabled, false otherwise
	 */
	public void setDiscovery(boolean discovery) {
		clientConfigBuilder.discoveryEnabled(discovery);
	}

	/**
	 * Sets the frequency to discover the nodes in the cluster.
	 * Note: This only works if discovery is enabled.
	 *
	 * @param discoveryFrequencyInSeconds frequency in seconds
	 */
	public void setDiscoveryFrequency(int discoveryFrequencyInSeconds) {
		clientConfigBuilder.discoveryFrequency(discoveryFrequencyInSeconds, TimeUnit.SECONDS);
	}

	public void setMaxConnectionIdleTime(int timeout, TimeUnit unit) {
		clientConfigBuilder.maxConnectionIdleTime(timeout, unit);
	}

	public void setCredentials(CmPropertyPlaceholder credentials) throws IOException {
		final Configuration configuration = credentials.getConfigAdmin().getConfiguration(credentials.getPersistentId());
		final Dictionary<String, Object> properties = configuration.getProperties();
		final List<CredentialsDTO> credentialList = new CredentialsParser().parse(properties);
		if (!credentialList.isEmpty()) {
			final BasicCredentialsProvider customCredentialsProvider = new BasicCredentialsProvider();
			clientConfigBuilder.credentialsProvider(customCredentialsProvider);

			credentialList.stream().forEach(c -> customCredentialsProvider.setCredentials(c.getAuthScope(), c.getCredentials()));
		}
	}

	public JestClient createClient() {
		if (this.client == null) {
			JestClientFactory factory = new JestClientFactory();
			factory.setHttpClientConfig(this.clientConfigBuilder.build());
			this.client = new OnmsJestClient(factory.getObject(), m_timeout, m_retries);
		}
		return this.client;
	}

	private List<String> parseUrl(String elasticURL) {
		if (elasticURL != null) {
			final Set<String> endpoints = Arrays.stream(elasticURL.split(","))
					.filter(url -> url != null && !url.trim().isEmpty())
					.map(url -> url.trim()).collect(Collectors.toSet());
			return new ArrayList<>(endpoints);
		}
		return Collections.emptyList();
	}

}
