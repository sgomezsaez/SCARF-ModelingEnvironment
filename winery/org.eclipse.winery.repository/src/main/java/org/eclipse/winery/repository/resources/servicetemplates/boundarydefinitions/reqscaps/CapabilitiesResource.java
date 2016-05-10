/*******************************************************************************
 * Copyright (c) 2014 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.reqscaps;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.winery.common.ModelUtilities;
import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TCapabilityRef;
import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources._support.collections.CollectionsHelper;
import org.eclipse.winery.repository.resources._support.collections.withoutid.EntityWithoutIdCollectionResource;
import org.eclipse.winery.repository.resources.servicetemplates.ServiceTemplateResource;

import com.sun.jersey.api.view.Viewable;

/**
 * This class is an adaption from
 * {@link org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.reqscaps.RequirementsResource}
 */
public class CapabilitiesResource extends EntityWithoutIdCollectionResource<CapabilityResource, TCapabilityRef> {
	
	public CapabilitiesResource(IPersistable res, List<TCapabilityRef> refs) {
		super(CapabilityResource.class, TCapabilityRef.class, refs, res);
	}
	
	@Override
	public Viewable getHTML() {
		throw new IllegalStateException("Not yet required: boundarydefinitions.jsp renders all tab content.");
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addNewElement(@FormParam("name") String name, @FormParam("ref") String reference) {
		// Implementation adapted from super addNewElement
		
		if (reference == null) {
			return Response.status(Status.BAD_REQUEST).entity("A reference has to be provided").build();
		}
		
		TCapabilityRef ref = new TCapabilityRef();
		ref.setName(name); // may also be null
		
		// The XML model fordces us to put a reference to the object and not just the string
		ServiceTemplateResource rs = (ServiceTemplateResource) this.res;
		TCapability resolved = ModelUtilities.resolveCapability(rs.getServiceTemplate(), reference);
		// In case nothing was found: report back to the user
		if (resolved == null) {
			return Response.status(Status.BAD_REQUEST).entity("Reference could not be resolved").build();
		}
		
		ref.setRef(resolved);
		
		// "this.alreadyContains(ref)" cannot be called as this leads to a mappable exception: The data does not contain an id where the given ref attribute may point to
		
		this.list.add(ref);
		return CollectionsHelper.persist(this.res, this, ref);
	}
	
}
