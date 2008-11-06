<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xlink="http://www.w3.org/1999/xlink"
xmlns:fo="http://www.w3.org/1999/XSL/Format">

<!-- define some useful attribute sets -->

<xsl:attribute-set name = "date-attrs">
  <xsl:attribute name="text-align">start</xsl:attribute>
  <xsl:attribute name="font-size">8pt</xsl:attribute>
  <xsl:attribute name="color">black</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "pct-val-attrs">
  <xsl:attribute name="text-align">center</xsl:attribute>
  <xsl:attribute name="font-size">12pt</xsl:attribute>
  <xsl:attribute name="color">black</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "zero-cell-attrs">
  <xsl:attribute name="border">solid 1px black</xsl:attribute>
  <xsl:attribute name="background-color">red</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "zero-cell-attrs">
  <xsl:attribute name="border">solid 1px black</xsl:attribute>
  <xsl:attribute name="background-color">#eee</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "normal-cell-attrs">
  <xsl:attribute name="border">solid 1px black</xsl:attribute>
  <xsl:attribute name="background-color">green</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "warning-cell-attrs">
  <xsl:attribute name="border">solid 1px black</xsl:attribute>
  <xsl:attribute name="background-color">yellow</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "critical-cell-attrs">
  <xsl:attribute name="border">solid 1px black</xsl:attribute>
  <xsl:attribute name="background-color">red</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name = "empty-cell-attrs">
  <xsl:attribute name="border">solid 1px black</xsl:attribute>
  <xsl:attribute name="background-color">#eee</xsl:attribute>
</xsl:attribute-set>

<xsl:template match="report">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
                <fo:layout-master-set>
                        <!-- C O V E R P A G E M A S T E R -->
                        <fo:simple-page-master master-name="cover" page-width="8.5in" page-height="11in" margin-top="4in"
                                margin-bottom="1in" margin-left="1in" margin-right="1in">
                                <fo:region-body border-width="1pt" vertical-align="middle" margin-top="0.5in"/>
                                <fo:region-after extent="10mm"/>
                        </fo:simple-page-master>
                        <!-- R E S T P A G E M A S T E R -->
                        <fo:simple-page-master master-name="rest" page-width="8.5in" page-height="11in" margin-top="0.5in"
                                margin-bottom="0.25in" margin-left="1in" margin-right="1in">
                                <fo:region-body margin-top="0.5in" margin-bottom="0.5in"/>
                                <fo:region-before vertical-align="top" border-bottom="thin solid black" margin-bottom="0in"
                                margin-left="4in" margin-right="0.1in" margin-top="0in" extent="0.5in"/>
                                <fo:region-after margin-bottom="0in" margin-left="0in" margin-right="0in"
                                margin-top="0.1in" extent="0.25in"/>
                        </fo:simple-page-master>
                </fo:layout-master-set>
                                                                                                                             
                <!-- Cover Page -->
                <fo:page-sequence master-reference="cover">
                        <fo:flow flow-name="xsl-region-body">
                                <fo:block text-align="start" font-family="Helvetica">
                                        <xsl:apply-templates select="viewInfo"/>
				Availability Report
                                </fo:block>
                        </fo:flow>
                </fo:page-sequence>
                                                                                                                             
                <!-- rest of doc -->
                <fo:page-sequence master-reference="rest">
                        <fo:static-content flow-name="xsl-region-after">
                                <fo:block font-family="Helvetica" font-size="9pt" text-align="start"> - Page
                                        <fo:page-number/>
                                </fo:block>
                        </fo:static-content>
                        <fo:static-content flow-name="xsl-region-before">
                                <fo:block text-align="end"  font-weight="bold" color="#006699">
                                        Availability Report
                                </fo:block>
                                <fo:block text-align="end"  font-weight="bold" color="#006699">
                                        <xsl:apply-templates select="/report/viewInfo/viewTitle"/>
                                </fo:block>
                                <fo:block text-align="end"  font-weight="bold" color="#006699">
                                        <xsl:value-of select="/report/created/@month"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="/report/created/@day"/>
                                        <xsl:text>, </xsl:text>
                                        <xsl:value-of select="/report/created/@year"/>
                                </fo:block>
                        </fo:static-content>
                        <fo:flow flow-name="xsl-region-body">
                                <fo:block font-size="10pt" line-height="14pt" font-family="Helvetica">
                                        <xsl:apply-templates select="categories"/>
                                </fo:block>
                        </fo:flow>
                </fo:page-sequence>
        </fo:root>
