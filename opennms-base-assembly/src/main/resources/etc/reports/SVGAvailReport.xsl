<xsl:stylesheet version="1.5" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xlink="http://www.w3.org/1999/xlink" 
xmlns:fo="http://www.w3.org/1999/XSL/Format"
xmlns:svg="http://www.w3.org/2000/svg">
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
				<fo:block text-align="end" font-weight="bold" color="#006699"> 
					Availability Report<!-- xsl:apply-templates select="/report/viewInfo/viewName"/--> 
				</fo:block> 
				<fo:block text-align="end" font-weight="bold" color="#006699"> 
					<xsl:apply-templates select="/report/viewInfo/viewTitle"/> 
				</fo:block> 
				<fo:block text-align="end" font-weight="bold" color="#006699"> 
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
		Availability Report
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
		<xsl:value-of select="/report/created/@month"/> 
		<xsl:text> </xsl:text> 
		<xsl:value-of select="/report/created/@day"/> 
		<xsl:text>, </xsl:text> 
		<xsl:value-of select="/report/created/@year"/> 
	</fo:block> 
        <fo:block>
                <xsl:text>For Period : </xsl:text>
                <xsl:value-of select="/report/created/@period"/>
        </fo:block>
	<!--fo:block font-size="12pt" line-height="16pt"> Prepared by OpenNMS. 
	</fo:block--> 
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
		<xsl:variable name="index">
			<xsl:value-of select="catIndex"/>
		</xsl:variable>
		<xsl:variable name="warn">
			<xsl:value-of select="warning"/>
		</xsl:variable>
		<xsl:variable name="norm">
			<xsl:value-of select="normal"/>
		</xsl:variable>
		<xsl:variable name="nodeCount">
			<xsl:value-of select="nodeCount"/>
		</xsl:variable>
		<fo:block font-size="16pt" line-height="24pt" font-weight="bold" color="#006699"> 
			<xsl:apply-templates select="catName"/> 
		</fo:block> 
		<fo:block>
			<xsl:apply-templates select="catTitle"/> 
		</fo:block> 
		<fo:block> 
			<xsl:apply-templates select="catComments"/> 
		</fo:block> 
		<fo:block> 
			Nodes having outages:<xsl:apply-templates select="nodeCount"/> 
		</fo:block> 
		<fo:block> 
			Interfaces:<xsl:apply-templates select="ipaddrCount"/> 
		</fo:block> 
		<fo:block> 
			Services:<xsl:apply-templates select="serviceCount"/> 
		</fo:block> 
                <xsl:variable name="count">
                        <xsl:value-of select="/report/catCount"/>
		</xsl:variable>
		<fo:block>
				<xsl:if test="$count!=catIndex">
			           <xsl:attribute name="break-after">
					<xsl:value-of select="'page'"/>
			</xsl:attribute>
				</xsl:if>
			<xsl:if test='$nodeCount=0'>
				<fo:block font-weight="bold">There are no node outages for this category</fo:block>
			</xsl:if>
			<xsl:apply-templates select="catSections"> 
				<xsl:with-param name="warning" select="$warn"/>
				<xsl:with-param name="normal" select="$norm"/>
				<xsl:with-param name="nodeCount" select="$nodeCount"/>
			</xsl:apply-templates>
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
	<xsl:param name="warning"/>
	<xsl:param name="normal"/>
	<xsl:param name="nodeCount"/>
	<xsl:if test='$nodeCount>0'>
	<fo:table>
		<fo:table-column column-width="8in"/>
		<fo:table-body>
			<xsl:choose>
				<xsl:when test="sectionName='lastMoSvcAvail'"/>
				<xsl:when test="sectionName='lastMoTop20ServiceOut'"/>
				<xsl:when test="sectionName='last30SvcAvail'"/>
				<xsl:otherwise>
					<fo:table-row>
						<fo:table-cell padding-bottom="40pt" padding-left="150pt" >
							<fo:block>
							<xsl:apply-templates select="section">
								<xsl:with-param name="namesec" select="'last12MoAvail'"/>
								<xsl:with-param name="warning" select="$warning"/>
								<xsl:with-param name="normal" select="$normal"/>
							</xsl:apply-templates>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell>
							<fo:table>
								<fo:table-column column-width="3.5in" />
								<fo:table-column column-width="3.5in" />
								<fo:table-body>
									<fo:table-row>
										<fo:table-cell padding-bottom="40pt" >
											<fo:block>
											<xsl:apply-templates select="section">
												<xsl:with-param name="namesec" select="'LastMonthsDailyAvailability'"/>
												<xsl:with-param name="warning" select="$warning"/>
												<xsl:with-param name="normal" select="$normal"/>
											</xsl:apply-templates>
											</fo:block>
										</fo:table-cell>
										<fo:table-cell padding-bottom="40pt" >
											<fo:block>
											<xsl:apply-templates select="section">
												<xsl:with-param name="namesec" select="'MonthToDateDailyAvailability'"/>
												<xsl:with-param name="warning" select="$warning"/>
												<xsl:with-param name="normal" select="$normal"/>
											</xsl:apply-templates>
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
								</fo:table-body>
							</fo:table>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell padding-left="70pt">
							<fo:block>
							<xsl:apply-templates select="section">
								<xsl:with-param name="namesec" select="'lastMoTop20offenders'"/>
								<xsl:with-param name="warning" select="$warning"/>
								<xsl:with-param name="normal" select="$normal"/>
							</xsl:apply-templates>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>

				</xsl:otherwise>
			</xsl:choose>
		</fo:table-body>
	</fo:table>
	</xsl:if>
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

