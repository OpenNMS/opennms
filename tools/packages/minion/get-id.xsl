<xsl:stylesheet version="1.0"
 xmlns:pom="http://maven.apache.org/POM/4.0.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/pom:project">
        <xsl:apply-templates select="pom:groupId|pom:parent/pom:groupId" mode="copy-coordinate"/>
        <xsl:text>:</xsl:text>
        <xsl:apply-templates select="pom:artifactId|pom:parent/pom:artifactId" mode="copy-coordinate"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="*" mode="copy-coordinate">
        <xsl:if test="not(../../*[name(.)=name(current())])">
            <xsl:value-of select="."/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
