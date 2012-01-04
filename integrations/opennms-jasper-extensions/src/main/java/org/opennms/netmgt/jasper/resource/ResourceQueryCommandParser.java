package org.opennms.netmgt.jasper.resource;


public class ResourceQueryCommandParser{
    
    /**
     * @param resourceQueryParserTest
     */
    public ResourceQueryCommandParser() {}

    private ResourceQuery m_currentQuery;
    private StringBuffer m_buffer;
    
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
        
        for(int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if(c == '-') {
                if(i > 0) {
                    processCommand();
                }
                continue;
            }
            appendWord(c);
            
            if(i == command.length() -1) {
                processCommand();
            }
        }
        
        return retVal;
    }

    private void processCommand() {
        String command = m_buffer.toString();
        m_buffer = null;
        
        if(command.toLowerCase().contains("rrddir")) {
            processRrdDir(command);
        }else if(command.toLowerCase().contains("nodeid")) {
            processNodeId(command);
        }else if(command.toLowerCase().contains("resourcename")) {
            processResourceName(command);
        }else if(command.toLowerCase().contains("filters")) {
            processFilters(command);
        }
    }

    private void processFilters(String command) {
        String value = command.substring(command.toLowerCase().indexOf("filters") + "filters".length(), command.length());
        String[] strFilters = value.trim().split(";");
        
        getCurrentQuery().setFilters(strFilters);
    }

    private void processResourceName(String command) {
        String value = command.substring(command.toLowerCase().indexOf("resourcename") + "resourcename".length(), command.length());
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

    private void appendWord(char c) {
        if(m_buffer == null) {
            m_buffer = new StringBuffer();
        }
        m_buffer.append(c);
    }

}