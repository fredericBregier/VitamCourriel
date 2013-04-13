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
			<xsl:choose>
			<xsl:when test="exiftool/CompressedSize">
				<oneEntryCompressedSize>
					<xsl:value-of select="exiftool/CompressedSize"/>
				</oneEntryCompressedSize>
			</xsl:when>
			<xsl:when test="exiftool/ZipCompressedSize">
				<oneEntryCompressedSize>
					<xsl:value-of select="exiftool/ZipCompressedSize"/>
				</oneEntryCompressedSize>
			</xsl:when>
			</xsl:choose>
			<xsl:choose>
			<xsl:when test="exiftool/UncompressedSize">
				<oneEntryUncompressedSize>
					<xsl:value-of select="exiftool/UncompressedSize"/>
				</oneEntryUncompressedSize>
			</xsl:when>
			<xsl:when test="exiftool/ZipUncompressedSize">
				<oneEntryUncompressedSize>
					<xsl:value-of select="exiftool/ZipUncompressedSize"/>
				</oneEntryUncompressedSize>
			</xsl:when>
			</xsl:choose>
			<xsl:choose>
			<xsl:when test="exiftool/ArchivedFileName">
				<oneEntryName>
					<xsl:value-of select="exiftool/ArchivedFileName"/>
				</oneEntryName>
			</xsl:when>
			<xsl:when test="exiftool/ZipFileName">
				<oneEntryName>
					<xsl:value-of select="exiftool/ZipFileName"/>
				</oneEntryName>
			</xsl:when>
			</xsl:choose>
			<xsl:choose>
			<xsl:when test="exiftool/PackingMethod">
				<packingMethod>
					<xsl:value-of select="exiftool/PackingMethod"/>
				</packingMethod>
			</xsl:when>
			<xsl:when test="exiftool/ZipCompression">
				<packingMethod>
					<xsl:value-of select="exiftool/ZipCompression"/>
				</packingMethod>
			</xsl:when>
			<xsl:when test="exiftool/Compression">
				<packingMethod>
					<xsl:value-of select="exiftool/Compression"/>
				</packingMethod>
			</xsl:when>
			</xsl:choose>
		</document>				
		</metadata>
	</fits>	

</xsl:template>
</xsl:stylesheet>