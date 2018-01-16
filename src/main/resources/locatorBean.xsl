<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<body>
				<h2>Web Locators Grouped By Pages</h2>
				<xsl:for-each select="locatorBean/page">
					<h3>
						Page
						<xsl:value-of select="@name"/>
					</h3>
					<table border="1">
						<tr bgcolor="#00CD00">
							<th align="left">id</th>
							<th align="left">name</th>
							<th align="left">description</th>
						</tr>
						<tr>
							<td>
								<xsl:value-of select="@id"/>
							</td>
							<td>
								<xsl:value-of select="@name"/>
							</td>
							<td>
								<xsl:value-of select="@description"/>
							</td>
						</tr>
					</table>
					<h3>
						Page
						<xsl:value-of select="@name"/>
						locators
					</h3>
					<table border="1">
						<tr bgcolor="#EE9A00">
							<th align="left">id</th>
							<th align="left">ref</th>
							<th align="left">type</th>
							<th align="left">by</th>
							<th align="left">expression</th>
							<th align="left">name</th>
							<th align="left">description</th>
						</tr>
						<xsl:for-each select="current()/locator">
						<!--<xsl:sort select="@id"/>
						<xsl:sort select="@type"/>-->
							<tr>
								<td>
									<xsl:value-of select="@id"/>
								</td>
								<td>
									<xsl:value-of select="@ref"/>
								</td>
								<td>
									<xsl:value-of select="@type"/>
								</td>
								<td>
									<xsl:value-of select="@by"/>
								</td>
								<td>
									<xsl:value-of select="@expression"/>
								</td>
								<td>
									<xsl:value-of select="@name"/>
								</td>
								<td>
									<xsl:value-of select="@description"/>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
