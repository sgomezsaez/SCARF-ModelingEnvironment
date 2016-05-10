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
package org.eclipse.winery.repository.resources.servicetemplates.boundarydefinitions.policies;

import java.util.List;

import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import org.eclipse.winery.model.tosca.TPolicy;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources._support.collections.withoutid.EntityWithoutIdCollectionResource;

import com.sun.jersey.api.view.Viewable;

public class PoliciesResource extends EntityWithoutIdCollectionResource<PolicyResource, TPolicy> {
	
	public PoliciesResource(List<TPolicy> list, IPersistable res) {
		super(PolicyResource.class, TPolicy.class, list, res);
	}
	
	@Override
	public Viewable getHTML() {
		throw new IllegalStateException("Not required: boundarydefinitions.jsp also includes the content of the Policy tab.");
	}
	
	@PUT
	public Response replaceAll(List<TPolicy> newList) {
		this.list.clear();
		for (TPolicy policy : newList) {
			this.list.add(policy);
		}
		return BackendUtils.persist(this.res);
	}
	
}
