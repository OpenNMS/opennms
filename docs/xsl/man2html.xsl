<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"></xsl:output>
	<xsl:param name="sep">no</xsl:param>
	<xsl:param name="anchor">no</xsl:param>
	<xsl:param name="internalanchor">no</xsl:param>
	<xsl:param name="toc">no</xsl:param>
	<xsl:template match="/">
		<html>
		<xsl:comment>This file was generated automatically by man2html.xsl.</xsl:comment>
			<body bgcolor="#ffffff" LINK="#000000" VLINK="#006a6a">
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="book">
		<xsl:apply-templates/>
	</xsl:template>
	<!--  
         Beginning of BOOKINFO section 
-->
	<xsl:template match="bookinfo">
		<p>
		<table width="100%" border="0" cellspacing="0" cellpadding="3">
			<tr>
				<td bgcolor="#000099">
					<b>
						<font color="#ffffff" SIZE="+4" FACE="Arial">
							<xsl:apply-templates select="title"></xsl:apply-templates>
						</font>
					</b>
				</td>
			</tr>
			<tr>
				<td bgcolor="#ffffff">
					<p>
						<xsl:apply-templates select="corpname"></xsl:apply-templates><br />
						Published in <xsl:apply-templates select="pubdate"></xsl:apply-templates><br />
						<xsl:apply-templates select="pubsnumber"></xsl:apply-templates><br />
						<xsl:apply-templates select="copyright"></xsl:apply-templates><br />
						<!-- <xsl:apply-templates select="legalnotice"></xsl:apply-templates><br /> -->
						<a href="http://www.opennms.org/">www.opennms.org</a><br />
					</p>
				</td>
			</tr>
		</table>
		</p>
		<!-- time to build the table of contents -->
		<xsl:if test="$anchor='yes'">
			<hr />
			<table width="100%" border="0" cellspacing="0" cellpadding="2">
				<tr>
					<td align="left" bgcolor="#000099">
						<font color="#ffffff" face="Arial" size="+1">Introduction</font>
					</td>
				</tr>
				<tr>
					<td align="left">
						<b>
							<font size="+2" face="Arial">Table of Contents</font>
						</b>
					</td>
				</tr>
			</table>
			<xsl:for-each select="/book/chapter|/book/preface|/book/appendix">
				<dl>
					<dt>
						<font size="+1" face="Arial">
							<a href="#{@id}">
								<xsl:value-of select="@label"/>
								<xsl:text>  </xsl:text>
								<xsl:value-of select="subtitle"/>
							</a>
						</font>
					</dt>
					<xsl:for-each select="sect1">
						<dd>
							<a href="#{@id}">
								<xsl:value-of select="@label"/>
								<xsl:text>  </xsl:text>
								<xsl:value-of select="title"></xsl:value-of>
							</a>
							<xsl:for-each select="sect2">
								<dl>
									<dd>
										<a href="#{@id}">
											<xsl:value-of select="@label"/>
											<xsl:text>  </xsl:text>
											<xsl:value-of select="title"/>
										</a>
									</dd>
								</dl>
							</xsl:for-each>
						</dd>
					</xsl:for-each>
				</dl>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	<xsl:template match="title">
		<p>
		<b>
			<font face="Arial">
				<xsl:value-of select="parent::node()/@label"></xsl:value-of>
				<xsl:text>  </xsl:text>
				<xsl:value-of select="."></xsl:value-of>
			</font>
		</b>
		</p>
	</xsl:template>
	<xsl:template match="formalpara/title">
		<b>
			<xsl:value-of select="."></xsl:value-of>
		</b>
	</xsl:template>
	<xsl:template match="figure/title">
		<i>
			<font face="Arial" size="-1">
	Figure: <xsl:value-of select="."></xsl:value-of>
			</font>
		</i>
	</xsl:template>

	<xsl:template match="bookinfo/title">
		<xsl:value-of select="."></xsl:value-of>
	</xsl:template>

	<xsl:template match="chapter/title|appendix/title|preface/title">
		<p>
		<table width="100%" border="0" cellspacing="0" cellpadding="2">
			<tr>
				<td align="left" bgcolor="#000099">
				<b><font size="+2" color="#ffffff" face="Arial"><xsl:value-of select="parent::node()/@label"/><xsl:text> </xsl:text><xsl:value-of select="."/></font></b></td> 
			</tr>
			<tr>
				<td align="left">
					<b><font size="+1" face="Arial"><xsl:value-of select="../subtitle"/></font></b>
				</td>
			</tr>
			<!--
			<tr>
 				<td colspan="2">
 				<b><font size="+2" face="Arial"><xsl:value-of select="../subtitle"/></font></b></td>
			</tr>
			-->
		</table>
		</p>
	</xsl:template>

	<xsl:template match="subtitle">
		<!--
	<B>
		<FONT FACE="Arial"><xsl:value-of select="."/></FONT>
	</B>
-->
	</xsl:template>
	<xsl:template match="authorgroup">
		<p>
			<xsl:apply-templates select="author"/>
		</p>
	</xsl:template>

	<xsl:template match="author">
		<xsl:value-of select="firstname"/>
		<xsl:value-of select="surname"/>, 
	<xsl:value-of select="affiliation/orgname"/>
		<br />
	</xsl:template>
	<xsl:template match="pubdate">
		<xsl:value-of select="."/>
	</xsl:template>
	<xsl:template match="pubsnumber">
		<xsl:value-of select="."/>
	</xsl:template>
	<xsl:template match="legalnotice">
			<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="copyright">
			Copyright (c) <xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="year">
		<xsl:value-of select="."/>
		<xsl:if test="position()!=last()">
			<xsl:text>, </xsl:text>
		</xsl:if>
	</xsl:template>
	<xsl:template match="holder">
		<xsl:value-of select="."/>
	</xsl:template>
	<xsl:template match="corpname">
		<xsl:value-of select="."/>
	</xsl:template>
