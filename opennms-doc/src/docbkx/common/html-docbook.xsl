<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>

<!--
  This file is built from info that I found here:
    http://www.dpawson.co.uk/docbook/styling/custom.html
    http://www.dpawson.co.uk/docbook/styling/param.xweb.html
    http://sources.redhat.com/ml/docbook-apps/2003-q3/msg00758.html
-->

  <xsl:import href="../xsl/docbook/html/docbook.xsl"/>

  <xsl:param name="section.autolabel">1</xsl:param>
  <xsl:param name="section.label.includes.component.label">1</xsl:param>

  <xsl:param name="html.stylesheet">html.css</xsl:param>

  <xsl:template name="output.html.stylesheets">
    <xsl:variable name="style" select="document($html.stylesheet)"/>
    <xsl:copy-of select="$style"/>
  </xsl:template>

</xsl:stylesheet>
