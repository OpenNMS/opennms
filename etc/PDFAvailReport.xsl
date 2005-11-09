<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xlink="http://www.w3.org/1999/xlink"
xmlns:fo="http://www.w3.org/1999/XSL/Format">
<xsl:output indent="yes"/> 
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
					<!--xsl:apply-templates select="/report/viewInfo/viewName"/-->Availability Report 
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
				<!--fo:block><fo:leader leader-pattern="rule" leader-length="18cm" /></fo:block-->
			</fo:static-content> 
			<fo:flow flow-name="xsl-region-body"> 
				<fo:block font-size="10pt" line-height="14pt" font-family="Helvetica"> 
					<xsl:apply-templates select="categories"/> 
				</fo:block> 
			</fo:flow> 
		</fo:page-sequence> 
	</fo:root> 
</xsl:template> 
<!-- Cover Page Area --> 
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
		<fo:block start-indent="0.5in"> 
			<xsl:apply-templates select="catSections"/> 
		</fo:block> 
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
<!-- Category Sections --> 
<xsl:template match="catSections"> 
	<fo:block >
		<xsl:apply-templates select="section"/> 
	</fo:block>
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
	<fo:block start-indent="0.75in"> 
		<xsl:attribute name="break-after">
			<xsl:if test="$pdfcount!=sectionIndex">
				<xsl:value-of select="'page'"/>
			</xsl:if>
		</xsl:attribute>
		<fo:table>
			<xsl:for-each select="col/colTitle">
				<fo:table-column column-width="2in"/>
			</xsl:for-each>
			<fo:table-body> 
				<xsl:apply-templates select="col"/> 
				<xsl:apply-templates select="rows"/> 
			</fo:table-body> 
		</fo:table> 
	</fo:block> 
</xsl:template> 

<xsl:template match="col"> 
	<fo:table>
		<xsl:for-each select="colTitle">
			<fo:table-column column-width="2.5in">
			</fo:table-column>
		</xsl:for-each>
		<fo:table-body> 
			<xsl:apply-templates select="rows"/> 
		</fo:table-body> 
	</fo:table>	
</xsl:template>

<xsl:template match="col"> 
	<fo:table-row>
	<xsl:for-each select="colTitle">
		<fo:table-cell>
		<fo:block font-weight="bold"><xsl:value-of select="."/></fo:block>
		</fo:table-cell>
		<!-- xsl:apply-templates select="colTitle"/ --> 
	</xsl:for-each>
	</fo:table-row>
</xsl:template>
<xsl:template match="colTitle"> 
	<fo:table-column column-width="20mm"> 
		<fo:table-cell> 
			<xsl:value-of select="."/> 
		</fo:table-cell> 
	</fo:table-column>
</xsl:template>  
<xsl:template match="rows"> 
	<xsl:for-each select="row">
		<fo:table-row>
			<xsl:for-each select="value">
                                <fo:table-cell>
                                <fo:block font-weight="bold">
                                <xsl:call-template name="replace">
                                        <xsl:with-param name="string" select="."/>
                                        <xsl:with-param name="old" select="'.'"/>
                                        <xsl:with-param name="new" select="'.&#x200b;'"/>
                                </xsl:call-template>
                                </fo:block>
                                </fo:table-cell>
			</xsl:for-each>
			<!-- xsl:apply-templates select="colTitle"/ --> 
		</fo:table-row>
	</xsl:for-each>
	<!-- xsl:apply-templates select="row"/ --> 
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
<xsl:template match="row"> 
	<!-- temporary fo:block lines, remove after table works --> 
	<fo:table-row> 
		<fo:table-cell>
			<fo:block> 
				<xsl:apply-templates select="value"/> 
			</fo:block> 
		</fo:table-cell>
	</fo:table-row> 
</xsl:template> 
<xsl:template match="value"> 
<!-- fo:table-column column-width="20mm" --> 
	<fo:table-cell> 
		<xsl:value-of select="."/> 
	</fo:table-cell> 
<!-- /fo:table-column --> 

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
