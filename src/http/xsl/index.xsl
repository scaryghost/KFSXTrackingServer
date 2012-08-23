<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    Document   : index.xsl
    Created on : August 6, 2012, 10:09 PM
    Author     : eric
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
<xsl:include href="kfstatsx.xsl"/>

<xsl:template match="kfstatsx">
<html>
<xsl:copy-of select="$head" />
<body>
    <center>
        <xsl:copy-of select="$nav" />
        <xsl:apply-templates select="stats" />
    </center>
</body>
</html>
</xsl:template>

<xsl:template match="stats[@category='difficulties']|stats[@category='levels']">
    <div name="item" style="display: none">
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

</xsl:stylesheet>
