/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.reportd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Behavior configuration for the Enterprise Reporting Daemon
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "reportd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The base directory rendered reports are saved on the file system.
     */
    @XmlAttribute(name = "storage-location", required = true)
    private String storageLocation;

    /**
     * Should reports be kept after delivered?
     */
    @XmlAttribute(name = "persist-reports", required = true)
    private String persistReports;

    /**
     * Defines an report schedule with a cron expression
     *  
     *  http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
     *  Field Name Allowed Values Allowed Special Characters
     *  Seconds 0-59 , - /
     *  Minutes 0-59 , - /
     *  Hours 0-23 , - /
     *  Day-of-month 1-31 , - ? / L W C
     *  Month 1-12 or JAN-DEC , - /
     *  Day-of-Week 1-7 or SUN-SAT , - ? / L C #
     *  Year (Opt) empty, 1970-2099 , - /
     *  
     */
    @XmlElement(name = "report")
    private List<Report> reportList = new ArrayList<Report>();

    /**
     * 
     * 
     * @param vReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addReport(final Report vReport) throws IndexOutOfBoundsException {
        this.reportList.add(vReport);
    }

    /**
     * 
     * 
     * @param index
     * @param vReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addReport(final int index, final Report vReport) throws IndexOutOfBoundsException {
        this.reportList.add(index, vReport);
    }

    /**
     * Method enumerateReport.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Report> enumerateReport() {
        return Collections.enumeration(this.reportList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof ReportdConfiguration) {
            ReportdConfiguration temp = (ReportdConfiguration)obj;
            boolean equals = Objects.equals(temp.storageLocation, storageLocation)
                && Objects.equals(temp.persistReports, persistReports)
                && Objects.equals(temp.reportList, reportList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'persistReports'. The field 'persistReports' has
     * the following description: Should reports be kept after delivered?
     * 
     * @return the value of field 'PersistReports'.
     */
    public String getPersistReports() {
        return this.persistReports;
    }

    /**
     * Method getReport.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Report at the
     * given index
     */
    public Report getReport(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.reportList.size()) {
            throw new IndexOutOfBoundsException("getReport: Index value '" + index + "' not in range [0.." + (this.reportList.size() - 1) + "]");
        }
        
        return (Report) reportList.get(index);
    }

    /**
     * Method getReport.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Report[] getReport() {
        Report[] array = new Report[0];
        return (Report[]) this.reportList.toArray(array);
    }

    /**
     * Method getReportCollection.Returns a reference to 'reportList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Report> getReportCollection() {
        return this.reportList;
    }

    /**
     * Method getReportCount.
     * 
     * @return the size of this collection
     */
    public int getReportCount() {
        return this.reportList.size();
    }

    /**
     * Returns the value of field 'storageLocation'. The field 'storageLocation'
     * has the following description: The base directory rendered reports are
     * saved on the file system.
     * 
     * @return the value of field 'StorageLocation'.
     */
    public String getStorageLocation() {
        return this.storageLocation;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            storageLocation, 
            persistReports, 
            reportList);
        return hash;
    }

    /**
     * Method iterateReport.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Report> iterateReport() {
        return this.reportList.iterator();
    }

    /**
     */
    public void removeAllReport() {
        this.reportList.clear();
    }

    /**
     * Method removeReport.
     * 
     * @param vReport
     * @return true if the object was removed from the collection.
     */
    public boolean removeReport(final Report vReport) {
        boolean removed = reportList.remove(vReport);
        return removed;
    }

    /**
     * Method removeReportAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Report removeReportAt(final int index) {
        Object obj = this.reportList.remove(index);
        return (Report) obj;
    }

    /**
     * Sets the value of field 'persistReports'. The field 'persistReports' has
     * the following description: Should reports be kept after delivered?
     * 
     * @param persistReports the value of field 'persistReports'.
     */
    public void setPersistReports(final String persistReports) {
        this.persistReports = persistReports;
    }

    /**
     * 
     * 
     * @param index
     * @param vReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setReport(final int index, final Report vReport) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.reportList.size()) {
            throw new IndexOutOfBoundsException("setReport: Index value '" + index + "' not in range [0.." + (this.reportList.size() - 1) + "]");
        }
        
        this.reportList.set(index, vReport);
    }

    /**
     * 
     * 
     * @param vReportArray
     */
    public void setReport(final Report[] vReportArray) {
        //-- copy array
        reportList.clear();
        
        for (int i = 0; i < vReportArray.length; i++) {
                this.reportList.add(vReportArray[i]);
        }
    }

    /**
     * Sets the value of 'reportList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vReportList the Vector to copy.
     */
    public void setReport(final List<Report> vReportList) {
        // copy vector
        this.reportList.clear();
        
        this.reportList.addAll(vReportList);
    }

    /**
     * Sets the value of 'reportList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param reportList the Vector to set.
     */
    public void setReportCollection(final List<Report> reportList) {
        this.reportList = reportList;
    }

    /**
     * Sets the value of field 'storageLocation'. The field 'storageLocation' has
     * the following description: The base directory rendered reports are saved on
     * the file system.
     * 
     * @param storageLocation the value of field 'storageLocation'.
     */
    public void setStorageLocation(final String storageLocation) {
        this.storageLocation = storageLocation;
    }

}
