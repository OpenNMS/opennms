<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:doc="http://nwalsh.com/xsl/documentation/1.0"
		version="1.0"
                exclude-result-prefixes="doc">

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

<xsl:template name="chunk">
  <xsl:param name="node" select="."/>
  <!-- returns 1 if $node is a chunk -->

  <xsl:variable name="id">
    <xsl:choose>
      <xsl:when test="$node/@id">
        <xsl:value-of select="$node/@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="generate-id($node)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="chunks" select="document($chunk.datafile,.)"/>

  <xsl:choose>
    <xsl:when test="$chunks/chunks/chunk[@id=$id]">1</xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="*" mode="chunk-filename">
  <!-- returns the filename of a chunk -->

  <xsl:variable name="id">
    <xsl:choose>
      <xsl:when test="@id">
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="generate-id(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="chunks" select="document($chunk.datafile,.)"/>
  <xsl:variable name="chunk" select="$chunks/chunks/chunk[@id=$id]"/>

  <xsl:choose>
    <xsl:when test="$chunk">
      <xsl:value-of select="$chunk/@filename"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="parent::*" mode="chunk-filename"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="href.target">
  <xsl:param name="object" select="."/>
  <xsl:variable name="ischunk">
    <xsl:call-template name="chunk">
      <xsl:with-param name="node" select="$object"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:apply-templates mode="chunk-filename" select="$object"/>

  <xsl:if test="$ischunk='0'">
    <xsl:text>#</xsl:text>
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="$object"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="html.head">
  <xsl:param name="prev" select="/foo"/>
  <xsl:param name="next" select="/foo"/>
  <xsl:variable name="home" select="/*[1]"/>
  <xsl:variable name="up" select="parent::*"/>

  <head>
    <xsl:call-template name="head.content"/>
    <xsl:call-template name="user.head.content"/>

    <xsl:if test="$home">
      <link rel="home">
        <xsl:attribute name="href">
          <xsl:call-template name="href.target">
            <xsl:with-param name="object" select="$home"/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:apply-templates select="$home"
                               mode="object.title.markup.textonly"/>
        </xsl:attribute>
      </link>
    </xsl:if>

    <xsl:if test="$up">
      <link rel="up">
        <xsl:attribute name="href">
          <xsl:call-template name="href.target">
            <xsl:with-param name="object" select="$up"/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:apply-templates select="$up" mode="object.title.markup.textonly"/>
        </xsl:attribute>
      </link>
    </xsl:if>

    <xsl:if test="$prev">
      <link rel="previous">
        <xsl:attribute name="href">
          <xsl:call-template name="href.target">
            <xsl:with-param name="object" select="$prev"/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:apply-templates select="$prev" mode="object.title.markup.textonly"/>
        </xsl:attribute>
      </link>
    </xsl:if>

    <xsl:if test="$next">
      <link rel="next">
        <xsl:attribute name="href">
          <xsl:call-template name="href.target">
            <xsl:with-param name="object" select="$next"/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:apply-templates select="$next" mode="object.title.markup.textonly"/>
        </xsl:attribute>
      </link>
    </xsl:if>
  </head>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="header.navigation">
  <xsl:param name="prev" select="/foo"/>
  <xsl:param name="next" select="/foo"/>
  <xsl:variable name="home" select="/*[1]"/>
  <xsl:variable name="up" select="parent::*"/>

  <xsl:if test="$suppress.navigation = '0'">
    <div class="navheader">
      <table width="100%" summary="Navigation header">
        <tr>
          <th colspan="3" align="center">
            <xsl:apply-templates select="." mode="object.title.markup"/>
          </th>
        </tr>
        <tr>
          <td width="20%" align="left">
            <xsl:if test="count($prev)>0">
              <a accesskey="p">
                <xsl:attribute name="href">
                  <xsl:call-template name="href.target">
                    <xsl:with-param name="object" select="$prev"/>
                  </xsl:call-template>
                </xsl:attribute>
                <xsl:call-template name="gentext">
                  <xsl:with-param name="key">nav-prev</xsl:with-param>
                </xsl:call-template>
              </a>
            </xsl:if>
            <xsl:text>&#160;</xsl:text>
          </td>
          <th width="60%" align="center">
            <xsl:choose>
              <xsl:when test="count($up) > 0 and $up != $home">
                <xsl:apply-templates select="$up" mode="object.title.markup"/>
              </xsl:when>
              <xsl:otherwise>&#160;</xsl:otherwise>
            </xsl:choose>
          </th>
          <td width="20%" align="right">
            <xsl:text>&#160;</xsl:text>
            <xsl:if test="count($next)>0">
              <a accesskey="n">
                <xsl:attribute name="href">
                  <xsl:call-template name="href.target">
                    <xsl:with-param name="object" select="$next"/>
                  </xsl:call-template>
                </xsl:attribute>
                <xsl:call-template name="gentext">
                  <xsl:with-param name="key">nav-next</xsl:with-param>
                </xsl:call-template>
              </a>
            </xsl:if>
          </td>
        </tr>
      </table>
      <hr/>
    </div>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="footer.navigation">
  <xsl:param name="prev" select="/foo"/>
  <xsl:param name="next" select="/foo"/>
  <xsl:variable name="home" select="/*[1]"/>
  <xsl:variable name="up" select="parent::*"/>

  <xsl:if test="$suppress.navigation = '0'">
    <div class="navfooter">
      <hr/>
      <table width="100%" summary="Navigation footer">
        <tr>
          <td width="40%" align="left">
            <xsl:if test="count($prev)>0">
              <a accesskey="p">
                <xsl:attribute name="href">
                  <xsl:call-template name="href.target">
                    <xsl:with-param name="object" select="$prev"/>
                  </xsl:call-template>
                </xsl:attribute>
                <xsl:call-template name="gentext">
                  <xsl:with-param name="key">nav-prev</xsl:with-param>
                </xsl:call-template>
              </a>
            </xsl:if>
            <xsl:text>&#160;</xsl:text>
          </td>
          <td width="20%" align="center">
            <xsl:choose>
              <xsl:when test="$home != .">
                <a accesskey="h">
                  <xsl:attribute name="href">
                    <xsl:call-template name="href.target">
                      <xsl:with-param name="object" select="$home"/>
                    </xsl:call-template>
                  </xsl:attribute>
                  <xsl:call-template name="gentext">
                    <xsl:with-param name="key">nav-home</xsl:with-param>
                  </xsl:call-template>
                </a>
              </xsl:when>
              <xsl:otherwise>&#160;</xsl:otherwise>
            </xsl:choose>
          </td>
          <td width="40%" align="right">
            <xsl:text>&#160;</xsl:text>
            <xsl:if test="count($next)>0">
              <a accesskey="n">
                <xsl:attribute name="href">
                  <xsl:call-template name="href.target">
                    <xsl:with-param name="object" select="$next"/>
                  </xsl:call-template>
                </xsl:attribute>
                <xsl:call-template name="gentext">
                  <xsl:with-param name="key">nav-next</xsl:with-param>
                </xsl:call-template>
              </a>
            </xsl:if>
          </td>
        </tr>

        <tr>
          <td width="40%" align="left">
            <xsl:apply-templates select="$prev" mode="object.title.markup"/>
            <xsl:text>&#160;</xsl:text>
          </td>
          <td width="20%" align="center">
            <xsl:choose>
              <xsl:when test="count($up)>0">
                <a accesskey="u">
                  <xsl:attribute name="href">
                    <xsl:call-template name="href.target">
                      <xsl:with-param name="object" select="$up"/>
                    </xsl:call-template>
                  </xsl:attribute>
                  <xsl:call-template name="gentext">
                    <xsl:with-param name="key">nav-up</xsl:with-param>
                  </xsl:call-template>
                </a>
              </xsl:when>
              <xsl:otherwise>&#160;</xsl:otherwise>
            </xsl:choose>
          </td>
          <td width="40%" align="right">
            <xsl:text>&#160;</xsl:text>
            <xsl:apply-templates select="$next" mode="object.title.markup"/>
          </td>
        </tr>
      </table>
    </div>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="processing-instruction('dbhtml')">
  <!-- nop -->
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="process-chunk">
  <xsl:variable name="id">
    <xsl:choose>
      <xsl:when test="@id">
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="generate-id(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="chunks" select="document($chunk.datafile,.)"/>

  <xsl:variable name="chunk" select="$chunks/chunks/chunk[@id=$id]"/>
  <xsl:variable name="prev-id"
                select="$chunk/preceding-sibling::chunk[1]/@id"/>
  <xsl:variable name="next-id"
                select="$chunk/following-sibling::chunk[1]/@id"/>

  <xsl:variable name="prev" select="id($prev-id)"/>
  <xsl:variable name="next" select="id($next-id)"/>

  <xsl:variable name="ischunk">
    <xsl:call-template name="chunk"/>
  </xsl:variable>

  <xsl:variable name="chunkfn">
    <xsl:if test="$ischunk='1'">
      <xsl:apply-templates mode="chunk-filename" select="."/>
    </xsl:if>
  </xsl:variable>

  <xsl:if test="$ischunk='0'">
    <xsl:message>
      <xsl:text>Error </xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:text> is not a chunk!</xsl:text>
    </xsl:message>
  </xsl:if>

  <xsl:variable name="filename">
    <xsl:call-template name="make-relative-filename">
      <xsl:with-param name="base.dir" select="$base.dir"/>
      <xsl:with-param name="base.name" select="$chunkfn"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:call-template name="write.chunk">
    <xsl:with-param name="filename" select="$filename"/>
    <xsl:with-param name="content">
      <xsl:call-template name="chunk-element-content">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
      </xsl:call-template>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="chunk-element-content">
  <xsl:param name="prev"></xsl:param>
  <xsl:param name="next"></xsl:param>

  <html>
    <xsl:call-template name="html.head">
      <xsl:with-param name="prev" select="$prev"/>
      <xsl:with-param name="next" select="$next"/>
    </xsl:call-template>

    <body>
      <xsl:call-template name="body.attributes"/>
      <xsl:call-template name="user.header.navigation"/>

      <xsl:call-template name="header.navigation">
	<xsl:with-param name="prev" select="$prev"/>
	<xsl:with-param name="next" select="$next"/>
      </xsl:call-template>

      <xsl:call-template name="user.header.content"/>

      <xsl:apply-imports/>

      <xsl:call-template name="user.footer.content"/>

      <xsl:call-template name="footer.navigation">
	<xsl:with-param name="prev" select="$prev"/>
	<xsl:with-param name="next" select="$next"/>
      </xsl:call-template>

      <xsl:call-template name="user.footer.navigation"/>
    </body>
  </html>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="set">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="book">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="book/appendix">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="book/glossary">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="book/bibliography">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="preface|chapter">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="part|reference">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="refentry">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="colophon">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="article">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="article/appendix">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="article/glossary">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="article/bibliography">
  <xsl:call-template name="process-chunk"/>
