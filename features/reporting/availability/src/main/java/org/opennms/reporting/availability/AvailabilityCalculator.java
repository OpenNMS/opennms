package org.opennms.reporting.availability;

import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.opennms.reporting.core.svclayer.ReportStoreService;

public interface AvailabilityCalculator {

    public abstract void calculate() throws AvailabilityCalculationException;

    public abstract String writeXML() throws AvailabilityCalculationException;

    public abstract void writeXML(String outputFileName)
            throws AvailabilityCalculationException;
    
    public abstract void writeXML(OutputStream outputStream)
        throws AvailabilityCalculationException;

    public abstract String writeLocateableXML(String id)
            throws AvailabilityCalculationException;

    public abstract void marshal(File outputFile)
            throws AvailabilityCalculationException;

    public abstract String getLogoURL();

    public abstract void setLogoURL(String logoURL);

    public abstract String getOutputFileName();

    public abstract void setOutputFileName(String outputFileName);

    public abstract String getAuthor();

    public abstract void setAuthor(String author);

    public abstract String getCategoryName();

    public abstract void setCategoryName(String categoryName);

    public abstract String getMonthFormat();

    public abstract void setMonthFormat(String monthFormat);

    public abstract String getReportFormat();

    public abstract void setReportFormat(String reportFormat);

    public abstract Report getReport();

    public abstract void setCalendar(Calendar calendar);

    public abstract Date getPeriodEndDate();

    public abstract void setPeriodEndDate(Date periodEndDate);

    public abstract void setReportStoreService(
            ReportStoreService reportStoreService);

    public abstract String getBaseDir();

    public abstract void setBaseDir(String baseDir);

    public abstract void setAvailabilityData(AvailabilityData availabilityData);

}