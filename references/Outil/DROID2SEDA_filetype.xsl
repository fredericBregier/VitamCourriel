<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : DROID2SEDA
    Created on : 22/09/2012
    Author     : Frederic Bregier
    Description: translate DROID signature file to SEDA filetype_code xsd file seda_v1-0_filetype_code.xsd
-->

<xsl:stylesheet version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:sign="http://www.nationalarchives.gov.uk/pronom/SignatureFile"
 xmlns:ccts="urn:un:unece:uncefact:documentation:standard:CoreComponentsTechnicalSpecification:2"
exclude-result-prefixes="sign">
    <xsl:output method="xml" indent="yes"  />
    <xsl:variable name="version">1.00</xsl:variable>
	<xsl:variable name="fullinfo">yes</xsl:variable>
	<xsl:variable name="debug">false</xsl:variable>
	<xsl:variable name="verbose">yes</xsl:variable>
	<xsl:variable name="exclude">x-fmt</xsl:variable>
	<xsl:variable name="now">
   		<xsl:value-of  select="current-dateTime()"/>
	</xsl:variable>

    <!-- Objet : translate DROID signature file to SEDA filetype_code xsd file seda_v1-0_filetype_code.xsd

	      <FileFormat ID="80"
            MIMEType="application/dwf, application/x-dwf, drawing/x-dwf, image/vnd.dwf, image/x-dwf, model/vnd.dwf"
            Name="AutoCAD Design Web Format" PUID="x-fmt/49" Version="6.0">
            <InternalSignatureID>386</InternalSignatureID>
            <Extension>dwf</Extension>
        </FileFormat>
        <FileFormat ID="86" MIMEType="application/postscript"
            Name="Encapsulated PostScript File Format" PUID="fmt/122" Version="1.2">
            <InternalSignatureID>173</InternalSignatureID>
            <InternalSignatureID>174</InternalSignatureID>
            <Extension>eps</Extension>
            <Extension>epsf</Extension>
            <HasPriorityOverFileFormatID>138</HasPriorityOverFileFormatID>
            <HasPriorityOverFileFormatID>771</HasPriorityOverFileFormatID>
            <HasPriorityOverFileFormatID>772</HasPriorityOverFileFormatID>
            <HasPriorityOverFileFormatID>773</HasPriorityOverFileFormatID>
            <HasPriorityOverFileFormatID>1073</HasPriorityOverFileFormatID>
        </FileFormat>

to

	          <xsd:enumeration value="fmt/122">
                <xsd:annotation>
                    <xsd:documentation>
                        <ccts:Name>Encapsulated PostScript File Format</ccts:Name>
                        <ccts:Description>Encapsulated PostScript File Format
                            (Version=1.2)</ccts:Description>
                    </xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>

	//FFSignatureFile/FileFormatCollection/FileFormat
	=> value=@PUID
	=> Name=@Name
	=> Description=@Name\n (Version=@Version)

-->

<xsl:template match="/">
	<xsl:if test="$verbose='yes'">
		<xsl:message> Droid 2 Seda File Type - version <xsl:value-of select="$version"/> starting </xsl:message>
		<xsl:message> using</xsl:message>
		<xsl:message> - exclude <xsl:value-of select="$exclude"/></xsl:message>
		<xsl:message> - fullinfo <xsl:value-of select="$fullinfo"/></xsl:message>
		<xsl:message> - verbose <xsl:value-of select="$verbose"/></xsl:message>
		<xsl:message> - debug <xsl:value-of select="$debug"/></xsl:message>
		<xsl:message> Date : <xsl:value-of select="$now"/></xsl:message>
	</xsl:if>
	<xsl:variable name="droiddate" select="substring(sign:FFSignatureFile/@DateCreated,1,10)"/>
	<xsl:variable name="typenamespace">urn:un:unece:uncefact:codelist:draft:DAF:fileTypeCode:<xsl:value-of select="$droiddate"/></xsl:variable>
<xsl:comment>
 ===========================================================
 ===== Code List: File Type Code                       =====
 ===== Extrait des PUID du registre PRONOM Version=<xsl:value-of select="sign:FFSignatureFile/@Version" />  =====
 ===== au <xsl:value-of select="$droiddate" /> a l'exception des PUID en fmt/x-  =====
 ===========================================================

	Schema agency: Service interministériel des archives de France (SIAF)
	Schema version: 1.0
	Schema date: <xsl:value-of select="substring($now,1,10)" />

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
    elementFormDefault="qualified"
    attributeFormDefault="unqualified">