</xsl:template>

<xsl:template match="viewInfo">
        <fo:block>
                <fo:external-graphic height="auto" width="auto" content-height="auto" content-width="auto" >
                        <xsl:attribute name="src">
                                <xsl:value-of select="/report/logo"/>
                        </xsl:attribute>
                </fo:external-graphic>
        </fo:block>
        <fo:block font-size="24pt" line-height="30pt" font-weight="bold" color="#006699">
                <!--xsl:apply-templates select="viewTitle"/-->Availability Report
        </fo:block>
        <fo:block font-size="16pt" line-height="16pt">
                <xsl:apply-templates select="viewComments"/>
        </fo:block>
        <!--fo:block> Report:
                <xsl:apply-templates select="viewName"/>
        </fo:block-->
        <!--fo:block> Author:
                <xsl:value-of select="/report/author"/>
        </fo:block-->
        <fo:block> Created:
                <xsl:value-of select="/report/created/@day"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="/report/created/@month"/>
                <xsl:text>, </xsl:text>
                <xsl:value-of select="/report/created/@year"/>
        </fo:block>
        <fo:block>
                <xsl:text>For Period : </xsl:text>
                <xsl:value-of select="/report/created/@period"/>
        </fo:block>
        <!--fo:block font-size="12pt" line-height="16pt"> Prepared by OpenNMS.  </fo:block-->
</xsl:template>


<xsl:template match="viewName">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="viewTitle">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="viewComments">
        <xsl:value-of select="."/>
</xsl:template>

                                                                                                                             
<!-- Category Area -->
<xsl:template match="categories">
        <xsl:for-each select="category">
                <fo:block font-size="16pt" line-height="24pt" font-weight="bold"  color="#006699">
                        <xsl:apply-templates select="catName"/>
                </fo:block>
                <fo:block>
                        <xsl:apply-templates select="catTitle"/>
                </fo:block>
                <fo:block>
                        <xsl:apply-templates select="catComments"/>
                </fo:block>
                <fo:block>
                        Nodes having outages: <xsl:apply-templates select="nodeCount"/>
                </fo:block>
                <fo:block>
                        Interfaces: <xsl:apply-templates select="ipaddrCount"/>
                </fo:block>
                <fo:block>
                        Services: <xsl:apply-templates select="serviceCount"/>
                </fo:block>
<!--                <fo:block start-indent="0.5in"> -->
                        <xsl:apply-templates select="catSections"/>
<!--                </fo:block> -->
        </xsl:for-each>
</xsl:template>
<xsl:template match="catName">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="catTitle">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="catComments">
        <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="catSections">
  <xsl:apply-templates select="section"/> 
</xsl:template>
                                                                                                                             
<xsl:template match="calendarTable">
       <xsl:value-of select="@month"/>
  <fo:table width="105mm">
   <fo:table-column column-width="15mm" number-columns-repeated="7"/>
    <fo:table-header>
      <xsl:apply-templates select="daysOfWeek"/>
    </fo:table-header>
    <fo:table-body>
    <xsl:apply-templates select="week"/>
    </fo:table-body>
  </fo:table>
</xsl:template>

<xsl:template match="daysOfWeek">
    <fo:table-row height="10mm">
 <xsl:for-each select="dayName">
  <fo:table-cell border="none 1px">
   <fo:block
             text-align="center"
             color="#999"><xsl:value-of select="."/></fo:block>
  </fo:table-cell>
 </xsl:for-each>
</fo:table-row>
</xsl:template>

<xsl:template match="week">
  <fo:table-row height="15mm">
    <xsl:apply-templates select="day"/>
  </fo:table-row>
</xsl:template>

