/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.elasticsearch.eventforwarder.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This processor bean replaces the message body with the content of the elmapping.json file
 * which is a template mapping for Elasticsearch. The file is loaded from the classpath.
 *
 * Elasticsearch documentation:
 *
 * https://www.elastic.co/guide/en/elasticsearch/guide/current/index-management.html
 *
 * Created:
 * User: unicoletti
 * Date: 7:03 PM 6/25/15
 */
public class ElMappingLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ElMappingLoader.class);

    public void process(Exchange exchange) {
        StringBuffer body=new StringBuffer();

        BufferedReader is=new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("elmapping.json")));
        String l;
        try {
            while((l=is.readLine())!=null) {
                body.append(l);
            }
        } catch (IOException e) {
            LOG.error("Cannot read elasticsearch mapping file", e);
        }

        exchange.getOut().setBody(body.toString());
    }
}