<xsl:template match="colTitle"> 
	<fo:table-column column-width="20mm"> 
		<fo:table-cell> 
			<xsl:value-of select="."/> 
		</fo:table-cell> 
	</fo:table-column>
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
       <xsl:param name="graphtype"/>
       <xsl:param name="warning"/>
       <xsl:param name="normal"/>
       <xsl:param name="y-offset-hor"/>
       <xsl:param name="x-offset-hor"/>
       <xsl:param name="x-offset-ver"/>
       <xsl:param name="y-offset-ver"/>
	
	<xsl:variable name="keyval">
		<xsl:for-each select="value">
			<xsl:choose>
				<xsl:when test="@type='data'">
					<xsl:value-of select="."/>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:variable>

	<!-- Pass as parameters the text to be displayed on the x-axis -->
	<xsl:variable name="xval">
		<xsl:for-each select="value">
			<xsl:choose>
				<xsl:when test="@type='title'">
					<xsl:value-of select="."/>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:variable>

	<xsl:variable name="color">
		<xsl:choose> 
			<xsl:when test="$warning>=$keyval">
				<xsl:value-of select="'red'"/>
			</xsl:when>
			<xsl:when  test="$normal>=$keyval">
				<xsl:value-of select="'yellow'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'green'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:choose>
		<xsl:when test="$graphtype='lastMoTop20offenders'">
			<xsl:variable name="x">
				<xsl:for-each select="value">
					<xsl:choose>
						<xsl:when test="@type='data'">
							<xsl:value-of select="$x-offset-hor - 2 + . * 5"/>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:variable>
			<xsl:call-template name="drawLinesHori">
				<xsl:with-param name="x-offset" select="$x-offset-hor"/>
				<xsl:with-param name="y-offset" select="$y-offset-hor"/>
				<xsl:with-param name="x" select="$x"/>
				<xsl:with-param name="color" select="$color"/>
				<xsl:with-param name="value" select="$keyval"/>
				<xsl:with-param name="xval" select="$xval"/>
				<xsl:with-param name="graphtype" select="$graphtype"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="y">
				<xsl:for-each select="value">
					<xsl:choose>
						<xsl:when test="@type='data'">
							<xsl:value-of select="$y-offset-ver - . * 2"/>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:variable>
			<xsl:call-template name="drawLinesVer">
				<xsl:with-param name="x-offset" select="$x-offset-ver"/>
				<xsl:with-param name="y-offset" select="$y-offset-ver"/>
				<xsl:with-param name="y" select="$y"/>
				<xsl:with-param name="color" select="$color"/>
				<xsl:with-param name="value" select="$keyval"/>
				<xsl:with-param name="xval" select="$xval"/>
				<xsl:with-param name="graphtype" select="$graphtype"/>
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template> 

