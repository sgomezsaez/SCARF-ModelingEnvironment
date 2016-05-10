/*******************************************************************************
 * Copyright (c) 2012-2015 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.servicetemplates.plans;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.common.ids.XMLId;
import org.eclipse.winery.common.ids.definitions.ServiceTemplateId;
import org.eclipse.winery.common.ids.definitions.TOSCAComponentId;
import org.eclipse.winery.common.ids.elements.PlanId;
import org.eclipse.winery.common.ids.elements.PlansId;
import org.eclipse.winery.model.tosca.TPlan;
import org.eclipse.winery.model.tosca.TPlan.InputParameters;
import org.eclipse.winery.model.tosca.TPlan.OutputParameters;
import org.eclipse.winery.repository.Prefs;
import org.eclipse.winery.repository.Utils;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.resources.IHasName;
import org.eclipse.winery.repository.resources._support.collections.IIdDetermination;
import org.eclipse.winery.repository.resources._support.collections.withid.EntityWithIdResource;
import org.eclipse.winery.repository.resources.interfaces.ParametersResource;
import org.eclipse.winery.repository.resources.servicetemplates.ServiceTemplateResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does <em>not</em> implement
 * {@link org.eclipse.winery.repository.resources.IHasTypeReference}, because
 * the type of a plan is outside the system of TOSCA.
 */
public class PlanResource extends EntityWithIdResource<TPlan> implements IHasName {
	
	private static final Logger logger = LoggerFactory.getLogger(PlanResource.class);
	
	
	public PlanResource(IIdDetermination<TPlan> idDetermination, TPlan o, int idx, List<TPlan> list, ServiceTemplateResource res) {
		super(idDetermination, o, idx, list, res);
	}
	
	/**
	 * Ugly hack to get the parent service template resource
	 * 
	 */
	public ServiceTemplateResource getServiceTemplateResource() {
		// Solution proposal 1: Each sub-resource should know its parent service
		// template
		//
		// Solution proposal 2 (Generic solution): Each resource should know its
		// parent resource
		//
		// Does not work when plan is used at as component instance (then,
		// serviceTemplateResource is null). In this case, a plan is not associated
		// with a service template.
		
		// we cannot use "((PlanId) id).getParent()" as this "only" returns an
		// ID
		// we could create a newly resource based on that ID
		// However, the parent resource has already been created when the
		// PlanResource has been generated:
		// Jersey crawls down from the main resource through the service
		// template resource to the plan resource
		return (ServiceTemplateResource) this.res;
	}
	
	/**
	 * Determines the id of the current resource
	 */
	private PlanId getId() {
		ServiceTemplateId sId = (ServiceTemplateId) this.getServiceTemplateResource().getId();
		PlansId psId = new PlansId(sId);
		PlanId pId = new PlanId(psId, new XMLId(this.o.getId(), false));
		return pId;
	}
	
	@Override
	@DELETE
	public Response onDelete() {
		Response res = super.onDelete();
		if (Utils.isSuccessFulResponse(res)) {
			try {
				Repository.INSTANCE.forceDelete(this.getId());
			} catch (IOException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not remove plan file").build();
			}
			return BackendUtils.persist(this.res);
		} else {
			return res;
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getHTML(@Context UriInfo uriInfo) {
		boolean isBPMN4TOSCA = this.o.getPlanLanguage().equals(org.eclipse.winery.common.constants.Namespaces.URI_BPMN4TOSCA_20);
		String bpmn4toscaBaseURL = Prefs.INSTANCE.getBPMN4TOSCABaseURL();
		if (isBPMN4TOSCA && (!StringUtils.isEmpty(bpmn4toscaBaseURL))) {
			String uri = bpmn4toscaBaseURL;
			URI repositoryURI = uriInfo.getBaseUri();
			uri += "?repositoryURL=" + Util.URLencode(repositoryURI.toString());
			TOSCAComponentId serviceTemplateId = this.getServiceTemplateResource().getId();
			uri += "&namespace=" + serviceTemplateId.getNamespace().getEncoded();
			uri += "&id=" + serviceTemplateId.getXmlId().getEncoded();
			uri += "&plan=" + this.getName();
			return Response.temporaryRedirect(Utils.createURI(uri)).build();
		} else {
			// return Response.ok().entity("No editor plugin found for plan language " + this.o.getPlanLanguage()).build();
			URI fileURI = uriInfo.getAbsolutePath().resolve("file");
			return Response.seeOther(fileURI).build();
		}
	}
	
	@Override
	public String getName() {
		String name = this.o.getName();
		if (name == null) {
			name = this.o.getId();
		}
		return name;
	}
	
	@Override
	public Response setName(@FormParam("value") String name) {
		this.o.setName(name);
		return BackendUtils.persist(this.res);
	}
	
	@Path("file")
	public PlanFileResource getPlanFileResource() {
		return new PlanFileResource((ServiceTemplateResource) this.res, this.getId(), this.o);
	}
	
	@GET
	@Path("type")
	public String getType() {
		return this.o.getPlanType();
	}
	
	@PUT
	@Path("type")
	public Response setType(@FormParam("type") String type) {
		this.o.setPlanType(type);
		return BackendUtils.persist(this.res);
	}
	
	@GET
	@Path("language")
	public String getLanguage() {
		return this.o.getPlanLanguage();
	}
	
	@PUT
	@Path("language")
	public Response setLanguage(@FormParam("language") String language) {
		this.o.setPlanType(language);
		return BackendUtils.persist(this.res);
	}
	
	@Path("inputparameters/")
	public ParametersResource getInputParametersResource() {
		InputParameters inputParameters = this.o.getInputParameters();
		if (inputParameters == null) {
			inputParameters = new InputParameters();
			this.o.setInputParameters(inputParameters);
		}
		return new ParametersResource(inputParameters.getInputParameter(), this.getServiceTemplateResource());
	}
	
	@Path("outputparameters/")
	public ParametersResource getOutputParametersResource() {
		OutputParameters outputParameters = this.o.getOutputParameters();
		if (outputParameters == null) {
			outputParameters = new OutputParameters();
			this.o.setOutputParameters(outputParameters);
		}
		return new ParametersResource(outputParameters.getOutputParameter(), this.getServiceTemplateResource());
	}
}
