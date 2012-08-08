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
    <script type="text/javascript" src="http/js/kfstatsx.js" ></script>
</head>
<body>
    <center>
        <table width="630" class="graph" cellspacing="6" cellpadding="0">
            <thead>
                <tr>
                    <td colspan="2">
                        <a id="displayText" href="javascript:toggle('left');">&#171;</a>
                    </td>
                    <td style="text-align:right">
                        <a id="displayText" href="javascript:toggle('right');">&#187;</a>
                    </td>
                </tr>
            </thead>
        </table>
        <div name="item" style="display: block">
            <xsl:apply-templates select="aggregate[@category='weapons']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="aggregate[@category='perks']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="aggregate[@category='deaths']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="aggregate[@category='kills']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="aggregate[@category='difficulties']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="aggregate[@category='levels']" />            
        </div>
    </center>
</body>
</html>
</xsl:template>

<xsl:template match="aggregate[@category='difficulties']|aggregate[@category='levels']">
    <table class="graph" width="630" cellspacing="6" cellpadding="0">
        <thead>
            <tr>
                <th>
                <xsl:attribute name="colspan">
                    <xsl:value-of select="count(entry[1]/@*)" />
                </xsl:attribute>
                <xsl:attribute name="style">
                    text-transform: capitalize
                </xsl:attribute>
                <xsl:value-of select="@category" />
                </th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <xsl:for-each select="entry[1]/@*">
                <th style="text-transform: capitalize;"><xsl:value-of select="name()"/></th>
                </xsl:for-each>
            </tr>
            <xsl:for-each select="entry">
                <tr>
                    <xsl:for-each select="@*">
                        <td style="text-transform: capitalize;"><xsl:value-of select="."/></td>
                    </xsl:for-each>
                </tr>
            </xsl:for-each>
        </tbody>
    </table>
</xsl:template>

<xsl:template match="aggregate">
    <xsl:variable name="sum" select="sum(entry/@value)" />
    <table width="630" class="graph" cellspacing="6" cellpadding="0">
        <thead>
            <tr><th colspan="3" style="text-transform: capitalize;"><xsl:value-of select="@category" /></th></tr>
        </thead>
        <tbody>
            <xsl:for-each select="entry">
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
        </tbody>
        <tfoot>
            <tr>
                <td colspan="2">Total</td>
                <td><xsl:value-of select="$sum"/></td>
            </tr>
        </tfoot>
    </table>
    <br/>
</xsl:template>
</xsl:stylesheet>