<xsl:template name="drawLinesVer">
	<xsl:param name="graphtype"/>
	<xsl:param name="x-offset"/>
	<xsl:param name="color"/>
	<xsl:param name="y-offset"/>
	<xsl:param name="y"/>
	<xsl:param name="value"/>
	<xsl:param name="xval"/>
	<xsl:variable name="x">
		<xsl:choose>
			<xsl:when test="$graphtype='last12MoAvail'">
				<xsl:value-of select="$x-offset - 8"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$x-offset - 5"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="xx">
		<xsl:choose>
			<xsl:when test="$graphtype='last12MoAvail'">
				<xsl:value-of select="$x-offset + 8"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$x-offset + 5"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<svg:path>
		<xsl:attribute name="style">
			<xsl:text>stroke-width:2;stroke:black;fill:</xsl:text>
			<xsl:value-of select="$color"/>
		</xsl:attribute>
		<xsl:attribute name="d">
			<xsl:text> M </xsl:text>
			<xsl:value-of select="$x"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$y-offset"/>

			<xsl:text> L </xsl:text>
			<xsl:value-of select="$x"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$y"/>

			<xsl:text> L </xsl:text>
			<xsl:value-of select="$xx"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$y" />

			<xsl:text> L </xsl:text>
			<xsl:value-of select="$xx"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$y-offset"/>

			<xsl:text> Z </xsl:text>
		</xsl:attribute>
	</svg:path>

	<!-- Draw the value as text on top of the bars -->
	<xsl:choose>
		<xsl:when test="$value>='100.0'"/>
		<xsl:otherwise>
			<svg:text style="font-size:6;text-anchor:middle">
				<!--xsl:attribute name="style">
					<xsl:value-of select="font-size:6;text-anchor:middle"/>
				</xsl:attribute-->
				<xsl:attribute name="x">
					<xsl:value-of select="$x + 6"/>
				</xsl:attribute>
				<xsl:attribute name="y">
					<xsl:value-of select="$y - 5"/>
				</xsl:attribute>
				<xsl:value-of select="format-number($value, '0.0')"/>
			</svg:text>
		</xsl:otherwise>
	</xsl:choose>

	<!-- Draw the text on the x-axis --> 
	<svg:text style="font-size:6;text-anchor:middle">
		<xsl:attribute name="x">
			<xsl:value-of select="$x-offset - 5"/>
		</xsl:attribute>
		<xsl:attribute name="y">
			<xsl:value-of select="260"/>
		</xsl:attribute>
		<xsl:value-of select="$xval"/>
	</svg:text>

</xsl:template>
<xsl:template name="drawLinesHori">
	<xsl:param name="xval"/>
	<xsl:param name="color"/>
	<xsl:param name="x-offset"/>
	<xsl:param name="x"/>
	<xsl:param name="value"/>
	<xsl:param name="y-offset"/>

	<xsl:choose>
		<xsl:when test="$xval='100.0'"/>
		<xsl:otherwise>
			<svg:path>
				<xsl:attribute name="style">
					<xsl:text>stroke-width:2;stroke:black;fill:</xsl:text>
					<xsl:value-of select="$color"/>
				</xsl:attribute>
				<xsl:attribute name="d">
					<xsl:text> M </xsl:text>
					<xsl:value-of select="$x-offset + 150 "/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="(385 + (-1 * ($y-offset - 5))) "/>

					<xsl:text> L </xsl:text>
					<xsl:value-of select="$x + 150"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="(385 + (-1 * ($y-offset -5))) "/>

					<xsl:text> L </xsl:text>
					<xsl:value-of select="$x + 150"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="(385 + (-1 * ($y-offset + 5)))" />

					<xsl:text> L </xsl:text>
					<xsl:value-of select="$x-offset + 150"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="(385 + (-1 * ($y-offset + 5)))"/>

					<xsl:text> Z </xsl:text>
				</xsl:attribute>
			</svg:path>
		</xsl:otherwise>
	</xsl:choose>

	<!-- Draw the value as text on top of the bars -->
	<xsl:choose>
		<xsl:when test="$value>='100.0'"/>
		<xsl:otherwise>
			<svg:text style="font-size:9;text-anchor:middle">
				<xsl:attribute name="x">
					<xsl:value-of select="$x + 175"/>
				</xsl:attribute>
				<xsl:attribute name="y">
					<xsl:value-of select="(385 + (-1 * $y-offset)) "/>
				</xsl:attribute>
				<xsl:value-of select="format-number($value, '0.00')"/>
			</svg:text>
		</xsl:otherwise>
	</xsl:choose>

	<svg:text style="font-size:9">
		<xsl:attribute name="x">
			<xsl:value-of select="20"/>
		</xsl:attribute>
		<xsl:attribute name="y">
			<xsl:value-of select="385 + (-1 * $y-offset)"/>
		</xsl:attribute>
		<xsl:value-of select="$xval"/>
	</svg:text>

	<!-- Draw the text on the x-axis -->
	<svg:text style="font-size:6;text-anchor:middle">
		<xsl:attribute name="x">
			<xsl:value-of select="$x-offset - 5 + 200"/>
		</xsl:attribute>
		<xsl:attribute name="y">
			<xsl:value-of select="250"/>
		</xsl:attribute>
	</svg:text>
