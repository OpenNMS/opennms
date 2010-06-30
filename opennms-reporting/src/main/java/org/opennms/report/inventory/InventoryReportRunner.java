package org.opennms.report.inventory;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.opennms.report.ReportMailer;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;

/**
 * <p>InventoryReportRunner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
    
    /**
     * <p>getNullReportRenderer</p>
     *
     * @return a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public ReportRenderer getNullReportRenderer() {
        return m_nullReportRenderer;
    }

    /**
     * <p>setNullReportRenderer</p>
     *
     * @param nullReportRenderer a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public void setNullReportRenderer(ReportRenderer nullReportRenderer) {
        m_nullReportRenderer = nullReportRenderer;
    }

    /**
     * <p>getHtmlReportRenderer</p>
     *
     * @return a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public ReportRenderer getHtmlReportRenderer() {
        return m_htmlReportRenderer;
    }

    /**
     * <p>setHtmlReportRenderer</p>
     *
     * @param htmlReportRenderer a {@link org.opennms.reporting.availability.render.ReportRenderer} object.
     */
    public void setHtmlReportRenderer(ReportRenderer htmlReportRenderer) {
        m_htmlReportRenderer = htmlReportRenderer;
    }

    /**
     * <p>Getter for the field <code>theDate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTheDate() {
        return theDate;
    }

    /**
     * <p>Setter for the field <code>theDate</code>.</p>
     *
     * @param theDate a {@link java.lang.String} object.
     */
    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    /**
     * <p>Getter for the field <code>theField</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTheField() {
        return theField;
    }

    /**
     * <p>Setter for the field <code>theField</code>.</p>
     *
     * @param theField a {@link java.lang.String} object.
     */
    public void setTheField(String theField) {
        this.theField = theField;
    }

    /**
     * <p>Getter for the field <code>reportFormat</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportFormat() {
        return reportFormat;
    }

    /**
     * <p>Setter for the field <code>reportFormat</code>.</p>
     *
     * @param reportFormat a {@link java.lang.String} object.
     */
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    /**
     * <p>Getter for the field <code>reportEmail</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportEmail() {
        return reportEmail;
    }

    /**
     * <p>Setter for the field <code>reportEmail</code>.</p>
     *
     * @param reportEmail a {@link java.lang.String} object.
     */
    public void setReportEmail(String reportEmail) {
        this.reportEmail = reportEmail;
    }

    /**
     * <p>Getter for the field <code>user</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return user;
    }

    /**
     * <p>Setter for the field <code>user</code>.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(String user) {
        this.user = user;
    }


    /**
     * <p>Getter for the field <code>reportRequestDate</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    /**
     * <p>Setter for the field <code>reportRequestDate</code>.</p>
     *
     * @param reportRequestDate a {@link java.util.Date} object.
     */
    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    /**
     * <p>getInventoryReportCalculator</p>
     *
     * @return a {@link org.opennms.report.inventory.InventoryReportCalculator} object.
     */
    public InventoryReportCalculator getInventoryReportCalculator() {
        return calculator;
    }

    /**
     * <p>setInventoryReportCalculator</p>
     *
     * @param inventoryReportCalculator a {@link org.opennms.report.inventory.InventoryReportCalculator} object.
     */
    public void setInventoryReportCalculator(
            InventoryReportCalculator inventoryReportCalculator) {
        calculator = inventoryReportCalculator;
    }

    /**
     * <p>run</p>
     */
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
    
    private static Logger log() {
        return Logger.getLogger("Rancid");
    }

}
