package org.opennms.netmgt.jasper.resource;


public class ResourceQueryCommandParser{
    
    /**
     * @param resourceQueryParserTest
     */
    public ResourceQueryCommandParser() {}

    private ResourceQuery m_currentQuery;
    private static String DELIMETER = ",";
    
    private void setCurrentQuery(ResourceQuery q) {
        m_currentQuery = q;
    }
    
    private ResourceQuery getCurrentQuery() {
        return m_currentQuery;
    }
    
    public ResourceQuery parseQueryCommand(String queryCommand) {
        ResourceQuery retVal = new ResourceQuery();
        setCurrentQuery(retVal);
        
        String command = queryCommand.trim();
        
        String[] cmdArray = command.split("--");
        
        for(String cmd : cmdArray) {
            processCommand(cmd.trim());
        }
        
        return retVal;
    }


    private void processCommand(String command) {
        
        if(command.toLowerCase().contains("rrddir")) {
            processRrdDir(command);
        }else if(command.toLowerCase().contains("nodeid")) {
            processNodeId(command);
        }else if(command.toLowerCase().contains("resourcetype")) {
            processResourceName(command);
        }else if(command.toLowerCase().contains("dsname")) {
            processFilters(command);
        }else if(command.toLowerCase().contains("string")) {
            processStringProperties(command);
        }
    }

    private void processStringProperties(String command) {
        String value = command.substring(command.toLowerCase().indexOf("string") + "string".length(), command.length());
        String[] strProperties = value.trim().split(DELIMETER);
        
        getCurrentQuery().setStringProperties(strProperties);
    }

    private void processFilters(String command) {
        String value = command.substring(command.toLowerCase().indexOf("dsname") + "dsname".length(), command.length());
        String[] strFilters = value.trim().split(DELIMETER);
        
        getCurrentQuery().setFilters(strFilters);
    }

    private void processResourceName(String command) {
        String value = command.substring(command.toLowerCase().indexOf("resourcetype") + "resourcetype".length(), command.length());
        getCurrentQuery().setResourceName(value.trim());
    }

    private void processNodeId(String command) {
        String value = command.substring(command.toLowerCase().indexOf("nodeid") + "nodeid".length(), command.length());
        getCurrentQuery().setNodeId(value.trim());
    }

    private void processRrdDir(String command) {
        String value = command.substring(command.toLowerCase().indexOf("rrddir") + "rrdDir".length(), command.length());
        getCurrentQuery().setRrdDir(value.trim());
    }


}