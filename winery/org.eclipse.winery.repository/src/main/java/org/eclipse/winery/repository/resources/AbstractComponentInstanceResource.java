/*******************************************************************************
 * Copyright (c) 2012-2013,2015 University of Stuttgart.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import org.eclipse.winery.common.RepositoryFileReference;
import org.eclipse.winery.common.TOSCADocumentBuilderFactory;
import org.eclipse.winery.common.constants.MimeTypes;
import org.eclipse.winery.common.ids.Namespace;
import org.eclipse.winery.common.ids.XMLId;
import org.eclipse.winery.common.ids.definitions.TOSCAComponentId;
import org.eclipse.winery.model.tosca.Definitions;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TImport;
import org.eclipse.winery.repository.JAXBSupport;
import org.eclipse.winery.repository.Utils;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.backend.constants.MediaTypes;
import org.eclipse.winery.repository.export.TOSCAExportUtil;
import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources.documentation.DocumentationsResource;
import org.eclipse.winery.repository.resources.imports.genericimports.GenericImportResource;
import org.eclipse.winery.repository.resources.tags.TagsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.jersey.api.view.Viewable;

/**
 * Resource for a component (
 * <ul>
 * <li>ServiceTemplates,</li>
 * <li>EntityTypes,</li>
 * <li>EntityTypeImplementations,</li>
 * <li>EntityTemplates</li>
 * </ul>
 * ). A component is directly nested in a TDefinitions element. See also
 * {@link org.eclipse.winery.common.ids.definitions.TOSCAComponentId}
 * 
 * Bundles all operations required for all components. e.g., namespace+XMLid,
 * object comparison, import, export, tags
 * 
 * Uses a TDefinitions document as storage.
 * 
 * Additional setters and getters are added if it comes to Winery's extensions
 * such as the color of a relationship type
 */
