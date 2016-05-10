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
package org.eclipse.winery.repository.resources;

import java.util.List;

import org.eclipse.winery.model.tosca.TConstraint;
import org.eclipse.winery.repository.resources._support.collections.withoutid.EntityWithoutIdCollectionResource;
import org.eclipse.winery.repository.resources.entitytypes.nodetypes.NodeTypeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.view.Viewable;

public class ConstraintsResource extends EntityWithoutIdCollectionResource<ConstraintResource, TConstraint> {
	
	private static final Logger logger = LoggerFactory.getLogger(ConstraintsResource.class);
	
	
	public ConstraintsResource(List<TConstraint> constraints, NodeTypeResource res) {
		super(ConstraintResource.class, TConstraint.class, constraints, res);
	}
	
	@Override
	public Viewable getHTML() {
		// TODO Auto-generated method stub
		throw new IllegalStateException("Not yet implemented.");
	}
	
}
