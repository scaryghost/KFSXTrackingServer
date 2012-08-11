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
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>KFStatsX v1.0</title>
    <link rel="stylesheet" type="text/css" href="http/css/kfstatsx.css" />
    <script type="text/javascript" src="http/js/kfstatsx.js" ></script>
</head>
<body>
    <center>
        <xsl:copy-of select="$nav" />
        <div name="item" style="display: block">
            <xsl:apply-templates select="stats[@category='weapons']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='perks']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='deaths']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='kills']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='difficulties']" />
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='levels']" />            
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='player']" />            
        </div>
        
        <div name="item" style="display: none">
            <xsl:apply-templates select="stats[@category='actions']" />            
        </div>
    </center>
</body>
</html>
</xsl:template>

</xsl:stylesheet>
