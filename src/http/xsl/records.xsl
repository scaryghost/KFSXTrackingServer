<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    Document   : records.xsl
    Created on : August 8, 2012, 10:48 PM
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
        <center>
            <div style="background-color: #C8C8C8;width:630px" >
                <form action='profile.xml' method='get'>
                    <p style="text-align: left">Enter player's <a href="http://steamidconverter.com/" target="_blank">steamID64: </a>
                    <input type='text' name='steamid' />
                    <input type='submit' value='Search Player' />
                    </p>
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
                <th style="text-align:left">Name</th>
                <th>Wins</th>
                <th>Losses</th>
                <th>Disconnects</th>
            </tr>
            <xsl:for-each select="record">
                <tr>
                    <td style="text-align:right">
                        <xsl:value-of select="position()" />
                    </td>
                    <td style="text-align:left;text-transform: none">
                        <a>
                            <xsl:attribute name="href">
                                profile.xml?steamid=<xsl:value-of select="@steamid"/>
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