</xsl:template>

<xsl:template match="value"> 
	<fo:table-cell> 
		<xsl:value-of select="."/> 
	</fo:table-cell>
</xsl:template> 

<xsl:template match="section">
  <xsl:param name="sectionName"/>
  <xsl:param name="namesec"/>
  <xsl:param name="warning"/>
  <xsl:param name="normal"/>
  <xsl:if test="sectionName=$namesec">
 <fo:instream-foreign-object 	width="0.1mm" 
				height="5cm"
				number-columns-spanned="2" 
				maximum-repeats="5" 
				provisional-distance-between-starts="0.1mm"
				padding-after="2cm"
				overflow="scroll" > 
  <svg xmlns="http://www.w3.org/2000/svg" width="500" height="400">
   <defs>
    <svg:path id="Path4Text" d="M 100 250 L 100 900 Z"/>
   </defs>
   <xsl:apply-templates select="classicTable">
    <xsl:with-param name="sectionName" select="sectionName"/>
    <xsl:with-param name="namesec" select="$namesec"/>
    <xsl:with-param name="warning" select="$warning"/>
    <xsl:with-param name="normal" select="$normal"/>
   </xsl:apply-templates>
   <xsl:apply-templates select="calendarTable">
    <xsl:with-param name="sectionName" select="sectionName"/>
    <xsl:with-param name="namesec" select="$namesec"/>
    <xsl:with-param name="warning" select="$warning"/>
    <xsl:with-param name="normal" select="$normal"/>
   </xsl:apply-templates>
  </svg>
 </fo:instream-foreign-object>
 </xsl:if>
</xsl:template>

