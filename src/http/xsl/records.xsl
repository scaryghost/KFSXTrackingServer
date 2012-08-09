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
        <form action='profile.xml' method='get'>
            <p>Enter player's steam id</p>
            <input type='text' name='steamid' />
            <br />
            <input type='submit' value='submit' />
        </form>
        <xsl:apply-templates select="records"/>
    </body>
</html>
</xsl:template>

<xsl:template match="records">
    <table>
        <thead>
            <tr>
                <th>Player</th>
                <th>Wins</th>
                <th>Losses</th>
                <th>Disconnects</th>
            </tr>
        </thead>
        <tbody>
            <xsl:for-each select="record">
                <tr>
                    <td>
                        <img>
                            <xsl:attribute name="src">
                                <xsl:value-of select="@avatar"/>
                            </xsl:attribute>
                        </img>
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
