<%!
    /** intf can be null */           
    public String[] getRRDNames(int nodeId, String intf, PrefabGraph graph) {
        if(graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }            
    
        String[] columns = graph.getColumns();
        String[] rrds = new String[columns.length];
         
        for(int i=0; i < columns.length; i++ ) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(nodeId);            
            buffer.append(File.separator);
            
            if(intf != null && PerformanceModel.INTERFACE_GRAPH_TYPE.equals(graph.getType())) {             
                buffer.append(intf);
                buffer.append(File.separator);
            }
            
            buffer.append(columns[i]);
            buffer.append(GraphUtil.RRD_SUFFIX);            

            rrds[i] = buffer.toString();
        }   

        return rrds;             
    }



    public String encodeRRDNamesAsParmString(String[] rrds) {
        if(rrds == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        String parmString = "";
        
        if(rrds.length > 0) {
            StringBuffer buffer = new StringBuffer("rrd=");
            buffer.append(java.net.URLEncoder.encode(rrds[0]));
              
            for(int i=1; i < rrds.length; i++ ) {
                buffer.append("&rrd=");
                buffer.append(java.net.URLEncoder.encode(rrds[i]));
            }
            
            parmString = buffer.toString();              
        }
        
        return parmString;
    }
  
  
    /** currently only know how to handle ifSpeed external value; intf can be null */
    public String encodeExternalValuesAsParmString(int nodeId, String intf, PrefabGraph graph) throws java.sql.SQLException {
        if(graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");      
        }
        
        String parmString = "";        
        String[] externalValues = graph.getExternalValues();
        
        if(externalValues != null && externalValues.length > 0) {
            StringBuffer buffer = new StringBuffer();
            
            for(int i=0; i < externalValues.length; i++) {
                if("ifSpeed".equals(externalValues[i])) {
                    String speed = this.getIfSpeed(nodeId, intf);
                    
                    if(speed != null) {
                        buffer.append(externalValues[i]);
                        buffer.append("=");                        
                        buffer.append(speed);   
                        buffer.append("&");                        
                    }
                }
                else {
                    throw new IllegalStateException("Unsupported external value name: " + externalValues[i]);
                }                
            }
            
            parmString = buffer.toString();
        }        
        
        return parmString;
    }
    
    
    public String getIfSpeed(int nodeId, String intf) throws java.sql.SQLException {
        if(intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String speed = null;
        
        try {
            Map intfInfo = org.opennms.netmgt.utils.IfLabel.getInterfaceInfoFromIfLabel(nodeId, intf);

            //if the extended information was found correctly
            if(intfInfo != null) {
                speed = (String)intfInfo.get("snmpifspeed");
            }
        }
        catch (java.sql.SQLException e) {
            this.log("SQLException while trying to fetch extended interface info", e);
        }


        return speed;
    }
%>
