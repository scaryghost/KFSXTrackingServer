<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : kfstatsx.xsl
    Created on : August 10, 2012, 10:47 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="stats[@category='difficulties']|stats[@category='levels']">
    <table class="graph" width="630" cellspacing="6" cellpadding="0">
        <thead>
            <tr>
                <th>
                <xsl:attribute name="colspan">
                    <xsl:value-of select="count(entry[1]/@*)" />
                </xsl:attribute>
                <xsl:value-of select="@category" />
                </th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <xsl:for-each select="entry[1]/@*">
                <th><xsl:value-of select="name()"/></th>
                </xsl:for-each>
            </tr>
            <xsl:for-each select="entry">
                <tr>
                    <xsl:for-each select="@*">
                        <td><xsl:value-of select="."/></td>
                    </xsl:for-each>
                </tr>
            </xsl:for-each>
        </tbody>
    </table>
</xsl:template>

<xsl:template match="stats[@category='player']|stats[@category='actions']">
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
</xsl:template>

<xsl:template match="stats">
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