public abstract class AbstractComponentInstanceResource implements Comparable<AbstractComponentInstanceResource>, IPersistable {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractComponentInstanceResource.class);
	
	protected final TOSCAComponentId id;
	
	private final RepositoryFileReference ref;
	
	// the object representing the data of this resource
	private Definitions definitions = null;
	
	// shortcut for this.definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
	protected TExtensibleElements element = null;
	
	
	/**
	 * Instantiates the resource. Assumes that the resource should exist
	 * (assured by the caller)
	 * 
	 * The caller should <em>not</em> create the resource by other ways. E.g.,
	 * by instantiating this resource and then adding data.
	 */
	public AbstractComponentInstanceResource(TOSCAComponentId id) {
		this.id = id;
		
		// the resource itself exists
		assert (Repository.INSTANCE.exists(id));
		
		// the data file might not exist
		this.ref = BackendUtils.getRefOfDefinitions(id);
		if (Repository.INSTANCE.exists(this.ref)) {
			this.load();
		} else {
			this.createNew();
		}
	}
	
	/**
	 * Convenience method for getId().getNamespace()
	 */
	public final Namespace getNamespace() {
		return this.id.getNamespace();
	}
	
	/**
	 * Convenience method for getId().getXmlId()
	 */
	public final XMLId getXmlId() {
		return this.id.getXmlId();
	}
	
	/**
	 * Convenience method for getId().getQName();
	 * 
	 * @return the QName associated with this resource
	 */
	public final QName getQName() {
		return this.getId().getQName();
	}
	
	/**
	 * Returns the id associated with this resource
	 */
	public final TOSCAComponentId getId() {
		return this.id;
	}
	
	/**
	 * called from AbstractComponentResource
	 */
	@DELETE
	public final Response onDelete() {
		Response res = BackendUtils.delete(this.id);
		return res;
	}
	
	@Override
	public final int compareTo(AbstractComponentInstanceResource o) {
		return this.id.compareTo(o.id);
	}
	
	@Override
	public final boolean equals(Object o) {
		if (o instanceof String) {
			throw new IllegalStateException();
		} else if (o instanceof AbstractComponentInstanceResource) {
			if (o.getClass().equals(this.getClass())) {
				// only compare if the two objects are from the same class
				return ((AbstractComponentInstanceResource) o).getId().equals(this.getId());
			} else {
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalStateException();
		}
	}
	
	@Override
	public final int hashCode() {
		return this.getId().hashCode();
	}
	
	@GET
	@Path("id")
	public String getTOSCAId() {
		return this.id.getXmlId().getDecoded();
	}
	
	@PUT
	@Path("id")
	public Response putId(@FormParam("id") String id) {
		// this renames the entity type resource
		// TODO: implement rename functionality
		return Response.serverError().entity("not yet implemented").build();
	}
	
	/**
	 * Main page
	 */
	// @Produces(MediaType.TEXT_HTML) // not true because of ?csar leads to send
	// a csar. We nevertheless have to annotate that to be able to get a JSON
	// representation required for the file upload (in {@link
	// ArtifactTemplateResource})
	//
	// we cannot issue a request expecting content-type application/zip as it is
	// not possible to offer the result in a "save-as"-dialog:
	// http://stackoverflow.com/questions/7464665/ajax-response-content-disposition-attachment
	@GET
	@Produces(MediaType.TEXT_HTML)
	public final Response getHTML(@QueryParam(value = "definitions") String definitions, @QueryParam(value = "csar") String csar, @Context UriInfo uriInfo) {
		if (!Repository.INSTANCE.exists(this.id)) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (definitions != null) {
			return Utils.getDefinitionsOfSelectedResource(this, uriInfo.getBaseUri());
		} else if (csar != null) {
			return this.getCSAR();
		} else {
			String type = Utils.getTypeForInstance(this.getClass());
			String viewableName = "/jsp/" + Utils.getIntermediateLocationStringForType(type, "/") + "/" + type.toLowerCase() + ".jsp";
			Viewable viewable = new Viewable(viewableName, this);
			
			return Response.ok().entity(viewable).build();
			
			// we can't do the following as the GET request from the browser
			// cannot set the accept header properly
			// "vary: accept" header has to be set as we may also return a THOR
			// on the same URL
			// return Response.ok().header(HttpHeaders.VARY,
			// HttpHeaders.ACCEPT).entity(viewable).build();
		}
	}
	
	@GET
	@Produces(MimeTypes.MIMETYPE_ZIP)
	public final Response getCSAR() {
		if (!Repository.INSTANCE.exists(this.id)) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Utils.getCSARofSelectedResource(this);
	}
	
	/**
	 * Returns the definitions of this resource. Includes required imports of
	 * other definitions
	 * 
	 * @param csar used because plan generator's GET request lands here
	 */
	@GET
	@Produces({MimeTypes.MIMETYPE_TOSCA_DEFINITIONS, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response getDefinitionsAsResponse(@QueryParam(value = "csar") String csar) {
		if (!Repository.INSTANCE.exists(this.id)) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		if (csar != null) {
			return Utils.getCSARofSelectedResource(this);
		}
		
		StreamingOutput so = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				TOSCAExportUtil exporter = new TOSCAExportUtil();
				// we include everything related
				Map<String, Object> conf = new HashMap<>();
				try {
					exporter.exportTOSCA(AbstractComponentInstanceResource.this.id, output, conf);
				} catch (JAXBException e) {
					throw new WebApplicationException(e);
				}
			}
		};
		return Response.ok().type(MediaType.TEXT_XML).entity(so).build();
	}
	
	/**
	 * @throws IllegalStateException if an IOException occurred. We opted not to
	 *             propagate the IOException directly as this exception occurs
	 *             seldom and is a not an exception to be treated by all callers
	 *             in the prototype.
	 */
	private void load() {
		try {
			InputStream is = Repository.INSTANCE.newInputStream(this.ref);
			Unmarshaller u = JAXBSupport.createUnmarshaller();
			this.definitions = (Definitions) u.unmarshal(is);
		} catch (Exception e) {
			AbstractComponentInstanceResource.logger.error("Could not read content from file " + this.ref, e);
			throw new IllegalStateException(e);
		}
		try {
			this.element = this.definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		} catch (IndexOutOfBoundsException e) {
			if (this instanceof GenericImportResource) {
				// everything allright:
				// ImportResource is a quick hack using 99% of the functionality offered here
				// As only 1% has to be "quick hacked", we do that instead of a clean design
				// Clean design: Introduce a class between this and AbstractComponentInstanceResource, where this class and ImportResource inhertis from
				// A clean design introducing a super class AbstractDefinitionsBackedResource does not work, as we currently also support PropertiesBackedResources and such a super class would required multi-inheritance
			} else {
				throw new IllegalStateException("Wrong storage format: No ServiceTemplateOrNodeTypeOrNodeTypeImplementation found.");
			}
		}
	}
	
	@Override
	public void persist() throws IOException {
		BackendUtils.persist(this.definitions, this.ref, MediaTypes.MEDIATYPE_TOSCA_DEFINITIONS);
	}
	
	/**
	 * Creates a new instance of the object represented by this resource
	 */
	private void createNew() {
		this.definitions = BackendUtils.createWrapperDefinitions(this.getId());
		
		// create empty element
		this.element = this.createNewElement();
		
		// add the element to the definitions
		this.definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(this.element);
		
		// copy ns + id
		this.copyIdToFields();
		
		// ensure that the definitions is persisted. Ensures that export works.
		BackendUtils.persist(this);
	}
	
	/**
	 * Creates an empty instance of an Element.
	 * 
	 * The implementors do <em>not</em>have to copy the ns and the id to the
	 * appropriate fields.
	 * 
	 * we have two implementation possibilities:
	 * <ul>
	 * <li>a) each subclass implements this method and returns the appropriate
	 * object</li>
	 * <li>b) we use java reflection to invoke the right constructor as done in
	 * the resources</li>
	 * </ul>
	 * We opted for a) to increase readability of the code
	 */
	protected abstract TExtensibleElements createNewElement();
	
	/**
	 * Copies the current id of the resource to the appropriate fields in the
	 * element.
	 * 
	 * For instance, the id is put in the "name" field for EntityTypes
	 * 
	 * We opted for a separate method from createNewElement to enable renaming
	 * of the object
	 */
	protected abstract void copyIdToFields();
	
	/**
	 * Returns the Element belonging to this resource. As Java does not allow
	 * overriding returned classes, we expect the caller to either cast right or
	 * to use "getXY" defined by each subclass, where XY is the concrete type
	 * 
	 * Shortcut for
	 * getDefinitions().getServiceTemplateOrNodeTypeOrNodeTypeImplementation
	 * ().get(0);
	 * 
	 * @return TCapabilityType|...
	 */
	public TExtensibleElements getElement() {
		return this.element;
	}
	
	/**
	 * @return the reference to the internal list of imports. Can be changed if
	 *         some imports are required or should be removed
	 * @throws IllegalStateException if definitions was not loaded or not
	 *             initialized
	 */
	protected List<TImport> getImport() {
		if (this.definitions == null) {
			throw new IllegalStateException("Trying to access uninitalized definitions object");
		}
		return this.definitions.getImport();
	}
	
	/**
	 * Returns an XML representation of the definitions
	 * 
	 * We return the complete definitions to allow the user changes to it, such
	 * as adding imports, etc.
	 */
	public String getDefinitionsAsXMLString() {
		StringWriter w = new StringWriter();
		Marshaller m = JAXBSupport.createMarshaller(true);
		try {
			m.marshal(this.definitions, w);
		} catch (JAXBException e) {
			AbstractComponentInstanceResource.logger.error("Could not marshal definitions", e);
			throw new IllegalStateException(e);
		}
		String res = w.toString();
		return res;
	}
	
	/**
	 * @return the reference to the internal Definitions object
	 */
	public Definitions getDefinitions() {
		return this.definitions;
	}
	
	@PUT
	@Consumes({MimeTypes.MIMETYPE_TOSCA_DEFINITIONS, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response updateDefinitions(InputStream requestBodyStream) {
		Unmarshaller u;
		Definitions defs;
		Document doc;
		final StringBuilder sb = new StringBuilder();
		try {
			DocumentBuilder db = TOSCADocumentBuilderFactory.INSTANCE.getTOSCADocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					// we don't care
				}
				
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					sb.append("Fatal Error: ");
					sb.append(exception.getMessage());
					sb.append("\n");
				}
				
				@Override
				public void error(SAXParseException exception) throws SAXException {
					sb.append("Fatal Error: ");
					sb.append(exception.getMessage());
					sb.append("\n");
				}
			});
			doc = db.parse(requestBodyStream);
			if (sb.length() > 0) {
				// some error happened
				// doc is not null, because the parser parses even if it is not XSD conforming
				return Response.status(Status.BAD_REQUEST).entity(sb.toString()).build();
			}
		} catch (SAXException | IOException e) {
			AbstractComponentInstanceResource.logger.debug("Could not parse XML", e);
			return Utils.getResponseForException(e);
		}
		try {
			u = JAXBSupport.createUnmarshaller();
			defs = (Definitions) u.unmarshal(doc);
		} catch (JAXBException e) {
			AbstractComponentInstanceResource.logger.debug("Could not unmarshal from request body stream", e);
			return Utils.getResponseForException(e);
		}
		
		// initial validity check
		
		// we allow changing the target namespace and the id
		// This allows for inserting arbitrary definitions XML
		//		if (!this.definitions.getTargetNamespace().equals(this.id.getNamespace().getDecoded())) {
		//			return Response.status(Status.BAD_REQUEST).entity("Changing of the namespace is not supported").build();
		//		}
		//		this.definitions.setTargetNamespace(this.id.getNamespace().getDecoded());
		
		// TODO: check the provided definitions for validity
		
		TExtensibleElements tExtensibleElements = defs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		if (!tExtensibleElements.getClass().equals(this.createNewElement().getClass())) {
			return Response.status(Status.BAD_REQUEST).entity("First type in Definitions is not matching the type modeled by this resource").build();
		}
		
		this.definitions = defs;
		
		// replace existing element by retrieved data
		this.element = this.definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		
		// ensure that ids did not change
		// TODO: future work: raise error if user changed id or namespace
		this.copyIdToFields();
		
		return BackendUtils.persist(this);
	}
	
	@GET
	@Path("xml/")
	@Produces(MediaType.TEXT_HTML)
	public Response getXML() {
		Viewable viewable = new Viewable("/jsp/xmlSource.jsp", this);
		return Response.ok().entity(viewable).build();
	}
	
	@Path("documentation/")
	public DocumentationsResource getDocumentationsResource() {
		return new DocumentationsResource(this, this.getElement().getDocumentation());
	}
	
	@Path("tags/")
	public final TagsResource getTags() {
		return new TagsResource(this.id);
	}
	
}