<xsl:template match="day">
 <xsl:choose>
  <!-- these sections are all visible as they are vaild days of month -->
  <xsl:when test="@visible='true'">
   <xsl:choose>
    <!-- zero data, not the same as invisible -->
    <xsl:when test="number(@pctValue) = 0">
     <fo:table-cell xsl:use-attribute-sets = "zero-cell-attrs">
      <fo:block xsl:use-attribute-sets = "date-attrs"><xsl:value-of select="@date"/></fo:block>
     </fo:table-cell>
    </xsl:when>
    <!-- value is below warning level so colour it red -->
    <xsl:when test="../../../../../warning >= number(@pctValue)">
     <fo:table-cell xsl:use-attribute-sets = "critical-cell-attrs">
      <fo:block xsl:use-attribute-sets = "date-attrs"><xsl:value-of select="@date"/></fo:block>
      <fo:block xsl:use-attribute-sets = "pct-val-attrs"><xsl:value-of select="format-number(@pctValue,'0.00')"/></fo:block>
     </fo:table-cell>
    </xsl:when>
     <!-- value is below normal level so colour it yellow -->
    <xsl:when test="../../../../../normal >= number(@pctValue)">
     <fo:table-cell xsl:use-attribute-sets = "warning-cell-attrs">
      <fo:block xsl:use-attribute-sets = "date-attrs"><xsl:value-of select="@date"/></fo:block>
      <fo:block xsl:use-attribute-sets = "pct-val-attrs"><xsl:value-of select="format-number(@pctValue,'0.00')"/></fo:block>
     </fo:table-cell>
    </xsl:when>
    <xsl:otherwise>
     <!-- value is normal, so colout it green-->
     <fo:table-cell xsl:use-attribute-sets = "normal-cell-attrs">
      <fo:block xsl:use-attribute-sets = "date-attrs"><xsl:value-of select="@date"/></fo:block>
      <fo:block xsl:use-attribute-sets = "pct-val-attrs"><xsl:value-of select="format-number(@pctValue,'0.00')"/></fo:block>
     </fo:table-cell>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:when>
  <xsl:otherwise>
   <!-- this should be an empty block -->
   <fo:table-cell xsl:use-attribute-sets = "empty-cell-attrs">
   </fo:table-cell>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="section">
        <xsl:variable name="pdfcount">
                <xsl:value-of select="/report/sectionCount"/>
        </xsl:variable>
        <fo:block font-size="12pt" font-weight="bold" line-height="18pt">
                <xsl:apply-templates select="sectionTitle"/>
        </fo:block>
        <fo:block>
                <xsl:apply-templates select="sectionDescr"/>
        </fo:block>
        <fo:block>
                <xsl:attribute name="break-after">
                        <xsl:if test="$pdfcount!=sectionIndex">
                                <xsl:value-of select="'page'"/>
                        </xsl:if>
                </xsl:attribute>
	<xsl:apply-templates select="calendarTable"/>
	<xsl:apply-templates select="classicTable"/>
        </fo:block>
</xsl:template>

<xsl:template match="classicTable">
        <fo:block start-indent="0.20in">
                <fo:table>
                        <xsl:for-each select="col/colTitle">
                                <fo:table-column column-width="2.7in"/>
                        </xsl:for-each>
                        <fo:table-body>
                                <xsl:apply-templates select="col"/>
                                <xsl:apply-templates select="rows"/>
                        </fo:table-body>
                </fo:table>
        </fo:block>
</xsl:template>

<xsl:template match="col">
        <fo:table-row>
        <xsl:for-each select="colTitle">
                <fo:table-cell>
                <fo:block font-weight="bold"><xsl:value-of select="."/></fo:block>
                </fo:table-cell>
        </xsl:for-each>
        </fo:table-row>
</xsl:template>

<xsl:template match="rows">
  <xsl:for-each select="row">
    <fo:table-row>
      <xsl:for-each select="value">
        <fo:table-cell>
          <fo:block>
            <xsl:call-template name="replace">
              <xsl:with-param name="string" select="."/>
              <xsl:with-param name="old" select="'.'"/>
              <xsl:with-param name="new" select="'.&#x200b;'"/>
            </xsl:call-template>
          </fo:block>
        </fo:table-cell>
      </xsl:for-each>
    </fo:table-row>
  </xsl:for-each>
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

<xsl:template name="replace">
  <xsl:param name="string"/>
  <xsl:param name="old" />
  <xsl:param name="new"/>
  <xsl:choose>
    <xsl:when test="contains( $string, $old )">
      <xsl:value-of select="substring-before( $string, $old )"/>
      <xsl:value-of select="$new"/>
      <xsl:call-template name="replace">
        <xsl:with-param name="string" select="substring-after( $string, $old )"/>
        <xsl:with-param name="old" select="$old"/>
        <xsl:with-param name="new" select="$new"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$string"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
