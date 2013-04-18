<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : ISO693-3-2SEDA
    Created on : 27/10/2012
    Author     : Frederic Bregier
    Description: translate ISO693-3 to SEDA Languagetype_code xsd file seda_v1-0_language_code.xsd
-->

<xsl:stylesheet version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:ccts="urn:un:unece:uncefact:documentation:standard:CoreComponentsTechnicalSpecification:2">
    <xsl:output method="xml" indent="yes"  />
    <xsl:variable name="version">1.00</xsl:variable>
	<xsl:variable name="fullinfo">yes</xsl:variable>
	<xsl:variable name="debug">false</xsl:variable>
	<xsl:variable name="verbose">yes</xsl:variable>
	<xsl:variable name="mydate"><xsl:value-of select="current-date()"/></xsl:variable>
	<xsl:variable name="now">
   		<xsl:value-of  select="substring($mydate,1,10)"/>
	</xsl:variable>

<xsl:template match="/">
	<xsl:if test="$verbose='yes'">
		<xsl:message> ISO-693-3 2 Seda Language Type - version <xsl:value-of select="$version"/> starting </xsl:message>
		<xsl:message> using</xsl:message>
		<xsl:message> - fullinfo <xsl:value-of select="$fullinfo"/></xsl:message>
		<xsl:message> - verbose <xsl:value-of select="$verbose"/></xsl:message>
		<xsl:message> - debug <xsl:value-of select="$debug"/></xsl:message>
		<xsl:message> Date : <xsl:value-of select="$now"/></xsl:message>
	</xsl:if>
	<xsl:variable name="typenamespace">urn:un:unece:uncefact:codelist:DAF:languageCode</xsl:variable>
<xsl:comment>
 ======================================================== 
 ===== Code List: Language                              ===== 
 ===== Extrait de ISO-639-3 au <xsl:value-of select="$now" />               ===== 
 ===== http://www.sil.org/iso639-3/download.asp#LNIndex ===== 
 ======================================================== 

	Schema agency: Service interministériel des archives de France (SIAF)
	Schema version: 1.0
	Schema date: 06 Juillet 2012

	Copyright (c) 2011 Service interministériel des archives de France

	Ce document est sous licence Creative Commons Paternité 2.0 France.
	Pour accéder à une copie de cette licence, merci de vous rendre à l'adresse suivante
	http://creativecommons.org/licenses/by/2.0/fr/ ou envoyez un courrier à
	Creative Commons, 444 Castro Street, Suite 900,
	Mountain View, California, 94041, USA.
</xsl:comment>
<xsl:text>

</xsl:text>
<xsd:schema version="1.0" 
	    xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
            xmlns:ccts="urn:un:unece:uncefact:documentation:standard:CoreComponentsTechnicalSpecification:2" 
	    xmlns="urn:un:unece:uncefact:codelist:DAF:languageCode" 
	    xmlns:clmDAFLanguageCode="urn:un:unece:uncefact:codelist:DAF:languageCode"
	    targetNamespace="urn:un:unece:uncefact:codelist:DAF:languageCode"  
	    elementFormDefault="qualified" attributeFormDefault="unqualified">
	<xsl:attribute name="version"><xsl:value-of select="$now"/></xsl:attribute>
	<xsl:namespace name="" xpath-default-namespace="$typenamespace"><xsl:value-of select="$typenamespace"/></xsl:namespace>
	<xsl:attribute name="targetNamespace"><xsl:value-of select="$typenamespace"/></xsl:attribute>
<xsl:text>

</xsl:text>
<xsl:comment>
 ======================================================================
 =====  Element Declarations				      ===== 
 ====================================================================== 
 =====  Root Element Declaration				      ===== 
 ====================================================================== 
</xsl:comment>
<xsl:text>

</xsl:text>
 <xsd:element name="LanguageCode" type="clmDAFLanguageCode:LanguageCodeType"/>
<xsl:text>

</xsl:text>
<xsl:comment>
 ====================================================================== 
 ================ Type Definitions ============================= 
 =============================================================== 
 =====        Language Code List  : Language Code          ===== 
 =============================================================== 
</xsl:comment>
<xsl:text>

</xsl:text>
	<xsd:simpleType name="LanguageCodeType">
		<xsd:annotation>
		      <xsd:documentation xml:lang="fr">
			      <ccts:Name>Table des code de langues</ccts:Name>
			      <ccts:Description>Source: ISO-639-3 sur 3 caracteres (http://www.sil.org/iso639-3/)</ccts:Description>
		      </xsd:documentation>
		</xsd:annotation>
		<xsd:restriction base="xsd:token">
			<xsl:apply-templates select="//rows/row"/>
		</xsd:restriction>
  </xsd:simpleType>
</xsd:schema>
<xsl:if test="$verbose='yes'"><xsl:message> ISO-639-3 2 Seda Language Type - completed </xsl:message></xsl:if>
</xsl:template>

<xsl:template match="rows/row">
	<xsl:if test="$debug='yes'">
		<xsl:message> - Code: <xsl:value-of select="Idcode"/></xsl:message>
		<xsl:message> - Name: <xsl:value-of select="Print_Name"/></xsl:message>
	</xsl:if>
	<xsl:variable name="code" select="Idcode"/>
	<xsl:variable name="name" select="Print_Name"/>

    <xsd:enumeration><xsl:attribute name="value"><xsl:value-of select="$code"/></xsl:attribute>
      <xsd:annotation>
        <xsd:documentation xml:lang="en">
          <ccts:Name><xsl:value-of select="$name"/></ccts:Name>
        </xsd:documentation>
      </xsd:annotation>
    </xsd:enumeration>
</xsl:template>

</xsl:stylesheet>
