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
package org.opennms.netmgt.config.tokenauth;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class TokenAuthConfigurationTest extends XmlTestNoCastor<TokenAuthConfiguration> {

    public TokenAuthConfigurationTest(final TokenAuthConfiguration sampleObject,
                                               final String sampleXml,
                                               final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() {
        // Empty configuration.
        final TokenAuthConfiguration empty = new TokenAuthConfiguration();

        // A configuration with one auth definition that exercises every
        // optional element: basic-auth, headers, content, jsonpath
        // extraction, and an explicit ttl.
        final TokenAuthConfiguration populated = new TokenAuthConfiguration();
        final TokenAuth auth = new TokenAuth();
        auth.setName("catalyst-prod");
        auth.setUrl("https://catalyst.example.com/dna/system/api/v1/auth/token");
        auth.setMethod("POST");
        auth.setBasicAuth(new BasicAuth("${scv:cc:user}", "${scv:cc:pass}"));
        auth.setHeaders(Arrays.asList(new Header("Content-Type", "application/json")));
        auth.setContent(new Content("application/json", "{}"));
        final TokenFrom tokenFrom = new TokenFrom();
        tokenFrom.setJsonpath("Token");
        auth.setTokenFrom(tokenFrom);
        auth.setTtlSeconds(3300L);
        populated.setAuths(Arrays.asList(auth));

        return Arrays.asList(new Object[][]{
                {
                        empty,
                        "<token-auth-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/token-auth\"/>",
                        "target/classes/xsds/token-auth-configuration.xsd"
                },
                {
                        populated,
                        "<token-auth-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/token-auth\">"
                                + "<token-auth name=\"catalyst-prod\">"
                                + "<url>https://catalyst.example.com/dna/system/api/v1/auth/token</url>"
                                + "<method>POST</method>"
                                + "<basic-auth username=\"${scv:cc:user}\" password=\"${scv:cc:pass}\"/>"
                                + "<header name=\"Content-Type\" value=\"application/json\"/>"
                                + "<content type=\"application/json\">{}</content>"
                                + "<token-from jsonpath=\"Token\"/>"
                                + "<ttl-seconds>3300</ttl-seconds>"
                                + "</token-auth>"
                                + "</token-auth-configuration>",
                        "target/classes/xsds/token-auth-configuration.xsd"
                }
        });
    }
}
