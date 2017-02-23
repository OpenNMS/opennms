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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The apply method is called on each message to determine in which index it should end up into.
 */
public class IndexNameFunction {

	private static final Logger LOG = LoggerFactory.getLogger(IndexNameFunction.class);

    private SimpleDateFormat df=null;

    public IndexNameFunction() {
        df=new SimpleDateFormat("yyyy.MM");
    }

    public IndexNameFunction(String dateFormat) {
        df=new SimpleDateFormat(dateFormat == null ? "yyyy.MM" : dateFormat);
    }


    public String apply(String rootIndexName) {
        String result=null;
        result=rootIndexName.toLowerCase()+"-"+df.format(new Date());

        if(LOG.isDebugEnabled()) {
            LOG.debug("IndexNameFunction.apply=" + result);
        }
        return result;
    }

    public String apply(String rootIndexName, Date date) {
            String result=null;
            result=rootIndexName.toLowerCase()+"-"+df.format(date);

            if(LOG.isDebugEnabled()) {
                LOG.debug("IndexNameFunction.apply=" + result);
            }

            return result;
        }
}
