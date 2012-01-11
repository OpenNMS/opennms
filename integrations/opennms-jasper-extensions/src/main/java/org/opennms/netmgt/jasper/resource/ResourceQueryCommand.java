package org.opennms.netmgt.jasper.resource;

import net.sf.jasperreports.engine.JRDataSource;

public class ResourceQueryCommand {
    
    JRDataSource executeCommand(String command) {
        
        ResourceQuery query = new ResourceQueryCommandParser().parseQueryCommand(command);
        
        JRDataSource dataSource = new ResourceDataSource(query);

        return dataSource;
    }

}
