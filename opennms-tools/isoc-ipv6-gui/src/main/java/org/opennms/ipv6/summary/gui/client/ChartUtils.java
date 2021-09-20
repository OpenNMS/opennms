/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.ipv6.summary.gui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;

public abstract class ChartUtils {
    
    public static final String[] SUPPORTED_APPLICATIONS = {"IPV6", "IPV4", "QUAD A", "SINGLE A"};
    
    public static DataTable convertJSONToDataTable(String text) {
        
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.DATETIME, "Date");
        dataTable.addColumn(ColumnType.NUMBER, "DNS-AAAA");
        dataTable.addColumn(ColumnType.STRING, "title1");
        dataTable.addColumn(ColumnType.STRING, "text1");
        dataTable.addColumn(ColumnType.NUMBER, "DNS-A");
        dataTable.addColumn(ColumnType.STRING, "title2");
        dataTable.addColumn(ColumnType.STRING, "text2");
        dataTable.addColumn(ColumnType.NUMBER, "HTTP-v6");
        dataTable.addColumn(ColumnType.STRING, "title2");
        dataTable.addColumn(ColumnType.STRING, "text2");
        dataTable.addColumn(ColumnType.NUMBER, "HTTP-v4");
        dataTable.addColumn(ColumnType.STRING, "title2");
        dataTable.addColumn(ColumnType.STRING, "text2");
        
        final JSONObject jsonData = (JSONObject) JSONParser.parseStrict(text);
        final JSONArray d = jsonData.isArray();

        for (int j = 0; j < d.size(); j++) {
            final JSONObject dataPoint = d.get(j).isObject();
            final JSONArray values = dataPoint.get("values").isArray();
            dataTable.addRow();

            final Date date = new Date(Double.valueOf(dataPoint.get("time").isNumber().doubleValue()).longValue());
            dataTable.setValue(j, 0, date);

            for (int i = 0; i < values.size(); i++) {
                final JSONObject value = values.get(i).isObject();
                if (value != null) {
                    final String application = value.get("application").isString().stringValue();
                    insertApplicationData(dataTable, j, value, application);
                }
            }
        }        
        
        return dataTable;
    }

    private static void insertApplicationData(DataTable dataTable, int index, JSONObject value, String application) {
        String decimal = value.get("availability").isString().stringValue().substring(0, 2);
        if(application.equals("HTTP-v6")) {
            double avail = Double.valueOf(decimal);
            dataTable.setValue(index, 7, avail);
            //dataTable.setValue(index, 1, avail);
        }else if(application.equals("HTTP-v4")) {
            double avail = Double.valueOf(value.get("availability").isString().stringValue());
            dataTable.setValue(index, 10, avail);
            //dataTable.setValue(index, 4, avail);
        }else if(application.equals("DNS-AAAA")) {
            double avail = Double.valueOf(value.get("availability").isString().stringValue());
            dataTable.setValue(index, 1, avail);
        }else if(application.equals("DNS-A")) {
            double avail = Double.valueOf(value.get("availability").isString().stringValue());
            dataTable.setValue(index, 4, avail);
        }
    }

    public static List<String> convertJSONToLocationList(String jsonString) {
        List<String> locs = new ArrayList<>();
        JSONObject locationList = JSONParser.parseStrict(jsonString).isObject();
         
        if(locationList.get("locations").isArray() != null) {
            JSONArray locations = locationList.get("locations").isArray();
            for(int i = 0; i < locations.size(); i++) {
                String value = locations.get(i).isObject().get("name").isString().stringValue();
                locs.add(value);
            }
            return locs;
        }else if(locationList.get("locations").isObject() != null) {
            JSONObject location = locationList.get("locations").isObject();
            locs.add(location.get("area").isString().stringValue());
            return locs;
        }else {
            return locs;
        }
        
    }

    public static List<String> convertJSONToParticipants(String jsonString) {
        List<String> locs = new ArrayList<>();
        
        JSONObject participantList = JSONParser.parseStrict(jsonString).isObject();
         
        if(participantList.get("participants").isArray() != null) {
            JSONArray participants = participantList.get("participants").isArray();
            
            for(int i = 0; i < participants.size(); i++) {
                String value = participants.get(i).isObject().get("name").isString().stringValue();
                locs.add(value);
            }
            return locs;
        }else if(participantList.get("participants").isObject() != null) {
            JSONObject participant = participantList.get("participants").isObject();
            locs.add(participant.get("name").isString().stringValue());
            return locs;
        }else {
            return locs;
        }
    }

    private static List<String> parseJSONArrayToList(JSONArray participants) {
        if(participants != null) {
            List<String> locs = new ArrayList<>();
            for(int i = 0; i < participants.size(); i++) {
                String value = participants.get(i).isObject().get("value").isString().stringValue();
                locs.add(value);
            }
            return locs;
        }else {
            return null;
        }
    }

}
