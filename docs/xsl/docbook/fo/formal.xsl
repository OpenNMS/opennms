<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>

<!-- ********************************************************************
     ********************************************************************

     This file is part of the XSL DocBook Stylesheet distribution.
     See ../README or http://nwalsh.com/docbook/xsl/ for copyright
     and other information.

     ******************************************************************** -->

<xsl:template name="formal.object">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <fo:block id="{$id}"
            xsl:use-attribute-sets="formal.object.properties">
    <xsl:call-template name="formal.object.heading"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template name="formal.object.heading">
  <xsl:param name="title"></xsl:param>
  <fo:block xsl:use-attribute-sets="formal.title.properties">
    <xsl:apply-templates select="." mode="object.title.markup"/>
  </fo:block>
</xsl:template>

<xsl:template name="informal.object">
  <fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template name="semiformal.object">
  <xsl:choose>
    <xsl:when test="./title">
      <xsl:call-template name="formal.object"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="informal.object"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="figure|example">
  <xsl:call-template name="formal.object"/>
</xsl:template>

<xsl:template match="table">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="prop-columns"
    select=".//colspec[contains(@colwidth, '*')]"/>

  <fo:table-and-caption id="{$id}"
                        xsl:use-attribute-sets="formal.object.properties"
                        keep-together.within-column="1">
    <fo:table-caption>
      <fo:block xsl:use-attribute-sets="formal.title.properties">
        <xsl:apply-templates select="." mode="object.title.markup"/>
      </fo:block>
    </fo:table-caption>
    <fo:table>
      <xsl:if test="count($prop-columns) != 0">
	<xsl:attribute name="table-layout">fixed</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </fo:table>
  </fo:table-and-caption>
</xsl:template>

<xsl:template match="equation">
  <xsl:call-template name="semiformal.object"/>
</xsl:template>

<xsl:template match="figure/title"></xsl:template>
<xsl:template match="table/title"></xsl:template>
<xsl:template match="example/title"></xsl:template>
<xsl:template match="equation/title"></xsl:template>

<xsl:template match="informalfigure">
  <xsl:call-template name="informal.object"/>
</xsl:template>

<xsl:template match="informalexample">
  <xsl:call-template name="informal.object"/>
</xsl:template>

<xsl:template match="informaltable">
  <xsl:variable name="prop-columns"
    select=".//colspec[contains(@colwidth, '*')]"/>

  <fo:table>
    <xsl:if test="count($prop-columns) != 0">
      <xsl:attribute name="table-layout">fixed</xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>
  </fo:table>
</xsl:template>

<xsl:template match="informalequation">
  <xsl:call-template name="informal.object"/>
</xsl:template>

</xsl:stylesheet>
