<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:include href="docbook-elements.xsl"/>

<xsl:param name="tdg-baseuri" select="'http://www.docbook.org/tdg/en/html/'"/>

<xsl:template match="sgmltag">
  <xsl:variable name="tagmarkup">
    <xsl:apply-imports/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="not(@class) or @class='element'">
      <xsl:variable name="base-element">
        <xsl:choose>
          <xsl:when test="contains(normalize-space(.), ' ')">
            <xsl:value-of select="substring-before(normalize-space(.), ' ')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="normalize-space(.)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="element"
                    select="translate($base-element,
                                      'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
                                      'abcdefghijklmnopqrstuvwxyz')"/>

      <xsl:variable name="isdocbook">
        <xsl:call-template name="is-docbook-element">
          <xsl:with-param name="element" select="$element"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="$isdocbook != 0">
          <a href="{concat($tdg-baseuri, normalize-space(.))}.html">
            <xsl:copy-of select="$tagmarkup"/>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$tagmarkup"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="$tagmarkup"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
