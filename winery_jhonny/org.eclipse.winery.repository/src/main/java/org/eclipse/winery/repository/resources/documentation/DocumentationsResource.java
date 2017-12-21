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
package org.eclipse.winery.repository.resources.documentation;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.winery.model.tosca.TDocumentation;
import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources._support.collections.CollectionsHelper;
import org.eclipse.winery.repository.resources._support.collections.withoutid.EntityWithoutIdCollectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.view.Viewable;

public class DocumentationsResource extends EntityWithoutIdCollectionResource<DocumentationResource, TDocumentation> {
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentationResource.class);
	
	
	public DocumentationsResource(IPersistable res, List<TDocumentation> documentations) {
		super(DocumentationResource.class, TDocumentation.class, documentations, res);
	}
	
	@Override
	public Viewable getHTML() {
		return new Viewable("/jsp/documentation.jsp", this.list);
	}
	
	/**
	 * Adds a new documentation
	 */
	@POST
	@Consumes(MediaType.TEXT_HTML)
	public Response addNewElement(String documentation) {
		if (documentation == null) {
			return Response.status(Status.BAD_REQUEST).entity("No content provided").build();
		}
		TDocumentation doc = new TDocumentation();
		doc.getContent().add(documentation);
		// TODO: check for duplicates as in instance states
		this.list.add(doc);
		return CollectionsHelper.persist(this.res, this, doc);
	}
}
