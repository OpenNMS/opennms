/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.jrobin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignField;

import com.jaspersoft.ireport.designer.FieldsProvider;
import com.jaspersoft.ireport.designer.FieldsProviderEditor;
import com.jaspersoft.ireport.designer.IReportConnection;
import com.jaspersoft.ireport.designer.data.ReportQueryDialog;

public class JRobinFieldsProvider implements FieldsProvider  {
    
    
    Pattern m_pattern = Pattern.compile("(?s)XPORT+.*?:+.*?:([a-z,A-Z]+.*?)");
    
    public String designQuery(IReportConnection reportConnection, String arg1, ReportQueryDialog arg2) throws JRException, UnsupportedOperationException {
        
        return null;
    }

    public FieldsProviderEditor getEditorComponent(ReportQueryDialog dialog) {
        
        return null;
    }

    public JRField[] getFields(IReportConnection irConn, JRDataset reportDataset, Map parameters) throws JRException, UnsupportedOperationException {
        String query = "";
        
        if(reportDataset.getQuery() == null || reportDataset.getQuery().getText() == null || reportDataset.getQuery().getText().length() == 0) {
            return new JRField[0];
        }
        
        List<JRField> fields = new ArrayList<>();
        
        //Add timestamp field
        addTimestampField(fields);
        
        query = reportDataset.getQuery().getText();
        
        if(query.contains("--step")) {
            addStepField(fields);
        }
        
        
        Matcher matcher = m_pattern.matcher(query);
        boolean matchFound = matcher.find();
        
        int i =0;
        while(matchFound) {
            JRDesignField field = new JRDesignField();
            String fieldName = matcher.group();
            if(fieldName.contains(":")) {
                String[] split = fieldName.split(":");
                fieldName = split[split.length -1];
            }
            field.setName(fieldName);
            field.setValueClass(Double.class);
            field.setDescription(fieldName);
            fields.add(field);

            matchFound = matcher.find();
            i++;
        }
        
        System.out.println("match count: " + matcher.groupCount());
        
        return fields.toArray(new JRField[fields.size()]);
    }

  private void addStepField(List<JRField> fields) {
        JRDesignField field = new JRDesignField();
        field.setName("Step");
        field.setDescription("Step");
        field.setValueClass(Integer.class);
        fields.add(field);
        
    }

  private void addTimestampField(List<JRField> fields) {
        JRDesignField field = new JRDesignField();
        field.setName("Timestamp");
        field.setDescription("Timestamp");
        field.setValueClass(Date.class);
        fields.add(field);
    }

    public boolean hasEditorComponent() {
        return false;
    }

    public boolean hasQueryDesigner() {
        return false;
    }

    public boolean supportsAutomaticQueryExecution() {
        return false;
    }

    public boolean supportsGetFieldsOperation() {
        return true;
    }

}
