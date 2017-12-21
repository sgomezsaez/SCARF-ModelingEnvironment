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
package org.eclipse.winery.repository.resources.admin.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.repository.datatypes.TypeWithShortName;
import org.eclipse.winery.repository.datatypes.ids.admin.TypesId;
import org.eclipse.winery.repository.datatypes.select2.Select2DataItem;
import org.eclipse.winery.repository.resources.admin.AbstractAdminResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.view.Viewable;

/**
 * Handles longname/shortname by using properties
 * 
 * FIXME: This class does NOT support dynamic reloading of the underlying
 * Configuration instance
 * 
 */
public abstract class AbstractTypesManager extends AbstractAdminResource {
	
	@Context
	private UriInfo uriInfo;
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractTypesManager.class);
	
	// hashes from a long type string to the type object holding complete type data
	private final HashMap<String, TypeWithShortName> hashTypeStringToType;
	
	
	public AbstractTypesManager(TypesId id) {
		super(id);
		// now, this.configuration is filled with stored data
		
		// copy over information from configuration to internal data structure
		this.hashTypeStringToType = new HashMap<String, TypeWithShortName>();
		Iterator<String> keys = this.configuration.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = this.configuration.getString(key);
			TypeWithShortName typeInfo = new TypeWithShortName(key, value);
			this.hashTypeStringToType.put(key, typeInfo);
		}
	}
	
	protected void addData(String longName, String shortName) {
		TypeWithShortName t = new TypeWithShortName(longName, shortName);
		this.addData(t);
	}
	
	/**
	 * Adds data to the internal data structure WITHOUT persisting it
	 * 
	 * More or less a quick hack to enable adding default types without
	 * persisting them in the storage
	 * 
	 * @param t the type to add
	 */
	private void addData(TypeWithShortName t) {
		this.hashTypeStringToType.put(t.getType(), t);
	}
	
	public synchronized void addTypeWithShortName(TypeWithShortName type) {
		this.addData(type);
		this.configuration.setProperty(type.getType(), type.getShortName());
	}
	
	/**
	 * Removes a type. Will not remove a type added by "addData"
	 */
	@DELETE
	@Path("{type}")
	public Response removeTypeWithResponse(@PathParam("type") String type) {
		type = Util.URLdecode(type);
		if (this.configuration.containsKey(type)) {
			this.hashTypeStringToType.remove(type);
			this.configuration.clearProperty(type);
			return Response.noContent().build();
		} else if (this.hashTypeStringToType.containsKey(type)) {
			// predefined types may not be deleted
			// this branch is hit at types added via addData (e.g., predefined plantypes)
			return Response.status(Status.FORBIDDEN).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
	
	/**
	 * Returns a sorted list of all available types
	 */
	public Collection<TypeWithShortName> getTypes() {
		Collection<TypeWithShortName> res = new TreeSet<TypeWithShortName>(this.hashTypeStringToType.values());
		return res;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getTypesAsJSONArrayList(@QueryParam("select2") String select2) {
		if (select2 == null) {
			return this.getTypes();
		} else {
			// select2 mode
			SortedSet<Select2DataItem> res = new TreeSet<>();
			for (TypeWithShortName t : this.getTypes()) {
				Select2DataItem item = new Select2DataItem(t.getType(), t.getShortName());
				res.add(item);
			}
			return res;
		}
	}
	
	/**
	 * <b>SIDEEFFECT:</b> If there currently isn't any short type name, it is
	 * created
	 */
	public TypeWithShortName getTypeWithShortName(String typeString) {
		TypeWithShortName t = this.hashTypeStringToType.get(typeString);
		if (t == null) {
			String shortName = this.getShortName(typeString);
			t = new TypeWithShortName(typeString, shortName);
			this.addTypeWithShortName(t);
		}
		return t;
	}
	
	/**
	 * <b>SIDEEFFECT:</b> If there currently isn't any short type name, it is
	 * created
	 */
	public String getShortName(String typeString) {
		TypeWithShortName type = this.hashTypeStringToType.get(typeString);
		String res;
		if (type == null) {
			// happens if artifact type is not registered in artifacttypes.list
			// (DATAFILENAME)
			res = typeString;
		} else {
			res = type.getShortName();
		}
		return res;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable getHTML(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		return new Viewable("/jsp/admin/types/types.jsp", this);
	}
	
	@POST
	public Response updateTypeMapping(@FormParam("shortname") String shortName, @FormParam("type") String type) {
		if (StringUtils.isEmpty(shortName)) {
			return Response.status(Status.BAD_REQUEST).entity("shortName has to be given").build();
		}
		if (StringUtils.isEmpty(type)) {
			return Response.status(Status.BAD_REQUEST).entity("type has to be given").build();
		}
		shortName = Util.URLdecode(shortName);
		type = Util.URLdecode(type);
		TypeWithShortName tws = new TypeWithShortName(type, shortName);
		this.addTypeWithShortName(tws);
		return Response.noContent().build();
	}
	
	/**
	 * Required by types.jsp
	 */
	public String getURL() {
		return this.uriInfo.getAbsolutePath().toString();
	}
	
}
