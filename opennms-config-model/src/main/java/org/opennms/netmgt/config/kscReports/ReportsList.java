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

package org.opennms.netmgt.config.kscReports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the ksc-performance-reports.xml
 *  configuration file. 
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "ReportsList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportsList implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "Report")
    private List<Report> reportList = new ArrayList<>();

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
        
        if (obj instanceof ReportsList) {
            ReportsList temp = (ReportsList)obj;
            boolean equals = Objects.equals(temp.reportList, reportList);
            return equals;
        }
        return false;
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
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
        this.reportList = reportList == null? new ArrayList<>() : reportList;
    }

}
