<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : kfstatsx.xsl
    Created on : August 10, 2012, 10:47 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:variable name="head">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>KFStatsX</title>
    <link rel="stylesheet" type="text/css" href="http/css/kfstatsx.css" />
    <link rel='icon' type='image/vnd.microsoft.icon' href='http/ico/favicon.ico' />
    <script type="text/javascript" src="http/js/kfstatsx.js" ></script>
</head>
</xsl:variable>

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

<xsl:variable name="altnav" >
    <table width="630" class="graph" cellspacing="6" cellpadding="0">
        <thead>
            <tr>
                <td></td>
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
                <td></td>
            </tr>
        </thead>
    </table>
</xsl:variable>

<xsl:template match="stats[@category='sessions']" >
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
            <th style="text-align:left"><a>
                <xsl:attribute name="href">
                    sessions.xml?steamid64=<xsl:value-of select="@steamid64" />&#38;page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=level&#38;order=<xsl:value-of select="@level"/>
                </xsl:attribute>
                Level
            </a></th>
            <th><a>
                <xsl:attribute name="href">
                    sessions.xml?steamid64=<xsl:value-of select="@steamid64" />&#38;page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=difficulty&#38;order=<xsl:value-of select="@difficulty"/>
                </xsl:attribute>
                Difficulty
            </a></th>
            <th><a>
                <xsl:attribute name="href">
                    sessions.xml?steamid64=<xsl:value-of select="@steamid64" />&#38;page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=length&#38;order=<xsl:value-of select="@length"/>
                </xsl:attribute>
                Length
            </a></th>
            <th><a>
                <xsl:attribute name="href">
                    sessions.xml?steamid64=<xsl:value-of select="@steamid64" />&#38;page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=result&#38;order=<xsl:value-of select="@result"/>
                </xsl:attribute>
                Result
            </a></th>
            <th><a>
                <xsl:attribute name="href">
                    sessions.xml?steamid64=<xsl:value-of select="@steamid64" />&#38;page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=wave&#38;order=<xsl:value-of select="@wave"/>
                </xsl:attribute>
                Wave
            </a></th>
            <th><a>
                <xsl:attribute name="href">
                    sessions.xml?steamid64=<xsl:value-of select="@steamid64" />&#38;page=<xsl:value-of select="@page" />&#38;rows=<xsl:value-of select="@rows"/>&#38;group=timestamp&#38;order=<xsl:value-of select="@timestamp"/>
                </xsl:attribute>
                Timestamp
            </a></th>
        </tr>
            <xsl:for-each select="entry">
                <tr>
                    <xsl:for-each select="@*">
                        <td><xsl:value-of select="."/></td>
                    </xsl:for-each>
                </tr>
            </xsl:for-each>
        </tbody>
        <tfoot>
            <tr>
                <xsl:for-each select="total/@*">
                    <td><xsl:value-of select="."/></td>
                </xsl:for-each>
            </tr>
        </tfoot>
    </table>
</xsl:template>
<xsl:template match="stats[@category='difficulties']|stats[@category='levels']">
    <div name="item">
        <xsl:attribute name="style">
            <xsl:choose>
                <xsl:when test="@category = 'sessions'">
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
            <tfoot>
                <tr>
                    <xsl:for-each select="total/@*">
                        <td><xsl:value-of select="."/></td>
                    </xsl:for-each>
                </tr>
            </tfoot>
        </table>
    </div>
</xsl:template>

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
                        <td>
                            <xsl:attribute name="title">
                                <xsl:value-of select="@hint" />
                            </xsl:attribute>
                            <xsl:value-of select="@value" />
                        </td>
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
