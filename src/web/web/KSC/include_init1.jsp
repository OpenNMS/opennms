<%!
    protected KSC_PerformanceReportFactory reportFactory = null; 
   
    public void init() throws ServletException {
        try {
            KSC_PerformanceReportFactory.init(); 
            this.reportFactory = KSC_PerformanceReportFactory.getInstance();
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the KSC Reports factory", e );
        }
    }
%>
