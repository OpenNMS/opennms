<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:rx="http://www.renderx.com/XSL/Extensions"
                version='1.0'>

<!-- ********************************************************************
     ********************************************************************
     (c) Stephane Bline Peregrine Systems 2001
     Implementation of xep extensions:
       * Pdf bookmarks (based on the XEP 2.5 implementation)
       * Document information (XEP 2.5 meta information extensions)
     ******************************************************************** -->

<!-- ********************************************************************
     Document information
     In PDF bookmarks can't be used characters with code>255. This version of file
     translates characters with code>255 back to ASCII.

        Pavel Zampach (zampach@volny.cz)

     ********************************************************************-->

<xsl:template name="xep-document-information">
  <rx:meta-info>
    <xsl:if test="//author[1]">
      <xsl:element name="rx:meta-field">
        <xsl:attribute name="name">author</xsl:attribute>
        <xsl:attribute name="value">
          <xsl:call-template name="person.name">
            <xsl:with-param name="node" select="//author[1]"/>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:element>
    </xsl:if>

    <xsl:variable name="title">
      <xsl:apply-templates select="/*[1]" mode="label.markup"/>
      <xsl:apply-templates select="/*[1]" mode="title.markup"/>
    </xsl:variable>

    <xsl:element name="rx:meta-field">
      <xsl:attribute name="name">title</xsl:attribute>
      <xsl:attribute name="value">
        <xsl:value-of select="$title"/>
      </xsl:attribute>
    </xsl:element>
  </rx:meta-info>
</xsl:template>

<!-- ********************************************************************
     Pdf bookmarks
     ******************************************************************** -->
<xsl:template match="set" mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.content"/>
    <xsl:apply-templates select="." mode="title.content"/>
  </xsl:variable>
  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>

  <xsl:if test="book">
      <xsl:apply-templates select="book"
                           mode="xep.outline"/>
  </xsl:if>
  </rx:bookmark>
</xsl:template>

<xsl:template match="book" mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>

    <xsl:if test="part|preface|chapter|appendix">
      <xsl:apply-templates select="part|preface|chapter|appendix"
                           mode="xep.outline"/>
    </xsl:if>
  </rx:bookmark>
</xsl:template>


<xsl:template match="part" mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>

  <xsl:if test="chapter|appendix|preface|reference">
      <xsl:apply-templates select="chapter|appendix|preface|reference"
                           mode="xep.outline"/>
  </xsl:if>
  </rx:bookmark>
</xsl:template>

<xsl:template match="preface|chapter|appendix"
              mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>

  <xsl:if test="section|sect1">
      <xsl:apply-templates select="section|sect1"
                           mode="xep.outline"/>
  </xsl:if>
  </rx:bookmark>
</xsl:template>

<xsl:template match="section|sect1|sect2|sect3|sect4|sect5"
              mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>

  <xsl:if test="section|sect2|sect3|sect4|sect5">
      <xsl:apply-templates select="section|sect2|sect3|sect4|sect5"
                           mode="xep.outline"/>
  </xsl:if>
  </rx:bookmark>
</xsl:template>

<xsl:template match="bibliography|glossary|index"
              mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>
  </rx:bookmark>
</xsl:template>
<!-- Added missing template for "article" -->
<xsl:template match="article"
              mode="xep.outline">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <xsl:variable name="bookmark-label">
    <xsl:apply-templates select="." mode="label.markup"/>
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <rx:bookmark internal-destination="{$id}">
    <rx:bookmark-label>
      <xsl:value-of select="translate($bookmark-label, $a-dia, $a-asc)"/>
    </rx:bookmark-label>

  <xsl:if test="section|sect1|appendix|bibliography|glossary|index">
      <xsl:apply-templates select="section|sect1|appendix|bibliography|glossary|index"
                           mode="xep.outline"/>
  </xsl:if>
  </rx:bookmark>
</xsl:template>



<xsl:template match="title" mode="xep.outline">
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
