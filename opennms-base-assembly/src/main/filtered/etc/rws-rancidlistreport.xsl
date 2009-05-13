<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
  <h2>Rancid List</h2>
    <tr>
      <td>Total groups: </td>
      <td><xsl:value-of select="rws-rancidlistreport/totalGroups"/></td>
    </tr>
  <xsl:for-each select="rws-rancidlistreport/groupXSet">
  <table border="1">
    <tr>
      <th>Group Name</th>
      <th>Device Name</th>
      <th>Status</th>
      <th>Version</th>
      <th>Configuration</th>
    </tr>
    <tr>
    </tr>
        <xsl:for-each select="nodeSet">
        <tr>
          <td><xsl:value-of select="groupname"/></td>
          <td><xsl:value-of select="devicename"/></td>
          <td><xsl:value-of select="status"/></td>
          <td><xsl:value-of select="version"/></td>
          <td><xsl:value-of select="configurationurl"/></td>
        </tr>
        </xsl:for-each>
  </table>
      <td>Total nodes: </td>
      <td><xsl:value-of select="totalNodes"/></td>
    </xsl:for-each>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>