<!-- 
                 Beginning of major sections section 
-->
	<xsl:template match="chapter|appendix|preface">
		<xsl:if test="$anchor='yes'">
			<a name="{@id}" />
		</xsl:if>
		<xsl:if test="$sep='yes'">
			<hr />
		</xsl:if>
		<xsl:apply-templates></xsl:apply-templates>
	</xsl:template>
	<xsl:template match="sect1|sect2|sect3|sect4|sect5">
		<dl><dd>
			<a name="{@id}" />
			<xsl:apply-templates/>
		</dd></dl>
	</xsl:template>
<!-- 
                 Beginning of COMMON section 
-->
	<xsl:template match="para">
		<p>
			<xsl:apply-templates/>
		</p>
	</xsl:template>

	<xsl:template match="simplelist">
		<dl>
			<xsl:apply-templates/>
		</dl>
	</xsl:template>

	<xsl:template match="citation">
		[<xsl:apply-templates/>]
	</xsl:template>

	<xsl:template match="blockquote">
		<blockquote>
			<xsl:apply-templates/>
		</blockquote>
	</xsl:template>
	<xsl:template match="programlisting">
		<pre>
			<xsl:apply-templates/>
		</pre>
	</xsl:template>
	<xsl:template match="member">
		<dd>
			<xsl:apply-templates/>
		</dd>
	</xsl:template>
	<xsl:template match="itemizedlist">
		<ul>
			<xsl:apply-templates/>
		</ul>
	</xsl:template>
	<xsl:template match="orderedlist">
		<ol>
			<xsl:apply-templates/>
		</ol>
	</xsl:template>
	<xsl:template match="listitem">
		<li>
			<xsl:apply-templates/>
		</li>
	</xsl:template>
	<xsl:template match="figure">
		<br />
		<table><tr><td>
		<xsl:apply-templates select="graphic"/>
		</td></tr><tr><td align="center">
		<xsl:apply-templates select="title"/>
		</td></tr></table>
	</xsl:template>

	<xsl:template match="ulink">
		<a href="{@url}">
			<xsl:if test="@type = 'separate'">
				<xsl:attribute name="target">_blank</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="."/>
		</a>
</xsl:template>

	<xsl:template match="xref">
		<a href="{@linkend}.html">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>

	<xsl:template match="graphic">
		<img src="{@fileref}" naturalsizeflag="{@natflag}" align="{@align}">
<!--
			<xsl:if test="@size=''">
				<xsl:attribute name="width">
					<xsl:text>160</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@size='tiny'">
				<xsl:attribute name="width">
					<xsl:text>100</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@size='small'">
				<xsl:attribute name="width">
					<xsl:text>180</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@size='medium'">
				<xsl:attribute name="width">
					<xsl:text>300</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@size='large'">
				<xsl:attribute name="width">
					<xsl:text>400</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@size='huge'">
				<xsl:attribute name="width">
					<xsl:text>500</xsl:text>
				</xsl:attribute>
			</xsl:if>
-->

		</img>
	</xsl:template>

	<xsl:template match="emphasis">
		<xsl:choose>
			<xsl:when test="@role='bold'">
				<b>
					<xsl:value-of select="."></xsl:value-of>
				</b>
			</xsl:when>
			<xsl:otherwise>
				<i>
					<xsl:value-of select="."></xsl:value-of>
				</i>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	<xsl:template match="formalpara">
		<dl>
			<dt>
				<xsl:apply-templates select="title"/>
			</dt>
			<dd>
				<xsl:apply-templates select="para"/>
			</dd>
		</dl>
	</xsl:template>
	-->
	<xsl:template match="formalpara">
		<p><b><font size="+1"><xsl:apply-templates select="title"/></font></b></p>
		<xsl:apply-templates select="para"/>
	</xsl:template>

	<xsl:template match="literal|function">
		<tt><xsl:value-of select="."/></tt>
	</xsl:template>

<!-- T A B L E   D E F S -->

	<xsl:template match="table">
		<table cellpadding="2" cellspacing="1">
			<xsl:apply-templates/>
		</table>
	</xsl:template>

	<xsl:template match="colspec">
<!--		<fo:table-column>
			<xsl:attribute name="column-width">
				<xsl:value-of select="@colwidth"/>
			</xsl:attribute>
		</fo:table-column> 
-->
	</xsl:template>

	<xsl:template match="tgroup">
		<xsl:apply-templates select="colspec"/>
		<xsl:apply-templates select="thead"/>
		<xsl:apply-templates select="tbody"/>
	</xsl:template>

	<xsl:template match="thead">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="tbody">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="row">
		<tr>
			<xsl:apply-templates/>
		</tr>
	</xsl:template>

	<xsl:template match="thead/row/entry">
		<th align="left" bgcolor="#e0e0e0"><font size="-1" face="Arial" color="#000000">
			<xsl:apply-templates/>
		</font>
		</th>
	</xsl:template>

	<xsl:template match="tbody/row/entry">
		<td bgcolor="#ffffff">
			<xsl:apply-templates/>
		</td>
	</xsl:template>

</xsl:stylesheet>
