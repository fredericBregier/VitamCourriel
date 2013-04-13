<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="exiftool_common_to_fits.xslt"/>
<xsl:template match="/">

    <fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output">  
		<xsl:apply-imports/>
		
		<metadata>
		<document>
			<xsl:variable name="mime" select="exiftool/MIMEType"/>
			<pageCount>
			<xsl:choose>
				<xsl:when test="exiftool/PageCount">
					<xsl:value-of select="exiftool/PageCount"/>
				</xsl:when>
				<xsl:when test="exiftool/Pages">
					<xsl:value-of select="exiftool/Pages"/>
				</xsl:when>
				<xsl:when test="exiftool/Document-statisticPage-count">
					<xsl:value-of select="exiftool/Document-statisticPage-count"/>
				</xsl:when>
				<xsl:when test="exiftool/Slides">
					<xsl:value-of select="exiftool/Slides"/>
				</xsl:when>
			</xsl:choose>	
			</pageCount>
			<xsl:choose>
			<xsl:when test="exiftool/Words">
				<words>
					<xsl:value-of select="exiftool/Words"/>
				</words>
			</xsl:when>
			<xsl:when test="exiftool/Document-statisticWord-count">
				<words>
					<xsl:value-of select="exiftool/Document-statisticWord-count"/>
				</words>
			</xsl:when>
			</xsl:choose>
			<xsl:choose>
			<xsl:when test="exiftool/Characters">
				<characters>
					<xsl:value-of select="exiftool/Characters"/>
				</characters>
			</xsl:when>
			<xsl:when test="exiftool/Document-statisticCharacter-count">
				<characters>
					<xsl:value-of select="exiftool/Document-statisticCharacter-count"/>
				</characters>
			</xsl:when>
			</xsl:choose>
			<xsl:if test="exiftool/CodePage">
				<codePage>
					<xsl:value-of select="exiftool/CodePage"/>
				</codePage>
			</xsl:if>
			<xsl:choose>
			<xsl:when test="exiftool/Paragraphs">
				<paragraphs>
					<xsl:value-of select="exiftool/Paragraphs"/>
				</paragraphs>
			</xsl:when>
			<xsl:when test="exiftool/Document-statisticParagraph-count">
				<paragraphs>
					<xsl:value-of select="exiftool/Document-statisticParagraph-count"/>
				</paragraphs>
			</xsl:when>
			</xsl:choose>
			<title>
				<xsl:value-of select="exiftool/Title"/>
			</title>	

			<author>
				<xsl:value-of select="exiftool/Author"/>
			</author>
			
			<isRightsManaged>
				<xsl:choose>
					<xsl:when test="exiftool/Rights or exiftool/xmpRights">
						<xsl:value-of select="string('yes')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="string('no')"/>
					</xsl:otherwise>
				</xsl:choose>
			</isRightsManaged>

			<isProtected>
				<xsl:choose>
					<xsl:when test="exiftool/Encryption">
						<xsl:value-of select="string('yes')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="string('no')"/>
					</xsl:otherwise>
				</xsl:choose>
			</isProtected>
			<xsl:if test="exiftool/Description">
				<description>
					<xsl:value-of select="exiftool/Description"/>
				</description>
			</xsl:if>
			<xsl:if test="exiftool/Subject">
				<subject>
					<xsl:value-of select="exiftool/Subject"/>
				</subject>
			</xsl:if>
			<xsl:if test="exiftool/Keywords">
				<keywords>
					<xsl:value-of select="exiftool/Keywords"/>
				</keywords>
			</xsl:if>
			<xsl:if test="exiftool/PageLayout">
				<pageLayout>
					<xsl:value-of select="exiftool/PageLayout"/>
				</pageLayout>
			</xsl:if>
			<xsl:if test="exiftool/Document-statisticTable-count">
				<documentTable>
					<xsl:value-of select="exiftool/Document-statisticTable-count"/>
				</documentTable>
			</xsl:if>
			<xsl:if test="exiftool/Document-statisticCell-count">
				<documentCell>
					<xsl:value-of select="exiftool/Document-statisticCell-count"/>
				</documentCell>
			</xsl:if>
			<xsl:if test="exiftool/Document-statisticImage-count">
				<documentImage>
					<xsl:value-of select="exiftool/Document-statisticImage-count"/>
				</documentImage>
			</xsl:if>
			<xsl:if test="exiftool/Document-statisticObject-count">
				<documentObject>
					<xsl:value-of select="exiftool/Document-statisticObject-count"/>
				</documentObject>
			</xsl:if>
			<xsl:if test="exiftool/TitleOfParts">
				<titleParts>
					<xsl:value-of select="exiftool/TitleOfParts"/>
				</titleParts>
			</xsl:if>
			<xsl:if test="exiftool/HeadingPairs">
				<headParts>
					<xsl:value-of select="exiftool/HeadingPairs"/>
				</headParts>
			</xsl:if>
			<xsl:if test="exiftool/LastModifiedBy">
				<lastModifiedBy>
					<xsl:value-of select="exiftool/LastModifiedBy"/>
				</lastModifiedBy>
			</xsl:if>
		</document>				
		</metadata>
	</fits>	

</xsl:template>
</xsl:stylesheet>