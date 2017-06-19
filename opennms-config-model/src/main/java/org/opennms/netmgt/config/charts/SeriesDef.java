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

package org.opennms.netmgt.config.charts;


import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "series-def")
@XmlAccessorType(XmlAccessType.FIELD)
public class SeriesDef implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "number", required = true)
    private Integer number;

    @XmlAttribute(name = "series-name", required = true)
    private String seriesName;

    @XmlAttribute(name = "use-labels")
    private Boolean useLabels;

    @XmlElement(name = "jdbc-data-set", required = true)
    private JdbcDataSet jdbcDataSet;

    @XmlElement(name = "rgb")
    private Rgb rgb;

    public SeriesDef() {
    }

    /**
     */
    public void deleteNumber() {
        this.number= null;
    }

    /**
     */
    public void deleteUseLabels() {
        this.useLabels= null;
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
        
        if (obj instanceof SeriesDef) {
            SeriesDef temp = (SeriesDef)obj;
            boolean equals = Objects.equals(temp.number, number)
                && Objects.equals(temp.seriesName, seriesName)
                && Objects.equals(temp.useLabels, useLabels)
                && Objects.equals(temp.jdbcDataSet, jdbcDataSet)
                && Objects.equals(temp.rgb, rgb);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'jdbcDataSet'.
     * 
     * @return the value of field 'JdbcDataSet'.
     */
    public JdbcDataSet getJdbcDataSet() {
        return this.jdbcDataSet;
    }

    /**
     * Returns the value of field 'number'.
     * 
     * @return the value of field 'Number'.
     */
    public Integer getNumber() {
        return this.number;
    }

    /**
     * Returns the value of field 'rgb'.
     * 
     * @return the value of field 'Rgb'.
     */
    public Optional<Rgb> getRgb() {
        return Optional.ofNullable(this.rgb);
    }

    /**
     * Returns the value of field 'seriesName'.
     * 
     * @return the value of field 'SeriesName'.
     */
    public String getSeriesName() {
        return this.seriesName;
    }

    /**
     * Returns the value of field 'useLabels'.
     * 
     * @return the value of field 'UseLabels'.
     */
    public Boolean getUseLabels() {
        return this.useLabels != null ? this.useLabels : Boolean.valueOf("true");
    }

    /**
     * Method hasNumber.
     * 
     * @return true if at least one Number has been added
     */
    public boolean hasNumber() {
        return this.number != null;
    }

    /**
     * Method hasUseLabels.
     * 
     * @return true if at least one UseLabels has been added
     */
    public boolean hasUseLabels() {
        return this.useLabels != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            number, 
            seriesName, 
            useLabels, 
            jdbcDataSet, 
            rgb);
        return hash;
    }

    /**
     * Returns the value of field 'useLabels'.
     * 
     * @return the value of field 'UseLabels'.
     */
    public Boolean isUseLabels() {
        return this.useLabels != null ? this.useLabels : Boolean.valueOf("true");
    }

    /**
     * Sets the value of field 'jdbcDataSet'.
     * 
     * @param jdbcDataSet the value of field 'jdbcDataSet'.
     */
    public void setJdbcDataSet(final JdbcDataSet jdbcDataSet) {
        if (seriesName == null) {
            throw new IllegalArgumentException("'jdbc-data-set' is a required element!");
        }
        this.jdbcDataSet = jdbcDataSet;
    }

    /**
     * Sets the value of field 'number'.
     * 
     * @param number the value of field 'number'.
     */
    public void setNumber(final Integer number) {
        if (seriesName == null) {
            throw new IllegalArgumentException("'number' is a required attribute!");
        }
        this.number = number;
    }

    /**
     * Sets the value of field 'rgb'.
     * 
     * @param rgb the value of field 'rgb'.
     */
    public void setRgb(final Rgb rgb) {
        this.rgb = rgb;
    }

    /**
     * Sets the value of field 'seriesName'.
     * 
     * @param seriesName the value of field 'seriesName'.
     */
    public void setSeriesName(final String seriesName) {
        if (seriesName == null) {
            throw new IllegalArgumentException("'series-name' is a required attribute!");
        }
        this.seriesName = seriesName;
    }

    /**
     * Sets the value of field 'useLabels'.
     * 
     * @param useLabels the value of field 'useLabels'.
     */
    public void setUseLabels(final Boolean useLabels) {
        this.useLabels = useLabels;
    }

}
