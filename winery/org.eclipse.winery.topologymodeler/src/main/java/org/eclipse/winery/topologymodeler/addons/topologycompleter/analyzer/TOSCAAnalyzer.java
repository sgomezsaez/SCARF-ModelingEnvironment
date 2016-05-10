/*******************************************************************************
 * Copyright (c) 2013 Pascal Hirmer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Pascal Hirmer - initial API and implementation
 *******************************************************************************/

package org.eclipse.winery.topologymodeler.addons.topologycompleter.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TRelationshipTemplate;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.model.tosca.TRequirement;
import org.eclipse.winery.model.tosca.TRequirementType;
import org.eclipse.winery.model.tosca.TTopologyTemplate;

/**
 * This class contains several methods to analyze the content of a TOSCA {@link TTopologyTemplate} and to fill a data model
 * with the analyzed information. This class serves the access to all types and templates of a topology.
 */
public class TOSCAAnalyzer {

	// lists containing the elements of a topology
	List<TNodeTemplate>             nodeTemplates = new ArrayList<TNodeTemplate>();
	List<TRelationshipTemplate>     relationshipTemplates = new ArrayList<TRelationshipTemplate>();
	List<TRequirement>              requirements = new ArrayList<TRequirement>();

	List<TNodeType>                 nodeTypes;
	List<TRelationshipType>         relationshipTypes;
	List<TRequirementType>          requirementTypes;

	/**
	 * This method analyzes the TOSCA {@link TTopologyTemplate} for {@link TNodeTemplate}s, {@link TRelationshipTemplate}s
	 * and existing {@link TRequirement}s and adds them to a list.
	 *
	 * @param topology
	 *            the TOSCA {@link TTopologyTemplate}
	 */
	public void analyzeTOSCATopology(TTopologyTemplate topology) {

		// fill the data model with content of the topology
		List<TEntityTemplate> templateNodes = topology.getNodeTemplateOrRelationshipTemplate();

		for (TEntityTemplate entityTemplate : templateNodes) {
			if (entityTemplate instanceof TNodeTemplate) {
				// add the node templates and their requirements to the data model
				nodeTemplates.add((TNodeTemplate) entityTemplate);
				if (((TNodeTemplate) entityTemplate).getRequirements() != null) {
					requirements.addAll(((TNodeTemplate) entityTemplate).getRequirements().getRequirement());
				}
			} else if (entityTemplate instanceof TRelationshipTemplate) {
				// add RelationshipTemplates
				relationshipTemplates.add((TRelationshipTemplate) entityTemplate);
			}
		}
	}

	/**
	 * Setter for the types received from the Winery repository.
	 *
	 * @param nodeTypeXMLStrings
	 *            a list of {@link TNodeType}s from the Winery repository
	 * @param relationshipTypeXMLStrings
	 *            a list of {@link TRelationshipType}s from the Winery repository
	 * @param requirementTypeList
	 *            a list of {@link TRequirementType}s from the Winery repository
	 */
	public void setTypes(List<TNodeType> nodeTypes, List<TRelationshipType> relationshipTypes, List<TRequirementType> requirementTypes) {
		this.nodeTypes = nodeTypes;
		this.relationshipTypes = relationshipTypes;
		this.requirementTypes = requirementTypes;
	}

	/**
	 * Returns the {@link TNodeTemplate}s of the topology.
	 *
	 * @return the {@link TNodeTemplate}s as a list
	 */
	public List<TNodeTemplate> getNodeTemplates() {
		return nodeTemplates;
	}

	/**
	 * Returns the {@link TRelationshipTemplate}s of the topology.
	 *
	 * @return the {@link TRelationshipTemplate}s as a list
	 */
	public List<TRelationshipTemplate> getRelationshipTemplates() {
		return relationshipTemplates;
	}

	/**
	 * Returns the {@link TRequirement}s of the topology.
	 *
	 * @return the {@link TRequirement}s as a list
	 */
	public List<TRequirement> getRequirements() {
		return requirements;
	}

	/**
	 * Returns the {@link TRelationshipType}s of the topology.
	 *
	 * @return the {@link TRelationshipType}s as a list
	 */
	public List<TRelationshipType> getRelationshipTypes() {
		return relationshipTypes;
	}

	/**
	 * Returns the {@link TNodeType}s of the topology.
	 *
	 * @return the {@link TNodeType}s as a list
	 */
	public List<TNodeType> getNodeTypes() {
		return nodeTypes;
	}

	/**
	 * Returns the {@link TRequirementType}s of the topology.
	 *
	 * @return the {@link TRequirementType}s as a list
	 */
	public List<TRequirementType> getRequirementTypes() {
		return requirementTypes;
	}

	/**
	 * Clears all the templates from the data model before the analysis of a topology is restarted.
	 */
	public void clear() {
		nodeTemplates.clear();
		relationshipTemplates.clear();
		requirements.clear();
	}
}
