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
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_REPORT_FORMAT = "pdf";
    private static final String DEFAULT_REPORT_ENGINE = "opennms";

    @XmlAttribute(name = "report-template", required = true)
    private String reportTemplate;

    @XmlAttribute(name = "report-name", required = true)
    private String reportName;

    @XmlAttribute(name = "report-format")
    private String reportFormat;

    @XmlAttribute(name = "report-engine")
    private String reportEngine;

    @XmlElement(name = "cron-schedule", required = true)
    private String cronSchedule;

    @XmlElement(name = "recipient")
    private List<String> recipientList = new ArrayList<>();

    @XmlElement(name = "mailer")
    private String mailer;

    @XmlElement(name = "parameter")
    private List<Parameter> parameterList = new ArrayList<>();

    /**
     * 
     * 
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameter(final Parameter vParameter) throws IndexOutOfBoundsException {
        this.parameterList.add(vParameter);
    }

    /**
     * 
     * 
     * @param index
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameter(final int index, final Parameter vParameter) throws IndexOutOfBoundsException {
        this.parameterList.add(index, vParameter);
    }

    /**
     * 
     * 
     * @param vRecipient
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRecipient(final String vRecipient) throws IndexOutOfBoundsException {
        this.recipientList.add(vRecipient);
    }

    /**
     * 
     * 
     * @param index
     * @param vRecipient
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRecipient(final int index, final String vRecipient) throws IndexOutOfBoundsException {
        this.recipientList.add(index, vRecipient);
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Parameter> enumerateParameter() {
        return Collections.enumeration(this.parameterList);
    }

    /**
     * Method enumerateRecipient.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateRecipient() {
        return Collections.enumeration(this.recipientList);
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
        
        if (obj instanceof Report) {
            Report temp = (Report)obj;
            boolean equals = Objects.equals(temp.reportTemplate, reportTemplate)
                && Objects.equals(temp.reportName, reportName)
                && Objects.equals(temp.reportFormat, reportFormat)
                && Objects.equals(temp.reportEngine, reportEngine)
                && Objects.equals(temp.cronSchedule, cronSchedule)
                && Objects.equals(temp.recipientList, recipientList)
                && Objects.equals(temp.mailer, mailer)
                && Objects.equals(temp.parameterList, parameterList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'cronSchedule'.
     * 
     * @return the value of field 'CronSchedule'.
     */
    public String getCronSchedule() {
        return this.cronSchedule;
    }

    /**
     * Returns the value of field 'mailer'.
     * 
     * @return the value of field 'Mailer'.
     */
    public String getMailer() {
        return this.mailer;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Parameter at the
     * given index
     */
    public Parameter getParameter(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterList.size()) {
            throw new IndexOutOfBoundsException("getParameter: Index value '" + index + "' not in range [0.." + (this.parameterList.size() - 1) + "]");
        }
        
        return (Parameter) parameterList.get(index);
    }

    /**
     * Method getParameter.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Parameter[] getParameter() {
        Parameter[] array = new Parameter[0];
        return (Parameter[]) this.parameterList.toArray(array);
    }

    /**
     * Method getParameterCollection.Returns a reference to 'parameterList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Parameter> getParameterCollection() {
        return this.parameterList;
    }

    /**
     * Method getParameterCount.
     * 
     * @return the size of this collection
     */
    public int getParameterCount() {
        return this.parameterList.size();
    }

    /**
     * Method getRecipient.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getRecipient(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.recipientList.size()) {
            throw new IndexOutOfBoundsException("getRecipient: Index value '" + index + "' not in range [0.." + (this.recipientList.size() - 1) + "]");
        }
        
        return (String) recipientList.get(index);
    }

    /**
     * Method getRecipient.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getRecipient() {
        String[] array = new String[0];
        return (String[]) this.recipientList.toArray(array);
    }

    /**
     * Method getRecipientCollection.Returns a reference to 'recipientList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getRecipientCollection() {
        return this.recipientList;
    }

    /**
     * Method getRecipientCount.
     * 
     * @return the size of this collection
     */
    public int getRecipientCount() {
        return this.recipientList.size();
    }

    /**
     * Returns the value of field 'reportEngine'.
     * 
     * @return the value of field 'ReportEngine'.
     */
    public String getReportEngine() {
        return this.reportEngine != null ? this.reportEngine : DEFAULT_REPORT_ENGINE;
    }

    /**
     * Returns the value of field 'reportFormat'.
     * 
     * @return the value of field 'ReportFormat'.
     */
    public String getReportFormat() {
        return this.reportFormat != null ? this.reportFormat : DEFAULT_REPORT_FORMAT;
    }

    /**
     * Returns the value of field 'reportName'.
     * 
     * @return the value of field 'ReportName'.
     */
    public String getReportName() {
        return this.reportName;
    }

    /**
     * Returns the value of field 'reportTemplate'.
     * 
     * @return the value of field 'ReportTemplate'.
     */
    public String getReportTemplate() {
        return this.reportTemplate;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            reportTemplate, 
            reportName, 
            reportFormat, 
            reportEngine, 
            cronSchedule, 
            recipientList, 
            mailer, 
            parameterList);
        return hash;
    }

    /**
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Parameter> iterateParameter() {
        return this.parameterList.iterator();
    }

    /**
     * Method iterateRecipient.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateRecipient() {
        return this.recipientList.iterator();
    }

    /**
     */
    public void removeAllParameter() {
        this.parameterList.clear();
    }

    /**
     */
    public void removeAllRecipient() {
        this.recipientList.clear();
    }

    /**
     * Method removeParameter.
     * 
     * @param vParameter
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameter(final Parameter vParameter) {
        boolean removed = parameterList.remove(vParameter);
        return removed;
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Parameter removeParameterAt(final int index) {
        Object obj = this.parameterList.remove(index);
        return (Parameter) obj;
    }

    /**
     * Method removeRecipient.
     * 
     * @param vRecipient
     * @return true if the object was removed from the collection.
     */
    public boolean removeRecipient(final String vRecipient) {
        boolean removed = recipientList.remove(vRecipient);
        return removed;
    }

    /**
     * Method removeRecipientAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeRecipientAt(final int index) {
        Object obj = this.recipientList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'cronSchedule'.
     * 
     * @param cronSchedule the value of field 'cronSchedule'.
     */
    public void setCronSchedule(final String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    /**
     * Sets the value of field 'mailer'.
     * 
     * @param mailer the value of field 'mailer'.
     */
    public void setMailer(final String mailer) {
        this.mailer = mailer;
    }

    /**
     * 
     * 
     * @param index
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setParameter(final int index, final Parameter vParameter) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterList.size()) {
            throw new IndexOutOfBoundsException("setParameter: Index value '" + index + "' not in range [0.." + (this.parameterList.size() - 1) + "]");
        }
        
        this.parameterList.set(index, vParameter);
    }

    /**
     * 
     * 
     * @param vParameterArray
     */
    public void setParameter(final Parameter[] vParameterArray) {
        //-- copy array
        parameterList.clear();
        
        for (int i = 0; i < vParameterArray.length; i++) {
                this.parameterList.add(vParameterArray[i]);
        }
    }

    /**
     * Sets the value of 'parameterList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vParameterList the Vector to copy.
     */
    public void setParameter(final List<Parameter> vParameterList) {
        // copy vector
        this.parameterList.clear();
        
        this.parameterList.addAll(vParameterList);
    }

    /**
     * Sets the value of 'parameterList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param parameterList the Vector to set.
     */
    public void setParameterCollection(final List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    /**
     * 
     * 
     * @param index
     * @param vRecipient
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setRecipient(final int index, final String vRecipient) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.recipientList.size()) {
            throw new IndexOutOfBoundsException("setRecipient: Index value '" + index + "' not in range [0.." + (this.recipientList.size() - 1) + "]");
        }
        
        this.recipientList.set(index, vRecipient);
    }

    /**
     * 
     * 
     * @param vRecipientArray
     */
    public void setRecipient(final String[] vRecipientArray) {
        //-- copy array
        recipientList.clear();
        
        for (int i = 0; i < vRecipientArray.length; i++) {
                this.recipientList.add(vRecipientArray[i]);
        }
    }

    /**
     * Sets the value of 'recipientList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vRecipientList the Vector to copy.
     */
    public void setRecipient(final List<String> vRecipientList) {
        // copy vector
        this.recipientList.clear();
        
        this.recipientList.addAll(vRecipientList);
    }

    /**
     * Sets the value of 'recipientList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param recipientList the Vector to set.
     */
    public void setRecipientCollection(final List<String> recipientList) {
        this.recipientList = recipientList;
    }

    /**
     * Sets the value of field 'reportEngine'.
     * 
     * @param reportEngine the value of field 'reportEngine'.
     */
    public void setReportEngine(final String reportEngine) {
        this.reportEngine = reportEngine;
    }

    /**
     * Sets the value of field 'reportFormat'.
     * 
     * @param reportFormat the value of field 'reportFormat'.
     */
    public void setReportFormat(final String reportFormat) {
        this.reportFormat = reportFormat;
    }

    /**
     * Sets the value of field 'reportName'.
     * 
     * @param reportName the value of field 'reportName'.
     */
    public void setReportName(final String reportName) {
        this.reportName = reportName;
    }

    /**
     * Sets the value of field 'reportTemplate'.
     * 
     * @param reportTemplate the value of field 'reportTemplate'.
     */
    public void setReportTemplate(final String reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

}