<xsl:template match="classicTable">
	<xsl:param name="sectionName"/>
	<xsl:param name="namesec"/>
    <xsl:param name="warning"/>
    <xsl:param name="normal"/>
	<xsl:variable name="graphtype" select="../sectionName"/>
	

				<xsl:choose>
				<xsl:when test="$graphtype='lastMoTop20offenders'">
					<svg:g transform="scale(0.55)">
						<!-- Drawing the title -->
						<svg:text style="font-size:14;text-anchor:middle" x="150"  y="25">
							<xsl:value-of select="../sectionTitle"/>
						</svg:text>

						<!-- Drawing the subtitle -->
						<svg:text style="font-size:12;text-anchor:middle" x="180" y="38">(Percentage Availability)</svg:text>
			
						<!-- Draw the x-axis and y-axis -->
						<svg:g style="stroke-width:2; stroke:black">
							<svg:path d="M 175 355 L 175 50 L 175 355 L 700 355 Z"/>
						</svg:g>

						<svg:g style="fill:none; stroke:#B0B0B0; stroke-width:1; stroke-dasharray:2 4">
							<svg:path d="M 223 50 L 223 350 Z"/>
							<svg:path d="M 273 50 L 273 350 Z"/>
							<svg:path d="M 323 50 L 323 350 Z"/>
							<svg:path d="M 373 50 L 373 350 Z"/>
							<svg:path d="M 423 50 L 423 350 Z"/>
							<svg:path d="M 473 50 L 473 350 Z"/>
							<svg:path d="M 523 50 L 523 350 Z"/>
							<svg:path d="M 573 50 L 573 350 Z"/>
							<svg:path d="M 623 50 L 623 350 Z"/>
							<svg:path d="M 673 50 L 673 350 Z"/>
						</svg:g>

						<svg:g style="font-size:9">
							<svg:text style="text-anchor:end" x="673" y="365">100%</svg:text>
							<svg:text style="text-anchor:end" x="623" y="365">90</svg:text>
							<svg:text style="text-anchor:end" x="573" y="365">80</svg:text>
							<svg:text style="text-anchor:end" x="523" y="365">70</svg:text>
							<svg:text style="text-anchor:end" x="473" y="365">60</svg:text>
							<svg:text style="text-anchor:end" x="423" y="365">50</svg:text>
							<svg:text style="text-anchor:end" x="373" y="365">40</svg:text>
							<svg:text style="text-anchor:end" x="323" y="365">30</svg:text>
							<svg:text style="text-anchor:end" x="273" y="365">20</svg:text>
							<svg:text style="text-anchor:end" x="223" y="365">10</svg:text>
							<svg:text style="text-anchor:end" x="173" y="365">0</svg:text>

							<!-- Drawing the description -->
							<svg:text style="font-size:9" x="20" y="380"><xsl:value-of select="../sectionDescr"/></svg:text>
							<svg:text style="font-size:9" x="20" y="400"><xsl:value-of select="../period"/></svg:text>
						</svg:g>
					</svg:g>
				</xsl:when>
				<xsl:otherwise>
					<svg:g transform="scale(0.45)">
						<xsl:choose>
							<xsl:when test="$graphtype='last12MoAvail'">
								<xsl:attribute name="transform">
									<xsl:value-of select="'scale(0.50)'"/>
								</xsl:attribute>
							</xsl:when>
							<xsl:when test="$graphtype='LastMonthsDailyAvailability'">
								<xsl:attribute name="transform">
									<xsl:value-of select="'scale(0.45)'"/>
								</xsl:attribute>
							</xsl:when>
							<xsl:when test="$graphtype='MonthToDateDailyAvailability'">
								<xsl:attribute name="transform">
									<xsl:value-of select="'scale(0.45)'"/>
								</xsl:attribute>
							</xsl:when>
						</xsl:choose>

						<!-- Drawing the title -->
						<svg:text style="font-size:14;text-anchor:middle" x="150"  y="25">
							<xsl:value-of select="../sectionTitle"/>
						</svg:text>

						<!-- Drawing the subtitle -->
						<svg:text style="font-size:12;text-anchor:middle" x="180" y="38">(Percentage Availability)</svg:text>
			
						<!-- Draw the x-axis and y-axis -->
						<svg:g style="stroke-width:2; stroke:black">
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text>M 25 250 L 25 40 L 25 250 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 250 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
						</svg:g>

						<svg:g style="fill:none; stroke:#B0B0B0; stroke-width:1; stroke-dasharray:2 4">
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 50 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 50 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 70 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 70 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 90 L </xsl:text>
									<xsl:choose>
										<!-- xsl:when test="$graphtype = 'Last30DaysDailyAvailability'" -->
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 90 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 110 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 110 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 130 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 130 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 150 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 150 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 170 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 170 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 190 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 190 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 210 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 210 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
							<svg:path>
								<xsl:attribute name="d">
									<xsl:text> M 20 230 L </xsl:text>
									<xsl:choose>
										<xsl:when test="$graphtype = 'last12MoAvail'">
											<xsl:text>500 </xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>550 </xsl:text>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text> 230 Z</xsl:text>
								</xsl:attribute>
							</svg:path>
						</svg:g>

						<svg:g style="font-size:7">
							<svg:text style="text-anchor:end" y="250" x="20"> 0</svg:text>
							<svg:text style="text-anchor:end" y="230" x="20"> 10</svg:text>
							<svg:text style="text-anchor:end" y="210" x="20"> 20</svg:text>
							<svg:text style="text-anchor:end" y="190" x="20"> 30</svg:text>
							<svg:text style="text-anchor:end" y="170" x="20"> 40</svg:text>
							<svg:text style="text-anchor:end" y="150" x="20"> 50</svg:text>
							<svg:text style="text-anchor:end" y="130" x="20"> 60</svg:text>
							<svg:text style="text-anchor:end" y="110" x="20"> 70</svg:text>
							<svg:text style="text-anchor:end" y="90" x="20"> 80</svg:text>
							<svg:text style="text-anchor:end" y="70" x="20"> 90</svg:text>
							<svg:text style="text-anchor:end" y="50" x="20">100%</svg:text>
						</svg:g>

						<!-- Drawing the description -->
						<svg:text style="font-size:9" x="15" y="280"><xsl:value-of select="../sectionDescr"/></svg:text>
						<svg:text style="font-size:9" x="15" y="300"><xsl:value-of select="../period"/></svg:text>
					  </svg:g>

				</xsl:otherwise>
				</xsl:choose>

				<svg:g transform="scale(0.50)">
					<xsl:choose>
					<xsl:when test="$graphtype='last12MoAvail'">
						<xsl:attribute name="transform">
							<xsl:value-of select="'scale(0.50)'"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test="$graphtype='LastMonthsDailyAvailability'">
						<xsl:attribute name="transform">
							<xsl:value-of select="'scale(0.45)'"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test="$graphtype='MonthToDateDailyAvailability'">
						<xsl:attribute name="transform">
							<xsl:value-of select="'scale(0.45)'"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test="$graphtype='lastMoTop20offenders'">
						<xsl:attribute name="transform">
							<xsl:value-of select="'scale(0.55)'"/>
						</xsl:attribute>
					</xsl:when>
					</xsl:choose>
					<xsl:for-each select="rows/row">
						<xsl:variable name="position">
							<xsl:value-of select="position()"/>
						</xsl:variable>
						<xsl:apply-templates select=".">
							<xsl:with-param name="graphtype" select="$graphtype"/>
							<xsl:with-param name="warning" select="$warning"/>
							<xsl:with-param name="normal" select="$normal"/>

							<!-- JS tweaks for Tarus -->
							<xsl:with-param name="y-offset-hor">
								<xsl:choose>
									<xsl:when test="$graphtype='lastMoTop20offenders'">
										<xsl:value-of select="30 + (21 - $position) * 15" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="30 + $position * 15" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:with-param>
							<xsl:with-param name="x-offset-hor" select="25"/>
							<xsl:with-param name="x-offset-ver">
								<xsl:choose>
									<xsl:when test="$graphtype='last12MoAvail'">
										<xsl:value-of select="25 + $position * 25"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="25 + $position * 15"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:with-param>
							<xsl:with-param name="y-offset-ver" select="250"/>
						</xsl:apply-templates>
					</xsl:for-each>
				</svg:g>
