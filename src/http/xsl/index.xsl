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
    <title>KFStatsX v1.0</title>
    <link rel="stylesheet" type="text/css" href="http/css/kfstatsx.css" />
</head>
<body>
    <center>
        <xsl:apply-templates select="aggregate/stats[@category='weapons']" />
        <xsl:apply-templates select="aggregate/stats[@category='perks']" />
        <xsl:apply-templates select="aggregate/stats[@category='deaths']" />
        <xsl:apply-templates select="aggregate/stats[@category='kills']" />
    </center>
</body>
</html>
</xsl:template>

<xsl:template match="aggregate/stats">
    <table width="630" class="graph" cellspacing="6" cellpadding="0">
        <thead>
            <tr><th colspan="3" style="text-transform: capitalize;"><xsl:value-of select="@category" /></th></tr>
        </thead>
        <tbody>
            <xsl:variable name="sum" select="sum(stat/@value)" />
            <xsl:for-each select="stat">
                <tr>
                    <td width="200" style="text-transform: capitalize;"><xsl:value-of select="@name"/></td>
                    <td width="400" class="bar">
                        <div>
                            <xsl:attribute name="style">
                                <xsl:value-of select="concat('width:', format-number(@value div $sum, '#%'))"/>
                            </xsl:attribute>
                        </div>
                        <!--
                        <xsl:value-of select="format-number(@value div $sum, '#.0000')"/>
                        -->
                    </td>
                    <td><xsl:value-of select="@value" /></td>
                </tr>
            </xsl:for-each>
            <tr>
                <td width="100" colspan="2">Total</td>
                <td><xsl:value-of select="$sum"/></td>
            </tr>
        </tbody>
    </table>
    <br/>
</xsl:template>
</xsl:stylesheet>
