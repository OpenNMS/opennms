package org.opennms.netmgt.jasper.resource;

import net.sf.jasperreports.engine.JRDataSource;

public class ResourceQueryCommand {
    
    JRDataSource executeCommand(String command) {
        JRDataSource dataSource = new ResourceDataSource();
        //TODO: stopped here
        return dataSource;
    }
}
