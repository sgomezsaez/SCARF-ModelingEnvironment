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
package org.eclipse.winery.repository.resources.admin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.common.ids.Namespace;
import org.eclipse.winery.repository.Utils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.datatypes.ids.admin.NamespacesId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.view.Viewable;

/**
 * Manages prefixes for the namespaces
 */
public class NamespacesResource extends AbstractAdminResource {
	
	private static final Logger logger = LoggerFactory.getLogger(NamespacesResource.class);
	
	public final static NamespacesResource INSTANCE = new NamespacesResource();
	
	private Integer nsCount = 0;
	
	
	private NamespacesResource() {
		super(new NamespacesId());
		
		// globally set prefixes
		// if that behavior is not desired, the code has to be moved to "generatePrefix" which checks for existence, ...
		this.configuration.setProperty("http://www.w3.org/2001/XMLSchema", "xsd");
		this.configuration.setProperty("http://www.w3.org/XML/1998/namespace", "xmlns");
		this.configuration.setProperty(org.eclipse.winery.common.constants.Namespaces.TOSCA_NAMESPACE, "tosca");
		this.configuration.setProperty(org.eclipse.winery.common.constants.Namespaces.TOSCA_WINERY_EXTENSIONS_NAMESPACE, "winery");
	}
	
	private Collection<String> getAllPrefixes() {
		Iterator<String> keys = this.configuration.getKeys();
		HashSet<String> res = new HashSet<String>();
		while (keys.hasNext()) {
			String key = keys.next();
			String prefix = this.configuration.getString(key);
			res.add(prefix);
		}
		return res;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getHTML() {
		Viewable viewable = new Viewable("/jsp/admin/namespaces.jsp", this);
		return Response.ok().entity(viewable).build();
	}
	
	/**
	 * Sets / overwrites prefix/namespace mapping
	 * 
	 * In case the prefix is already bound to another namespace, BAD_REQUEST is
	 * returned.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addNamespace(@FormParam("namespace") String namespace, @FormParam("nsPrefix") String prefix) {
		if (StringUtils.isEmpty(namespace)) {
			return Response.status(Status.BAD_REQUEST).entity("namespace must be given.").build();
		}
		if (StringUtils.isEmpty(prefix)) {
			return Response.status(Status.BAD_REQUEST).entity("prefix must be given.").build();
		}
		namespace = Util.URLdecode(namespace);
		prefix = Util.URLdecode(prefix);
		Collection<String> allPrefixes = this.getAllPrefixes();
		if (allPrefixes.contains(prefix)) {
			if (NamespacesResource.getPrefix(namespace).equals(prefix)) {
				return Response.notModified().build();
			} else {
				// the requested prefix is already bound to a different namespace
				return Response.status(Status.BAD_REQUEST).entity("prefix already bound to a different namespace.").build();
			}
		}
		this.configuration.setProperty(namespace, prefix);
		return Response.noContent().build();
	}
	
	/**
	 * Deletes given namespace from the repository
	 * 
	 * @param URI to delete. The namespace is URLencoded.
	 * @return
	 */
	@DELETE
	@Path("{namespace}")
	public Response onDelete(@PathParam("namespace") String URI) {
		Response res;
		URI = Util.URLdecode(URI);
		if (this.configuration.containsKey(URI)) {
			this.configuration.clearProperty(URI);
			res = Response.noContent().build();
		} else {
			res = Response.status(Status.NOT_FOUND).build();
		}
		return res;
	}
	
	/**
	 * SIDEFFECT: URI is added to list of known namespaces if it did not exist
	 * before
	 */
	public static String getPrefix(Namespace namespace) {
		String ns = namespace.getDecoded();
		return NamespacesResource.getPrefix(ns);
	}
	
	@Path("{namespace}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getPrefixForEncodedNamespace(@PathParam("namespace") String URI) {
		URI = Util.URLdecode(URI);
		return NamespacesResource.getPrefix(URI);
	}
	
	/**
	 * SIDEFFECT: URI is added to list of known namespaces if it did not exist
	 * before
	 */
	public static String getPrefix(String namespace) {
		if (namespace == null) {
			throw new IllegalArgumentException("Namespace must not be null");
		}
		String prefix = NamespacesResource.INSTANCE.configuration.getString(namespace);
		if (prefix == null) {
			prefix = NamespacesResource.generatePrefix(namespace);
			NamespacesResource.INSTANCE.configuration.setProperty(namespace, prefix);
		}
		return prefix;
	}
	
	private static String generatePrefix(String namespace) {
		String prefix = null;
		Collection<String> allPrefixes = NamespacesResource.INSTANCE.getAllPrefixes();
		
		// TODO: generate prefix using URI (and not "arbitrary" prefix)
		do {
			prefix = String.format("ns%d", NamespacesResource.INSTANCE.nsCount);
			NamespacesResource.INSTANCE.nsCount++;
		} while (allPrefixes.contains(prefix));
		return prefix;
	}
	
	/**
	 * Returns the list of all namespaces registered with his manager. It could
	 * be incomplete, if entries have been added manually to the repository
	 * 
	 * @return all namespaces registered with this manager.
	 */
	private HashSet<Namespace> getRegisteredNamespaces() {
		HashSet<Namespace> res = new HashSet<Namespace>();
		Iterator<String> keys = this.configuration.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			Namespace ns = new Namespace(key, false);
			res.add(ns);
		}
		return res;
	}
	
	/**
	 * Returns the list of all namespaces registered with his manager and used
	 * at component instances.
	 */
	public static Collection<Namespace> getNamespaces() {
		HashSet<Namespace> res = NamespacesResource.INSTANCE.getRegisteredNamespaces();
		res.addAll(Repository.INSTANCE.getUsedNamespaces());
		return res;
	}
	
	/**
	 * This method is required because static methods cannot be accessed by EL
	 * 
	 * @return see getNamespaces()
	 */
	public Collection<Namespace> getNamespacesForJSP() {
		return NamespacesResource.getNamespaces();
	}
	
	/**
	 * Returns the list of all namespaces registered with his manager and used
	 * at component instances.
	 * 
	 * @return a JSON list containing the non-encoded URIs of each known
	 *         namespace
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getNamespacesAsJSONlist() {
		Collection<Namespace> namespaces = NamespacesResource.getNamespaces();
		
		// We now have all namespaces
		// We need to convert from Namespace to String
		
		TreeSet<String> stringNamespaces = new TreeSet<String>();
		for (Namespace ns : namespaces) {
			stringNamespaces.add(ns.getDecoded());
		}
		
		String res;
		try {
			res = Utils.mapper.writeValueAsString(stringNamespaces);
		} catch (JsonProcessingException e) {
			NamespacesResource.logger.error(e.getMessage(), e);
			res = "[]";
		}
		return res;
	}
	
	/**
	 * Checks whether a prefix is registered for a namespace
	 * 
	 * Used at CSARImporter
	 */
	public boolean getIsPrefixKnownForNamespace(String namespace) {
		return this.configuration.containsKey(namespace);
	}
}
