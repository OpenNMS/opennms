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


import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bar-chart")
@XmlAccessorType(XmlAccessType.FIELD)
public class BarChart implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "domain-axis-label", required = true)
    private String domainAxisLabel;

    @XmlAttribute(name = "range-axis-label", required = true)
    private String rangeAxisLabel;

    @XmlAttribute(name = "sub-label-class")
    private String subLabelClass;

    @XmlAttribute(name = "series-color-class")
    private String seriesColorClass;

    @XmlAttribute(name = "draw-bar-outline")
    private Boolean drawBarOutline;

    @XmlAttribute(name = "show-legend")
    private Boolean showLegend;

    @XmlAttribute(name = "show-tool-tips")
    private Boolean showToolTips;

    @XmlAttribute(name = "show-urls")
    private Boolean showUrls;

    @XmlAttribute(name = "variation")
    private String variation;

    @XmlAttribute(name = "plot-orientation")
    private String plotOrientation;

    @XmlElement(name = "title", required = true)
    private Title title;

    @XmlElement(name = "image-size", required = true)
    private ImageSize imageSize;

    @XmlElement(name = "sub-title")
    private java.util.List<SubTitle> subTitleList;

    @XmlElement(name = "grid-lines")
    private GridLines gridLines;

    @XmlElement(name = "series-def")
    private java.util.List<SeriesDef> seriesDefList;

    @XmlElement(name = "plot-background-color")
    private PlotBackgroundColor plotBackgroundColor;

    @XmlElement(name = "chart-background-color")
    private ChartBackgroundColor chartBackgroundColor;

    public BarChart() {
        this.subTitleList = new java.util.ArrayList<SubTitle>();
        this.seriesDefList = new java.util.ArrayList<SeriesDef>();
    }

    /**
     * 
     * 
     * @param vSeriesDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSeriesDef(final SeriesDef vSeriesDef) throws IndexOutOfBoundsException {
        this.seriesDefList.add(vSeriesDef);
    }

    /**
     * 
     * 
     * @param index
     * @param vSeriesDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSeriesDef(final int index, final SeriesDef vSeriesDef) throws IndexOutOfBoundsException {
        this.seriesDefList.add(index, vSeriesDef);
    }

    /**
     * 
     * 
     * @param vSubTitle
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSubTitle(final SubTitle vSubTitle) throws IndexOutOfBoundsException {
        this.subTitleList.add(vSubTitle);
    }

    /**
     * 
     * 
     * @param index
     * @param vSubTitle
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSubTitle(final int index, final SubTitle vSubTitle) throws IndexOutOfBoundsException {
        this.subTitleList.add(index, vSubTitle);
    }

    /**
     */
    public void deleteDrawBarOutline() {
        this.drawBarOutline= null;
    }

    /**
     */
    public void deleteShowLegend() {
        this.showLegend= null;
    }

    /**
     */
    public void deleteShowToolTips() {
        this.showToolTips= null;
    }

    /**
     */
    public void deleteShowUrls() {
        this.showUrls= null;
    }

    /**
     * Method enumerateSeriesDef.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<SeriesDef> enumerateSeriesDef() {
        return java.util.Collections.enumeration(this.seriesDefList);
    }

    /**
     * Method enumerateSubTitle.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<SubTitle> enumerateSubTitle() {
        return java.util.Collections.enumeration(this.subTitleList);
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
        
        if (obj instanceof BarChart) {
            BarChart temp = (BarChart)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.domainAxisLabel, domainAxisLabel)
                && Objects.equals(temp.rangeAxisLabel, rangeAxisLabel)
                && Objects.equals(temp.subLabelClass, subLabelClass)
                && Objects.equals(temp.seriesColorClass, seriesColorClass)
                && Objects.equals(temp.drawBarOutline, drawBarOutline)
                && Objects.equals(temp.showLegend, showLegend)
                && Objects.equals(temp.showToolTips, showToolTips)
                && Objects.equals(temp.showUrls, showUrls)
                && Objects.equals(temp.variation, variation)
                && Objects.equals(temp.plotOrientation, plotOrientation)
                && Objects.equals(temp.title, title)
                && Objects.equals(temp.imageSize, imageSize)
                && Objects.equals(temp.subTitleList, subTitleList)
                && Objects.equals(temp.gridLines, gridLines)
                && Objects.equals(temp.seriesDefList, seriesDefList)
                && Objects.equals(temp.plotBackgroundColor, plotBackgroundColor)
                && Objects.equals(temp.chartBackgroundColor, chartBackgroundColor);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'chartBackgroundColor'.
     * 
     * @return the value of field 'ChartBackgroundColor'.
     */
    public Optional<ChartBackgroundColor> getChartBackgroundColor() {
        return Optional.ofNullable(this.chartBackgroundColor);
    }

    /**
     * Returns the value of field 'domainAxisLabel'.
     * 
     * @return the value of field 'DomainAxisLabel'.
     */
    public String getDomainAxisLabel() {
        return this.domainAxisLabel;
    }

    /**
     * Returns the value of field 'drawBarOutline'.
     * 
     * @return the value of field 'DrawBarOutline'.
     */
    public Boolean getDrawBarOutline() {
        return this.drawBarOutline != null ? this.drawBarOutline : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'gridLines'.
     * 
     * @return the value of field 'GridLines'.
     */
    public Optional<GridLines> getGridLines() {
        return Optional.ofNullable(this.gridLines);
    }

    /**
     * Returns the value of field 'imageSize'.
     * 
     * @return the value of field 'ImageSize'.
     */
    public ImageSize getImageSize() {
        return this.imageSize;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'plotBackgroundColor'.
     * 
     * @return the value of field 'PlotBackgroundColor'.
     */
    public Optional<PlotBackgroundColor> getPlotBackgroundColor() {
        return Optional.ofNullable(this.plotBackgroundColor);
    }

    /**
     * Returns the value of field 'plotOrientation'.
     * 
     * @return the value of field 'PlotOrientation'.
     */
    public Optional<String> getPlotOrientation() {
        return Optional.ofNullable(this.plotOrientation);
    }

    /**
     * Returns the value of field 'rangeAxisLabel'.
     * 
     * @return the value of field 'RangeAxisLabel'.
     */
    public String getRangeAxisLabel() {
        return this.rangeAxisLabel;
    }

    /**
     * Returns the value of field 'seriesColorClass'.
     * 
     * @return the value of field 'SeriesColorClass'.
     */
    public Optional<String> getSeriesColorClass() {
        return Optional.ofNullable(this.seriesColorClass);
    }

    /**
     * Method getSeriesDef.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the SeriesDef at the
     * given index
     */
    public SeriesDef getSeriesDef(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.seriesDefList.size()) {
            throw new IndexOutOfBoundsException("getSeriesDef: Index value '" + index + "' not in range [0.." + (this.seriesDefList.size() - 1) + "]");
        }
        
        return (SeriesDef) seriesDefList.get(index);
    }

    /**
     * Method getSeriesDef.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public SeriesDef[] getSeriesDef() {
        SeriesDef[] array = new SeriesDef[0];
        return (SeriesDef[]) this.seriesDefList.toArray(array);
    }

    /**
     * Method getSeriesDefCollection.Returns a reference to 'seriesDefList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<SeriesDef> getSeriesDefCollection() {
        return this.seriesDefList;
    }

    /**
     * Method getSeriesDefCount.
     * 
     * @return the size of this collection
     */
    public int getSeriesDefCount() {
        return this.seriesDefList.size();
    }

    /**
     * Returns the value of field 'showLegend'.
     * 
     * @return the value of field 'ShowLegend'.
     */
    public Boolean getShowLegend() {
        return this.showLegend != null ? this.showLegend : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'showToolTips'.
     * 
     * @return the value of field 'ShowToolTips'.
     */
    public Boolean getShowToolTips() {
        return this.showToolTips != null ? this.showToolTips : Boolean.valueOf("false");
    }

    /**
     * Returns the value of field 'showUrls'.
     * 
     * @return the value of field 'ShowUrls'.
     */
    public Boolean getShowUrls() {
        return this.showUrls != null ? this.showUrls : Boolean.valueOf("false");
    }

    /**
     * Returns the value of field 'subLabelClass'.
     * 
     * @return the value of field 'SubLabelClass'.
     */
    public Optional<String> getSubLabelClass() {
        return Optional.ofNullable(this.subLabelClass);
    }

    /**
     * Method getSubTitle.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the SubTitle at the
     * given index
     */
    public SubTitle getSubTitle(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.subTitleList.size()) {
            throw new IndexOutOfBoundsException("getSubTitle: Index value '" + index + "' not in range [0.." + (this.subTitleList.size() - 1) + "]");
        }
        
        return (SubTitle) subTitleList.get(index);
    }

    /**
     * Method getSubTitle.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public SubTitle[] getSubTitle() {
        SubTitle[] array = new SubTitle[0];
        return (SubTitle[]) this.subTitleList.toArray(array);
    }

    /**
     * Method getSubTitleCollection.Returns a reference to 'subTitleList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<SubTitle> getSubTitleCollection() {
        return this.subTitleList;
    }

    /**
     * Method getSubTitleCount.
     * 
     * @return the size of this collection
     */
    public int getSubTitleCount() {
        return this.subTitleList.size();
    }

    /**
     * Returns the value of field 'title'.
     * 
     * @return the value of field 'Title'.
     */
    public Title getTitle() {
        return this.title;
    }

    /**
     * Returns the value of field 'variation'.
     * 
     * @return the value of field 'Variation'.
     */
    public Optional<String> getVariation() {
        return Optional.ofNullable(this.variation);
    }

    /**
     * Method hasDrawBarOutline.
     * 
     * @return true if at least one DrawBarOutline has been added
     */
    public boolean hasDrawBarOutline() {
        return this.drawBarOutline != null;
    }

    /**
     * Method hasShowLegend.
     * 
     * @return true if at least one ShowLegend has been added
     */
    public boolean hasShowLegend() {
        return this.showLegend != null;
    }

    /**
     * Method hasShowToolTips.
     * 
     * @return true if at least one ShowToolTips has been added
     */
    public boolean hasShowToolTips() {
        return this.showToolTips != null;
    }

    /**
     * Method hasShowUrls.
     * 
     * @return true if at least one ShowUrls has been added
     */
    public boolean hasShowUrls() {
        return this.showUrls != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            domainAxisLabel, 
            rangeAxisLabel, 
            subLabelClass, 
            seriesColorClass, 
            drawBarOutline, 
            showLegend, 
            showToolTips, 
            showUrls, 
            variation, 
            plotOrientation, 
            title, 
            imageSize, 
            subTitleList, 
            gridLines, 
            seriesDefList, 
            plotBackgroundColor, 
            chartBackgroundColor);
        return hash;
    }

    /**
     * Returns the value of field 'drawBarOutline'.
     * 
     * @return the value of field 'DrawBarOutline'.
     */
    public Boolean isDrawBarOutline() {
        return this.drawBarOutline != null ? this.drawBarOutline : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'showLegend'.
     * 
     * @return the value of field 'ShowLegend'.
     */
    public Boolean isShowLegend() {
        return this.showLegend != null ? this.showLegend : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'showToolTips'.
     * 
     * @return the value of field 'ShowToolTips'.
     */
    public Boolean isShowToolTips() {
        return this.showToolTips != null ? this.showToolTips : Boolean.valueOf("false");
    }

    /**
     * Returns the value of field 'showUrls'.
     * 
     * @return the value of field 'ShowUrls'.
     */
    public Boolean isShowUrls() {
        return this.showUrls != null ? this.showUrls : Boolean.valueOf("false");
    }

    /**
     * Method iterateSeriesDef.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<SeriesDef> iterateSeriesDef() {
        return this.seriesDefList.iterator();
    }

    /**
     * Method iterateSubTitle.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<SubTitle> iterateSubTitle() {
        return this.subTitleList.iterator();
    }

    /**
     */
    public void removeAllSeriesDef() {
        this.seriesDefList.clear();
    }

    /**
     */
    public void removeAllSubTitle() {
        this.subTitleList.clear();
    }

    /**
     * Method removeSeriesDef.
     * 
     * @param vSeriesDef
     * @return true if the object was removed from the collection.
     */
    public boolean removeSeriesDef(final SeriesDef vSeriesDef) {
        boolean removed = seriesDefList.remove(vSeriesDef);
        return removed;
    }

    /**
     * Method removeSeriesDefAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public SeriesDef removeSeriesDefAt(final int index) {
        Object obj = this.seriesDefList.remove(index);
        return (SeriesDef) obj;
    }

    /**
     * Method removeSubTitle.
     * 
     * @param vSubTitle
     * @return true if the object was removed from the collection.
     */
    public boolean removeSubTitle(final SubTitle vSubTitle) {
        boolean removed = subTitleList.remove(vSubTitle);
        return removed;
    }

    /**
     * Method removeSubTitleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public SubTitle removeSubTitleAt(final int index) {
        Object obj = this.subTitleList.remove(index);
        return (SubTitle) obj;
    }

    /**
     * Sets the value of field 'chartBackgroundColor'.
     * 
     * @param chartBackgroundColor the value of field 'chartBackgroundColor'.
     */
    public void setChartBackgroundColor(final ChartBackgroundColor chartBackgroundColor) {
        this.chartBackgroundColor = chartBackgroundColor;
    }

    /**
     * Sets the value of field 'domainAxisLabel'.
     * 
     * @param domainAxisLabel the value of field 'domainAxisLabel'.
     */
    public void setDomainAxisLabel(final String domainAxisLabel) {
        this.domainAxisLabel = domainAxisLabel;
    }

    /**
     * Sets the value of field 'drawBarOutline'.
     * 
     * @param drawBarOutline the value of field 'drawBarOutline'.
     */
    public void setDrawBarOutline(final Boolean drawBarOutline) {
        this.drawBarOutline = drawBarOutline;
    }

    /**
     * Sets the value of field 'gridLines'.
     * 
     * @param gridLines the value of field 'gridLines'.
     */
    public void setGridLines(final GridLines gridLines) {
        this.gridLines = gridLines;
    }

    /**
     * Sets the value of field 'imageSize'.
     * 
     * @param imageSize the value of field 'imageSize'.
     */
    public void setImageSize(final ImageSize imageSize) {
        this.imageSize = imageSize;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'plotBackgroundColor'.
     * 
     * @param plotBackgroundColor the value of field 'plotBackgroundColor'.
     */
    public void setPlotBackgroundColor(final PlotBackgroundColor plotBackgroundColor) {
        this.plotBackgroundColor = plotBackgroundColor;
    }

    /**
     * Sets the value of field 'plotOrientation'.
     * 
     * @param plotOrientation the value of field 'plotOrientation'.
     */
    public void setPlotOrientation(final String plotOrientation) {
        this.plotOrientation = plotOrientation;
    }

    /**
     * Sets the value of field 'rangeAxisLabel'.
     * 
     * @param rangeAxisLabel the value of field 'rangeAxisLabel'.
     */
    public void setRangeAxisLabel(final String rangeAxisLabel) {
        this.rangeAxisLabel = rangeAxisLabel;
    }

    /**
     * Sets the value of field 'seriesColorClass'.
     * 
     * @param seriesColorClass the value of field 'seriesColorClass'.
     */
    public void setSeriesColorClass(final String seriesColorClass) {
        this.seriesColorClass = seriesColorClass;
    }

    /**
     * 
     * 
     * @param index
     * @param vSeriesDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setSeriesDef(final int index, final SeriesDef vSeriesDef) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.seriesDefList.size()) {
            throw new IndexOutOfBoundsException("setSeriesDef: Index value '" + index + "' not in range [0.." + (this.seriesDefList.size() - 1) + "]");
        }
        
        this.seriesDefList.set(index, vSeriesDef);
    }

    /**
     * 
     * 
     * @param vSeriesDefArray
     */
    public void setSeriesDef(final SeriesDef[] vSeriesDefArray) {
        //-- copy array
        seriesDefList.clear();
        
        for (int i = 0; i < vSeriesDefArray.length; i++) {
                this.seriesDefList.add(vSeriesDefArray[i]);
        }
    }

    /**
     * Sets the value of 'seriesDefList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vSeriesDefList the Vector to copy.
     */
    public void setSeriesDef(final java.util.List<SeriesDef> vSeriesDefList) {
        // copy vector
        this.seriesDefList.clear();
        
        this.seriesDefList.addAll(vSeriesDefList);
    }

    /**
     * Sets the value of 'seriesDefList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param seriesDefList the Vector to set.
     */
    public void setSeriesDefCollection(final java.util.List<SeriesDef> seriesDefList) {
        this.seriesDefList = seriesDefList == null? new ArrayList<>() : seriesDefList;
    }

    /**
     * Sets the value of field 'showLegend'.
     * 
     * @param showLegend the value of field 'showLegend'.
     */
    public void setShowLegend(final Boolean showLegend) {
        this.showLegend = showLegend;
    }

    /**
     * Sets the value of field 'showToolTips'.
     * 
     * @param showToolTips the value of field 'showToolTips'.
     */
    public void setShowToolTips(final Boolean showToolTips) {
        this.showToolTips = showToolTips;
    }

    /**
     * Sets the value of field 'showUrls'.
     * 
     * @param showUrls the value of field 'showUrls'.
     */
    public void setShowUrls(final Boolean showUrls) {
        this.showUrls = showUrls;
    }

    /**
     * Sets the value of field 'subLabelClass'.
     * 
     * @param subLabelClass the value of field 'subLabelClass'.
     */
    public void setSubLabelClass(final String subLabelClass) {
        this.subLabelClass = subLabelClass;
    }

    /**
     * 
     * 
     * @param index
     * @param vSubTitle
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setSubTitle(final int index, final SubTitle vSubTitle) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.subTitleList.size()) {
            throw new IndexOutOfBoundsException("setSubTitle: Index value '" + index + "' not in range [0.." + (this.subTitleList.size() - 1) + "]");
        }
        
        this.subTitleList.set(index, vSubTitle);
    }

    /**
     * 
     * 
     * @param vSubTitleArray
     */
    public void setSubTitle(final SubTitle[] vSubTitleArray) {
        //-- copy array
        subTitleList.clear();
        
        for (int i = 0; i < vSubTitleArray.length; i++) {
                this.subTitleList.add(vSubTitleArray[i]);
        }
    }

    /**
     * Sets the value of 'subTitleList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vSubTitleList the Vector to copy.
     */
    public void setSubTitle(final java.util.List<SubTitle> vSubTitleList) {
        // copy vector
        this.subTitleList.clear();
        
        this.subTitleList.addAll(vSubTitleList);
    }

    /**
     * Sets the value of 'subTitleList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param subTitleList the Vector to set.
     */
    public void setSubTitleCollection(final java.util.List<SubTitle> subTitleList) {
        this.subTitleList = subTitleList == null? new ArrayList<>() : subTitleList;
    }

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(final Title title) {
        this.title = title;
    }

    /**
     * Sets the value of field 'variation'.
     * 
     * @param variation the value of field 'variation'.
     */
    public void setVariation(final String variation) {
        this.variation = variation;
    }

}
