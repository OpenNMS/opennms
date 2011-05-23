package org.opennms.ipv6.summary.gui.client;

import java.util.Date;

import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;

public class ChartUtils {

    public static DataTable convertJSONToDataTable(String text) {
        //FIXME: convert the text to DataTable
        
        
        DataTable data = DataTable.create();
        data.addColumn(ColumnType.DATE, "Date");
        data.addColumn(ColumnType.NUMBER, "Quad A records");
        data.addColumn(ColumnType.STRING, "title1");
        data.addColumn(ColumnType.STRING, "text1");
        data.addColumn(ColumnType.NUMBER, "Single A Records");
        data.addColumn(ColumnType.STRING, "title2");
        data.addColumn(ColumnType.STRING, "text2");
        data.addColumn(ColumnType.NUMBER, "IPv6");
        data.addColumn(ColumnType.STRING, "title2");
        data.addColumn(ColumnType.STRING, "text2");
        data.addColumn(ColumnType.NUMBER, "IPv4");
        data.addColumn(ColumnType.STRING, "title2");
        data.addColumn(ColumnType.STRING, "text2");
        data.addRows(12);
        data.setValue(0, 0, new Date(1209614400000L));
        data.setValue(0, 1, Math.random() * 65000);
        data.setValue(0, 4, Math.random() * 65000);
        data.setValue(0, 7, Math.random() * 65000);
        data.setValue(0, 10, Math.random() * 65000);
        
        data.setValue(1, 0, new Date(1209700800000L));
        data.setValue(1, 1, Math.random() * 65000);
        data.setValue(1, 4, Math.random() * 65000);
        data.setValue(1, 7, Math.random() * 65000);
        data.setValue(1, 10, Math.random() * 65000);
        
        data.setValue(2, 0, new Date(1209787200000L));
        data.setValue(2, 1, Math.random() * 65000);
        data.setValue(2, 4, Math.random() * 65000);
        data.setValue(2, 7, Math.random() * 65000);
        data.setValue(2, 10, Math.random() * 65000);
        
        data.setValue(3, 0, new Date(1209873600000L));
        data.setValue(3, 1, Math.random() * 65000);
        data.setValue(3, 4, Math.random() * 65000);
        data.setValue(3, 5, "Outage");
        data.setValue(3, 6, "Google.com IPv6 outage");
        data.setValue(3, 7, Math.random() * 65000);
        data.setValue(3, 10, Math.random() * 65000);
        
        data.setValue(4, 0, new Date(1209960000000L));
        data.setValue(4, 1, 41476);
        data.setValue(4, 2, "Outage");
        data.setValue(4, 3, "yahoo.com outage at 3pm");
        data.setValue(4, 4, Math.random() * 65000);
        data.setValue(4, 7, Math.random() * 65000);
        data.setValue(4, 10, Math.random() * 65000);
        
        data.setValue(5, 0, new Date(1210046400000L));
        data.setValue(5, 1, Math.random() * 65000);
        data.setValue(5, 4, Math.random() * 65000);
        data.setValue(5, 7, Math.random() * 65000);
        data.setValue(5, 10, Math.random() * 65000);
        
        return data;
    }

}
