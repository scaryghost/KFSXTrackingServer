<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    Document   : profile.xsl
    Created on : August 9, 2012, 8:09 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="kfstatsx">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>KFStatsX v1.0</title>
        <link rel="stylesheet" type="text/css" href="http/css/kfstatsx.css" />
        <script type="text/javascript" src="http/js/kfstatsx.js" ></script>
    </head>
    <body>
        <xsl:apply-templates select="profile/stats"/>
    </body>
</html>
</xsl:template>

<xsl:template match="stats" >
    <table class="graph" width="630" cellspacing="6" cellpadding="0">
        <thead>
            <tr>
                <th colspan="2">
                    <xsl:value-of select="@category" />
                </th>
            </tr>
        </thead>
        <tbody>
            <xsl:for-each select="stat">
                <tr>
                    <td><xsl:value-of select="@name"/></td>
                    <td style="text-align: right"><xsl:value-of select="@value"/></td>
                </tr>
            </xsl:for-each>
        </tbody>
    </table>
</xsl:template>
</xsl:stylesheet>
