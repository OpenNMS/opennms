/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignField;

import com.jaspersoft.ireport.designer.FieldsProvider;
import com.jaspersoft.ireport.designer.FieldsProviderEditor;
import com.jaspersoft.ireport.designer.IReportConnection;
import com.jaspersoft.ireport.designer.data.ReportQueryDialog;

public class ResourceQueryFieldsProvider implements FieldsProvider{

    public String designQuery(IReportConnection connection, String arg1, ReportQueryDialog arg2) throws JRException, UnsupportedOperationException {
        return null;
    }

    public FieldsProviderEditor getEditorComponent(ReportQueryDialog arg0) {
        return null;
    }

    public JRField[] getFields(IReportConnection irConn, JRDataset reportDataset, Map parameters) throws JRException, UnsupportedOperationException {
        String query = "";
        
        if(reportDataset.getQuery() == null || reportDataset.getQuery().getText() == null || reportDataset.getQuery().getText().length() == 0) {
            return new JRField[0];
        }
        
        List<JRField> fields = new ArrayList<>();
        query = reportDataset.getQuery().getText();
        
        addPathColumn(fields);
        if(query.contains("--dsName")) {
            String[] splitDsNames = query.split("--dsName");
            String dsNames = splitDsNames[splitDsNames.length - 1];
            String[] dsNameList = checkForCommandsAndTrim(dsNames).split(","); 
            for(String ds : dsNameList) {
                JRDesignField field = new JRDesignField();
                field.setName(ds.trim());
                field.setDescription(ds.trim());
                field.setValueClass(String.class);
                fields.add(field);
            }
        }
        
        if(query.contains("--string")) {
            String[] splitStrProps = query.split("--string");
            String strProp = splitStrProps[splitStrProps.length - 1];
            String[] strPropList = checkForCommandsAndTrim(strProp).split(","); 
            for(String prop : strPropList) {
                JRDesignField field = new JRDesignField();
                field.setName(prop.trim());
                field.setDescription(prop.trim());
                field.setValueClass(String.class);
                fields.add(field);
            }
        }
        
        return fields.toArray(new JRField[fields.size()]);
        
    }

    private String checkForCommandsAndTrim(String str) {
        if(str.contains("--")) {
            String[] splitStr = str.split("--");
            return splitStr[0];
        }
        
        return str;
    }

    private void addPathColumn(List<JRField> fields) {
        JRDesignField field = new JRDesignField();
        field.setName("Path");
        field.setDescription("Path");
        field.setValueClass(String.class);
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
