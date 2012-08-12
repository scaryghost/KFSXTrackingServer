<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : kfstatsx.xsl
    Created on : August 10, 2012, 10:47 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:variable name="nav" >
    <table width="630" class="graph" cellspacing="6" cellpadding="0">
            <thead>
                <tr>
                    <td>
                        <a id="displayText" href="javascript:toggle('left');">&#171;</a>
                    </td>
                    <td>
                        <div style="overflow: auto">
                            <div style="width:50%;float:left">
                                <a href="index.xml">Home</a> 
                            </div>
                            <div>
                                <a href="records.xml">Records</a>
                            </div>
                        </div>
                    </td>
                    <td style="text-align:right">
                        <a id="displayText" href="javascript:toggle('right');">&#187;</a>
                    </td>
                </tr>
            </thead>
        </table>
</xsl:variable>

<xsl:template match="stats[@category='player']|stats[@category='actions']|stats[@category='totals']">
    <div name="item">
        <xsl:attribute name="style">
            <xsl:choose>
                <xsl:when test="@category = 'totals'">
                    display: block
                </xsl:when>
                <xsl:otherwise>
                    display: none
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
        <table class="graph" width="630" cellspacing="6" cellpadding="0">
            <thead>
                <tr>
                    <th colspan="2">
                        <xsl:value-of select="@category" />
                    </th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="entry">
                    <tr>
                        <td><xsl:value-of select="@name"/></td>
                        <td style="text-align: right"><xsl:value-of select="@value"/></td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </div>
</xsl:template>

<xsl:template match="stats">
    <div name="item" style="display: none">
        <xsl:variable name="sum" select="sum(entry/@value)" />
        <table width="630" class="graph" cellspacing="6" cellpadding="0">
            <thead>
                <tr><th colspan="3"><xsl:value-of select="@category" /></th></tr>
            </thead>
            <tbody>
                <xsl:for-each select="entry">
                    <tr>
                        <td width="200"><xsl:value-of select="@name"/></td>
                        <td width="400" class="bar">
                            <div>
                                <xsl:attribute name="style">
                                    <xsl:value-of select="concat('width:', format-number(@value div $sum, '#%'))"/>
                                </xsl:attribute>
                            </div>
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
    </div>
</xsl:template>

</xsl:stylesheet>
