<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output indent="yes"></xsl:output>
 <xsl:param name="sep">no</xsl:param>
 <xsl:param name="anchor">no</xsl:param>
 <xsl:param name="internalanchor">no</xsl:param>
 <xsl:param name="toc">no</xsl:param>
 <xsl:template match="/">
  <html>
   <xsl:comment> This file was generated automatically by man2html.xsl </xsl:comment>
   <head>
    <style type="text/css">

     .huge           { font-size: 16pt; color: #000000 }
     .large          { font-size: 14pt; color: #000000 }
     .medium         { font-size: 12pt; color: #000000 }
     .small          { font-size: 10pt; color: #000000 }
     .chapter        { font-size: 16pt; font-weight: bold; color: #000000 }
     .title          { font-size: 14pt; font-weight: bold; color: #000000 }
     .section        { font-size: 13pt; color: #000000; margin-left: 10px }
     .sectionHead    { font-size: 13pt; font-weight: bold; color: #000000; margin-left: -10px }
     .subsection     { font-size: 12pt; color: #000000; margin-left: 20px }
     .subsectionHead { font-size: 12pt; font-weight: bold; color: #000000; margin-left: -20px }
     .background     { background-color: #dddddd; color: #000000 }

     body            { font-face: Tahoma, Verdana, Helvetica, sans-serif; font-size: 12pt; color: #000000 }
     select          { font-size: 10pt }
     input           { font-size: 10pt }

     a:active        { color: #000000 }
     a:link          { color: #000000 }
     a:visited       { color: #000000 }
     a:hover         { color: #006600; text-decoration: none }

    </style>
   </head>
   <body bgcolor="#ffffff">
    <xsl:apply-templates/>
   </body>
  </html>
 </xsl:template>
 <xsl:template match="chapter">
  <a name="{@id}"></a>
   <xsl:apply-templates/> 
 </xsl:template>
 <xsl:template match="chapter/title">
  <table cellpadding="2" cellspacing="0" border="0" width="100%" class="background">
   <tr>
    <td width="1%" align="left">
     <font class="chapter"><xsl:value-of select="../@label"/>.</font>
    </td>
    <td align="left">
     <font class="chapter"><xsl:apply-templates/></font>
    </td>
   </tr>
  </table>
 </xsl:template>
 <xsl:template match="subtitle">
 </xsl:template>
 <xsl:template match="sect1">
  <div class="section">
   <a name="{@id}"></a>
   <xsl:apply-templates/>
  </div>
 </xsl:template>
 <xsl:template match="sect1/title">
  <p><font class="sectionHead">
   <xsl:value-of select="../@label"/><xsl:text> </xsl:text><xsl:apply-templates/>
  </font></p>
 </xsl:template>
 <xsl:template match="sect2">
  <div class="subsection">
   <a name="{@id}"></a>
   <xsl:apply-templates/>
  </div>
 </xsl:template>
 <xsl:template match="sect2/title">
  <p><font class="subsectionHead">
   <xsl:value-of select="../@label"/><xsl:text> </xsl:text><xsl:apply-templates/>
  </font></p>
 </xsl:template>
 <xsl:template match="para">
  <p>
   <xsl:apply-templates/>
  </p>
 </xsl:template>
 <xsl:template match="itemizedlist">
  <ul>
   <xsl:apply-templates/>
  </ul>
 </xsl:template>
 <xsl:template match="listitem">
  <li><xsl:apply-templates/></li>
 </xsl:template>
 <xsl:template match="programlisting"><pre><xsl:value-of select="."/></pre></xsl:template>
 <xsl:template match="ulink">
  <a href="{@url}"><xsl:if test="@type = 'separate'"><xsl:attribute name="TARGET">_blank</xsl:attribute></xsl:if><xsl:value-of select="."/></a>
 </xsl:template>
 <xsl:template match="emphasis">
  <strong><xsl:apply-templates/></strong>
 </xsl:template>
</xsl:stylesheet>
