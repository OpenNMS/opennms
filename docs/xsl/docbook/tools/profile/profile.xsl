<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<!-- Generate DocBook instance with correct DOCTYPE -->
<xsl:output method="xml" 
            doctype-public="-//OASIS//DTD DocBook XML V4.1.2//EN"
            doctype-system="http://www.oasis-open.org/docbook/xml/4.0/docbookx.dtd"/>

<!-- Which OSes to select -->
<xsl:param name="os"/>

<!-- Which UserLevels to select -->
<xsl:param name="ul"/>

<!-- Which Archs to select -->
<xsl:param name="arch"/>

<!-- Name of attribute with profiling information -->
<xsl:param name="attr"/>

<!-- Which $attrs to select -->
<xsl:param name="val"/>

<!-- Seperator for profiling values -->
<xsl:param name="sep" select="';'"/>  

<!-- Copy all non-element nodes -->
<xsl:template match="@*|text()|comment()|processing-instruction()">
  <xsl:copy/>
</xsl:template>

<!-- Profile elements based on input parameters -->
<xsl:template match="*">
  <xsl:variable name="os.ok" select="not(@os) or not($os) or
                contains(concat($sep, @os, $sep), concat($sep, $os, $sep)) or
                @os = ''"/>
  <xsl:variable name="ul.ok" select="not(@userlevel) or not($ul) or
                contains(concat($sep, @userlevel, $sep), concat($sep, $ul, $sep)) or
                @userlevel = ''"/>
  <xsl:variable name="arch.ok" select="not(@arch) or not($arch) or
                contains(concat($sep, @arch, $sep), concat($sep, $arch, $sep)) or
                @arch = ''"/>
  <xsl:variable name="attr.ok" select="not(@*[local-name()=$attr]) or not($val) or
                contains(concat($sep, @*[local-name()=$attr], $sep), concat($sep, $val, $sep)) or
                @*[local-name()=$attr] = '' or not($attr)"/>
  <xsl:if test="$os.ok and $ul.ok and $arch.ok and $attr.ok">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>

