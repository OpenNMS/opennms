<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output indent="yes"/>
	<xsl:template match="book">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
**				 C O V E R   P A G E   M A S T E R
				<fo:simple-page-master master-name="cover" 
					page-width="8.5in" 
					page-height="11in" 
					margin-top="4in" 
					margin-bottom="0in" 
					margin-left="1in" 
					margin-right=".75in">
					<fo:region-body margin-top="0.5in"/>
				</fo:simple-page-master>

**				 C O P Y R I G H T   P A G E   M A S T E R 
				<fo:simple-page-master master-name="copyright" 
					page-width="8.5in" 
					page-height="11in" 
					margin-top="7.5in" 
					margin-bottom="1in" 
					margin-left="1in" 
					margin-right="1in">
					<fo:region-body margin-top="0.5in"/>
				</fo:simple-page-master>

**				 T O C   P A G E   M A S T E R 
				<fo:simple-page-master master-name="toc" 
					page-width="8.5in" 
					page-height="11in" 
					margin-top=".25in" 
					margin-bottom=".75in" 
					margin-left="1in" 
					margin-right="1in">
					<fo:region-body margin-top="0.5in"/>
				</fo:simple-page-master>

**				 L E F T   P A G E   M A S T E R 
				<fo:simple-page-master master-name="left" 
					page-width="8.5in" 
					page-height="11in" 
					margin-top=".25in" 
					margin-bottom=".25in" 
					margin-left=".75in" 
					margin-right="1in">
					<fo:region-body 
						margin-top="0.5in" 
						margin-bottom=".4in"/>
					<fo:region-before 
						margin-bottom="0.1in" 
						margin-left="0in" 
						margin-right="0in" 
						margin-top="0in" 
						extent="0.5in"/>
					<fo:region-after 
						margin-bottom="0in" 
						margin-left="0in" 
						margin-right="0in" 
						margin-top="0.2in" 
						extent="0.3in"/>
				</fo:simple-page-master>

**				   R I G H T   P A G E   M A S T E R 
				<fo:simple-page-master master-name="right" 
					page-width="8.5in" 
					page-height="11in" 
					margin-top=".25in" 
					margin-bottom=".25in" 
					margin-left="1in" 
					margin-right=".75in">
					<fo:region-body 
						margin-top="0.5in" 
						margin-bottom=".4in"/>
					<fo:region-before 
						margin-bottom="0.1in" 
						margin-left="0in" 
						margin-right="0in" 
						margin-top="0in" 
						extent="0.5in"/>
					<fo:region-after 
						margin-bottom="0in" 
						margin-left="0in" 
						margin-right="0in" 
						margin-top="0.2in" 
						extent="0.3in"/>
				</fo:simple-page-master>


				<fo:page-sequence-master master-name="all">
					<fo:repeatable-page-master-alternatives>
						<fo:conditional-page-master-reference master-name="right"
							page-position="first" />
						<fo:conditional-page-master-reference master-name="right"
							odd-or-even="odd" />
						<fo:conditional-page-master-reference master-name="left"
							odd-or-even="even" />
					</fo:repeatable-page-master-alternatives>
				</fo:page-sequence-master>


			</fo:layout-master-set>

		<!-- End of page masters, start sequences -->

			<!-- C O V E R    P A G E -->
			<fo:page-sequence master-name="cover">
				<fo:flow flow-name="xsl-region-body">
					<fo:block text-align="center" font-family="sans-serif" space-after.optimum="3.5in">
						<xsl:apply-templates select="bookinfo"/>
					</fo:block>
					<fo:block text-align="center">
						<fo:external-graphic width="2.5in" src="file:images/opennmsf.gif"/>
					</fo:block>
				</fo:flow>
			</fo:page-sequence>

			<!--   C O P Y R I G H T      P A G E      -->
			<fo:page-sequence master-name="copyright">
				<fo:flow flow-name="xsl-region-body">
            				<fo:block-container 
						border-color="black" 
						border-style="solid" 
						border-width="1pt" 
						height=".85in" 
						width="4in" 
						top="3cm" 
						left="0in" 
						padding="2pt" 
						background-color="#e0e0e0"
						position="absolute">
						<fo:block 
							text-align="center" 
							font-family="serif" 
							font-size="10pt">
							<fo:block
								font-size="12pt" 
								font-weight="bold">
								<xsl:value-of select="/book/bookinfo/corpname"></xsl:value-of>
							</fo:block>
							<fo:block>
								Publication Date: <xsl:value-of select="/book/bookinfo/pubdate"/>
							</fo:block>
							<fo:block>
								<xsl:value-of select="/book/bookinfo/pubsnumber"/>
							</fo:block>
							<fo:block>
								<xsl:apply-templates select="/book/bookinfo/copyright"/>
							</fo:block>
							<fo:block>
								<xsl:apply-templates select="/book/bookinfo/legalnotice"/>
							</fo:block>
						</fo:block>
					</fo:block-container>
				</fo:flow>
			</fo:page-sequence>

			<!--   T O C      P A G E      -->
			<fo:page-sequence master-name="toc">
				<fo:flow flow-name="xsl-region-body">
					<fo:block font-family="sans-serif" font-size="12pt">
						<fo:block font-size="20pt" 
							font-family="sans-serif" 
							line-height="30pt"
							space-after.optimum="0in"
							background-color="#6b8e23"
							color="white"
							text-align="end"
							start-indent="4in" >
						Table of Contents<xsl:text> </xsl:text>
					</fo:block>
						<xsl:for-each select="chapter|preface|appendix">
							<fo:block font-size="10pt" 
								font-weight="bold" 
								space-before.optimum="0in">
								<xsl:value-of select="@label"/><xsl:text>   </xsl:text>
								<fo:basic-link color="#008000" internal-destination="{@id}">
								<xsl:value-of select="subtitle"/>
								</fo:basic-link>
								<xsl:for-each select="sect1">
									<fo:block font-size="10pt" 
										start-indent=".5in" 
										line-height="11pt" 
										font-weight="normal">
										<xsl:value-of select="@label"/><xsl:text>  </xsl:text>
										<fo:basic-link color="#008000" internal-destination="{@id}">
										<xsl:value-of select="title"/>
										</fo:basic-link>
										<xsl:for-each select="sect2">
											<fo:block 
												font-size="9pt" 
												start-indent="1in"
												line-height="10pt" >
												<xsl:value-of select="@label"/><xsl:text>  </xsl:text>
												<fo:basic-link  color="#008000" internal-destination="{@id}">
													<xsl:value-of select="title"/>
												</fo:basic-link>
											</fo:block>
										</xsl:for-each>
									</fo:block>
								</xsl:for-each>
							</fo:block>
						</xsl:for-each>
					</fo:block>
				</fo:flow>
			</fo:page-sequence>
