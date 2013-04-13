<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:w2e="http://utils.cam.org" xmlns:saxon="http://saxon.sf.net/" exclude-result-prefixes="saxon w2e" extension-element-prefixes="saxon">
	<!-- CAM kit utility program -->
	<!-- Provided "as is" License: Creative Commons - http://creativecommons.org/licenses/by-sa/3.0/
		Author: Martin ME Roberts, March, 2008 -->
	<!--
		Purpose  : Parse XSD schema into CAM template XML structure components.
		Creates XML instance to be used as Structure in CAM template editor.
		Original : 03/21/2008
		Initials : mmer
		CAM ver    : 1.1 
		Description : Part of a set of tools to ease alignment of XSD based validations and documentation
		and CAM templates. Generates an XML instance in CAM structure format
		Updated  - Inits - Change
		03/21/08   mmer  Initial release
		03/22/08   drrw    Added simple type handling - enumeration value
		03/27/08   drrw    Added sub-path method for nested collections of schema includes
		04/10/08   drrw    Handle schema without base targetNamespace
		04/10/08   drrw    Fix outputting of documentation content (node to text)
		01/09/11   drrw    Handle include with no namespace
		01/09/11   drrw    Solve recursion issues with imports and includes
		01/11/11   drrw    Block redundant includes
		01/12/11   drrw    Add depth check to trap looping
		06/22/11   drrw    Improve relative path handling for includes within includes
	-->
	<xsl:param name="path"/>
	<xsl:param name="sub-path"/>
	<xsl:param name="chkPath"/>
	<xsl:param name="sub-sub-path"></xsl:param>
	<xsl:param name="chksubPath"></xsl:param>
	
	<xsl:variable name="version">1.06</xsl:variable>
	<xsl:variable name="debug">false</xsl:variable>
	<xsl:variable name="verbose">false</xsl:variable>
	
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="/">
		<xsl:if test="$verbose='yes'">
			<xsl:message> xsd 2 schema - version <xsl:value-of select="$version"/> starting </xsl:message>
			<xsl:message> using</xsl:message>
			<xsl:message> - path <xsl:value-of select="$path"/></xsl:message>
			<xsl:message> - sub-path <xsl:value-of select="$sub-path"/></xsl:message>
			<xsl:message> - chkpath <xsl:value-of select="$chkPath"/></xsl:message>
			<xsl:message> - sub-sub-path <xsl:value-of select="$sub-sub-path"/></xsl:message>
			<xsl:message> - chksubpath <xsl:value-of select="$chksubPath"/></xsl:message>
		</xsl:if>
		<xsl:variable name="imports-list">
			<xsl:for-each select="//xsd:schema/xsd:import/@schemaLocation">|<xsl:value-of select="."/></xsl:for-each>
		</xsl:variable>
		<xsl:variable name="includes-list">
			<xsl:for-each select="//xsd:schema/xsd:include/@schemaLocation | include/@schemaLocation">|<xsl:value-of select="."/></xsl:for-each>
		</xsl:variable>
		
		<xsl:variable name="schema">
			<xsl:apply-templates select="//xsd:schema" mode="schema">
				<xsl:with-param name="imports" select="$imports-list"/>
				<xsl:with-param name="includes" select="$includes-list"/>
				<xsl:with-param name="depth" select="0"/>
			</xsl:apply-templates>
		</xsl:variable>
		
		<xsl:copy-of select="$schema"/>
		<xsl:if test="$verbose='yes'"><xsl:message> xsd 2 schema - completed </xsl:message></xsl:if>
	</xsl:template>

	<!-- schema templates -->

	<xsl:template match="/" mode="schema">
		<xsl:param name="currentPath"/>
		<xsl:param name="imports"/>
		<xsl:param name="includes"/>
		<xsl:param name="parent"/>
		<xsl:param name="depth"/>
		<xsl:apply-templates mode="schema">
			<xsl:with-param name="currentPath" select="$currentPath"/>
			<xsl:with-param name="imports" select="$imports"/>
			<xsl:with-param name="includes" select="$includes"/>
			<xsl:with-param name="parent" select="$parent"/>
			<xsl:with-param name="depth" select="$depth"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="xsd:import" mode="schema"><!-- Process imports -->
		<xsl:param name="currentPath"/>
		<xsl:param name="imports"/>
		<xsl:param name="includes"/>
		<xsl:param name="parent"/>
		<xsl:param name="depth"/>
		<xsl:if test="$verbose='yes'"><xsl:message> - import<xsl:if test="string-length($parent) &gt; 0">ing (<xsl:value-of select="$depth"/>) <xsl:value-of select="$parent"/></xsl:if> [<xsl:value-of select="@schemaLocation"/>]</xsl:message></xsl:if>
		<xsl:if test="$debug='yes' and($depth &gt; 1) and(contains(substring-after($imports,'|'),'|'))"><xsl:message> - imports <xsl:value-of select="$imports"/></xsl:message></xsl:if>
		<xsl:variable name="importPath" select="w2e:extractPath(@schemaLocation)"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="schema"/>
			<xsl:apply-templates select="*" mode="schema">
				<xsl:with-param name="currentPath" select="@schemaLocation"/>
				<xsl:with-param name="imports" select="concat($imports,'|',@schemaLocation)"/>
				<xsl:with-param name="includes" select="$includes"/>
				<xsl:with-param name="parent" select="$parent"/>
				<xsl:with-param name="depth" select="$depth"/>
			</xsl:apply-templates>
			<xsl:choose>
				<xsl:when test="$depth &gt; 0 and(contains($imports,concat('|',@schemaLocation)))"><xsl:if test="$verbose='yes'"><xsl:message> - DUPLICATE IMPORT IGNORED : <xsl:value-of select="@schemaLocation"/></xsl:message></xsl:if></xsl:when>
				<xsl:when test="string-length($chksubPath) &gt; 0 and(contains(@schemaLocation,$chksubPath))">
					<xsl:apply-templates select="document(concat($sub-sub-path,@schemaLocation)) " mode="schema">
						<xsl:with-param name="currentPath"><xsl:value-of select="$sub-sub-path"/></xsl:with-param>
						<xsl:with-param name="imports" select="concat($imports,'|',@schemaLocation)"/>
						<xsl:with-param name="includes" select="$includes"/>
						<xsl:with-param name="parent" select="if(string-length($importPath) &gt; 0) then substring-after(@schemaLocation,$importPath) else @schemaLocation"/>
						<xsl:with-param name="depth" select="xsd:integer($depth) + 1"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:when test="string-length($chkPath) &gt; 0 and(not(contains(@schemaLocation,$chkPath)))">
				   <xsl:apply-templates select="document(concat($sub-path,@schemaLocation)) " mode="schema">
				   	<xsl:with-param name="currentPath"><xsl:value-of select="$sub-path"/></xsl:with-param>
				   	<xsl:with-param name="imports" select="concat($imports,'|',@schemaLocation)"/>
				   	<xsl:with-param name="includes" select="$includes"/>
				   	<xsl:with-param name="parent" select="if(string-length($importPath) &gt; 0) then substring-after(@schemaLocation,$importPath) else @schemaLocation"/>
				   	<xsl:with-param name="depth" select="xsd:integer($depth) + 1"/>
				   </xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
				   <xsl:apply-templates select="document(concat($path,@schemaLocation)) " mode="schema">
				   	<xsl:with-param name="currentPath"><xsl:value-of select="w2e:extractPath(concat($path,@schemaLocation))"/></xsl:with-param>
				   	<xsl:with-param name="imports" select="concat($imports,'|',@schemaLocation)"/>
				   	<xsl:with-param name="includes" select="$includes"/>
				   	<xsl:with-param name="parent" select="if(string-length($importPath) &gt; 0) then substring-after(@schemaLocation,$importPath) else @schemaLocation"/>
				   	<xsl:with-param name="depth" select="xsd:integer($depth) + 1"/>
				   </xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="xsd:include | include" mode="schema"><!-- Process includes -->
		<xsl:param name="currentPath"/>
		<xsl:param name="imports"/>
		<xsl:param name="includes"/>
		<xsl:param name="parent"/>
		<xsl:param name="depth"/>
		<xsl:if test="$verbose='yes'"><xsl:message> - include (<xsl:value-of select="$depth"/>) <xsl:if test="string-length($parent) &gt; 0"><xsl:value-of select="$parent"/> + </xsl:if><xsl:value-of select="@schemaLocation"/></xsl:message></xsl:if>
		<xsl:if test="$debug='yes' and(contains(substring-after($includes,'|'),'|'))"><xsl:message> - includes <xsl:value-of select="$includes"/></xsl:message></xsl:if>
		<xsl:variable name="includePath" select="w2e:extractPath(@schemaLocation)"/>
		<xsl:variable name="defaultPath" select="if(string-length($currentPath) &gt; 0) then $currentPath else $path"/>
		<xsl:variable name="nextDepth" select="xsd:integer($depth) + 1"></xsl:variable>
		
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="schema"/>
			<xsl:apply-templates select="*" mode="schema">
				<xsl:with-param name="currentPath" select="$currentPath"/>
				<xsl:with-param name="imports" select="$imports"/>
				<xsl:with-param name="includes" select="concat($includes,'|',@schemaLocation)"/>
				<xsl:with-param name="depth" select="$nextDepth"/>
			</xsl:apply-templates>
			<xsl:choose>
				<xsl:when test="$depth &gt; 9"><xsl:if test="$debug='yes'"><xsl:message> - DEPTH EXCEEDED : <xsl:value-of select="@schemaLocation"/></xsl:message></xsl:if></xsl:when>
				<xsl:when test="$depth &gt; 2 and(contains($includes,concat('|',@schemaLocation)))"><xsl:if test="$verbose='yes'"><xsl:message> - DUPLICATE INCLUDE IGNORED : <xsl:value-of select="@schemaLocation"/></xsl:message></xsl:if></xsl:when>
				<xsl:when test="not(contains(@schemaLocation,'..')) and(not(contains(@schemaLocation,'\')) and(not(contains(@schemaLocation,'/'))))"><!-- no relative path provided - so look in current directory -->
					<xsl:if test="$debug='yes'"><xsl:message> - from current : <xsl:value-of select="concat($defaultPath,@schemaLocation)"/></xsl:message></xsl:if>
					<xsl:variable name="included" select="document(concat($defaultPath,@schemaLocation))"/>
					<xsl:variable name="more-includes-list">
						<xsl:for-each select="$included//xsd:include/@schemaLocation | include/@schemaLocation">|<xsl:value-of select="."/></xsl:for-each>
					</xsl:variable>
					<xsl:apply-templates select="$included" mode="schema">
						<xsl:with-param name="currentPath" select="$currentPath"/>
						<xsl:with-param name="imports" select="$imports"/>
						<xsl:with-param name="includes" select="concat($includes,'|',@schemaLocation,$more-includes-list)"/>
						<xsl:with-param name="parent" select="if(string-length($includePath) &gt; 0) then substring-after(@schemaLocation,$includePath) else @schemaLocation"/>
						<xsl:with-param name="depth" select="$nextDepth"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:when test="string-length($chksubPath) &gt; 0 and(contains(@schemaLocation,$chksubPath))">
					<xsl:if test="$verbose='yes'"><xsl:message> - from sub sub path : <xsl:value-of select="concat($sub-sub-path,@schemaLocation)"/></xsl:message></xsl:if>
					<xsl:variable name="included" select="document(concat($path,$defaultPath,@schemaLocation))"/>
					<xsl:variable name="more-includes-list">
						<xsl:for-each select="$included//xsd:include/@schemaLocation | include/@schemaLocation">|<xsl:value-of select="."/></xsl:for-each>
					</xsl:variable>
					<xsl:apply-templates select="$included" mode="schema">
						<xsl:with-param name="currentPath" select="$sub-sub-path"/>
						<xsl:with-param name="imports" select="$imports"/>
						<xsl:with-param name="includes" select="concat($includes,'|',@schemaLocation,$more-includes-list)"/>
						<xsl:with-param name="parent" select="if(string-length($includePath) &gt; 0) then substring-after(@schemaLocation,$includePath) else @schemaLocation"/>
						<xsl:with-param name="depth" select="xsd:integer($depth) + 1"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:when test="string-length($chkPath) &gt; 0 and(not(contains(@schemaLocation,$chkPath)))">
					<xsl:if test="$verbose='yes'"><xsl:message> - from sub path : <xsl:value-of select="concat($sub-path,@schemaLocation)"/></xsl:message></xsl:if>
					<xsl:variable name="included" select="document(concat($path,$defaultPath,@schemaLocation))"/>
					<xsl:variable name="more-includes-list">
						<xsl:for-each select="$included//xsd:include/@schemaLocation | include/@schemaLocation">|<xsl:value-of select="."/></xsl:for-each>
					</xsl:variable>
					<xsl:apply-templates select="$included" mode="schema">
						<xsl:with-param name="currentPath" select="$sub-path"/>
						<xsl:with-param name="imports" select="$imports"/>
						<xsl:with-param name="includes" select="concat($includes,'|',@schemaLocation,$more-includes-list)"/>
						<xsl:with-param name="parent" select="if(string-length($includePath) &gt; 0) then substring-after(@schemaLocation,$includePath) else @schemaLocation"/>
						<xsl:with-param name="depth" select="xsd:integer($depth) + 1"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$debug='yes'"><xsl:message> - from path : <xsl:value-of select="concat($defaultPath,@schemaLocation)"/></xsl:message></xsl:if>
					<xsl:variable name="included" select="document(concat($defaultPath,@schemaLocation))"/>
					<xsl:variable name="more-includes-list">
						<xsl:for-each select="$included//xsd:include/@schemaLocation | include/@schemaLocation">|<xsl:value-of select="."/></xsl:for-each>
					</xsl:variable>
					<xsl:apply-templates select="$included" mode="schema">
						<xsl:with-param name="currentPath" select="w2e:extractPath(concat($defaultPath,@schemaLocation))"/>
						<xsl:with-param name="imports" select="$imports"/>
						<xsl:with-param name="includes" select="concat($includes,'|',@schemaLocation,$more-includes-list)"/>
						<xsl:with-param name="parent" select="if(string-length($includePath) &gt; 0) then substring-after(@schemaLocation,$includePath) else @schemaLocation"/>
						<xsl:with-param name="depth" select="xsd:integer($depth) + 1"/>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>

	</xsl:template>

	<xsl:template match="*" mode="schema"><!-- goes here for all of schema content details -->
		<xsl:param name="currentPath"/>
		<xsl:param name="imports"/>
		<xsl:param name="includes"/>
		<xsl:param name="parent"/>
		<xsl:param name="depth"/>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="schema"/>
			<xsl:apply-templates select="*" mode="schema">
				<xsl:with-param name="currentPath" select="$currentPath"/>
				<xsl:with-param name="imports" select="$imports"/>
				<xsl:with-param name="includes" select="$includes"/>
				<xsl:with-param name="parent" select="$parent"/>
				<xsl:with-param name="depth" select="$depth"/>
			</xsl:apply-templates>
			<xsl:apply-templates select="text()[1]" mode="schema"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*" mode="schema">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<xsl:function name="w2e:extractPath"><!-- Gets file path from full file name path -->
		<xsl:param name="fileLocation"/>
		<xsl:variable name="componentsCount" select="count(if(contains($fileLocation,'/')) then tokenize($fileLocation,'/') else tokenize($fileLocation,'\\'))"/>
		<xsl:variable name="resultPath">
			<xsl:for-each select="if(contains($fileLocation,'/')) then tokenize($fileLocation,'/') else tokenize($fileLocation,'\\')"><xsl:if test="position() &lt; $componentsCount"><xsl:value-of select="."/>/</xsl:if></xsl:for-each>
		</xsl:variable>
		<xsl:value-of select="$resultPath"/><!-- return path value -->
	</xsl:function>
	
	<!-- These are not used any more - but left here in case needed in future -->
	<!-- xsl:variable name="duplicate-includes">
		<xsl:apply-templates select="$schema//xsd:include | include" mode="duplicates"/>
		</xsl:variable -->
	<!-- xsl:message>[<xsl:copy-of select="$duplicate-includes"/>]</xsl:message -->
	
	<!-- xsl:template match="xsd:include | include" mode="duplicates">
		<xsl:element name="includeOf">
			<xsl:attribute name="schema" select="@schemaLocation"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="*" mode="duplicates">
		<xsl:apply-templates select="*" mode="duplicates"/>
	</xsl:template -->
	
</xsl:stylesheet>