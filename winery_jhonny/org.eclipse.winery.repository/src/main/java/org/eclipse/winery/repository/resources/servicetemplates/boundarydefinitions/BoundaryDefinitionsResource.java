/*******************************************************************************
 * Copyright (c) 2013-2014 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.winery.common.ModelUtilities;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Capabilities;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Interfaces;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Policies;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties.PropertyMappings;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Requirements;
import org.eclipse.winery.model.tosca.TCapabilityRef;
import org.eclipse.winery.model.tosca.TRequirementRef;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.resources.servicetemplates.ServiceTemplateResource;
import org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.interfaces.InterfacesResource;
import org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.policies.PoliciesResource;
import org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.reqscaps.CapabilitiesResource;
import org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.reqscaps.RequirementsResource;
import org.restdoc.annotations.RestDoc;
import org.restdoc.annotations.RestDocParam;
import org.w3c.dom.Document;

import com.sun.jersey.api.view.Viewable;

public class BoundaryDefinitionsResource {
	
	private final ServiceTemplateResource serviceTemplateResource;
	private final TBoundaryDefinitions boundaryDefinitions;
	
	
	public BoundaryDefinitionsResource(ServiceTemplateResource serviceTemplateResource, TBoundaryDefinitions boundaryDefinitions) {
		this.serviceTemplateResource = serviceTemplateResource;
		this.boundaryDefinitions = boundaryDefinitions;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable getHTML(@Context UriInfo uriInfo) {
		return new Viewable("/jsp/servicetemplates/boundarydefinitions/boundarydefinitions.jsp", new BoundaryDefinitionsJSPData(this.serviceTemplateResource.getServiceTemplate(), uriInfo.getBaseUri()));
	}
	
	@PUT
	@RestDoc(methodDescription = "Replaces the boundary definitions by the information given in the XML")
	@Consumes(MediaType.TEXT_XML)
	public Response setModel(TBoundaryDefinitions boundaryDefinitions) {
		this.serviceTemplateResource.getServiceTemplate().setBoundaryDefinitions(boundaryDefinitions);
		return BackendUtils.persist(this.serviceTemplateResource);
	}
	
	@Path("properties/")
	@PUT
	@Consumes(MediaType.TEXT_XML)
	@RestDoc(resourceDescription = "Models the user-defined properties. The property mappings go into a separate resource propertymappings.")
	public Response putProperties(@RestDocParam(description = "Stored properties. The XSD allows a single element only. Therefore, we go for the contained element") Document doc) {
		org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties properties = ModelUtilities.getProperties(this.boundaryDefinitions);
		properties.setAny(doc.getDocumentElement());
		return BackendUtils.persist(this.serviceTemplateResource);
	}
	
	@Path("requirements/")
	public RequirementsResource getRequiremensResource() {
		Requirements requirements = this.boundaryDefinitions.getRequirements();
		if (requirements == null) {
			requirements = new Requirements();
			this.boundaryDefinitions.setRequirements(requirements);
		}
		List<TRequirementRef> refs = requirements.getRequirement();
		return new RequirementsResource(this.serviceTemplateResource, refs);
	}
	
	@Path("capabilities/")
	public CapabilitiesResource getCapabilitiesResource() {
		Capabilities caps = this.boundaryDefinitions.getCapabilities();
		if (caps == null) {
			caps = new Capabilities();
			this.boundaryDefinitions.setCapabilities(caps);
		}
		List<TCapabilityRef> refs = caps.getCapability();
		return new CapabilitiesResource(this.serviceTemplateResource, refs);
	}
	
	@Path("policies/")
	public PoliciesResource getPoliciesResource() {
		Policies policies = this.boundaryDefinitions.getPolicies();
		if (policies == null) {
			policies = new Policies();
			this.boundaryDefinitions.setPolicies(policies);
		}
		return new PoliciesResource(policies.getPolicy(), this.serviceTemplateResource);
	}
	
	/**
	 * This path is below "boundary definitions" to ease implementation If it
	 * was modeled following the XSD, it would have been nested below
	 * "properties". We did not do that
	 */
	@Path("propertymappings/")
	public PropertyMappingsResource getPropertyMappings() {
		Properties properties = this.boundaryDefinitions.getProperties();
		if (properties == null) {
			properties = new Properties();
			this.boundaryDefinitions.setProperties(properties);
		}
		PropertyMappings propertyMappings = properties.getPropertyMappings();
		if (propertyMappings == null) {
			propertyMappings = new PropertyMappings();
			properties.setPropertyMappings(propertyMappings);
		}
		return new PropertyMappingsResource(propertyMappings, this.serviceTemplateResource);
	}
	
	@Path("interfaces/")
	public InterfacesResource getInterfacesResource() {
		Interfaces interfaces = this.boundaryDefinitions.getInterfaces();
		if (interfaces == null) {
			interfaces = new Interfaces();
			this.boundaryDefinitions.setInterfaces(interfaces);
		}
		return new InterfacesResource(interfaces.getInterface(), this.serviceTemplateResource);
	}
}
