<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ptbl="http://nwalsh.com/xslt/ext/xsltproc/python/Table"
                exclude-result-prefixes="ptbl"
                version='1.0'>

<xsl:output method="html"/>

<xsl:param name="stylesheet.result.type" select="'html'"/>
<xsl:param name="use.extensions" select="1"/>
<xsl:param name="tablecolumns.extension" select="1"/>

<xsl:template match="tgroup">
  <table>
    <xsl:variable name="colgroup">
      <colgroup>
        <xsl:call-template name="generate.colgroup">
          <xsl:with-param name="cols" select="@cols"/>
        </xsl:call-template>
      </colgroup>
    </xsl:variable>

    <xsl:if test="function-available('ptbl:adjustColumnWidths')">
      <xsl:copy-of select="ptbl:adjustColumnWidths($colgroup)"/>
    </xsl:if>

    <xsl:apply-templates select="tbody"/>
  </table>
</xsl:template>

<xsl:template name="generate.colgroup">
  <xsl:param name="cols" select="1"/>
  <xsl:param name="count" select="1"/>
  <xsl:choose>
    <xsl:when test="$count &gt; $cols"></xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="generate.col">
        <xsl:with-param name="countcol" select="$count"/>
      </xsl:call-template>
      <xsl:call-template name="generate.colgroup">
        <xsl:with-param name="cols" select="$cols"/>
        <xsl:with-param name="count" select="$count+1"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="generate.col">
  <xsl:param name="countcol">1</xsl:param>
  <xsl:param name="colspecs" select="./colspec"/>
  <xsl:param name="count">1</xsl:param>
  <xsl:param name="colnum">1</xsl:param>

<!--
  <xsl:message>generate.col: <xsl:value-of select="$count"/></xsl:message>
-->

  <xsl:choose>
    <xsl:when test="$count>count($colspecs)">
      <col/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="colspec" select="$colspecs[$count=position()]"/>
      <xsl:variable name="colspec.colnum">
        <xsl:choose>
          <xsl:when test="$colspec/@colnum">
            <xsl:value-of select="$colspec/@colnum"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$colnum"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="$colspec.colnum=$countcol">
          <col>
            <xsl:if test="$colspec/@colwidth
                          and $use.extensions != 0
                          and $tablecolumns.extension != 0">
              <xsl:attribute name="width">
                <xsl:value-of select="$colspec/@colwidth"/>
              </xsl:attribute>
            </xsl:if>

            <xsl:choose>
              <xsl:when test="$colspec/@align">
                <xsl:attribute name="align">
                  <xsl:value-of select="$colspec/@align"/>
                </xsl:attribute>
              </xsl:when>
              <!-- Suggested by Pavel ZAMPACH <zampach@nemcb.cz> -->
              <xsl:when test="$colspecs/ancestor::tgroup/@align">
                <xsl:attribute name="align">
                  <xsl:value-of select="$colspecs/ancestor::tgroup/@align"/>
                </xsl:attribute>
              </xsl:when>
            </xsl:choose>

            <xsl:if test="$colspec/@char">
              <xsl:attribute name="char">
                <xsl:value-of select="$colspec/@char"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:if test="$colspec/@charoff">
              <xsl:attribute name="charoff">
                <xsl:value-of select="$colspec/@charoff"/>
              </xsl:attribute>
            </xsl:if>
          </col>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="generate.col">
            <xsl:with-param name="countcol" select="$countcol"/>
            <xsl:with-param name="colspecs" select="$colspecs"/>
            <xsl:with-param name="count" select="$count+1"/>
            <xsl:with-param name="colnum">
              <xsl:choose>
                <xsl:when test="$colspec/@colnum">
                  <xsl:value-of select="$colspec/@colnum + 1"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$colnum + 1"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:with-param>
           </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="table">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="title">
  <!-- nop -->
</xsl:template>

<xsl:template match="colspec">
  <!-- nop -->
</xsl:template>

<xsl:template match="tbody">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="row">
  <tr>
    <xsl:apply-templates/>
  </tr>
</xsl:template>

<xsl:template match="entry">
  <td>
    <xsl:apply-templates/>
  </td>
</xsl:template>

</xsl:stylesheet>
