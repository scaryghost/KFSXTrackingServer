<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    Document   : profile.xsl
    Created on : August 9, 2012, 8:09 PM
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
            <xsl:copy-of select="$nav" />
            <xsl:apply-templates select="error" />
            <xsl:apply-templates select="profile" />
            <xsl:apply-templates select="profile/stats" />
        </center>
    </body>
</html>
</xsl:template>

<xsl:template match="error">
    <div style="background-color: #C8C8C8;width:630px" >
        <xsl:value-of select="."/>
    </div>
</xsl:template>

<xsl:template match="profile">
    <div name="item" style="display: block">
        <table class="graph" width="630" cellspacing="6" cellpadding="0">
            <thead>
                <tr>
                    <th colspan="3">Profile</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Name</td>
                    <td><xsl:value-of select="@name" /></td>
                </tr>
                <tr>
                    <td>Wins</td>
                    <td><xsl:value-of select="@wins" /></td>
                    <td rowspan="4">
                        <img>
                            <xsl:attribute name="src">
                                <xsl:value-of select="@avatar" />
                            </xsl:attribute>
                        </img>
                    </td>
                </tr>
                <tr>
                    <td>Losses</td>
                    <td><xsl:value-of select="@losses" /></td>
                </tr>
                <tr>
                    <td>Disconnects</td>
                    <td><xsl:value-of select="@disconnects" /></td>
                </tr>
                <tr>
                    <td>Steam Community</td>
                    <td>
                        <a target="_blank">
                            <xsl:attribute name="href">
                                http://steamcommunity.com/profiles/<xsl:value-of select="@steamid" />
                            </xsl:attribute>
                            <xsl:value-of select="@steamid" />
                        </a>
                    </td>
                </tr>

            </tbody>
        </table>
    </div>
</xsl:template>
</xsl:stylesheet>
