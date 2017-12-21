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
package org.eclipse.winery.repository.resources.entitytypes.policytypes;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.winery.model.tosca.TPolicyType;

import com.sun.jersey.api.view.Viewable;

public class AppliesToResource {
	
	private PolicyTypeResource policyTypeResource;
	
	
	public AppliesToResource(PolicyTypeResource policyTypeResource) {
		this.policyTypeResource = policyTypeResource;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable getHTML() {
		return new Viewable("/jsp/entitytypes/policytypes/appliesto.jsp", this);
	}
	
	public TPolicyType getPolicyType() {
		return this.policyTypeResource.getPolicyType();
	}
	
}
