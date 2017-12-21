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
import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.eclipse.winery.common.ids.definitions.RequirementTypeId;
import org.eclipse.winery.model.tosca.TRequirementDefinition;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.resources.entitytypes.nodetypes.NodeTypeResource;

import com.sun.jersey.api.view.Viewable;

public class RequirementDefinitionsResource extends RequirementOrCapabilityDefinitionsResource<RequirementDefinitionResource, TRequirementDefinition> {
	
	public RequirementDefinitionsResource(NodeTypeResource res, List<TRequirementDefinition> defs) {
		super(RequirementDefinitionResource.class, TRequirementDefinition.class, defs, res);
	}
	
	@Override
	public Viewable getHTML() {
		return new Viewable("/jsp/entitytypes/nodetypes/reqandcapdefs/reqdefs.jsp", this);
	}
	
	@Override
	public Collection<QName> getAllTypes() {
		SortedSet<RequirementTypeId> allTOSCAComponentIds = Repository.INSTANCE.getAllTOSCAComponentIds(RequirementTypeId.class);
		return BackendUtils.convertTOSCAComponentIdCollectionToQNameCollection(allTOSCAComponentIds);
	}
	
}
