/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.jrobin;

import java.util.Date;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;

public class JRobinQueryExecutor extends JRAbstractQueryExecuter {

    protected JRobinQueryExecutor(JRDataset dataset, Map<?,?> parametersMap) {
        super(dataset, parametersMap);
        parseQuery();
    }

    @Override
    public boolean cancelQuery() throws JRException {
        return false;
    }

    @Override
    public void close() {
        
    }

    @Override
    public JRDataSource createDatasource() throws JRException {
        try {
            return new RrdXportCmd().executeCommand(getQueryString());
        } catch (Throwable e) {
            throw new JRException("Error creating JRobinDataSource with command: " + getQueryString(), e);
        }
    }

    @Override
    protected String getParameterReplacement(String parameterName) {
        Object parameterVal = getParameterValue(parameterName);
        if(parameterVal instanceof Date) {
            Date date = (Date) parameterVal;
            return String.valueOf(date.getTime()/1000);
        }

        return String.valueOf(parameterVal);
    }


}
