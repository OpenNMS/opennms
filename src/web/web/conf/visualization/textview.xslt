<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:myns="http://xmlns.opennms.org/xsd/config/inventory/parser">
	<xsl:output method="xml" encoding="iso-8859-1" indent="yes"/>

	<xsl:template match="myns:inventory">
		<table><tr><td><pre><xsl:value-of select="@name"/>
			</pre></td></tr>
			<xsl:apply-templates select="myns:item"/>
		</table>
	</xsl:template>



	<xsl:template match="myns:item">

		<xsl:variable name="pad" select="(count(ancestor::*))+10"/>
		<tr style='margin: 0px 0px 0px 0px;padding: 0px 0px 0px 0px'>
		  <td style='margin: 0px 0px 0px 0px;padding: 0px 0px 0px {$pad}px'>
			<pre style='margin: 0px 0px 0px 0px;padding: 0px 0px 0px 0px'>
				<xsl:value-of select="@name"/>
				<xsl:if test="count(myns:item)>0">
					<xsl:apply-templates  select="myns:item"/>
				</xsl:if>
				<xsl:if test="count(myns:item)=0">
					<xsl:if test="myns:dataitem!='' " >
						<div  style='margin: 0px 0px 0px 0px;padding: 0px 0px 0px {$pad+10}px'><xsl:value-of select="myns:dataitem"/>&#160;</div>
					</xsl:if>
				</xsl:if>
			</pre>
		  </td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
