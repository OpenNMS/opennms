<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"></xsl:output>
	<xsl:template match="/">
		<xsl:apply-templates></xsl:apply-templates>
	</xsl:template>
	<xsl:template match="book">
		<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE manual [</xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT manual (maninfo,levels) &gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT levels (level+) &gt;</xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT maninfo (title, pubdate, rev) &gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT pubdate (#PCDATA) &gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT rev (#PCDATA) &gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT level (title, url,sublevel1*) &gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT title (#PCDATA) &gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT url (#PCDATA) &gt;</xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT sublevel1 (title, url, sublevel2*) &gt;</xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT sublevel2 (title, url, sublevel3*) &gt;</xsl:text>
		<xsl:text disable-output-escaping="yes">&lt;!ELEMENT sublevel3 (title, url) &gt; ]&gt;</xsl:text>
		<xsl:comment>This file was generated automatically by makexmltoc.xsl.</xsl:comment>
		<xsl:variable name="toptitle" select="/book/bookinfo/title"></xsl:variable>
		<xsl:variable name="rev" select="/book/bookinfo/pubsnumber"></xsl:variable>
		<xsl:variable name="date" select="/book/bookinfo/pubdate"></xsl:variable>
		<manual>
			<maninfo>
				<title>
					<xsl:value-of select="$toptitle"></xsl:value-of>
				</title>
				<pubdate>
					<xsl:value-of select="$date"></xsl:value-of>
				</pubdate>
				<rev>
					<xsl:value-of select="$rev"></xsl:value-of>
				</rev>
			</maninfo>
			<levels>
				<level>
					<xsl:apply-templates select="bookinfo"></xsl:apply-templates>
					<xsl:apply-templates select="preface|chapter|appendix"></xsl:apply-templates>
				</level>
			</levels>
		</manual>
	</xsl:template>
	<xsl:template match="bookinfo">
		<title>
			<xsl:value-of select="title"></xsl:value-of>
		</title>
		<url>
			<xsl:value-of select="@id"></xsl:value-of>.html</url>
	</xsl:template>
	<xsl:template match="chapter|preface|appendix">
		<sublevel1>
			<xsl:variable name="idname" select="@id"></xsl:variable>
			<title>
				<xsl:value-of select="@label"></xsl:value-of>
				<xsl:text>.  </xsl:text>
				<xsl:value-of select="subtitle"></xsl:value-of>
			</title>
			<url>
				<xsl:value-of select="$idname"></xsl:value-of>.html</url>
			<xsl:for-each select="sect1">
				<sublevel2>
					<title>
						<xsl:value-of select="@label"></xsl:value-of>
						<xsl:text>  </xsl:text>
						<xsl:value-of select="title"></xsl:value-of>
					</title>
					<url>
						<xsl:value-of select="$idname"></xsl:value-of>.html#<xsl:value-of select="@id"></xsl:value-of>
					</url>
					<xsl:for-each select="sect2">
						<sublevel3>
							<title>
								<xsl:value-of select="@label"></xsl:value-of>
								<xsl:text>  </xsl:text>
								<xsl:value-of select="title"></xsl:value-of>
							</title>
							<url>
								<xsl:value-of select="$idname"></xsl:value-of>.html#<xsl:value-of select="@id"></xsl:value-of>
							</url>
						</sublevel3>
					</xsl:for-each>
				</sublevel2>
			</xsl:for-each>
		</sublevel1>
	</xsl:template>
</xsl:stylesheet>