**
** Page Sequences here, must match a page master above
**			

			<fo:page-sequence master-name="all">
				** FOOTER
				<fo:static-content flow-name="xsl-region-after">
					<fo:block font-family="sans-serif" font-size="9pt" text-align="center">
						<fo:leader 
							leader-pattern="rule" 
							space-before.optimum="0pt"
							leader-length.minimum="6.75in"
                  					leader-length.maximum="6.75in"
							space-after.optimum="0pt"/>
						<fo:block>
							<xsl:value-of select="/book/bookinfo/pubsnumber"/>
							<xsl:text> -- Page </xsl:text>
							<fo:page-number/>
						</fo:block>
					</fo:block>
				</fo:static-content>
				** HEADER
				<fo:static-content flow-name="xsl-region-before">
					<fo:block text-align="start" font-family="sans-serif" font-size="10pt">	
						<xsl:value-of select="/book/bookinfo/title"/>
					</fo:block>
				</fo:static-content>
				<fo:flow flow-name="xsl-region-body">
					<fo:block font-family="serif" font-size="11pt">
						<xsl:apply-templates select="chapter|preface|appendix"/>
					</fo:block>
				</fo:flow>
			</fo:page-sequence>

		</fo:root>
	</xsl:template>

	<!--		Cover Page Area			-->
	<xsl:template match="bookinfo">
		<xsl:apply-templates select="title"/>
		<xsl:apply-templates select="subtitle"/>
		<fo:block font-size="11pt" line-height="16pt">
			<xsl:value-of select="corpname"/>
		</fo:block>
	</xsl:template>

	<xsl:template match="pubsnumber">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="bookinfo/title">
		<fo:block font-size="24pt" 
			line-height="30pt" 
			font-weight="bold" 
			color="#666666" 
			space-after.optimum=".1in">
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

	<xsl:template match="bookinfo/subtitle">
		<fo:block font-size="14pt" 
			line-height="30pt" 
			font-weight="bold" 
			space-after.optimum=".2in">
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

	<xsl:template match="preface/title|appendix/title|chapter/title">
<!--
		<fo:block font-size="20pt" 
			line-height="30pt" 
			font-weight="bold" 
			space-after.optimum="0in" 
			color="#666666" 
			text-align="end" 
			font-family="sans-serif"
			keep-with-next="true">
			<xsl:value-of select="."/>
		</fo:block>
