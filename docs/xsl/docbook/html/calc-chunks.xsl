<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:doc="http://nwalsh.com/xsl/documentation/1.0"
		version="1.0"
                exclude-result-prefixes="doc">

<xsl:import href="docbook.xsl"/>
<xsl:include href="chunker.xsl"/>

<xsl:output method="xml" indent="yes"/>

<xsl:param name="html.ext" select="'.html'"/>
<doc:param name="html.ext" xmlns="">
<refpurpose>Extension for chunked files</refpurpose>
<refdescription>
<para>The extension identified by <parameter>html.ext</parameter> will
be used as the filename extension for chunks created by this stylesheet.
</para>
</refdescription>
</doc:param>

<xsl:param name="root.filename" select="'index'"/>
<doc:param name="root.filename" xmlns="">
<refpurpose>Filename for the root chunk</refpurpose>
<refdescription>
<para>The <parameter>root.filename</parameter> is the base filename for
the chunk created for the root of each document processed.
</para>
</refdescription>
</doc:param>

<xsl:param name="base.dir" select="''"/>
<doc:param name="base.dir" xmlns="">
<refpurpose>Output directory for chunks</refpurpose>
<refdescription>
<para>If specified, the <literal>base.dir</literal> identifies
the output directory for chunks. (If not specified, the output directory
is system dependent.)</para>
</refdescription>
</doc:param>

<xsl:param name="chunk.sections" select="'1'"/>
<doc:param name="chunk.sections" xmlns="">
<refpurpose>Create chunks for top-level sections in components?</refpurpose>
<refdescription>
<para>If non-zero, chunks will be created for top-level
<sgmltag>sect1</sgmltag> and <sgmltag>section</sgmltag> elements in
each component.
</para>
</refdescription>
</doc:param>

<xsl:param name="chunk.first.sections" select="'0'"/>
<doc:param name="chunk.first.sections" xmlns="">
<refpurpose>Create a chunk for the first top-level section in each component?</refpurpose>
<refdescription>
<para>If non-zero, a chunk will be created for the first top-level
<sgmltag>sect1</sgmltag> or <sgmltag>section</sgmltag> elements in
each component. Otherwise, that section will be part of the chunk for
its parent.
</para>
</refdescription>
</doc:param>

<xsl:param name="chunk.datafile" select="'.chunks'"/>
<doc:param name="chunk.datafile" xmlns="">
<refpurpose>Name of the temporary file used to hold chunking data</refpurpose>
<refdescription>
<para>Chunking is now a two-step process. The
<parameter>chunk.datafile</parameter> is the name of the file used to
hold the chunking data.
</para>
</refdescription>
</doc:param>

<!-- ==================================================================== -->
<!-- What's a chunk?

     appendix
     article
     bibliography  in article or book
     book
     chapter
     colophon
     glossary      in article or book
     index         in article or book
     part
     preface
     refentry
     reference
     sect1         if position()>1
     section       if position()>1 && parent != section
     set
     setindex
                                                                          -->
<!-- ==================================================================== -->

<xsl:template name="chunk.info">
  <xsl:param name="node" select="."/>
  <xsl:variable name="id">
    <xsl:choose>
      <xsl:when test="$node/@id">
        <xsl:value-of select="$node/@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          <xsl:text>Chunk for </xsl:text>
          <xsl:value-of select="local-name($node)"/>
          <xsl:text> has no id</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <chunk name="{local-name($node)}" id="{$id}">
    <xsl:attribute name="filename">
      <xsl:apply-templates select="." mode="chunk-filename-calc"/>
    </xsl:attribute>
  </chunk>
</xsl:template>

<xsl:template match="set" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="book" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="setindex" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="book" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="book/appendix" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="book/glossary" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="book/bibliography" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="book/index" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="preface|chapter" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="part|reference" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="refentry" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="colophon" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="article" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="article/appendix" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="article/glossary" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="article/bibliography" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="article/index" mode="calculate.chunks">
  <xsl:call-template name="chunk.info"/>
</xsl:template>

