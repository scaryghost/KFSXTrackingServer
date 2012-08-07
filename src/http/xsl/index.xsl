<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    Document   : index.xsl
    Created on : August 6, 2012, 10:09 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="kfstatsx">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Simple Bar Graph with CSS and HTML</title>
    <link rel="stylesheet" type="text/css" href="http/css/kfstatsx.css" />
  </head>
<body>
    <table>
        <thead width="530" class="graph" cellspacing="6" cellpadding="0">
            <tr><th colspan="3">Some Title Here</th></tr>
        </thead>
        <tbody>
            <xsl:apply-templates select="deaths" />
        </tbody>
    </table>
    <table>
        <thead width="530" class="graph" cellspacing="6" cellpadding="0">
            <tr><th colspan="3">Some Title Here</th></tr>
        </thead>
        <tbody>
            <xsl:apply-templates select="aggregate/stats[@category='weapons']" />
        </tbody>
    </table>
</body>
</html>
</xsl:template>

<xsl:template match="aggregate/stats">
    <xsl:variable name="sum" select="sum(stat/@value)" />
    <xsl:for-each select="stat">
        <tr>
            <td width="100"><xsl:value-of select="@name"/></td>
            <td width="400" class="bar">
                <div>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('width:', format-number(@value div $sum, '#%'))"/>
                    </xsl:attribute>
                </div>
                <xsl:value-of select="format-number(@value div $sum, '#.00')"/>
            </td>
            <td><xsl:value-of select="@value" /></td>
        </tr>
    </xsl:for-each>
</xsl:template>

<xsl:template match="deaths">
    <xsl:variable name="sum" select="sum(death/@count)" />
    <xsl:for-each select="death">
        <tr>
            <td width="100"><xsl:value-of select="@source"/></td>
            <td width="400" class="bar">
                <div>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('width:', (@count div $sum)*100, '%')"/>
                    </xsl:attribute>
                    <xsl:value-of select="@count div $sum"/>
                </div>
            </td>
            <td><xsl:value-of select="@count" /></td>
        </tr>
    </xsl:for-each>
</xsl:template>
</xsl:stylesheet>
