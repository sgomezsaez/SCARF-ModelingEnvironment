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
package org.eclipse.winery.repository.resources.entitytypes.nodetypes.reqandcapdefs;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.model.tosca.TCapabilityDefinition;
import org.eclipse.winery.model.tosca.TRequirementDefinition;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.resources._support.collections.withid.EntityWithIdCollectionResource;
import org.eclipse.winery.repository.resources.entitytypes.nodetypes.NodeTypeResource;

import com.sun.jersey.api.view.Viewable;

/**
 * This superclass has only a few methods as we cannot easily abstract from the
 * subclasses: We would need Java reflection to invoke "getName" (to get the
 * subresource). The hope is that this copy'n'paste programming will not
 * introduce bugs when changing childs
 * 
 * We try to abstract from the problems by using generics and reflections
 * 
 * @param <ReqDefOrCapDef> TRequirementDefinition or TCapabilityDefinition
 * @param <ReqDefOrCapDefResource> the resource managing ReqDefOrCapDef
 */
public abstract class RequirementOrCapabilityDefinitionsResource<ReqDefOrCapDefResource extends AbstractReqOrCapDefResource<ReqDefOrCapDef>, ReqDefOrCapDef> extends EntityWithIdCollectionResource<ReqDefOrCapDefResource, ReqDefOrCapDef> {
	
	protected final NodeTypeResource res;
	
	
	public RequirementOrCapabilityDefinitionsResource(Class<ReqDefOrCapDefResource> entityResourceTClazz, Class<ReqDefOrCapDef> entityTClazz, List<ReqDefOrCapDef> list, NodeTypeResource res) {
		super(entityResourceTClazz, entityTClazz, list, res);
		this.res = res;
	}
	
	@Override
	public abstract Viewable getHTML();
	
	/**
	 * @return collection of all available types
	 */
	public abstract Collection<QName> getAllTypes();
	
	@POST
	// As there is no supertype of TCapabilityType and TRequirementType containing the common attributes, we have to rely on unchecked casts
	@SuppressWarnings("unchecked")
	public Response onPost(@FormParam("name") String name, @FormParam("type") String type, @FormParam("lowerbound") String lowerBound, @FormParam("upperbound") String upperbound) {
		if (StringUtils.isEmpty(name)) {
			return Response.status(Status.BAD_REQUEST).entity("Name has to be provided").build();
		}
		if (StringUtils.isEmpty(type)) {
			return Response.status(Status.BAD_REQUEST).entity("Type has to be provided").build();
		}
		
		int lbound = 1;
		if (!StringUtils.isEmpty(lowerBound)) {
			try {
				lbound = Integer.parseInt(lowerBound);
			} catch (NumberFormatException e) {
				return Response.status(Status.BAD_REQUEST).entity("Bad format of lowerbound: " + e.getMessage()).build();
			}
		}
		
		String ubound = "1";
		if (!StringUtils.isEmpty(upperbound)) {
			ubound = upperbound;
		}
		
		// we also support replacement of existing requirements
		// therefore, we loop through the existing requirements
		int idx = -1;
		boolean found = false;
		for (ReqDefOrCapDef d : this.list) {
			idx++;
			if (this.getId(d).equals(name)) {
				found = true;
				break;
			}
		}
		
		QName typeQName = QName.valueOf(type);
		// Create object and put type in it
		ReqDefOrCapDef def;
		if (this instanceof CapabilityDefinitionsResource) {
			def = (ReqDefOrCapDef) new TCapabilityDefinition();
			((TCapabilityDefinition) def).setCapabilityType(typeQName);
		} else {
			assert (this instanceof RequirementDefinitionsResource);
			def = (ReqDefOrCapDef) new TRequirementDefinition();
			((TRequirementDefinition) def).setRequirementType(typeQName);
		}
		
		// copy all other data into object
		AbstractReqOrCapDefResource.invokeSetter(def, "setName", name);
		AbstractReqOrCapDefResource.invokeSetter(def, "setLowerBound", lbound);
		AbstractReqOrCapDefResource.invokeSetter(def, "setUpperBound", ubound);
		
		if (found) {
			// replace element
			this.list.set(idx, def);
		} else {
			// add new element
			this.list.add(def);
		}
		
		Response r = BackendUtils.persist(this.res);
		return r;
	}
	
	@Override
	public String getId(ReqDefOrCapDef reqDefOrCapDef) {
		return AbstractReqOrCapDefResource.getName(reqDefOrCapDef);
	}
}
