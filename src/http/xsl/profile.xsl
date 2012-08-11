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
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>KFStatsX v1.0</title>
        <link rel="stylesheet" type="text/css" href="http/css/kfstatsx.css" />
        <script type="text/javascript" src="http/js/kfstatsx.js" ></script>
    </head>
    <body>
        <xsl:apply-templates select="profile" />
        <xsl:apply-templates select="profile/stats"/>
    </body>
</html>
</xsl:template>

<xsl:template match="profile">
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
                    <a>
                        <xsl:attribute name="href">
                            http://steamcommunity.com/profiles/<xsl:value-of select="@steamid" />
                        </xsl:attribute>
                        <xsl:value-of select="@steamid" />
                    </a>
                </td>
            </tr>
            
        </tbody>
    </table>
</xsl:template>
</xsl:stylesheet>
