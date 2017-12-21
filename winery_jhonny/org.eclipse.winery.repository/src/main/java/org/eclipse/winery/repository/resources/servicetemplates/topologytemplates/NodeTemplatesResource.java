/*******************************************************************************
 * Copyright (c) 2012-2014 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.servicetemplates.topologytemplates;

import java.util.List;

import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources.entitytemplates.TEntityTemplatesResource;

import com.sun.jersey.api.view.Viewable;

public class NodeTemplatesResource extends TEntityTemplatesResource<NodeTemplateResource, TNodeTemplate> {
	
	public NodeTemplatesResource(List<TNodeTemplate> list, IPersistable res) {
		super(NodeTemplateResource.class, TNodeTemplate.class, list, res);
	}
	
	@Override
	public String getId(TNodeTemplate entity) {
		return entity.getId();
	}
	
	@Override
	public Viewable getHTML() {
		// TODO Auto-generated method stub
		throw new IllegalStateException("Not yet implemented.");
	}
	
}