-->
		<fo:block font-size="20pt" 
			font-family="sans-serif" 
			line-height="30pt"
			space-after.optimum="0in"
			background-color="#6b8e23"
			color="white"
			text-align="end"
			start-indent="4in" >
			<xsl:value-of select="."/><xsl:text> </xsl:text>
		</fo:block>
	</xsl:template>

	<xsl:template match="preface/subtitle|appendix/subtitle|chapter/subtitle">
		<fo:block font-size="14pt" 
			line-height="20pt" 
			font-weight="bold" 
			space-after.optimum=".5in" 
			color="#666666" 
			text-align="end" 
			font-family="sans-serif">
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect1/title">
		<fo:block font-size="18pt" 
			line-height="24pt" 
			font-weight="bold" 
			space-before.optimum=".1in" 
			space-after.optimum=".05in" 
			font-family="sans-serif"
			keep-with-next="true">
			<xsl:value-of select="parent::node()/@label"/>
			<xsl:text>  </xsl:text>
			<xsl:value-of select="."/>
			</fo:block>
	</xsl:template>


	<xsl:template match="sect2/title">
		<fo:block  font-size="14pt" 
			line-height="24pt" 
			font-weight="bold" 
			space-before.optimum=".1in" 
			space-after.optimum=".05in"
			keep-with-next="true">
			<xsl:value-of select="parent::node()/@label"/>
			<xsl:text>  </xsl:text>
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect3/title|sect4/title|sect5/title">
		<fo:block id="{@id}" font-size="12pt" 
			line-height="20pt" 
			font-weight="bold" 
			space-before.optimum=".1in" 
			space-after.optimum=".05in">
			<xsl:value-of select="parent::node()/@label"/>
			<xsl:text>  </xsl:text>
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

	<xsl:template match="copyright">
		<fo:block>
		Copyright (c) 
		<xsl:apply-templates select="year"/>
			<xsl:text>  </xsl:text>
			<xsl:apply-templates select="holder"/>
		</fo:block>
	</xsl:template>

	<xsl:template match="year">
		<xsl:value-of select="."></xsl:value-of>
		<xsl:if test="position()!=last()">
			<xsl:text>, </xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="holder">
		<xsl:value-of select="."></xsl:value-of>
		<xsl:if test="position()=last()">
			<xsl:text>.</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="legalnotice">
		<fo:block space-before.optimum=".25in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="pubdate">
		<xsl:value-of select="."/>
	</xsl:template>

	<!--		Chapter Area		-->
	<xsl:template match="chapter|appendix|preface">
		<fo:block id="{@id}" break-before="odd-page">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect1">
		<fo:block id="{@id}" start-indent=".25in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect2">
		<fo:block id="{@id}" start-indent=".5in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect3">
		<fo:block start-indent=".75in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect4">
		<fo:block start-indent="1in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="sect5">
		<fo:block start-indent="1.25in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="figure">
		<xsl:apply-templates select="graphic"/>
		<xsl:apply-templates select="title"/>
	</xsl:template>

	<xsl:template match="figure/title">
		<fo:block id="{@id}" font-size="8pt" 
			space-before.optimum=".1in" 
			line-height="11pt" 
			font-family="sans-serif"
			font-style="italic">
			<xsl:text>Figure: </xsl:text>
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>
 
	<xsl:template match="graphic">
		<fo:external-graphic src="file:{@fileref}">
				<xsl:if test="@size=''">
					<xsl:attribute name="width">
						<xsl:text>2.5in</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="@size='tiny'">
					<xsl:attribute name="width">
						<xsl:text>1.25in</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="@size='small'">
					<xsl:attribute name="width">
						<xsl:text>2in</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="@size='medium'">
					<xsl:attribute name="width">
						<xsl:text>3.5in</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="@size='large'">
					<xsl:attribute name="width">
						<xsl:text>4in</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="@size='huge'">
					<xsl:attribute name="width">
						<xsl:text>6in</xsl:text>
					</xsl:attribute>
				</xsl:if>
		</fo:external-graphic>
	</xsl:template>

	<xsl:template match="xref">
		<fo:basic-link color="#008000" internal-destination="{@id}">
			<xsl:value-of select="."/>
		</fo:basic-link>
	</xsl:template>
    
	<xsl:template match="ulink">
			<fo:basic-link color="#008000" external-destination="{@url}">
				<xsl:value-of select="."/>
			</fo:basic-link>
	</xsl:template>

	<xsl:template match="para">
		<fo:block space-after.optimum=".1in">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="emphasis">
		<xsl:choose>
			<xsl:when test="@role='bold'">
				<fo:inline font-weight="bold">
					<xsl:value-of select="."/>
				</fo:inline>
			</xsl:when>
			<xsl:otherwise>
				<fo:inline font-style="italic">
					<xsl:value-of select="."/>
				</fo:inline>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="programlisting">
		<fo:block 
			text-align="start" 
			white-space-collapse="false" 
			font-family="monospace" 
			font-size="8pt">
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

	<!-- List section -->
	<xsl:template match="itemizedlist">
		<fo:list-block provisional-distance-between-starts=".05in">
			<xsl:apply-templates/>
		</fo:list-block>
	</xsl:template>

	<xsl:template match="itemizedlist/listitem">
		<fo:list-item>
			<fo:list-item-label>
				<fo:block>
					<xsl:text>&#x2022;</xsl:text>
				</fo:block>
			</fo:list-item-label>
			<fo:list-item-body>
				<xsl:apply-templates/>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>

	<xsl:template match="orderedlist">
		<fo:list-block>
			<xsl:apply-templates/>
		</fo:list-block>
	</xsl:template>

	<xsl:template match="orderedlist/listitem">
		<fo:list-item>
			<fo:list-item-label>
				<fo:block>
					<xsl:number count="listitem" format="1."/>
				</fo:block>
			</fo:list-item-label>
			<fo:list-item-body>
				<xsl:apply-templates/>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>

	<xsl:template match="formalpara">
		<fo:block space-after.optimum=".1in">
		<fo:table>
			<fo:table-column>
				<xsl:attribute name="column-width">
					<xsl:text>1.25in</xsl:text>
				</xsl:attribute>
			</fo:table-column>
			<fo:table-column>
				<xsl:attribute name="column-width">
					<xsl:text>.1in</xsl:text>
				</xsl:attribute>
			</fo:table-column>
			<fo:table-column>
				<xsl:attribute name="column-width">
					<xsl:text>5.25in</xsl:text>
				</xsl:attribute>
			</fo:table-column>
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell>
						<xsl:apply-templates select="title"/>
					</fo:table-cell>
					<fo:table-cell>
						<xsl:text> </xsl:text>
					</fo:table-cell>
					<fo:table-cell>
						<xsl:apply-templates select="para"/>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
		</fo:block>
	</xsl:template>

	<xsl:template match="formalpara/title">
		<fo:block font-weight="bold">
			<xsl:value-of select="."/>
		</fo:block>
	</xsl:template>

