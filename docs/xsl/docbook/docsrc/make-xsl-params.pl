#!/usr/bin/perl -- # -*- Perl -*- 

# Must be run from the "docssrc" directory

print <<EOF1;
<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exsl="http://exslt.org/common"
                version="1.0">


<xsl:variable name="xsl-html-parameters-list">
  <simplelist>
EOF1

open (F, "../html/param.xsl");
read (F, $_, -s "../html/param.xsl");
close (F);
foreach my $param (/<xsl:param name="[^\"]+"/gs) {
    $param =~ /name=\"(.*)\"/;
    print "    <member>$1</member>\n", 
}

print <<EOF2;
  </simplelist>
</xsl:variable>

<xsl:variable name="xsl-fo-parameters-list">
  <simplelist>
EOF2

open (F, "../fo/param.xsl");
read (F, $_, -s "../fo/param.xsl");
close (F);
foreach my $param (/<xsl:param name="[^\"]+"/gs) {
    $param =~ /name=\"(.*)\"/;
    print "    <member>$1</member>\n", 
}

print <<EOF3;
  </simplelist>
</xsl:variable>

<xsl:variable name="xsl-html-parameters"
              select="exsl:node-set(\$xsl-html-parameters-list)/simplelist"/>

<xsl:variable name="xsl-fo-parameters"
              select="exsl:node-set(\$xsl-fo-parameters-list)/simplelist"/>

<xsl:template name="is-html-parameter">
  <xsl:param name="param" select="''"/>

  <xsl:choose>
    <xsl:when test="\$xsl-html-parameters/member[. = \$param]">1</xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="is-fo-parameter">
  <xsl:param name="param" select="''"/>

  <xsl:choose>
    <xsl:when test="\$xsl-fo-parameters/member[. = \$param]">1</xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
EOF3
