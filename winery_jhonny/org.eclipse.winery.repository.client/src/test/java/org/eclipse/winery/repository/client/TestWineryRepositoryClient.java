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
package org.eclipse.winery.repository.client;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.model.tosca.TTopologyTemplate;
import org.eclipse.winery.common.ids.definitions.ArtifactTemplateId;
import org.eclipse.winery.common.interfaces.QNameAlreadyExistsException;

/**
 * Tests client methods with a pre-configured client stored in a local static
 * field.
 * 
 * Client creation and multiple repositories are not tested. This should be
 * subject to other test classes.
 * 
 * TODO: This class expects things to be existent in the namespace "test". This
 * should be enforced in a preload.
 */
@RunWith(JUnit4.class)
public class TestWineryRepositoryClient {
	
	// private final String repositoryURI = "http://2471.de:8080/wineydev";
	private static final String repositoryURI = "http://localhost:8080/winery";
	
	private static final boolean USE_PROXY = true;
	
	private static final IWineryRepositoryClient client = new WineryRepositoryClient(TestWineryRepositoryClient.USE_PROXY);
	static {
		TestWineryRepositoryClient.client.addRepository(TestWineryRepositoryClient.repositoryURI);
	}
	
	/**
	 * The namespace to put new things in. <br />
	 * TODO: Is deleted completely after testing
	 */
	private static final String namespaceForNewArtifacts = "http://www.example.org/test/wineryclient/";
	
	
	@Test
	public void getAllNodeTypes() {
		Collection<TNodeType> allTypes = TestWineryRepositoryClient.client.getAllTypes(TNodeType.class);
		for (TNodeType type : allTypes) {
			Assert.assertNotNull("name is null", type.getName());
			Assert.assertNotNull("target namespace is null", type.getTargetNamespace());
		}
	}
	
	@Test
	public void getAllRelationshipTypes() {
		Collection<TRelationshipType> allTypes = TestWineryRepositoryClient.client.getAllTypes(TRelationshipType.class);
		for (TRelationshipType type : allTypes) {
			Assert.assertNotNull("name is null", type.getName());
			Assert.assertNotNull("target namespace is null", type.getTargetNamespace());
		}
	}
	
	@Test
	public void getAllNodeTypesWithAssociatedElements() {
		Collection<TDefinitions> allTypes = TestWineryRepositoryClient.client.getAllTypesWithAssociatedElements(TNodeType.class);
		Assert.assertNotNull(allTypes);
	}
	
	@Test
	public void getAllRelationshipTypesWithAssociatedElements() {
		Collection<TDefinitions> allTypes = TestWineryRepositoryClient.client.getAllTypesWithAssociatedElements(TRelationshipType.class);
		Assert.assertNotNull(allTypes);
	}
	
	@Test
	public void getPropertiesOfAllNodeTypes() {
		// TODO
	}
	
	@Test
	public void getPropertiesOfAllRelationshipTypes() {
		// TODO
	}
	
	@Test
	public void getTestTopologyTemplate() {
		QName serviceTemplate = new QName("test", "test");
		TTopologyTemplate topologyTemplate = TestWineryRepositoryClient.client.getTopologyTemplate(serviceTemplate);
		Assert.assertNotNull(topologyTemplate);
	}
	
	@Test
	public void getPropertiesOfTestTopologyTemplate() {
		QName serviceTemplate = new QName("test", "test");
		TTopologyTemplate topologyTemplate = TestWineryRepositoryClient.client.getTopologyTemplate(serviceTemplate);
		Assert.assertNotNull(topologyTemplate);
		List<TEntityTemplate> allTemplates = topologyTemplate.getNodeTemplateOrRelationshipTemplate();
		for (TEntityTemplate e : allTemplates) {
			// TODO
		}
	}
	
	@Test
	public void artifactTypeForWARfiles() {
		QName artifactType = TestWineryRepositoryClient.client.getArtifactTypeQNameForExtension("war");
		Assert.assertNotNull("Artifact Type for .war does not exist", artifactType);
	}
	
	@Test
	public void createArtifactTemplate() throws IOException, QNameAlreadyExistsException {
		// assure that the artifact type exists
		QName artifactTypeQName = TestWineryRepositoryClient.client.getArtifactTypeQNameForExtension("war");
		Assert.assertNotNull("Artifact Type for .war does not exist", artifactTypeQName);
		
		// assure that the artifact template does not yet exist
		// one possibility is to delete the artifact template, the other
		// possibility is to
		
		QName artifactTemplateQName = new QName(TestWineryRepositoryClient.namespaceForNewArtifacts, "artifactTemplate");
		ArtifactTemplateId atId = new ArtifactTemplateId(artifactTemplateQName);
		
		// ensure that the template does not exist yet
		TestWineryRepositoryClient.client.forceDelete(atId);
		
		TestWineryRepositoryClient.client.createArtifactTemplate(artifactTemplateQName, artifactTypeQName);
	}
}
