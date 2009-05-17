<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<html>
			<body>
				<h1>RWS Inventory</h1>
				<h2>
					Date:
					<xsl:value-of select="rws-nbinventoryreport/dateInventory" />
				</h2>
				<h2>
					Total groups:
					<xsl:value-of select="rws-nbinventoryreport/totalGroups" />
				</h2>
				<xsl:for-each select="rws-nbinventoryreport/groupSet">
					<h2>
						Group:
						<xsl:value-of select="nbisinglenode/groupname" />
					</h2>
					<h3>
						Total nodes:
						<xsl:value-of select="totalNodes" />
					</h3>
					<xsl:for-each select="nbisinglenode">
						<table border="1" width="1000">
							<tr>
								<th>Group Name</th>
								<th>Device Name</th>
								<th>Configuration</th>
								<th>Creation Date</th>
								<th>Status</th>
								<th>Software configuration</th>
								<th>Version</th>
								<th>Comment</th>
							</tr>
							<tr>
								<td>
									<xsl:value-of select="groupname" />
								</td>
								<td>
									<xsl:value-of select="devicename" />
								</td>
								<td>
									<xsl:value-of select="configurationurl" />
								</td>
								<td>
									<xsl:value-of select="creationdate" />
								</td>
								<td>
									<xsl:value-of select="status" />
								</td>
								<td>
									<xsl:value-of select="swconfigurationurl" />
								</td>
								<td>
									<xsl:value-of select="version" />
								</td>
								<td>
									<xsl:value-of select="comment" />
								</td>
							</tr>
						</table>
						<table border="1" width="1000">
							<xsl:for-each select="inventoryElement2RP">
								<tr>
									<th> Inventory Element </th>
								</tr>
								<xsl:choose>
									<xsl:when test="inventoryMemoryRP/type">
										<tr>
											<th>Memory Type</th>
											<th>Size</th>
										</tr>
										<xsl:for-each select="inventoryMemoryRP">
											<tr>
												<td>
													<xsl:value-of select="type" />
												</td>
												<td>
													<xsl:value-of select="size" />
												</td>
											</tr>
										</xsl:for-each>
									</xsl:when>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="inventorySoftwareRP/type">
										<tr>
											<th>Software </th>
											<th>Type</th>
										</tr>
										<xsl:for-each select="inventorySoftwareRP">
											<tr>
												<td>
													<xsl:value-of select="type" />
												</td>
												<td>
													<xsl:value-of select="version" />
												</td>
											</tr>
										</xsl:for-each>
									</xsl:when>
								</xsl:choose>
								<tr>
									<th>Item Name </th>
									<th>Description </th>
								</tr>
								<xsl:for-each select="tupleRP">
									<tr>
										<td>
											<xsl:value-of select="name" />
										</td>
										<td>
											<xsl:value-of select="description" />
										</td>
									</tr>
								</xsl:for-each>

							</xsl:for-each>
						</table>
					</xsl:for-each>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>
