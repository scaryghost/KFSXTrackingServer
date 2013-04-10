<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    Document   : records.xsl
    Created on : August 8, 2012, 10:48 PM
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
                <form action='profile.xml' method='get' style="text-align:left">
                    Enter player's <a href="http://steamidconverter.com/" target="_blank">steamID64: </a>
                    <input type='text' name='steamid64' />
                    <input type='submit' value='Search Player' />
                </form>
                <form action="records.xml" method="get" style="text-align:left">
                    Goto page 
                    <input type='text' name='page'>
                        <xsl:attribute name="value">
                            <xsl:value-of select="records/@page"/>
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
                        records.xml?page=<xsl:value-of select="(records/@page)-1" />&#38;rows=<xsl:value-of select="records/@rows"/><xsl:value-of select="records/@query"/>
                    </xsl:attribute>
                    &#171;
                    </a>
                    <a id="displayText">
                        <xsl:attribute name="href">
                            records.xml?page=<xsl:value-of select="(records/@page)+1" />&#38;rows=<xsl:value-of select="records/@rows"/><xsl:value-of select="records/@query"/>
                        </xsl:attribute>
                        &#187;
                    </a>
                </form>
                <xsl:apply-templates select="records"/>
            </div>
        </center>
    </body>
</html>
</xsl:template>

<xsl:template match="records">
    <table class="graph" width="630" cellspacing="6" cellpadding="0">
        <thead>
            <tr>
                <th colspan="5">Player Records</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <th style="text-align:right">#</th>
                <th style="text-align:left"><a>
                    <xsl:attribute name="href">
                        records.xml?page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=name&#38;order=<xsl:value-of select="@name"/>
                    </xsl:attribute>
                    Name
                </a></th>
                <th><a>
                    <xsl:attribute name="href">
                        records.xml?page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=wins&#38;order=<xsl:value-of select="@wins"/>
                    </xsl:attribute>
                    Wins
                </a></th>
                <th><a>
                    <xsl:attribute name="href">
                        records.xml?page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=losses&#38;order=<xsl:value-of select="@losses"/>
                    </xsl:attribute>
                    Losses
                </a></th>
                <th><a>
                    <xsl:attribute name="href">
                        records.xml?page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=disconnects&#38;order=<xsl:value-of select="@disconnects"/>
                    </xsl:attribute>
                    Disconnects
                </a></th>
            </tr>
            <xsl:for-each select="record">
                <tr>
                    <td style="text-align:right">
                        <xsl:value-of select="@pos" />
                    </td>
                    <td style="text-align:left;text-transform: none">
                        <a>
                            <xsl:attribute name="href">
                                profile.xml?steamid64=<xsl:value-of select="@steamid64"/>
                            </xsl:attribute>
                            <xsl:value-of select="@name"/>
                        </a>
                    </td>
                    <td><xsl:value-of select="@wins"/></td>
                    <td><xsl:value-of select="@losses"/></td>
                    <td><xsl:value-of select="@disconnects"/></td>
                </tr>
            </xsl:for-each>
        </tbody>
    </table>
</xsl:template>

</xsl:stylesheet>