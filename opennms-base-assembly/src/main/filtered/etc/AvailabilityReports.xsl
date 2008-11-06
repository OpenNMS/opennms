<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml">
<xsl:output method="html"/>
    

<xsl:template match="report">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>OpenNMS Availability Report</title>
<style type="text/css">
div#title { border:1px solid #666; background-color:#eee; padding:7px; margin-bottom:20px; }
div#title h1 { margin-top: 0px }
table.calendar { width:350px; border-collapse:collapse; }
table.calendar th { font:bold 70% Tahoma, sans-serif; padding:5px; border-bottom:1px solid #666; color:#999; }
table.calendar td { padding:3px; border:1px solid #ccc; vertical-align:top; font-family:Tahoma, sans-serif; width:50px; }
table.calendar td.empty { background-color:#eee; }
table.calendar td.critical { background-color:#ff0000; }
table.calendar td.warning { background-color:#ffff00; }
table.calendar td.normal { background-color:#00ff00; }
div.date { color:#666; font-size: 60%; }
div.dataValue { width:50px, font-size:90%; font-weight:bold; margin:4px auto; text-align:center; }
.normal { color:#000000; }
.warning { color:#FFFF00; }
.critical { color:#FF0000; }
h1, h2, h3, p, td, th { font-family: Tahoma,sans-serif; }
p, td, th { font-size: 75%; }
table.data { 350px; border-collapse:collapse; }
table.data td { border:1px solid #ccc; padding:3px; }
table.data th { color:#666; text-align:left; padding:5px }

</style>
</head>
<body bgcolor="white">
  <xsl:apply-templates select="viewInfo"/>
  <xsl:apply-templates select="categories"/>
</body>
</html>
</xsl:template>


<xsl:template match="viewInfo">
    <div id="title">
    <h1>Availability Report</h1>
    <xsl:apply-templates select="viewName"/>
    <xsl:apply-templates select="viewTitle"/>
    <xsl:apply-templates select="viewComments"/>

    <p>Generated on:
        <xsl:value-of select="/report/created/@month"/>/ 
        <xsl:value-of select="/report/created/@day"/>/ 
        <xsl:value-of select="/report/created/@year"/>.
    </p>
    <p>For period:
	<xsl:value-of select="/report/created/@period"/>
    </p> 
    </div>

</xsl:template>

<xsl:template match="viewName">
        <h1><xsl:value-of select="."/></h1>
</xsl:template>

<xsl:template match="viewTitle">
        <p><xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="viewComments">
        <p><xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="categories">
    <xsl:for-each select="category">
           <xsl:apply-templates select="catName"/>
           <xsl:apply-templates select="catComments"/>
	   <xsl:choose>
		   <xsl:when test='nodeCount>0'>
			   <xsl:apply-templates select="catSections"/>
		   </xsl:when>
		   <xsl:otherwise>
			   <h4>There are no nodes experiencing outages</h4>
		   </xsl:otherwise>
	   </xsl:choose>
    </xsl:for-each>
</xsl:template>

<xsl:template match="catName">
        <h2><xsl:value-of select="."/></h2>
</xsl:template>

<xsl:template match="catTitle">
        <p><xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="catComments">
        <p><xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="catSections">
        <xsl:apply-templates select="section"/>
</xsl:template>

<xsl:template match="section">
        <h3><xsl:apply-templates select="sectionTitle"/></h3>
        <p><xsl:apply-templates select="sectionDescr"/></p>
	<xsl:apply-templates select="classicTable"/>
	<xsl:apply-templates select="calendarTable"/>
</xsl:template>

<xsl:template match="sectionName">
        <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="sectionTitle">
        <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="sectionDescr">
        <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="classicTable">
        <table class="data" >
          <xsl:apply-templates select="col"/>
          <xsl:apply-templates select="rows"/>
        </table>
</xsl:template>

<xsl:template match="col">
        <tr>
                <xsl:apply-templates select="colTitle"/>
        </tr>
</xsl:template>

<xsl:template match="colTitle">
       <th><xsl:value-of select="."/></th>
</xsl:template>

<xsl:template match="rows">
        <xsl:apply-templates select="row"/>
</xsl:template>

<xsl:template match="row">
        <tr>
          <xsl:apply-templates select="value"/>
        </tr>
</xsl:template>

<xsl:template match="value">
        <xsl:if test="@type='data'">
          <td align="right"><xsl:value-of select="."/></td>
        </xsl:if>
        <xsl:if test="@type='title'">
          <td><xsl:value-of select="."/></td>
        </xsl:if>
        <xsl:if test="@type='other'">
          <td align="right"><xsl:value-of select="."/></td>
        </xsl:if>
</xsl:template>

<xsl:template match="calendarTable">
  <h3><xsl:value-of select="@month"/></h3>
  <table class="calendar">
  <tr>
  <xsl:apply-templates select="daysOfWeek"/>
  </tr>
  <xsl:apply-templates select="week"/>
  </table>
</xsl:template>

<xsl:template match="daysOfWeek">
         <xsl:for-each select="dayName">
          <th>
           <xsl:value-of select="."/>
          </th>
         </xsl:for-each>
</xsl:template>
                                                                                                                             
<xsl:template match="week">
        <tr>
        <xsl:apply-templates select="day"/>
        </tr>
</xsl:template>

<xsl:template match="day">
        <xsl:choose>
          <xsl:when test="@visible='true'">
            <xsl:choose>
             <xsl:when test="@pctValue=0">
               <td class="empty">
            <div class="date"><xsl:value-of select="@date"/></div>
            <div class="dataValue normal"><xsl:value-of select="format-number(@pctValue,'0.00')"/></div>
            </td>
             </xsl:when>
             <xsl:when test="../../../../../warning >= number(@pctValue)">
               <td class="critical">
            <div class="date"><xsl:value-of select="@date"/></div>
            <div class="dataValue normal"><xsl:value-of select="format-number(@pctValue,'0.00')"/></div>
            </td>
             </xsl:when>
             <xsl:when test="../../../../../normal >= number(@pctValue)">
               <td class="warning">
            <div class="date"><xsl:value-of select="@date"/></div>
            <div class="dataValue normal"><xsl:value-of select="format-number(@pctValue,'0.00')"/></div>
            </td>
             </xsl:when>
             <xsl:otherwise>
               <td class="normal">
            <div class="date"><xsl:value-of select="@date"/></div>
            <div class="dataValue normal"><xsl:value-of select="format-number(@pctValue,'0.00')"/></div>
            </td>
             </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <td class="empty">
              <div class="dataValue normal">&#xA0;</div>
            </td>
          </xsl:otherwise>
        </xsl:choose>
</xsl:template>


</xsl:stylesheet>