</xsl:template>


<xsl:template match="calendarTable">
 <xsl:param name="sectionName"/>
 <xsl:param name="namesec"/>
<!-- <xsl:param name="y-offset"/> -->
 <xsl:param name="warning"/>
 <xsl:param name="normal"/>
 <svg:g transform="scale(0.45)">
 <!-- Drawing the title -->
  <svg:text style="font-size:14;text-anchor:middle" x="180"  y="10">
   <xsl:value-of select="../sectionTitle"/>
  </svg:text>
  <!-- Drawing the subtitle -->
  <svg:text style="font-size:12;text-anchor:middle" x="180" y="22">(Percentage Availability)</svg:text>
  <xsl:apply-templates select="daysOfWeek"/>
   <xsl:for-each select="week">
    <xsl:variable name="position">
     <xsl:value-of select="position()"/>
    </xsl:variable>
   <xsl:apply-templates select=".">
    <xsl:with-param name="x-offset">
     <xsl:value-of select="10"/>
    </xsl:with-param>
    <xsl:with-param name="y-offset">
     <!--<xsl:value-of select="$position * 50 - 20"/> -->
     <xsl:value-of select="$position * 50"/>
     </xsl:with-param>
    <xsl:with-param name="warning" select="$warning"/>
    <xsl:with-param name="normal" select="$normal"/>
   </xsl:apply-templates>
  </xsl:for-each>
  <svg:text style="font-size:9" x="10" y="360">
   <xsl:value-of select="../sectionDescr"/>
  </svg:text>
  <svg:text style="font-size:9" x="10" y="380">
   <xsl:value-of select="../period"/>
  </svg:text>
 </svg:g>
</xsl:template>

<xsl:template match="daysOfWeek">
 <xsl:for-each select="dayName">
  <xsl:variable name="weekposition">
   <xsl:value-of select="position()"/>
  </xsl:variable>
  <xsl:apply-templates select=".">
  <xsl:with-param name="x-offset">
<!--   <xsl:value-of select="25 + $weekposition * 50"/> -->
   <xsl:value-of select="$weekposition * 50 - 25"/>
  </xsl:with-param>
  <xsl:with-param name="y-offset">
   <!--<xsl:value-of select="10"/> -->
   <xsl:value-of select="45"/>
  </xsl:with-param>
  </xsl:apply-templates>
 </xsl:for-each>
</xsl:template>

<xsl:template match="dayName">
 <xsl:param name="x-offset"/>
 <xsl:param name="y-offset"/>
 <svg:text style="font-size:10;text-anchor:middle">
      <xsl:attribute name="x">
       <xsl:value-of select="$x-offset"/>
      </xsl:attribute>
      <xsl:attribute name="y">
       <xsl:value-of select="$y-offset"/>
      </xsl:attribute>
       <xsl:value-of select="."/>
     </svg:text> 
