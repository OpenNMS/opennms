package org.opennms.report.inventory;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.report.ReportMailer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;

public class InventoryReportRunner implements Runnable {
        
    String theDate;
    String theField;
    String reportFormat;
    String reportEmail;
    String user;
    Date reportRequestDate;

    InventoryReportCalculator calculator;

    ReportRenderer m_htmlReportRenderer;
    ReportRenderer m_nullReportRenderer;
    
    public ReportRenderer getNullReportRenderer() {
        return m_nullReportRenderer;
    }

    public void setNullReportRenderer(ReportRenderer nullReportRenderer) {
        m_nullReportRenderer = nullReportRenderer;
    }

    public ReportRenderer getHtmlReportRenderer() {
        return m_htmlReportRenderer;
    }

    public void setHtmlReportRenderer(ReportRenderer htmlReportRenderer) {
        m_htmlReportRenderer = htmlReportRenderer;
    }

    public String getTheDate() {
        return theDate;
    }

    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    public String getTheField() {
        return theField;
    }

    public void setTheField(String theField) {
        this.theField = theField;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getReportEmail() {
        return reportEmail;
    }

    public void setReportEmail(String reportEmail) {
        this.reportEmail = reportEmail;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    public InventoryReportCalculator getInventoryReportCalculator() {
        return calculator;
    }

    public void setInventoryReportCalculator(
            InventoryReportCalculator inventoryReportCalculator) {
        calculator = inventoryReportCalculator;
    }

    public void run() {

        log().debug("run: getting inventory report on Date ["+ theDate +"] for key [" + theField + "]" + ". Requested by User: " + user + "on Date " 
                    + reportRequestDate.toString());
        ReportRenderer renderer;
        calculator.setReportRequestDate(reportRequestDate);
        calculator.setTheDate(theDate);
        calculator.setUser(user);
        calculator.setTheField(theField);
        
        if (reportFormat.compareTo("pdftype") == 0){
            log().debug("run: generating pdf is still not supported :( sending xml");
            
            renderer = m_nullReportRenderer;
        } else {
            log().debug("runRancidListReport generating html");
            renderer =  m_htmlReportRenderer;
        }

        try {            
            calculator.calculate();
            calculator.writeXML();

            String outputFile = calculator.getOutputFileName();
            log().debug("Written Configuration Report as XML to " + outputFile);
            renderer.setInputFileName(outputFile);
            log().debug("rendering XML " + outputFile + " as "
                    + renderer.getOutputFileName());
            renderer.render();
            ReportMailer mailer = new ReportMailer(
                                                   reportEmail,
                                                   renderer.getBaseDir()
                                                           + renderer.getOutputFileName(), "OpenNMS Inventory Report");
            mailer.send();
        } catch (InventoryCalculationException ce) {
            log().fatal("Unable to calculate report data ", ce);
        } catch (ReportRenderException re) {
            log().fatal("Unable to render report ", re);
        } catch (IOException ioe) {
            log().fatal("Unable to render report ", ioe);
        }
            

/*
            log().debug("InventoryService runNodeBaseInventoryReport object filled");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            String datestamp = fmt.format(new java.util.Date()) ;
            String xmlFileName = ConfigFileConstants.getHome() + "/share/reports/NODEINVENTORY" + datestamp + ".xml";

            // Generate source XML
            FileWriter writer = new FileWriter(xmlFileName);
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(rnbi);
            writer.close();
            log().debug("runNodeBaseInventoryReport marshal done");

            if (reportFormat.compareTo("pdftype") == 0){

                log().debug("runNodeBaseInventoryReport generating pdf is still not supported :( sending xml");
                log().debug("runNodeBaseInventoryReport xml sending email");
                ReportMailer mailer = new ReportMailer(reportEmail,xmlFileName,"OpenNMS Inventory Report");
                mailer.send();


            } else {

                log().debug("runNodeBaseInventoryReport generating html");

                String htmlFileName=ConfigFileConstants.getHome() + "/share/reports/NODEINVENTORY" + datestamp + ".html";

                File file = new File(htmlFileName);
                FileOutputStream hmtlFileWriter = new FileOutputStream(file);
                PDFWriter htmlWriter = new PDFWriter(ConfigFileConstants.getFilePathString() + "/rws-nbinventoryreport.xsl");
                File fileR = new File(xmlFileName);
                Reader fileReader = new InputStreamReader(new FileInputStream(fileR), "UTF-8");
                htmlWriter.generateHTML(fileReader, hmtlFileWriter);
                log().debug("runNodeBaseInventoryReport html sending email");
                ReportMailer mailer = new ReportMailer(reportEmail,htmlFileName,"OpenNMS Inventory Report");
                mailer.send();

            }
        }
        catch (Exception e){
            log().debug("InventoryService runNodeBaseInventoryReport exception "+ e.getMessage() );
        }
            */

    }
    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }

}
