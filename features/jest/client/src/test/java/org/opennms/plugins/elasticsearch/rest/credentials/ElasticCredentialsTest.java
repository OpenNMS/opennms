/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.credentials;


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