/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.availability;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;

import org.opennms.reporting.core.svclayer.ReportStoreService;

/**
 * <p>AvailabilityCalculator interface.</p>
 */
public interface AvailabilityCalculator {

    /**
     * <p>calculate</p>
     *
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    void calculate() throws AvailabilityCalculationException;

    /**
     * <p>writeXML</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    String writeXML() throws AvailabilityCalculationException;

    /**
     * <p>writeXML</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    void writeXML(String outputFileName)
            throws AvailabilityCalculationException;
    
    /**
     * <p>writeXML</p>
     *
     * @param outputStream a {@link java.io.OutputStream} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    void writeXML(OutputStream outputStream)
        throws AvailabilityCalculationException;

    /**
     * <p>writeLocateableXML</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    String writeLocateableXML(String id)
            throws AvailabilityCalculationException;

    /**
     * <p>marshal</p>
     *
     * @param outputFile a {@link java.io.File} object.
     * @throws org.opennms.reporting.availability.AvailabilityCalculationException if any.
     */
    void marshal(File outputFile)
            throws AvailabilityCalculationException;

    /**
     * <p>getLogoURL</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getLogoURL();

    /**
     * <p>setLogoURL</p>
     *
     * @param logoURL a {@link java.lang.String} object.
     */
    void setLogoURL(String logoURL);

    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getOutputFileName();

    /**
     * <p>setOutputFileName</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     */
    void setOutputFileName(String outputFileName);

    /**
     * <p>getAuthor</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getAuthor();

    /**
     * <p>setAuthor</p>
     *
     * @param author a {@link java.lang.String} object.
     */
    void setAuthor(String author);

    /**
     * <p>getCategoryName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getCategoryName();

    /**
     * <p>setCategoryName</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    void setCategoryName(String categoryName);

    /**
     * <p>getMonthFormat</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getMonthFormat();

    /**
     * <p>setMonthFormat</p>
     *
     * @param monthFormat a {@link java.lang.String} object.
     */
    void setMonthFormat(String monthFormat);

    /**
     * <p>getReportFormat</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getReportFormat();

    /**
     * <p>setReportFormat</p>
     *
     * @param reportFormat a {@link java.lang.String} object.
     */
    void setReportFormat(String reportFormat);

    /**
     * <p>getReport</p>
     *
     * @return a {@link org.opennms.reporting.availability.Report} object.
     */
    Report getReport();

    /**
     * <p>getPeriodEndDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getPeriodEndDate();

    /**
     * <p>setPeriodEndDate</p>
     *
     * @param periodEndDate a {@link java.util.Date} object.
     */
    void setPeriodEndDate(Date periodEndDate);

    /**
     * <p>setReportStoreService</p>
     *
     * @param reportStoreService a {@link org.opennms.reporting.core.svclayer.ReportStoreService} object.
     */
    void setReportStoreService(
            ReportStoreService reportStoreService);

    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getBaseDir();

    /**
     * <p>setBaseDir</p>
     *
     * @param baseDir a {@link java.lang.String} object.
     */
    void setBaseDir(String baseDir);

    /**
     * <p>setAvailabilityData</p>
     *
     * @param availabilityData a {@link org.opennms.reporting.availability.AvailabilityData} object.
     */
    void setAvailabilityData(AvailabilityData availabilityData);

}
