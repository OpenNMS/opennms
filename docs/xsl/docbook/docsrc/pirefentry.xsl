<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:import href="clrefentry.xsl"/>

<xsl:template match="parameter">
  <!-- link parameters to their reference pages -->
  <a href="../{@role}/{.}.html">
    <xsl:call-template name="inline.italicmonoseq"/>
  </a>
</xsl:template>


</xsl:stylesheet>