<xsl:template match="sect1
                     |/section
                     |section[local-name(parent::*) != 'section']"
              mode="calculate.chunks">
  <xsl:choose>
    <xsl:when test=". = /section">
      <xsl:call-template name="chunk.info"/>
    </xsl:when>
    <xsl:when test="$chunk.sections = 0">
      <!-- nop -->
    </xsl:when>
    <xsl:when test="ancestor::partintro">
      <!-- nop -->
    </xsl:when>
    <xsl:when test="$chunk.first.sections = 0">
      <xsl:if test="count(preceding-sibling::section) &gt; 0
                    or count(preceding-sibling::sect1) &gt; 0">
        <xsl:call-template name="chunk.info"/>
      </xsl:if>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="chunk.info"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="*" mode="calculate.chunks">
  <xsl:apply-templates select="*" mode="calculate.chunks"/>
</xsl:template>

<xsl:template match="text()" mode="calculate.chunks">
  <!-- nop -->
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="*" mode="chunk-filename-calc">
  <xsl:param name="recursive" select="false()"/>
  <!-- returns the filename of a chunk -->

  <xsl:variable name="dbhtml-filename">
    <xsl:call-template name="dbhtml-filename"/>
  </xsl:variable>

  <xsl:variable name="filename">
    <xsl:choose>
      <xsl:when test="$dbhtml-filename != ''">
        <xsl:value-of select="$dbhtml-filename"/>
      </xsl:when>
      <!-- if there's no dbhtml filename, and if we're to use IDs as -->
      <!-- filenames, then use the ID to generate the filename. -->
      <xsl:when test="@id and $use.id.as.filename != 0">
        <xsl:value-of select="@id"/>
        <xsl:value-of select="$html.ext"/>
      </xsl:when>
      <!-- if this is the root element, use the root.filename -->
      <xsl:when test="not(parent::*)">
        <xsl:value-of select="$root.filename"/>
        <xsl:value-of select="$html.ext"/>
      </xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="dir">
    <xsl:call-template name="dbhtml-dir"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="not($recursive) and $filename != ''">
      <!-- if this chunk has an explicit name, use it -->
      <xsl:if test="$dir != ''">
        <xsl:value-of select="$dir"/>
        <xsl:text>/</xsl:text>
      </xsl:if>
      <xsl:value-of select="$filename"/>
    </xsl:when>

    <xsl:when test="name(.)='set'">
      <xsl:value-of select="$root.filename"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='book'">
      <xsl:text>bk</xsl:text>
      <xsl:number level="any" format="01"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='article'">
      <xsl:if test="/set">
        <!-- in a set, make sure we inherit the right book info... -->
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>ar</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='preface'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>pr</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='chapter'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>ch</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='appendix'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>ap</xsl:text>
      <xsl:number level="any" format="a" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='part'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>pt</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='reference'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>rn</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='refentry'">
      <xsl:if test="parent::reference">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>re</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='colophon'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>co</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='sect1' or name(.)='section'">
      <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
        <xsl:with-param name="recursive" select="true()"/>
      </xsl:apply-templates>
      <xsl:text>s</xsl:text>
      <xsl:number level="any" format="01" from="preface|chapter|appendix"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='bibliography'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>bi</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='glossary'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>go</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='index'">
      <xsl:if test="/set">
        <xsl:apply-templates mode="chunk-filename-calc" select="parent::*">
          <xsl:with-param name="recursive" select="true()"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:text>ix</xsl:text>
      <xsl:number level="any" format="01" from="book"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:when test="name(.)='setindex'">
      <xsl:text>si</xsl:text>
      <xsl:number level="any" format="01" from="set"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:when>

    <xsl:otherwise>
      <xsl:text>chunk-filename-calc-error-</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:number level="any" format="01" from="set"/>
      <xsl:if test="not($recursive)">
        <xsl:value-of select="$html.ext"/>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="/" priority="-1">
  <xsl:call-template name="write.chunk">
    <xsl:with-param name="filename" select="$chunk.datafile"/>
    <xsl:with-param name="method" select="'xml'"/>
    <xsl:with-param name="encoding" select="'utf-8'"/>
    <xsl:with-param name="indent" select="'yes'"/>
    <xsl:with-param name="content">
      <!-- HACK! -->
      <xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE chunks [
&lt;!ELEMENT chunks (chunk+)&gt;
&lt;!ELEMENT chunk EMPTY&gt;
&lt;!ATTLIST chunk
        id       ID    #REQUIRED
        name     CDATA #REQUIRED
&gt;
]&gt;
      </xsl:text>
      <chunks>
        <xsl:apply-templates mode="calculate.chunks"/>
      </chunks>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
