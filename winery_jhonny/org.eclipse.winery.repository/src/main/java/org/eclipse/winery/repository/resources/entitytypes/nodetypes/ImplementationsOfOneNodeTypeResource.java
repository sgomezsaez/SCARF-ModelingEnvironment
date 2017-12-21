/*******************************************************************************
 * Copyright (c) 2012-2013,2015 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.entitytypes.nodetypes;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;

import org.eclipse.winery.common.ids.definitions.NodeTypeId;
import org.eclipse.winery.common.ids.definitions.NodeTypeImplementationId;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.resources.entitytypes.ImplementationsOfOneType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class ImplementationsOfOneNodeTypeResource extends ImplementationsOfOneType {
	
	private static final Logger logger = LoggerFactory.getLogger(ImplementationsOfOneNodeTypeResource.class);
	
	
	/**
	 * The constructor is different from the usual constructors as this resource
	 * does NOT store own data, but retrieves its data solely from the
	 * associated node type
	 * 
	 * @param nodeTypeId the node type id, where this list of implementations
	 *            belongs to
	 */
	public ImplementationsOfOneNodeTypeResource(NodeTypeId nodeTypeId) {
		super(nodeTypeId);
	}
	
	/**
	 * required by implementations.jsp
	 * 
	 * @return for each node type implementation implementing the associated
	 *         node type
	 */
	@Override
	public String getImplementationsTableData() {
		String res;
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter tableDataSW = new StringWriter();
		try {
			JsonGenerator jGenerator = jsonFactory.createGenerator(tableDataSW);
			jGenerator.writeStartArray();
			
			Collection<NodeTypeImplementationId> allNodeTypeImplementations = BackendUtils.getAllElementsRelatedWithATypeAttribute(NodeTypeImplementationId.class, this.getTypeId().getQName());
			for (NodeTypeImplementationId ntiID : allNodeTypeImplementations) {
				jGenerator.writeStartArray();
				jGenerator.writeString(ntiID.getNamespace().getDecoded());
				jGenerator.writeString(ntiID.getXmlId().getDecoded());
				jGenerator.writeEndArray();
			}
			jGenerator.writeEndArray();
			jGenerator.close();
			tableDataSW.close();
			res = tableDataSW.toString();
		} catch (Exception e) {
			ImplementationsOfOneNodeTypeResource.logger.error(e.getMessage(), e);
			res = "[]";
		}
		return res;
	}
	
	@Override
	public String getType() {
		return "nodetype";
	}
	
	@Override
	public String getTypeStr() {
		return "Node Type";
	}
	
	@Override
	public Response getJSON() {
		Collection<NodeTypeImplementationId> allImplementations = BackendUtils.getAllElementsRelatedWithATypeAttribute(NodeTypeImplementationId.class, this.getTypeId().getQName());
		ArrayList<QName> res = new ArrayList<QName>(allImplementations.size());
		for (NodeTypeImplementationId id : allImplementations) {
			res.add(id.getQName());
		}
		return Response.ok().entity(res).build();
	}
	
}
