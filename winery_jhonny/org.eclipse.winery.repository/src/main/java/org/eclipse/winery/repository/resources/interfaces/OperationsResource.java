/*******************************************************************************
 * Copyright (c) 2012-2013 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.interfaces;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.model.tosca.TOperation;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources._support.collections.withid.EntityWithIdCollectionResource;
import org.restdoc.annotations.RestDocParam;

import com.sun.jersey.api.view.Viewable;

public class OperationsResource extends EntityWithIdCollectionResource<OperationResource, TOperation> {
	
	public OperationsResource(List<TOperation> list, IPersistable res) {
		super(OperationResource.class, TOperation.class, list, res);
	}
	
	@Override
	public String getId(TOperation entity) {
		return entity.getName();
	}
	
	@Override
	public Viewable getHTML() {
		throw new IllegalStateException("Not yet implemented.");
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createOperation(@FormParam("name") @RestDocParam(description = "used as name and id") String operationName) {
		if (StringUtils.isEmpty(operationName)) {
			return Response.status(Status.BAD_REQUEST).entity("operationName not provided").build();
		}
		
		operationName = Util.URLdecode(operationName);
		
		// TODO: check for duplicates as in instance states
		
		TOperation operation = new TOperation();
		operation.setName(operationName);
		this.list.add(operation);
		
		return BackendUtils.persist(this.res);
	}
	
}
