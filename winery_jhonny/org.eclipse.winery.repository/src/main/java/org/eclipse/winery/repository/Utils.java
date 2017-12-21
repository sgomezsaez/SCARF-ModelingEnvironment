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
package org.eclipse.winery.repository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.taglibs.standard.functions.Functions;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.xerces.xs.XSConstants;
import org.eclipse.winery.common.RepositoryFileReference;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.common.ids.GenericId;
import org.eclipse.winery.common.ids.Namespace;
import org.eclipse.winery.common.ids.XMLId;
import org.eclipse.winery.common.ids.definitions.TOSCAComponentId;
import org.eclipse.winery.common.ids.definitions.imports.XSDImportId;
import org.eclipse.winery.model.tosca.TArtifactType;
import org.eclipse.winery.model.tosca.TConstraint;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TEntityType;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TPolicyType;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.datatypes.ids.admin.AdminId;
import org.eclipse.winery.repository.export.CSARExporter;
import org.eclipse.winery.repository.export.TOSCAExportUtil;
import org.eclipse.winery.repository.resources.AbstractComponentInstanceResource;
import org.eclipse.winery.repository.resources.AbstractComponentsResource;
import org.eclipse.winery.repository.resources.entitytypes.nodetypes.NodeTypeResource;
import org.eclipse.winery.repository.resources.entitytypes.nodetypes.NodeTypesResource;
import org.eclipse.winery.repository.resources.entitytypes.relationshiptypes.RelationshipTypeResource;
import org.eclipse.winery.repository.resources.entitytypes.relationshiptypes.RelationshipTypesResource;
import org.eclipse.winery.repository.resources.imports.xsdimports.XSDImportResource;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Contains utility functionality concerning with everything that is
 * <em>not</em> related only to the repository, but more. For instance, resource
 * functionality. Utility functionality for the repository is contained at
 * {@link BackendUtils}
 */
