<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:doc="http://nwalsh.com/xsl/documentation/1.0"
		version="1.0"
                exclude-result-prefixes="doc">

<xsl:import href="../html/xtchunk.xsl"/>
<xsl:include href="javahelp-common.xsl"/>

<xsl:template match="/">
  <xsl:message terminate="yes">
    <xsl:text>JavaHelp cannot work with XT; XT does not support</xsl:text>
    <xsl:text> writing doctype public and system identifiers</xsl:text>
  </xsl:message>
</xsl:template>

</xsl:stylesheet>
