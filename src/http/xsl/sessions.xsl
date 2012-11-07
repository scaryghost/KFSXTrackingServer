<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : sessions.xsl
    Created on : November 6, 2012, 11:00 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:include href="kfstatsx.xsl"/>

<xsl:template match="kfstatsx">
<html>
    <xsl:copy-of select="$head" />
    <body>
        <center>
            <xsl:copy-of select="$altnav" />
            <div style="background-color: #C8C8C8;width:630px" >
                <br/>
                <form action="sessions.xml" method="get" style="text-align:left">
                    Goto page 
                    <input type='text' name='page'>
                        <xsl:attribute name="value">
                            <xsl:value-of select="stats/@page"/>
                        </xsl:attribute>
                    </input>
                    Rows 
                    <select name="rows">
                        <option value="25">25</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                        <option value="250">250</option>
                    </select>
                    <input type='submit' value='Update'/>
                    <a id="displayText">
                        <xsl:attribute name="href">
                            sessions.xml?steamid64=<xsl:value-of select="stats/@steamid64" />&#38;page=<xsl:value-of select="(stats/@page)-1" />&#38;rows=<xsl:value-of select="stats/@rows"/>
                        </xsl:attribute>
                    &#171;
                    </a>
                    <a id="displayText">
                        <xsl:attribute name="href">
                            sessions.xml?steamid64=<xsl:value-of select="stats/@steamid64" />&#38;page=<xsl:value-of select="(stats/@page)+1" />&#38;rows=<xsl:value-of select="stats/@rows"/>
                        </xsl:attribute>
                        &#187;
                    </a>
                </form>
                <xsl:apply-templates select="stats" />
            </div>
        </center>
    </body>
</html>
</xsl:template>

</xsl:stylesheet>