</xsl:template>

<xsl:template match="week">
 <xsl:param name="y-offset"/>
 <xsl:param name="warning"/>
 <xsl:param name="normal"/>
 <xsl:for-each select="day">
  <xsl:variable name="position">
   <xsl:value-of select="position()"/>
  </xsl:variable>
  <xsl:apply-templates select=".">
  <xsl:with-param name="x-offset">
<!--   <xsl:value-of select="$position * 50" /> -->
   <xsl:value-of select="$position * 50 - 50 " />
  </xsl:with-param>
  <xsl:with-param name="y-offset" select="$y-offset"/> 
  <xsl:with-param name="warning" select="$warning"/>
  <xsl:with-param name="normal" select="$normal"/>
  </xsl:apply-templates>
 </xsl:for-each>
</xsl:template>

<xsl:template match="day">
 <xsl:param name="x-offset"/>
 <xsl:param name="y-offset"/>
 <xsl:param name="warning"/>
 <xsl:param name="normal"/>
 <xsl:choose>
  <!-- these sections are all visible as they are vaild days of month -->
  <xsl:when test="@visible='true'">
     <!-- add date to top right of circle -->
     <svg:text style="font-size:9;text-anchor:middle">
      <xsl:attribute name="x">
       <xsl:value-of select="$x-offset + 43 "/>
      </xsl:attribute>
      <xsl:attribute name="y">
       <xsl:value-of select="$y-offset + 8 "/>
      </xsl:attribute>
      <xsl:value-of select="@date"/>
     </svg:text>
   <xsl:choose>
    <xsl:when test="number(@pctValue) = 0">
     <!-- add empty circle for empty data  -->
     <svg:circle>
     <xsl:attribute name="fill">#eee</xsl:attribute>
      <xsl:attribute name="cx">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="cy">
       <xsl:value-of select="$y-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="r">
       20 
      </xsl:attribute>
     </svg:circle>
    </xsl:when>
    <!-- critical value -->
    <xsl:when test="$warning >= number(@pctValue)">
     <!-- add red circle for critical data  -->
     <svg:circle>
     <xsl:attribute name="fill">red</xsl:attribute>
      <xsl:attribute name="cx">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="cy">
       <xsl:value-of select="$y-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="r">
       20 
      </xsl:attribute>
     </svg:circle>
     <!-- draw number in circle -->
     <svg:text style="font-size:12;text-anchor:middle">
      <xsl:attribute name="x">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="y">
       <xsl:value-of select="$y-offset + 30 "/>
      </xsl:attribute>
      <xsl:value-of select="format-number(@pctValue, '0.00')"/>
     </svg:text>
    </xsl:when>
    <!-- warning value -->
    <xsl:when test="$normal >= number(@pctValue)">
     <!-- add yellow cricle for warning data  -->
     <svg:circle>
     <xsl:attribute name="fill">yellow</xsl:attribute>
      <xsl:attribute name="cx">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="cy">
       <xsl:value-of select="$y-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="r">
       20 
      </xsl:attribute>
     </svg:circle>
     <!-- draw number in circle -->
     <svg:text style="font-size:12;text-anchor:middle">
      <xsl:attribute name="x">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="y">
       <xsl:value-of select="$y-offset + 30 "/>
      </xsl:attribute>
      <xsl:value-of select="format-number(@pctValue, '0.00')"/>
     </svg:text>
    </xsl:when>
    <xsl:otherwise>
     <!-- add green cricle for normal data  -->
     <svg:circle>
      <xsl:attribute name="fill">green</xsl:attribute>
      <xsl:attribute name="cx">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="cy">
       <xsl:value-of select="$y-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="r">
       20 
      </xsl:attribute>
     </svg:circle>
     <!-- draw number in circle -->
     <svg:text style="font-size:12;text-anchor:middle">
      <xsl:attribute name="x">
       <xsl:value-of select="$x-offset + 25 "/>
      </xsl:attribute>
      <xsl:attribute name="y">
       <xsl:value-of select="$y-offset + 30 "/>
      </xsl:attribute>
      <xsl:value-of select="format-number(@pctValue, '0.00')"/>
     </svg:text>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:when>
  <xsl:otherwise>
  <!-- skip this location if it's not visible -->
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

</xsl:stylesheet>