</xsl:template>

<xsl:template match="sect1
                     |/section
                     |section[local-name(parent::*) != 'section']">
  <xsl:choose>
    <xsl:when test=". = /section">
      <xsl:call-template name="process-chunk"/>
    </xsl:when>
    <xsl:when test="$chunk.sections = 0">
      <xsl:apply-imports/>
    </xsl:when>
    <xsl:when test="ancestor::partintro">
      <xsl:apply-imports/>
    </xsl:when>
    <xsl:when test="$chunk.first.sections = 0">
      <xsl:choose>
        <xsl:when test="count(preceding-sibling::section) &gt; 0
                        or count(preceding-sibling::sect1) &gt; 0">
          <xsl:call-template name="process-chunk"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-imports/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="process-chunk"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="setindex
                     |book/index
                     |article/index">
  <!-- some implementations use completely empty index tags to indicate -->
  <!-- where an automatically generated index should be inserted. so -->
  <!-- if the index is completely empty, skip it. -->
  <xsl:if test="count(*)>0 or $generate.index != '0'">
    <xsl:call-template name="process-chunk"/>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="/">
  <xsl:choose>
    <xsl:when test="$rootid != ''">
      <xsl:choose>
        <xsl:when test="count(id($rootid)) = 0">
          <xsl:message terminate="yes">
            <xsl:text>ID '</xsl:text>
            <xsl:value-of select="$rootid"/>
            <xsl:text>' not found in document.</xsl:text>
          </xsl:message>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="id($rootid)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="/" mode="process.root"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="process.root">
  <xsl:apply-templates select="."/>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="in.other.chunk">
  <xsl:param name="chunk" select="."/>
  <xsl:param name="node" select="."/>

  <xsl:variable name="is.chunk">
    <xsl:call-template name="chunk">
      <xsl:with-param name="node" select="$node"/>
    </xsl:call-template>
  </xsl:variable>