public class Utils {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(Utils.class);
	
	
	public static URI createURI(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalStateException();
		}
	}
	
	
	// RegExp inspired by http://stackoverflow.com/a/5396246/873282
	// NameStartChar without ":"
	// stackoverflow: -dfff, standard: d7fff
	private static final String RANGE_NCNAMESTARTCHAR = "A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d" + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff" + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\x10000-\\xEFFFF";
	private static final String REGEX_NCNAMESTARTCHAR = "[" + Utils.RANGE_NCNAMESTARTCHAR + "]";
	
	private static final String RANGE_NCNAMECHAR = Utils.RANGE_NCNAMESTARTCHAR + "\\-\\.0-9\\u00b7\\u0300-\\u036f\\u203f-\\u2040";
	private static final String REGEX_INVALIDNCNAMESCHAR = "[^" + Utils.RANGE_NCNAMECHAR + "]";
	
	
	/**
	 * Creates a (valid) XML ID (NCName) based on the passed name
	 * 
	 * Valid NCNames: http://www.w3.org/TR/REC-xml-names/#NT-NCName /
	 * http://www.w3.org/TR/xml/#NT-Name http://www.w3.org/TR/xml/#NT-Name
	 * 
	 */
	public static XMLId createXMLid(String name) {
		return new XMLId(Utils.createXMLidAsString(name), false);
	}
	
	/**
	 * Creates a (valid) XML ID (NCName) based on the passed name
	 * 
	 * Valid NCNames: http://www.w3.org/TR/REC-xml-names/#NT-NCName /
	 * http://www.w3.org/TR/xml/#NT-Name http://www.w3.org/TR/xml/#NT-Name
	 * 
	 * TODO: this method seems to be equal to {@link
	 * org.eclipse.winery.common.Util.makeNCName(String)}. The methods should be
	 * merged into one.
	 * 
	 */
	public static String createXMLidAsString(String name) {
		String id = name;
		if (!id.substring(0, 1).matches(Utils.REGEX_NCNAMESTARTCHAR)) {
			id = "_".concat(id);
		}
		// id starts with a valid character
		
		// before we wipe out all invalid characters, we do a readable
		// replacement for appropriate characters
		id = id.replace(' ', '_');
		
		// keep length of ID, only wipe out invalid characters
		// alternative: replace invalid characters by URLencoded version. As the
		// ID is visible only in the URL, this quick hack should be OK
		// ID is visible only in the URL, this quick hack should be OK
		id = id.replaceAll(Utils.REGEX_INVALIDNCNAMESCHAR, "_");
		
		return id;
	}
	
	/**
	 * Returns the plain XML for the selected resource
	 * 
	 * @param uri
	 */
	public static Response getDefinitionsOfSelectedResource(final AbstractComponentInstanceResource resource, final URI uri) {
		final TOSCAExportUtil exporter = new TOSCAExportUtil();
		StreamingOutput so = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				Map<String, Object> conf = new HashMap<>();
				conf.put(TOSCAExportUtil.ExportProperties.REPOSITORY_URI.toString(), uri);
				try {
					exporter.exportTOSCA(resource.getId(), output, conf);
				} catch (JAXBException e) {
					throw new WebApplicationException(e);
				}
				output.close();
			}
		};
		/*
		 * this code is for offering a download action // Browser offers save as
		 * // .tosca is more or less needed for debugging, only a CSAR makes
		 * sense. // Therefore, we want to have the xml opened in the browser.
		 * StringBuilder sb = new StringBuilder();
		 * sb.append("attachment;filename=\"");
		 * sb.append(resource.getXmlId().getEncoded()); sb.append(" - ");
		 * sb.append(resource.getNamespace().getEncoded()); sb.append(".xml");
		 * sb.append("\""); return Response.ok().header("Content-Disposition",
		 * sb
		 * .toString()).type(MediaType.APPLICATION_XML_TYPE).entity(so).build();
		 */
		return Response.ok().type(MediaType.APPLICATION_XML).entity(so).build();
	}
	
	public static Response getCSARofSelectedResource(final AbstractComponentInstanceResource resource) {
		final CSARExporter exporter = new CSARExporter();
		StreamingOutput so = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					exporter.writeCSAR(resource.getId(), output);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};
		StringBuilder sb = new StringBuilder();
		sb.append("attachment;filename=\"");
		sb.append(resource.getXmlId().getEncoded());
		sb.append(org.eclipse.winery.repository.Constants.SUFFIX_CSAR);
		sb.append("\"");
		return Response.ok().header("Content-Disposition", sb.toString()).type(org.eclipse.winery.common.constants.MimeTypes.MIMETYPE_ZIP).entity(so).build();
	}
	
	/**
	 * @return Singular type name for the given resource. E.g.,
	 *         "ServiceTemplateResource" gets "ServiceTemplate"
	 */
	public static String getTypeForInstance(Class<? extends AbstractComponentInstanceResource> resClass) {
		String res = resClass.getName();
		// Everything between the last "." and before "Resource" is the Type
		int dotIndex = res.lastIndexOf('.');
		assert (dotIndex >= 0);
		return res.substring(dotIndex + 1, res.length() - "Resource".length());
	}
	
	/**
	 * @return Singular type name for the given id. E.g., "ServiceTemplateId"
	 *         gets "ServiceTemplate"
	 */
	public static String getTypeForAdminId(Class<? extends AdminId> idClass) {
		return Util.getEverythingBetweenTheLastDotAndBeforeId(idClass);
	}
	
	/**
	 * @return Singular type name for given AbstractComponentsResource. E.g,
	 *         "ServiceTemplatesResource" gets "ServiceTemplate"
	 */
	public static String getTypeForComponentContainer(Class<? extends AbstractComponentsResource> containerClass) {
		String res = containerClass.getName();
		// Everything between the last "." and before "sResource" is the Type
		int dotIndex = res.lastIndexOf('.');
		assert (dotIndex >= 0);
		return res.substring(dotIndex + 1, res.length() - "sResource".length());
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends TOSCAComponentId> getComponentIdClass(String idClassName) {
		String pkg = "org.eclipse.winery.common.ids.definitions.";
		if (idClassName.contains("Import")) {
			// quick hack to handle imports, which reside in their own package
			pkg = pkg + "imports.";
		}
		String fullClassName = pkg + idClassName;
		try {
			return (Class<? extends TOSCAComponentId>) Class.forName(fullClassName);
		} catch (ClassNotFoundException e) {
			// quick hack for Ids local to winery repository
			try {
				fullClassName = "org.eclipse.winery.repository.datatypes.ids.admin." + idClassName;
				return (Class<? extends TOSCAComponentId>) Class.forName(fullClassName);
			} catch (ClassNotFoundException e2) {
				String errorMsg = "Could not find id class for component container, " + fullClassName;
				Utils.logger.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			}
		}
	}
	
	/**
	 * Returns a class object for ids of components nested in the given
	 * AbstractComponentsResource
	 */
	public static Class<? extends TOSCAComponentId> getComponentIdClassForComponentContainer(Class<? extends AbstractComponentsResource> containerClass) {
		// the name of the id class is the type + "Id"
		String idClassName = Utils.getTypeForComponentContainer(containerClass) + "Id";
		
		return Utils.getComponentIdClass(idClassName);
	}
	
	public static Class<? extends TOSCAComponentId> getComponentIdClassForTExtensibleElements(Class<? extends TExtensibleElements> clazz) {
		// we assume that the clazzName always starts with a T.
		// Therefore, we fetch everything after the last dot (plus offest 1)
		String idClassName = clazz.getName();
		int dotIndex = idClassName.lastIndexOf('.');
		assert (dotIndex >= 0);
		idClassName = idClassName.substring(dotIndex + 2) + "Id";
		
		return Utils.getComponentIdClass(idClassName);
	}
	
	
	private static final String slashEncoded = Util.URLencode("/");
	
	
	public static String getURLforPathInsideRepo(String pathInsideRepo) {
		// first encode the whole string
		String res = Util.URLencode(pathInsideRepo);
		// issue: "/" is also encoded. This has to be undone:
		res = res.replaceAll(Utils.slashEncoded, "/");
		return res;
	}
	
	
	/**
	 * Shared object to map JSONs
	 */
	public static final ObjectMapper mapper = new ObjectMapper();
	
	
	public static String Object2JSON(Object o) {
		String res;
		try {
			res = Utils.mapper.writeValueAsString(o);
		} catch (Exception e) {
			Utils.logger.error(e.getMessage(), e);
			return null;
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends GenericId> getGenericIdClassForType(String typeIdType) {
		Class<? extends GenericId> res;
		// quick hack - we only need definitions right now
		String pkg = "org.eclipse.winery.repository.datatypes.ids.definitions.";
		String className = typeIdType;
		className = pkg + className;
		try {
			res = (Class<? extends GenericId>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			Utils.logger.error("Could not find id class for id type", e);
			res = null;
		}
		return res;
	}
	
	/**
	 * @return the absolute path for the given id
	 */
	public static String getAbsoluteURL(GenericId id) {
		return Prefs.INSTANCE.getResourcePath() + "/" + Utils.getURLforPathInsideRepo(BackendUtils.getPathInsideRepo(id));
	}
	
	/**
	 * @param baseURI the URI from which the path should start
	 * @param id the generic id to resolve
	 * 
	 * @return the relative path for the given id
	 */
	public static String getRelativeURL(URI baseURI, GenericId id) {
		String absolutePath = Prefs.INSTANCE.getResourcePath() + "/" + Utils.getURLforPathInsideRepo(BackendUtils.getPathInsideRepo(id));
		return baseURI.relativize(URI.create(absolutePath)).toString();
	}
	
	/**
	 * @return the absolute path for the given id
	 */
	public static String getAbsoluteURL(RepositoryFileReference ref) {
		return Prefs.INSTANCE.getResourcePath() + "/" + Utils.getURLforPathInsideRepo(BackendUtils.getPathInsideRepo(ref));
	}
	
	public static URI getAbsoluteURI(GenericId id) {
		return Utils.createURI(Utils.getAbsoluteURL(id));
	}
	
	public static String doubleEscapeHTMLAndThenConvertNL2BR(String txt) {
		String res = Functions.escapeXml(txt);
		res = Functions.escapeXml(res);
		res = res.replaceAll("\\n", "<br/>");
		return res;
	}
	
	/**
	 * This method is similar to {@link
	 * org.eclipse.winery.common.Util.qname2href()}, but treats winery's
	 * internal ID model instead of the global TOSCA model
	 * 
	 * @param id the id to create an <code>a href</code> element for
	 * @return an <code>a</code> HTML element pointing to the given id
	 */
	public static String getHREF(TOSCAComponentId id) {
		String res = "<a href=\"" + Utils.getAbsoluteURL(id) + "\">" + Functions.escapeXml(id.getXmlId().getDecoded()) + "</a>";
		return res;
	}
	
	public static String artifactTypeQName2href(QName qname) {
		return Util.qname2href(Prefs.INSTANCE.getResourcePath(), TArtifactType.class, qname);
	}
	
	public static String nodeTypeQName2href(QName qname) {
		return Util.qname2href(Prefs.INSTANCE.getResourcePath(), TNodeType.class, qname);
	}
	
	public static String relationshipTypeQName2href(QName qname) {
		return Util.qname2href(Prefs.INSTANCE.getResourcePath(), TRelationshipType.class, qname);
	}
	
	public static String policyTypeQName2href(QName qname) {
		return Util.qname2href(Prefs.INSTANCE.getResourcePath(), TPolicyType.class, qname);
	}
	
	/**
	 * Returns the middle part of the package name or the JSP location
	 * 
	 * @param type the type
	 * @param separator the separator to be used, "." or "/"
	 * @return string which can be used "in the middle" of a package or of a
	 *         path to a JSP
	 */
	public static String getIntermediateLocationStringForType(String type, String separator) {
		String location;
		if (type.contains("ServiceTemplate")) {
			location = "servicetemplates";
		} else {
			if (type.contains("TypeImplementation")) {
				location = "entitytypeimplementations";
			} else if (type.contains("Type")) {
				location = "entitytypes";
			} else if (type.contains("Import")) {
				location = "imports";
			} else {
				assert (type.contains("Template"));
				location = "entitytemplates";
			}
			// location now is the super pkg, we have to add a pkg of the type
			location = location + separator + type.toLowerCase() + "s";
		}
		return location;
	}
	
	/**
	 * Required by topologyedit.jsp
	 * 
	 * @return all known nodetype resources
	 */
	public static Collection<NodeTypeResource> getAllNodeTypeResources() {
		@SuppressWarnings("unchecked")
		Collection<NodeTypeResource> res = (Collection<NodeTypeResource>) (Collection<?>) new NodeTypesResource().getAll();
		return res;
	}
	
	/**
	 * Required by topologyedit.jsp
	 * 
	 * @return all known relation ship type resources
	 */
	public static Collection<RelationshipTypeResource> getAllRelationshipTypeResources() {
		@SuppressWarnings("unchecked")
		Collection<RelationshipTypeResource> res = (Collection<RelationshipTypeResource>) (Collection<?>) new RelationshipTypesResource().getAll();
		return res;
	}
	
	/**
	 * @return the path to the Winery topology modeler. Required by
	 *         functions.tld
	 */
	public static String getWineryTopologyModelerPath() {
		return Prefs.INSTANCE.getWineryTopologyModelerPath();
	}
	
	public static String getScarfUtilityModuleURL() {
		return Prefs.INSTANCE.getScarfUtilityModuleURL();
	}
	
	/**
	 * Detect the mime type of the stream. The stream is marked at the beginning
	 * and reset at the end
	 * 
	 * @param is the stream
	 * @param fileName the fileName of the file belonging to the stream
	 */
	public static String getMimeType(BufferedInputStream bis, String fn) throws IOException {
		AutoDetectParser parser = new AutoDetectParser();
		Detector detector = parser.getDetector();
		Metadata md = new Metadata();
		md.add(Metadata.RESOURCE_NAME_KEY, fn);
		org.apache.tika.mime.MediaType mediaType = detector.detect(bis, md);
		return mediaType.toString();
	}
	
	
	private static final MediaType MEDIATYPE_APPLICATION_OCTET_STREAM = MediaType.valueOf("application/octet-stream");
	
	
	/**
	 * Fixes the mediaType if it is too vague (such as application/octet-stream)
	 * 
	 * @return a more fitting MediaType or the original one if it is appropriate
	 *         enough
	 */
	public static MediaType getFixedMimeType(BufferedInputStream is, String fileName, MediaType mediaType) {
		if (mediaType.equals(Utils.MEDIATYPE_APPLICATION_OCTET_STREAM)) {
			// currently, we fix application/octet-stream only
			
			// TODO: instead of using apache tika, we could hve a user-configured map storing
			//  * media type
			//  * file extension
			
			try {
				return MediaType.valueOf(Utils.getMimeType(is, fileName));
			} catch (Exception e) {
				Utils.logger.debug("Could not determine mimetype for " + fileName, e);
				// just keep the old one
				return mediaType;
			}
		} else {
			return mediaType;
		}
	}
	
	/**
	 * Converts the given object to XML.
	 * 
	 * Used in cases the given element is not annotated with @XmlRoot
	 * 
	 * We cannot use {@literal Class<? extends TExtensibleElements>} as, for
	 * instance, {@link TConstraint} does not inherit from
	 * {@link TExtensibleElements}
	 * 
	 * @param clazz the Class of the passed object, required if obj is null
	 * @param obj the object to serialize
	 */
	public static <T extends Object> Response getXML(Class<T> clazz, T obj) {
		// see commit ab4b5c547619c058990 for an implementation using getJAXBElement,
		// which can be directly passed as entity
		// the issue is that we want to have a *formatted* XML
		// Therefore, we serialize "by hand".
		String xml = Utils.getXMLAsString(clazz, obj, false);
		
		return Response.ok().type(MediaType.TEXT_XML).entity(xml).build();
	}
	
	public static <T extends Object> String getXMLAsString(Class<T> clazz, T obj, boolean includeProcessingInstruction) {
		JAXBElement<T> rootElement = Util.getJAXBElement(clazz, obj);
		Marshaller m = JAXBSupport.createMarshaller(includeProcessingInstruction);
		StringWriter w = new StringWriter();
		try {
			m.marshal(rootElement, w);
		} catch (JAXBException e) {
			Utils.logger.error("Could not put content to string", e);
			throw new IllegalStateException(e);
		}
		String res = w.toString();
		return res;
	}
	
	public static String getXMLAsString(Object obj) {
		if (obj instanceof Element) {
			// in case the object is a DOM element, we use the DOM functionality
			return Util.getXMLAsString((Element) obj);
		} else {
			return Utils.getXMLAsString(obj, false);
		}
	}
	
	public static <T extends Object> String getXMLAsString(T obj, boolean includeProcessingInstruction) {
		if (obj == null) {
			return "";
		}
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) obj.getClass();
		return Utils.getXMLAsString(clazz, obj, includeProcessingInstruction);
	}
	
	public static String getAllXSDElementDefinitionsForTypeAheadSelection() {
		Utils.logger.entry();
		try {
			return Utils.getAllXSDefinitionsForTypeAheadSelection(XSConstants.ELEMENT_DECLARATION);
		} finally {
			Utils.logger.exit();
		}
	}
	
	public static String getAllXSDTypeDefinitionsForTypeAheadSelection() {
		Utils.logger.entry();
		try {
			return Utils.getAllXSDefinitionsForTypeAheadSelection(XSConstants.TYPE_DEFINITION);
		} finally {
			Utils.logger.exit();
		}
	}
	
	public static String getAllXSDefinitionsForTypeAheadSelection(short type) {
		SortedSet<XSDImportId> allImports = Repository.INSTANCE.getAllTOSCAComponentIds(XSDImportId.class);
		
		Map<Namespace, Collection<String>> data = new HashMap<Namespace, Collection<String>>();
		
		for (XSDImportId id : allImports) {
			XSDImportResource resource = new XSDImportResource(id);
			Collection<String> allLocalNames = resource.getAllDefinedLocalNames(type);
			
			Collection<String> list;
			if ((list = data.get(id.getNamespace())) == null) {
				// list does not yet exist
				list = new ArrayList<String>();
				data.put(id.getNamespace(), list);
			}
			assert (list != null);
			
			list.addAll(allLocalNames);
		}
		
		ArrayNode rootNode = Utils.mapper.createArrayNode();
		
		// ensure ordering in JSON object
		Collection<Namespace> allns = new TreeSet<Namespace>();
		allns.addAll(data.keySet());
		
		for (Namespace ns : allns) {
			Collection<String> localNames = data.get(ns);
			if (!localNames.isEmpty()) {
				ObjectNode groupEntry = Utils.mapper.createObjectNode();
				rootNode.add(groupEntry);
				groupEntry.put("text", ns.getDecoded());
				ArrayNode children = Utils.mapper.createArrayNode();
				groupEntry.put("children", children);
				Collection<String> sortedLocalNames = new TreeSet<String>();
				sortedLocalNames.addAll(localNames);
				for (String localName : sortedLocalNames) {
					String value = "{" + ns.getDecoded() + "}" + localName;
					String text = localName;
					ObjectNode o = Utils.mapper.createObjectNode();
					o.put("text", text);
					o.put("value", value);
					children.add(o);
				}
			}
		}
		
		try {
			return Utils.mapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not create JSON", e);
		}
	}
	
	public static Response getResponseForException(Exception e) {
		String msg;
		if (e.getCause() != null) {
			msg = e.getCause().getMessage();
		} else {
			msg = e.getMessage();
		}
		Response res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
		return res;
	}
	
	/**
	 * Returns the stored type for the given template
	 * 
	 * Goes to the repository to retrieve stored data
	 * 
	 * @param template the template to determine the type for
	 */
	// we suppress "unchecked" as we use Class.forName
	@SuppressWarnings("unchecked")
	public static TEntityType getTypeForTemplate(TEntityTemplate template) {
		QName type = template.getType();
		
		// Possibilities:
		// a) try all possibly types whether an appropriate QName exists
		// b) derive type class from template class. Determine appropriate resource afterwards.
		// We go for b)
		
		String instanceResourceClassName = template.getClass().toString();
		int idx = instanceResourceClassName.lastIndexOf('.');
		// get everything from ".T", where "." is the last dot
		instanceResourceClassName = instanceResourceClassName.substring(idx + 2);
		// strip off "Template"
		instanceResourceClassName = instanceResourceClassName.substring(0, instanceResourceClassName.length() - "Template".length());
		// add "Type"
		instanceResourceClassName += "Type";
		
		// an id is required to instantiate the resource
		String idClassName = "org.eclipse.winery.common.ids.definitions." + instanceResourceClassName + "Id";
		
		String packageName = "org.eclipse.winery.repository.resources.entitytypes." + instanceResourceClassName.toLowerCase() + "s";
		// convert from NodeType to NodeTypesResource
		instanceResourceClassName += "Resource";
		instanceResourceClassName = packageName + "." + instanceResourceClassName;
		
		Utils.logger.debug("idClassName: {}", idClassName);
		Utils.logger.debug("className: {}", instanceResourceClassName);
		
		// Get instance of id class having "type" as id
		Class<? extends TOSCAComponentId> idClass;
		try {
			idClass = (Class<? extends TOSCAComponentId>) Class.forName(idClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Could not determine id class", e);
		}
		Constructor<? extends TOSCAComponentId> idConstructor;
		try {
			idConstructor = idClass.getConstructor(QName.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not get QName id constructor", e);
		}
		TOSCAComponentId typeId;
		try {
			typeId = idConstructor.newInstance(type);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("Could not instantiate type", e);
		}
		
		// now instantiate the resource, where the type belongs to
		Class<? extends AbstractComponentInstanceResource> instanceResourceClass;
		try {
			instanceResourceClass = (Class<? extends AbstractComponentInstanceResource>) Class.forName(instanceResourceClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Could not determine component instance resource class", e);
		}
		Constructor<? extends AbstractComponentInstanceResource> resConstructor;
		try {
			resConstructor = instanceResourceClass.getConstructor(typeId.getClass());
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not get contructor", e);
		}
		AbstractComponentInstanceResource typeResource;
		try {
			typeResource = resConstructor.newInstance(typeId);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("Could not instantiate resoruce", e);
		}
		
		// read the data from the resource and store it
		TEntityType entityType = (TEntityType) typeResource.getElement();
		
		return entityType;
	}
	
	/**
	 * referenced by functions.tld
	 */
	public static Boolean isContainerLocallyAvailable() {
		return Prefs.INSTANCE.isContainerLocallyAvailable();
	}
	
	/**
	 * referenced by functions.tld
	 * 
	 * We need the bridge as functions (at tld) require a static method. We did
	 * not want to put two methods in Prefs and therefore, we put the method
	 * here.
	 */
	public static Boolean isRestDocDocumentationAvailable() {
		return Prefs.INSTANCE.isRestDocDocumentationAvailable();
	}
	
	public static boolean isSuccessFulResponse(Response res) {
		return Status.fromStatusCode(res.getStatus()).getFamily().equals(Family.SUCCESSFUL);
	}
	
	/**
	 * Converts the given String to an integer. Fallback if String is a float.
	 * If String is an invalid number, "0" is returned
	 */
	public static int convertStringToInt(String number) {
		int intTop = 0;
		try {
			intTop = Integer.parseInt(number);
		} catch (NumberFormatException e) {
			try {
				float floatTop = Float.parseFloat(number);
				intTop = Math.round(floatTop);
			} catch (NumberFormatException e2) {
				// do nothing
			}
		}
		
		return intTop;
	}
	
	/**
	 * Checks whether a given resource (with absolute URL!) is available with a
	 * HEAD request on it.
	 */
	public static boolean isResourceAvailable(String path) {
		Client client = Client.create();
		WebResource wr = client.resource(path);
		boolean res;
		try {
			ClientResponse response = wr.head();
			res = (response.getClientResponseStatus().getFamily().equals(Family.SUCCESSFUL));
		} catch (com.sun.jersey.api.client.ClientHandlerException ex) {
			// In the case of a java.net.ConnectException, return false
			res = false;
		}
		return res;
	}
}