<!--
    xmlns="urn:un:unece:uncefact:codelist:draft:DAF:fileTypeCode:{$droiddate}"
    xmlns:clmDAFFileTypeCode="urn:un:unece:uncefact:codelist:draft:DAF:fileTypeCode:{$droiddate}"
    targetNamespace="urn:un:unece:uncefact:codelist:draft:DAF:fileTypeCode:{$droiddate}"
 -->
	<xsl:namespace name="clmDAFFileTypeCode" ><xsl:value-of select="$typenamespace"/></xsl:namespace>
	<xsl:namespace name="" xpath-default-namespace="$typenamespace"><xsl:value-of select="$typenamespace"/></xsl:namespace>
	<xsl:attribute name="targetNamespace"><xsl:value-of select="$typenamespace"/></xsl:attribute>
<xsl:text>

</xsl:text>
<xsl:comment>
=====  Root Element Declaration				      =====
</xsl:comment>
<xsl:text>

</xsl:text>
 <xsd:element name="FileTypeCode" type="clmDAFFileTypeCode:FileTypeCodeType"/>
<xsl:text>

</xsl:text>
<xsl:comment>
===== Code List FileType: FileType Code =====
</xsl:comment>
<xsl:text>

</xsl:text>
  <xsd:simpleType name="FileTypeCodeType" >
    <xsd:annotation >
      <xsd:documentation xml:lang="fr">
        <ccts:Name>Table des formats de fichier</ccts:Name>
        <ccts:Description>Source: le registre PRONOM. Seuls ont été retenus les PUID non préfixés par "<xsl:value-of select="$exclude"/>"</ccts:Description>
        <ccts:MIMEType>MimeType associé</ccts:MIMEType>
        <ccts:Extension>Extensions associées dans une liste avec ',' comme séparateur</ccts:Extension>
      </xsd:documentation>
    </xsd:annotation>
    <xsd:restriction base="xsd:token">
  	  <xsl:apply-templates select="//sign:FFSignatureFile/sign:FileFormatCollection/sign:FileFormat"/>
    </xsd:restriction>
  </xsd:simpleType>
</xsd:schema>
<xsl:if test="$verbose='yes'"><xsl:message> Droid 2 Seda File Type - completed </xsl:message></xsl:if>
</xsl:template>

<xsl:template match="sign:FFSignatureFile/sign:FileFormatCollection/sign:FileFormat">
	<xsl:if test="$debug='yes'">
		<xsl:message> - Type: <xsl:value-of select="@PUID"/></xsl:message>
		<xsl:message> - Name: <xsl:value-of select="@Name"/></xsl:message>
		<xsl:message> - Version: <xsl:value-of select="@Version"/></xsl:message>
		<xsl:message> - MimeType: <xsl:value-of select="@MIMEType"/></xsl:message>
		<xsl:message> - Extension: <xsl:value-of select="@Extension"/></xsl:message>
	</xsl:if>
	<xsl:variable name="curpuid" select="@PUID"/>
	<xsl:if test="not(starts-with($curpuid, $exclude))">
    <xsd:enumeration><xsl:attribute name="value"><xsl:value-of select="@PUID"/></xsl:attribute>
      <xsd:annotation>
        <xsd:documentation>
          <ccts:Name><xsl:value-of select="@Name"/></ccts:Name>
          <ccts:Description><xsl:value-of select="@Name"/> 
            <xsl:if test="@Version">
              (Version=<xsl:value-of select="@Version"/>)</xsl:if></ccts:Description>
          <!-- fullinfo ? -->
          <xsl:if test="$fullinfo='yes'">
            <xsl:if test="@MIMEType"><ccts:MIMEType><xsl:value-of select="@MIMEType"/></ccts:MIMEType></xsl:if>
            <xsl:if test="sign:Extension"><ccts:Extension><xsl:for-each select="sign:Extension"><xsl:value-of select="."/><xsl:if test="not(position()=last())">,</xsl:if></xsl:for-each></ccts:Extension></xsl:if>
          </xsl:if>
        </xsd:documentation>
      </xsd:annotation>
    </xsd:enumeration>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>