<!--
  <xsl:message>
    <xsl:text>in.other.chunk: </xsl:text>
    <xsl:value-of select="name($chunk)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="name($node)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$chunk = $node"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$is.chunk"/>
  </xsl:message>
-->

  <xsl:choose>
    <xsl:when test="$chunk = $node">0</xsl:when>
    <xsl:when test="$is.chunk = 1">1</xsl:when>
    <xsl:when test="count($node) = 0">0</xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="in.other.chunk">
        <xsl:with-param name="chunk" select="$chunk"/>
        <xsl:with-param name="node" select="$node/parent::*"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="count.footnotes.in.this.chunk">
  <xsl:param name="node" select="."/>
  <xsl:param name="footnotes" select="$node//footnote"/>
  <xsl:param name="count" select="0"/>

<!--
  <xsl:message>
    <xsl:text>count.footnotes.in.this.chunk: </xsl:text>
    <xsl:value-of select="name($node)"/>
  </xsl:message>
-->

  <xsl:variable name="in.other.chunk">
    <xsl:call-template name="in.other.chunk">
      <xsl:with-param name="chunk" select="$node"/>
      <xsl:with-param name="node" select="$footnotes[1]"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="count($footnotes) = 0">
      <xsl:value-of select="$count"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$in.other.chunk != 0">
          <xsl:call-template name="count.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
            <xsl:with-param name="count" select="$count"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$footnotes[1]/ancestor::table
                        |$footnotes[1]/ancestor::informaltable">
          <xsl:call-template name="count.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
            <xsl:with-param name="count" select="$count"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="count.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
            <xsl:with-param name="count" select="$count + 1"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="process.footnotes.in.this.chunk">
  <xsl:param name="node" select="."/>
  <xsl:param name="footnotes" select="$node//footnote"/>

