<%!
    public PerformanceModel model = null;
    protected KSC_PerformanceReportFactory reportFactory = null;

    public void init() throws ServletException {
        try {
            initPerfModel();
            initReportFactory();
        }
        catch (Exception e) {
            throw new ServletException( "Could not initialize the Graph Form Page", e );
        }
    }
    
    public void initPerfModel() throws ServletException
    {
        try {
            this.model = new PerformanceModel( org.opennms.web.ServletInitializer.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
        }
    }
    
    public void initReportFactory() throws ServletException
    {
        try {
            KSC_PerformanceReportFactory.init();
            this.reportFactory = KSC_PerformanceReportFactory.getInstance();
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize KSC_PerformanceReportFactory", e );
        }
    }
%>

