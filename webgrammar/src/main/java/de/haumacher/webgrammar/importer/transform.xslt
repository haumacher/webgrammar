<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:ext="xalan://de.haumacher.webgrammar.importer.XSLTFunctions"
	exclude-result-prefixes="#default ext" 
>
	<xsl:output method="text" encoding="utf-8"/>

	<xsl:template match="/">
		<xsl:apply-templates select=".//xhtml:table[@class='grammar']"/>
	</xsl:template>
	
	<xsl:template match="xhtml:table">
		<xsl:apply-templates select=".//xhtml:tr"/>
	</xsl:template>
	
	<xsl:template match="xhtml:tr">
		<xsl:if test="@id or count(./xhtml:td[@id]) &gt; 0">
			<xsl:text>// Rule </xsl:text>
			<xsl:value-of select="ext:normalize(string(.//xhtml:span[@class='prod-number']))"/>
			<!-- 
			<xsl:text> #</xsl:text>
			<xsl:value-of select="@id"/>
			 -->
			<xsl:text>&#x0A;</xsl:text>
			
			<xsl:apply-templates select=".//xhtml:td"/>
			
			<xsl:text>&#x0A;</xsl:text>
			<xsl:text>&#x09;&#x09;</xsl:text>
			<xsl:text>;</xsl:text>
			<xsl:text>&#x0A;</xsl:text>
			<xsl:text>&#x0A;</xsl:text>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="xhtml:td">
		<xsl:choose>
			<xsl:when test="@class='regex'">
				<xsl:value-of select="ext:regexp(string(.))"/>
				<xsl:text>&#x09;</xsl:text>
			</xsl:when>
			<xsl:when test="count(.//xhtml:span[@class='prod-number']) = 0">
				<xsl:value-of select="ext:normalize(string(.))"/>
				<xsl:text>&#x09;</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	
</xsl:stylesheet>
