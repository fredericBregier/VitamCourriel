<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
    xmlns:schold="http://www.ascc.net/xml/schematron"
    xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:svrl="http://purl.oclc.org/dsdl/svrl">

    <xsl:output method="text" omit-xml-declaration="yes" media-type="text/plain"/>

    <xsl:template match="/">
        <xsl:text>Nb errors: </xsl:text><xsl:value-of select="count(.//svrl:failed-assert)"/><xsl:text>&#x0a;</xsl:text>
        <xsl:for-each select=".//svrl:failed-assert">
            <xsl:text>Error num </xsl:text><xsl:value-of select="position()"/><xsl:text>: </xsl:text>
            <xsl:apply-templates select="."/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="svrl:failed-assert">
        <xsl:apply-templates select="svrl:text"/>
    </xsl:template>

    <xsl:template match="svrl:text">
        <xsl:apply-templates/>
        <xsl:text>&#x0a;</xsl:text>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>
    <xsl:template match="comment()"/>
</xsl:stylesheet>
