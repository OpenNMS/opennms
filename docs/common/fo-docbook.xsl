<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>

<!--
  This file is built from info that I found here:
    http://www.sagehill.net/docbookxsl/PrintOutput.html
    http://www.dpawson.co.uk/docbook/styling/custom.html
    http://www.dpawson.co.uk/docbook/styling/param.xweb.html
    http://sources.redhat.com/ml/docbook-apps/2003-q3/msg00758.html
-->

  <xsl:import href="../xsl/docbook/fo/docbook.xsl"/>

  <!-- This prevents a lot of font warnings -->
  <xsl:param name="symbol.font.family" select="''"/>
</xsl:stylesheet>