<!-- T A B L E   D E F S -->

	<xsl:template match="table">
		<fo:block
			start-indent="0in">
		<fo:table 
		space-after.optimum=".1in">
			<xsl:apply-templates/>
		</fo:table>
		</fo:block>
	</xsl:template>

	<xsl:template match="colspec">
		<fo:table-column>
			<xsl:attribute name="column-width">
				<xsl:value-of select="@colwidth"/>
			</xsl:attribute>
		</fo:table-column> 
	</xsl:template>

	<xsl:template match="tgroup">
		<xsl:apply-templates select="colspec"/>
		<fo:table-body font-size="9pt" font-family="sans-serif">
			<xsl:apply-templates select="thead"/>
			<xsl:apply-templates select="tbody"/>
		</fo:table-body>
	</xsl:template>

	<xsl:template match="thead">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="tbody">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="row">
		<fo:table-row>
			<xsl:if test="@space-after!=''">
				<xsl:attribute name="space-after.optimum">
					<xsl:value-of select="@space-after"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@space-after=''">
				<xsl:attribute name="space-after.optimum">
					<xsl:text>.07in</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</fo:table-row>
	</xsl:template>

	<xsl:template match="tbody/row/entry">
		<fo:table-cell
			border-width=".5mm"
			padding="1mm"
			border-color="white">
			<fo:block>
				<xsl:if test="@text-align!=''">
					<xsl:attribute name="text-align">
						<xsl:value-of select="@text-align"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:apply-templates/>
			</fo:block>
		</fo:table-cell>
	</xsl:template>

	<xsl:template match="thead/row/entry">
		<fo:table-cell 
			background-color="#e0e0e0"
			border-width=".5mm"
			padding="1mm"
			border-color="white">
			<fo:block font-weight="bold" font-family="sans-serif">
				<xsl:apply-templates/>
			</fo:block>
		</fo:table-cell>
	</xsl:template>

</xsl:stylesheet>

