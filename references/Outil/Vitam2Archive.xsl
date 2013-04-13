<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : Vitam2Archive
    Created on : 22/09/2012
    Author     : Frederic Bregier
    Description: translate Vitam output of Full Format to Archive model
-->

<xsl:stylesheet version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns="fr:gouv:culture:archivesdefrance:seda:v1.0" 
 xmlns:vitam="vitam-output2.xsd"
exclude-result-prefixes="vitam">

    <xsl:output method="xml" indent="yes"  />
    <xsl:variable name="version">1.00</xsl:variable>
	<xsl:variable name="fullinfo">yes</xsl:variable>
	<xsl:variable name="debug">false</xsl:variable>
	<xsl:variable name="verbose">yes</xsl:variable>
	<xsl:variable name="exclude">x-fmt</xsl:variable>
	<xsl:variable name="now">
   		<xsl:value-of  select="current-dateTime()"/>
	</xsl:variable>

<xsl:template match="/">
	<xsl:if test="$verbose='yes'">
		<xsl:message> Vitam 2 Seda Archive - version <xsl:value-of select="$version"/> starting </xsl:message>
		<xsl:message> using</xsl:message>
		<xsl:message> - exclude <xsl:value-of select="$exclude"/></xsl:message>
		<xsl:message> - fullinfo <xsl:value-of select="$fullinfo"/></xsl:message>
		<xsl:message> - verbose <xsl:value-of select="$verbose"/></xsl:message>
		<xsl:message> - debug <xsl:value-of select="$debug"/></xsl:message>
		<xsl:message> Date : <xsl:value-of select="$now"/></xsl:message>
	</xsl:if>
<xsl:comment>
Created from Vitam Tool version <xsl:value-of select="$version"/>
</xsl:comment>
<xsl:text>

</xsl:text>
<xsl:apply-templates select="checkfiles"/>
<xsl:if test="$verbose='yes'"><xsl:message> Droid 2 Seda File Type - completed </xsl:message></xsl:if>
</xsl:template>

<xsl:template match="checkfiles">
<Archive xmlns="fr:gouv:culture:archivesdefrance:seda:v1.0" Id="IdArchive">

	<DescriptionLanguage>fra</DescriptionLanguage>
		<Name languageID="idName"><xsl:value-of select="@source"/></Name>
		<ContentDescription Id="idContentDescription">
		<DescriptionLevel>fonds</DescriptionLevel>
		<Language>fra</Language>
	</ContentDescription>
	<AccessRestrictionRule Id="idAccessRestrictionRule">
		<Code>AR038</Code>
		<StartDate><xsl:value-of select="substring($now,1,10)" /></StartDate>
	</AccessRestrictionRule>
  	<xsl:apply-templates select="//showformat"/>
</Archive>
</xsl:template>

<xsl:template match="showformat">
	<xsl:variable name="count">
    	<xsl:number/>
  	</xsl:variable>
	<xsl:variable name="filename" select="file/@filename"/>
	<xsl:if test="$debug='yes'">
		<xsl:message> - Status: <xsl:value-of select="@status"/></xsl:message>
		<xsl:message> - File: <xsl:value-of select="$filename"/></xsl:message>
		<xsl:message> - Rank: <xsl:value-of select="$count"/></xsl:message>
	</xsl:if>
	<xsl:element name="Document">
		<xsl:attribute name="Id">Id<xsl:value-of select="$count"/></xsl:attribute>
		<xsl:element name="Attachment">
			<xsl:attribute name="characterSetCode">111</xsl:attribute>
			<xsl:attribute name="encodingCode">7</xsl:attribute>
			<xsl:attribute name="filename"><xsl:value-of select="$filename"/></xsl:attribute>
			<xsl:apply-templates select="identification/identity"/>
			<xsl:attribute name="uri">file://<xsl:value-of select="$filename"/></xsl:attribute>
		</xsl:element>
		<xsl:element name="Integrity">
			<xsl:attribute name="algorithme">
				<xsl:choose>
					<xsl:when test="identification/identity/@sha-1">http://www.w3.org/2000/09/xmldsig#sha1</xsl:when>
					<xsl:when test="identification/identity/@sha-256">http://www.w3.org/2001/04/xmlenc#sha256</xsl:when>
					<xsl:when test="identification/identity/@sha-512">http://www.w3.org/2001/04/xmlenc#sha512</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="identification/identity/@sha-1">
<xsl:value-of select="identification/identity/@sha-1"/></xsl:when>
				<xsl:when test="identification/identity/@sha-256">
<xsl:value-of select="identification/identity/@sha-256"/></xsl:when>
				<xsl:when test="identification/identity/@sha-512">
<xsl:value-of select="identification/identity/@sha-512"/></xsl:when>
			</xsl:choose>
		</xsl:element>
		<xsl:variable name="size" select="fileinfo/size"/>
		<Size unitCode="AD"><xsl:value-of select="$size"/></Size>
		<Type>CDO</Type>
		<OtherMetadata>
			<xsl:apply-templates select="." mode="copyAndChangeNS"/>
			<!-- 
			<xsl:apply-templates select="identification" mode="copyAndChangeNS"/>
			<xsl:apply-templates select="fileinfo" mode="copyAndChangeNS"/>
			<xsl:apply-templates select="filestatus" mode="copyAndChangeNS"/>
			<xsl:apply-templates select="metadata" mode="copyAndChangeNS"/>
			 -->
		</OtherMetadata>
	</xsl:element>
</xsl:template>

<xsl:template match="identification/identity">
	<xsl:attribute name="format"><xsl:value-of select="@puid"/></xsl:attribute>
	<xsl:attribute name="mimeCode"><xsl:value-of select="@mime"/></xsl:attribute>
</xsl:template>

<xsl:template match="*" mode="copyAndChangeNS">
  <xsl:element name="{local-name()}" namespace="vitam">
	  <xsl:copy-of select="@*"/>
	  <xsl:apply-templates select="node()" mode="copyAndChangeNS"/>
  </xsl:element>
</xsl:template>
<xsl:template match="*" mode="copy">
  <xsl:element name="{local-name()}">
	  <xsl:copy-of select="@*"/>
	  <xsl:apply-templates select="node()" mode="copy"/>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>
