package org.opennms.ipv6.summary.gui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;

public class ChartUtils {
    
    public static final String[] SUPPORTED_APPLICATIONS = {"IPV6", "IPV4", "QUAD A", "SINGLE A"};
    
    public static DataTable convertJSONToDataTable(String text) {
        
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.DATE, "Date");
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
        
        
        JSONObject jsonData = (JSONObject) JSONParser.parseStrict(text);
        JSONObject data;
        
        if(jsonData.get("data").isObject() != null) {
            data = jsonData.get("data").isObject();
            JSONArray values = data.get("values").isArray();
            dataTable.addRow();
            Date date = new Date(Long.valueOf(data.get("time").isString().stringValue()));
            dataTable.setValue(0, 0, date);
            
            for(int i = 0; i < values.size(); i++) {
                JSONObject value = values.get(i).isObject();
                if(value != null) {
                    String application = value.get("application").isString().stringValue();
                    insertApplicationData(dataTable, 0, value, application);
                }
            }
            
        }else if(jsonData.get("data").isArray() != null) {
            JSONArray d = jsonData.get("data").isArray();
            
            for(int j = 0; j < d.size(); j++) {
                JSONObject dataPoint = d.get(j).isObject();
                JSONArray values = dataPoint.get("values").isArray(); 
                dataTable.addRow();
                Date date = new Date(Long.valueOf(dataPoint.get("time").isString().stringValue()));
                dataTable.setValue(j, 0, date);
                
                for(int i = 0; i < values.size(); i++) {
                    JSONObject value = values.get(i).isObject();
                    if(value != null) {
                        String application = value.get("application").isString().stringValue();
                        insertApplicationData(dataTable, j, value, application);
                    }
                }
                
            }
            
        }
        
        
        return dataTable;
    }

    private static void insertApplicationData(DataTable dataTable, int index, JSONObject value, String application) {
        if(application.equals("HTTP-v6")) {
            double avail = Double.valueOf(value.get("availability").isString().stringValue());
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
        List<String> locs = new ArrayList<String>();
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
        List<String> locs = new ArrayList<String>();
        
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
            List<String> locs = new ArrayList<String>();
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
