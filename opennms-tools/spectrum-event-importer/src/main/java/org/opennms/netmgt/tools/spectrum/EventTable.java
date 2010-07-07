package org.opennms.netmgt.tools.spectrum;

import java.util.HashMap;

public class EventTable extends HashMap<Integer,String> {
    private static final long serialVersionUID = 7016759899154786992L;
    private String m_tableName;
    
    public EventTable(String name) {
        super();
        
        if (name == null) {
            throw new IllegalArgumentException("The name must not be null");
        }
        m_tableName = name;
    }

    public String getTableName() {
        return m_tableName;
    }

    public void setTableName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name must not be null");
        }
        m_tableName = name;
    }
    
    public void put(String keyString, String valueString) {
        int keyInt;
        if (keyString.matches("^0x[0-9A-Fa-f]+$")) {
            keyInt = Integer.parseInt(keyString.substring(2), 16);
        } else if (keyString.matches("^0[0-7]+$")) {
            keyInt = Integer.parseInt(keyString.substring(1), 8);
        } else {
            keyInt = Integer.parseInt(keyString);
        }
        
        put(keyInt, valueString);
    }

}