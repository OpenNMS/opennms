<%!
    public Report buildNodeReport(int node_id)
    {
        Report node_report = new Report();
        String report_title = "Node Report for Node Number " + node_id;
        node_report.setTitle(report_title);
        node_report.setShow_timespan_button(true);
        node_report.setShow_graphtype_button(true);
 
        String query_interface[] = this.model.getQueryableInterfacesForNode(node_id);
        Arrays.sort (query_interface);
        for (int i=0; i < query_interface.length;  i++) {
            Graph graph = new Graph(); 
            graph.setTitle("");
            graph.setNodeId(String.valueOf(node_id));
            graph.setInterfaceId(query_interface[i]);  
            graph.setTimespan("7_day");
            graph.setGraphtype("octets");
            node_report.addGraph(graph);
        }
        return node_report; 
    }   
%>
