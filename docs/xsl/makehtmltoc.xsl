<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"></xsl:output>
	<xsl:template match="/">
		<html>
			<xsl:apply-templates></xsl:apply-templates>
		</html>
	</xsl:template>
	<xsl:template match="manual">
		<body>
			<xsl:apply-templates select="levels"></xsl:apply-templates>
		</body>
	</xsl:template>
	<xsl:template match="levels">
		<xsl:apply-templates></xsl:apply-templates>
	</xsl:template>
	<xsl:template match="level|sublevel1|sublevel2|sublevel3">
		<FONT SIZE="-1" FACE="Arial">
			<A>
				<xsl:attribute name="HREF">
					<xsl:value-of select="url"></xsl:value-of>
				</xsl:attribute>
				<xsl:value-of select="title"></xsl:value-of>
			</A>
		</FONT>
		<DL>
			<DD>
				<xsl:apply-templates select="level|sublevel1|sublevel2|sublevel3"></xsl:apply-templates>
			</DD>
		</DL>
	</xsl:template>
</xsl:stylesheet>