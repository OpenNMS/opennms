package org.opennms.reporting.availability;

import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.opennms.reporting.core.svclayer.ReportStoreService;

/**
 * <p>AvailabilityCalculator interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface AvailabilityCalculator {

    /**
     * <p>calculate</p>
     *
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    public abstract void calculate() throws AvailabilityCalculationException;

    /**
     * <p>writeXML</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    public abstract String writeXML() throws AvailabilityCalculationException;

    /**
     * <p>writeXML</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    public abstract void writeXML(String outputFileName)
            throws AvailabilityCalculationException;
    
    /**
     * <p>writeXML</p>
     *
     * @param outputStream a {@link java.io.OutputStream} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    public abstract void writeXML(OutputStream outputStream)
        throws AvailabilityCalculationException;

    /**
     * <p>writeLocateableXML</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    public abstract String writeLocateableXML(String id)
            throws AvailabilityCalculationException;

    /**
     * <p>marshal</p>
     *
     * @param outputFile a {@link java.io.File} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    public abstract void marshal(File outputFile)
            throws AvailabilityCalculationException;

    /**
     * <p>getLogoURL</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getLogoURL();

    /**
     * <p>setLogoURL</p>
     *
     * @param logoURL a {@link java.lang.String} object.
     */
    public abstract void setLogoURL(String logoURL);

    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getOutputFileName();

    /**
     * <p>setOutputFileName</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     */
    public abstract void setOutputFileName(String outputFileName);

    /**
     * <p>getAuthor</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getAuthor();

    /**
     * <p>setAuthor</p>
     *
     * @param author a {@link java.lang.String} object.
     */
    public abstract void setAuthor(String author);

    /**
     * <p>getCategoryName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getCategoryName();

    /**
     * <p>setCategoryName</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    public abstract void setCategoryName(String categoryName);

    /**
     * <p>getMonthFormat</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getMonthFormat();

    /**
     * <p>setMonthFormat</p>
     *
     * @param monthFormat a {@link java.lang.String} object.
     */
    public abstract void setMonthFormat(String monthFormat);

    /**
     * <p>getReportFormat</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getReportFormat();

    /**
     * <p>setReportFormat</p>
     *
     * @param reportFormat a {@link java.lang.String} object.
     */
    public abstract void setReportFormat(String reportFormat);

    /**
     * <p>getReport</p>
     *
     * @return a {@link org.opennms.reporting.availability.Report} object.
     */
    public abstract Report getReport();

    /**
     * <p>setCalendar</p>
     *
     * @param calendar a {@link java.util.Calendar} object.
     */
    public abstract void setCalendar(Calendar calendar);

    /**
     * <p>getPeriodEndDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public abstract Date getPeriodEndDate();

    /**
     * <p>setPeriodEndDate</p>
     *
     * @param periodEndDate a {@link java.util.Date} object.
     */
    public abstract void setPeriodEndDate(Date periodEndDate);

    /**
     * <p>setReportStoreService</p>
     *
     * @param reportStoreService a {@link org.opennms.reporting.core.svclayer.ReportStoreService} object.
     */
    public abstract void setReportStoreService(
            ReportStoreService reportStoreService);

    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getBaseDir();

    /**
     * <p>setBaseDir</p>
     *
     * @param baseDir a {@link java.lang.String} object.
     */
    public abstract void setBaseDir(String baseDir);

    /**
     * <p>setAvailabilityData</p>
     *
     * @param availabilityData a {@link org.opennms.reporting.availability.AvailabilityData} object.
     */
    public abstract void setAvailabilityData(AvailabilityData availabilityData);

}
