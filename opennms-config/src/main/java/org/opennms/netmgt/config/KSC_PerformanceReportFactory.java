/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

/**
 * <p>KSC_PerformanceReportFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class KSC_PerformanceReportFactory {
    /**
     * The static singleton instance object.
     * Null if init() hasn't been successfully called.
     */
    private static KSC_PerformanceReportFactory s_instance = null;

    /** File name of the KSC_PerformanceReport.xml */
    private static File s_configFile = null;

    /** An instance of the ReportsList configuration */
    private ReportsList m_config;

    /**
     * The array of values that may be used in the timespan declaration of a
     * graph
     */
    public static final String[] TIMESPAN_OPTIONS = {
            "1_hour",
            "2_hour",
            "4_hour",
            "6_hour",
            "8_hour",
            "12_hour",
            "1_day",
            "2_day",
            "7_day",
            "1_month",
            "3_month",
            "6_month",
            "1_year",
            "Today",
            "Yesterday",
            "Yesterday 9am-5pm",
            "Yesterday 5pm-10pm",
            "This Week",
            "Last Week",
            "This Month",
            "Last Month",
            "This Quarter",
            "Last Quarter",
            "This Year",
            "Last Year"
    };

    /**
     * Map of all Reports by their ID, ordered based on their order in the config file.
     */
    private Map<Integer, Report> m_reportList;

    /**
     * Empty Private Constructor. Cannot be instantiated outside itself.
     */
    private KSC_PerformanceReportFactory() {
    }

    /**
     * Init routine. Must be called before calling getInstance() to instantiate *
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (isInitialized()) {
            return;
        }

        KSC_PerformanceReportFactory newInstance = new KSC_PerformanceReportFactory();
        newInstance.reload();

        s_instance = newInstance;
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * KSC_PerformanceReportFactory
     *
     * @return the single KSC_PerformanceReportFactory instance
     * @throws java.lang.IllegalStateException if any.
     */
    public static synchronized KSC_PerformanceReportFactory getInstance() throws IllegalStateException {
        assertInitialized();

        return s_instance;
    }

    /**
     * Parses the KSC_PerformanceReport.xml via the Castor classes.
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (s_configFile == null) s_configFile = ConfigFileConstants.getFile(ConfigFileConstants.KSC_REPORT_FILE_NAME);

        m_config = CastorUtils.unmarshal(ReportsList.class, new FileSystemResource(s_configFile));

        setIdsOnAllReports();

        m_reportList = createReportList();
    }

    public static void setConfigFile(final File configFile) {
        s_configFile = configFile;
    }

    private void setIdsOnAllReports() {
        int i = 0;

        // Make sure that i is larger than the highest report ID
        for (Report report : m_config.getReportCollection()) {
            if (report.hasId() && report.getId() >= i) {
                i = report.getId() + 1;
            }
        }

        // Set IDs for any report lacking one.
        for (Report report : m_config.getReportCollection()) {
            if (!report.hasId()) {
                report.setId(i);
                i++;
            }
        }
    }

    /**
     * Saves the KSC_PerformanceReport.xml data.
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void saveCurrent() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        assertInitialized();

        sortByTitle();

        CastorUtils.marshalViaString(m_config, s_configFile);

        reload();
    }

    private static void assertInitialized() {
        Assert.state(isInitialized(), "KSC_PerformanceReportFactory.init() has not been called");
    }

    private static boolean isInitialized() {
        return s_instance != null;
    }

    /**
     * Sorts the Reports List by their title.
     */
    public void sortByTitle() {
        Arrays.sort(m_config.getReport(), new Comparator<Report>() {
            @Override
            public int compare(Report o1, Report o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
    }

    /**
     * <p>getReportByIndex</p>
     *
     * @param index a int.
     * @return a {@link org.opennms.netmgt.config.kscReports.Report} object.
     */
    public Report getReportByIndex(int index) {
        return m_reportList.get(index);
    }

    private Map<Integer, Report> createReportList() {
        Map<Integer, Report> reports = new LinkedHashMap<Integer, Report>(m_config.getReportCount());

        for (Report report : m_config.getReportCollection()) {
            if (reports.containsKey(report.getId())) {
                throw new IllegalArgumentException("Report id " + report.getId() + " is used by multiple reports in configuration file");
            }
            reports.put(report.getId(), report);
        }

        return reports;
    }

    /**
     * <p>getReportList</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, String> getReportList() {
        LinkedHashMap<Integer, String> reports = new LinkedHashMap<Integer, String>(m_config.getReportCount());

        List<Report> reportList = m_config.getReportCollection();
        Collections.sort(reportList, new Comparator<Report>() {
            @Override
            public int compare(Report o1, Report o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        for (Report report : reportList) {
            reports.put(report.getId(), report.getTitle());
        }

        return Collections.unmodifiableMap(reports);
    }

    /**
     * <p>getReportMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, Report> getReportMap() {
        Map<Integer, Report> reports = new HashMap<Integer, Report>(m_config.getReportCount());

        for (Report report : m_config.getReportCollection()) {
            reports.put(report.getId(), report);
        }

        return Collections.unmodifiableMap(reports);
    }

    /**
     * Deletes the indexed report and updates file configuration
     *
     * @param index a int.
     * @throws java.lang.ArrayIndexOutOfBoundsException if any.
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void deleteReportAndSave(int index) throws ArrayIndexOutOfBoundsException, IOException, FileNotFoundException, MarshalException, ValidationException {
        Report report = getReportByIndex(index);
        if (report == null) {
            throw new ArrayIndexOutOfBoundsException("Reports List index to be deleted is out of bounds: " + index);
        }

        m_config.removeReport(report);
        saveCurrent();
    }

    public void addReport(Report report) {
        m_config.addReport(report);
        setIdsOnAllReports();
    }

    public void setReport(int index, Report report) {
        int arrayIndex = getArrayIndex(index);
        if (arrayIndex == -1) {
            throw new IllegalArgumentException("Could not find report with ID of " + index);
        }

        // Make sure we preserve the existing ID, if it exists (which it should)
        if (m_config.getReport(arrayIndex).hasId()) {
            report.setId(m_config.getReport(arrayIndex).getId());
        }

        m_config.setReport(arrayIndex, report);
        setIdsOnAllReports();
    }

    private int getArrayIndex(int index) {
        int i = 0;
        for (Report report : m_config.getReportCollection()) {
            if (report.getId() == index) {
                return i;
            }

            i++;
        }

        return -1;
    }

    /**
     * This method requires begin time and end time to be set to the current
     * time prior to call. The start and stop times are relative to this time.
     * Init values as follows: begin_time = Calendar.getInstance(); end_time =
     * Calendar.getInstance();
     *
     * @param interval a {@link java.lang.String} object.
     * @param begin_time a {@link java.util.Calendar} object.
     * @param end_time a {@link java.util.Calendar} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public static synchronized void getBeginEndTime(String interval, Calendar begin_time, Calendar end_time) throws IllegalArgumentException {
        if (interval.equals("1_hour")) {
            begin_time.add(Calendar.HOUR, -1);
        } else if (interval.equals("2_hour")) {
            begin_time.add(Calendar.HOUR, -2);
        } else if (interval.equals("4_hour")) {
            begin_time.add(Calendar.HOUR, -4);
        } else if (interval.equals("6_hour")) {
            begin_time.add(Calendar.HOUR, -6);
        } else if (interval.equals("8_hour")) {
            begin_time.add(Calendar.HOUR, -8);
        } else if (interval.equals("12_hour")) {
            begin_time.add(Calendar.HOUR, -12);
        } else if (interval.equals("1_day")) {
            begin_time.add(Calendar.DATE, -1);
        } else if (interval.equals("2_day")) {
            begin_time.add(Calendar.DATE, -2);
        } else if (interval.equals("7_day")) {
            begin_time.add(Calendar.DATE, -7);
        } else if (interval.equals("1_month")) {
            begin_time.add(Calendar.DATE, -30);
        } else if (interval.equals("3_month")) {
            begin_time.add(Calendar.DATE, -90);
        } else if (interval.equals("6_month")) {
            begin_time.add(Calendar.DATE, -183);
        } else if (interval.equals("1_year")) {
            begin_time.add(Calendar.DATE, -365);
        } else {
            // From current time, lets zero out the small components
            begin_time.set(Calendar.HOUR_OF_DAY, 0);
            begin_time.set(Calendar.MINUTE, 0);
            begin_time.set(Calendar.SECOND, 0);
            end_time.set(Calendar.HOUR_OF_DAY, 0);
            end_time.set(Calendar.MINUTE, 0);
            end_time.set(Calendar.SECOND, 0);

            if (interval.equals("Today")) {
                end_time.add(Calendar.DATE, 1);
            } else if (interval.equals("Yesterday")) {
                begin_time.add(Calendar.DATE, -1);
            } else if (interval.equals("Yesterday 9am-5pm")) {
                begin_time.add(Calendar.DATE, -1);
                begin_time.set(Calendar.HOUR_OF_DAY, 9);
                end_time.add(Calendar.DATE, -1);
                end_time.set(Calendar.HOUR_OF_DAY, 17);
            } else if (interval.equals("Yesterday 5pm-10pm")) {
                begin_time.add(Calendar.DATE, -1);
                begin_time.set(Calendar.HOUR_OF_DAY, 17);
                end_time.add(Calendar.DATE, -1);
                end_time.set(Calendar.HOUR_OF_DAY, 22);
            } else if (interval.equals("This Week") || interval.equals("Last Week")) {
                begin_time.set(Calendar.DAY_OF_WEEK, begin_time.getFirstDayOfWeek());
                end_time.set(Calendar.DAY_OF_WEEK, end_time.getFirstDayOfWeek());
                end_time.add(Calendar.DATE, 6);
                end_time.set(Calendar.HOUR_OF_DAY, 23);
                end_time.set(Calendar.MINUTE, 59);
                if (interval.equals("Last Week")) {
                    begin_time.add(Calendar.DATE, -7);
                    end_time.add(Calendar.DATE, -7);
                }
            } else if (interval.equals("This Month")) {
                begin_time.set(Calendar.DATE, 1);
                end_time.add(Calendar.MONTH, 1);
                end_time.set(Calendar.DATE, 1);
            } else if (interval.equals("Last Month")) {
                begin_time.add(Calendar.MONTH, -1);
                begin_time.set(Calendar.DATE, 1);
                end_time.set(Calendar.DATE, 1);
            } else if (interval.equals("This Quarter") || interval.equals("Last Quarter")) {
                begin_time.set(Calendar.DATE, 1);
                end_time.set(Calendar.DATE, 1);

                switch (begin_time.get(Calendar.MONTH)) {
                case 0:
                case 1:
                case 2:
                    begin_time.set(Calendar.MONTH, 0);
                    end_time.set(Calendar.MONTH, 3);
                    break;
                case 3:
                case 4:
                case 5:
                    begin_time.set(Calendar.MONTH, 3);
                    end_time.set(Calendar.MONTH, 6);
                    break;
                case 6:
                case 7:
                case 8:
                    begin_time.set(Calendar.MONTH, 6);
                    end_time.set(Calendar.MONTH, 9);
                    break;
                case 9:
                case 10:
                case 11:
                    begin_time.set(Calendar.MONTH, 9);
                    end_time.set(Calendar.MONTH, 0);
                    end_time.add(Calendar.YEAR, 1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Calendar Month " + begin_time.get(Calendar.MONTH));
                }
                if (interval.equals("Last Quarter")) {
                    begin_time.add(Calendar.MONTH, -3);
                    end_time.add(Calendar.MONTH, -3);
                }
            } else if (interval.equals("This Year")) {
                begin_time.set(Calendar.MONTH, 0);
                begin_time.set(Calendar.DATE, 1);
                end_time.set(Calendar.MONTH, 0);
                end_time.set(Calendar.DATE, 1);
                end_time.add(Calendar.YEAR, 1);
            } else if (interval.equals("Last Year")) {
                begin_time.set(Calendar.MONTH, 0);
                begin_time.set(Calendar.DATE, 1);
                begin_time.add(Calendar.YEAR, -1);
                end_time.set(Calendar.MONTH, 0);
                end_time.set(Calendar.DATE, 1);
            } else {
                throw new IllegalArgumentException("Unknown graph timespan: " + interval);
            }
        }
    }
}
