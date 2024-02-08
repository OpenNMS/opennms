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
package org.opennms.features.jest.client.credentials;


import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;


public class ElasticCredentialsTest extends XmlTestNoCastor {

    public ElasticCredentialsTest(Object sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> getData() {
        return Arrays.asList(new Object[][]{
            {
                new ElasticCredentials(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<elastic-credentials/>",
                null
            },
            {
                new ElasticCredentials()
                        .withCredentials(new CredentialsScope("http://localhost:9200", "admin", "admin"))
                        .withCredentials(new CredentialsScope("https://localhost:9333", "ulf", "flu")),
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<elastic-credentials>\n" +
                "    <credentials url=\"http://localhost:9200\" username=\"admin\" password=\"admin\"/>\n" +
                "    <credentials url=\"https://localhost:9333\" username=\"ulf\" password=\"flu\"/>\n" +
                "</elastic-credentials>",
                null
            }
        });
    }


}