<!--
  <xsl:message>process.footnotes.in.this.chunk</xsl:message>
-->

  <xsl:variable name="in.other.chunk">
    <xsl:call-template name="in.other.chunk">
      <xsl:with-param name="chunk" select="$node"/>
      <xsl:with-param name="node" select="$footnotes[1]"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="count($footnotes) = 0">
      <!-- nop -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$in.other.chunk != 0">
          <xsl:call-template name="process.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$footnotes[1]/ancestor::table
                        |$footnotes[1]/ancestor::informaltable">
          <xsl:call-template name="process.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="$footnotes[1]"
                               mode="process.footnote.mode"/>
          <xsl:call-template name="process.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="process.footnotes">
  <xsl:variable name="footnotes" select=".//footnote"/>
  <xsl:variable name="fcount">
    <xsl:call-template name="count.footnotes.in.this.chunk">
      <xsl:with-param name="node" select="."/>
      <xsl:with-param name="footnotes" select="$footnotes"/>
    </xsl:call-template>
  </xsl:variable>

<!--
  <xsl:message>
    <xsl:value-of select="name(.)"/>
    <xsl:text> fcount: </xsl:text>
    <xsl:value-of select="$fcount"/>
  </xsl:message>
-->

  <!-- Only bother to do this if there's at least one non-table footnote -->
  <xsl:if test="$fcount &gt; 0">
    <div class="footnotes">
      <br/>
      <hr width="100" align="left"/>
      <xsl:call-template name="process.footnotes.in.this.chunk">
        <xsl:with-param name="node" select="."/>
        <xsl:with-param name="footnotes" select="$footnotes"/>
      </xsl:call-template>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template name="process.chunk.footnotes">
  <xsl:variable name="is.chunk">
    <xsl:call-template name="chunk"/>
  </xsl:variable>
  <xsl:if test="$is.chunk = 1">
    <xsl:call-template name="process.footnotes"/>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

</xsl:stylesheet>
