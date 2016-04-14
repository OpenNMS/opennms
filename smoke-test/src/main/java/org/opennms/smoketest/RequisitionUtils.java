/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.smoketest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

/**
 * Utility functions for dealing with provisioning requisitions.
 *
 * The {@link OpenNMSSeleniumTestCase} is meant to be version agnostic,
 * so we maintain these methods here instead.
 *
 * @author jwhite
 */
public class RequisitionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RequisitionUtils.class);

    private final OpenNMSSeleniumTestCase m_testCase;

    public RequisitionUtils(OpenNMSSeleniumTestCase testCase) {
        m_testCase = testCase;
    }

    protected void createNode(String nodeXML) throws IOException, InterruptedException {
        sendPost("/rest/nodes", nodeXML);
    }

    protected void setupTestRequisition(String requisitionXML, String foreignSourceXML) throws IOException, InterruptedException {
        sendPost("/rest/requisitions/", requisitionXML);
        if (foreignSourceXML != null && !"".equals(foreignSourceXML)) {
            sendPost("/rest/foreignSources", foreignSourceXML);
        }
        HttpRequestBase request = new HttpPut(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/requisitions/" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "/import");
        m_testCase.doRequest(request);
    }

    protected void deleteTestRequisition() throws Exception {
        final Integer responseCode = m_testCase.doRequest(new HttpGet(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/requisitions/" + OpenNMSSeleniumTestCase.REQUISITION_NAME));
        LOG.debug("Checking for existing test requisition: {}", responseCode);
        if (responseCode == 404 || responseCode == 204) {
            LOG.debug("deleteTestRequisition: already deleted");
            return;
        }

        for (OnmsNode node : getNodesInDatabase(OpenNMSSeleniumTestCase.REQUISITION_NAME)) {
            m_testCase.doRequest(new HttpDelete(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/nodes/" + node.getId()));
        }
        m_testCase.doRequest(new HttpDelete(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/requisitions/" + OpenNMSSeleniumTestCase.REQUISITION_NAME));
        m_testCase.doRequest(new HttpDelete(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/requisitions/deployed/" + OpenNMSSeleniumTestCase.REQUISITION_NAME));
        m_testCase.doRequest(new HttpDelete(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/foreignSources/" + OpenNMSSeleniumTestCase.REQUISITION_NAME));
        m_testCase.doRequest(new HttpDelete(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/foreignSources/deployed/" + OpenNMSSeleniumTestCase.REQUISITION_NAME));
        m_testCase.doRequest(new HttpGet(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/requisitions"));
    }

    public OnmsNodeList getNodesInDatabase(final String foreignSource) {
        try (HttpClientWrapper httpClient = HttpClientWrapper.create()) {
            httpClient.addBasicCredentials(OpenNMSSeleniumTestCase.BASIC_AUTH_USERNAME, OpenNMSSeleniumTestCase.BASIC_AUTH_PASSWORD);
            httpClient.usePreemptiveAuth();
            HttpGet request = new HttpGet(OpenNMSSeleniumTestCase.BASE_URL + "/opennms/rest/nodes");
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_UTF_8.toString());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                    return JaxbUtils.unmarshal(OnmsNodeList.class, reader);
                }
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected final class WaitForNodesInDatabase implements ExpectedCondition<Boolean> {
        private final int m_numberToMatch;
        public WaitForNodesInDatabase(int numberOfNodes) {
            m_numberToMatch = numberOfNodes;
        }

        @Override public Boolean apply(final WebDriver input) {
            return getNodesInDatabase(OpenNMSSeleniumTestCase.REQUISITION_NAME).size() == m_numberToMatch;
        }
    }

    protected void sendPost(final String urlFragment, final String body) throws ClientProtocolException, IOException, InterruptedException {
        final HttpPost post = new HttpPost(OpenNMSSeleniumTestCase.BASE_URL + "opennms" + (urlFragment.startsWith("/")? urlFragment : "/"+urlFragment));
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_XML));
        final Integer response = m_testCase.doRequest(post);
        if (response < 200 || response >= 300) {
            throw new RuntimeException("Bad response code! (" + response + ")");
        }
    }
}
