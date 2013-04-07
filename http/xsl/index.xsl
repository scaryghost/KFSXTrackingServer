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

</xsl:stylesheet>
