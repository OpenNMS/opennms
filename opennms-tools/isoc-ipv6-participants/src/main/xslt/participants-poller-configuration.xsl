<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://xmlns.opennms.org/xsd/config/model-import" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:java="http://xml.apache.org/xalan/java" version="1.0"> 

  <xsl:output method="xml"/> 

  <xsl:param name="rrdDirectory"/>

  <xsl:template match="/">
	<poller-configuration threads="30" serviceUnresponsiveEnabled="false">
	  <package name="example1">
		<filter>IPADDR != '0.0.0.0'</filter>
		<include-range begin="1.1.1.1" end="254.254.254.254" />
		<include-range begin="::1" end="ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff" />
		<rrd step="300">
		  <rra>RRA:AVERAGE:0.5:1:2016</rra>
		  <rra>RRA:AVERAGE:0.5:12:1488</rra>
		  <rra>RRA:AVERAGE:0.5:288:366</rra>
		  <rra>RRA:MAX:0.5:288:366</rra>
		  <rra>RRA:MIN:0.5:288:366</rra>
		</rrd>
		<xsl:apply-templates/>
	  </package>
	</poller-configuration>
  </xsl:template>

  <!--
  <xsl:template match="/participants">
	<xsl:if test="participant"> 
        <xsl:apply-templates/>
	</xsl:if>
  </xsl:template> 
  -->

  <xsl:template match="/participants/participant">
    <!-- 
      I tried all permutations of creating a primitive boolean with Xalan... 
      this seems to be the only one that works.
    -->
    <xsl:variable name="javaTrue" select="java:java.lang.Boolean.parseBoolean('true')"/>
    <xsl:variable name="javaFalse" select="java:java.lang.Boolean.parseBoolean('false')"/>
	<xsl:if test="v4_only != ''">
	  <service interval="300000" user-defined="false" status="on">
		<xsl:attribute name="name">DNS_<xsl:value-of select="translate(string(v4_only), '.:', '__')"/>_IPv4</xsl:attribute>
		<parameter key="retry" value="2" />
		<parameter key="timeout" value="5000" />
		<parameter key="port" value="53" />
		<parameter key="lookup">
		  <xsl:attribute name="value"><xsl:value-of select="v4_only"/></xsl:attribute>
		</parameter>
		<parameter key="fatal-response-codes" value="2,3,5" /><!-- ServFail, NXDomain, Refused -->
		<parameter key="rrd-repository" value="{$rrdDirectory}/rrd/response" />
		<parameter key="rrd-base-name" value="dns" />
		<parameter key="ds-name">
		  <xsl:attribute name="value"><xsl:value-of select="translate(substring(string(v4_only), 1, 19), '.:', '__')"/></xsl:attribute>
		</parameter>
	  </service>
	</xsl:if>
	<xsl:if test="v6_only != ''">
	  <service interval="300000" user-defined="false" status="on">
		<xsl:attribute name="name">DNS_<xsl:value-of select="translate(string(v6_only), '.:', '__')"/>_IPv6</xsl:attribute>
		<parameter key="retry" value="2" />
		<parameter key="timeout" value="5000" />
		<parameter key="port" value="53" />
		<parameter key="lookup">
		  <xsl:attribute name="value"><xsl:value-of select="v6_only"/></xsl:attribute>
		</parameter>
		<parameter key="fatal-response-codes" value="2,3,5" /><!-- ServFail, NXDomain, Refused -->
		<parameter key="rrd-repository" value="{$rrdDirectory}/rrd/response" />
		<parameter key="rrd-base-name" value="dns" />
		<parameter key="ds-name">
		  <xsl:attribute name="value"><xsl:value-of select="translate(substring(string(v6_only), 1, 19), '.:', '__')"/></xsl:attribute>
		</parameter>
	  </service>
	</xsl:if>
	<xsl:if test="dual_hostname != ''">
	  <service interval="300000" user-defined="false" status="on">
		<xsl:attribute name="name">DNS_<xsl:value-of select="translate(string(dual_hostname), '.:', '__')"/>_Dual</xsl:attribute>
		<parameter key="retry" value="2" />
		<parameter key="timeout" value="5000" />
		<parameter key="port" value="53" />
		<parameter key="lookup">
		  <xsl:attribute name="value"><xsl:value-of select="dual_hostname"/></xsl:attribute>
		</parameter>
		<parameter key="fatal-response-codes" value="2,3,5" /><!-- ServFail, NXDomain, Refused -->
		<parameter key="rrd-repository" value="{$rrdDirectory}/rrd/response" />
		<parameter key="rrd-base-name" value="dns" />
		<parameter key="ds-name">
		  <xsl:attribute name="value"><xsl:value-of select="translate(substring(string(dual_hostname), 1, 19), '.:', '__')"/></xsl:attribute>
		</parameter>
	  </service>
	</xsl:if>
	<!--
	<xsl:value-of select="join_date"/>
	<xsl:value-of select="name"/>
	<xsl:value-of select="hostname"/>
	<xsl:value-of select="v6_only"/>
	<xsl:value-of select="v4_only"/>
	<xsl:value-of select="dual_hostname"/>
	<xsl:value-of select="v4_fetchable-url/@small"/>
	<xsl:value-of select="v4_fetchable-url/@notsmall"/>
	<xsl:value-of select="dual_fetchable-url/@small"/>
	<xsl:value-of select="dual_fetchable-url/@notsmall"/>
	<xsl:value-of select="alerts_to"/>
	</tr>
	-->
  </xsl:template>

  <!-- Black hole any participants that do not have v4, v6, or dual-stack hostnames defined -->
  <xsl:template match="/participants/participant[v4_only = '' and v6_only = '' and dual_hostname = '']"/>

  <!-- Identity transform -->
  <xsl:template match="node()|@*">
	<xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>
