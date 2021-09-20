/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.camel;

import org.apache.camel.component.properties.PropertiesFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The apply method is called on each message to determine in which Elasticsearch index 
 * messages should end up into.
 *
 * http://unicolet.blogspot.com/2015/01/camel-elasticsearch-create-timestamped.html
 *
 * Created:
 * User: unicoletti
 * Date: 11:12 AM 6/24/15
 */
public class IndexNameFunction implements PropertiesFunction {

    private static final Logger LOG = LoggerFactory.getLogger(IndexNameFunction.class);

    public static final String DEFAULT_INDEX_DATE_FORMAT = "yyyy.MM";

    private final SimpleDateFormat m_df;

    public IndexNameFunction() {
        this(DEFAULT_INDEX_DATE_FORMAT);
    }

    public IndexNameFunction(String dateFormat) {
        m_df = new SimpleDateFormat(dateFormat == null ? DEFAULT_INDEX_DATE_FORMAT : dateFormat);
    }

    @Override
    public String getName() {
        return "index";
    }

    @Override
    public String apply(String remainder) {
        return apply(m_df, remainder, new Date());
    }

    public String apply(String remainder, Date date) {
        return apply(m_df, remainder, date);
    }

    public static String apply(SimpleDateFormat df, String remainder, Date date) {
        String result = remainder.toLowerCase() + "-" + df.format(date);

        if(LOG.isTraceEnabled()) {
            LOG.trace("IndexNameFunction.apply=" + result);
        }

        return result;
    }
}
