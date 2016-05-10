/*******************************************************************************
 * Copyright (c) 2013 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.common;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.common.constants.Namespaces;
import org.eclipse.winery.common.constants.QNames;
import org.eclipse.winery.common.propertydefinitionkv.PropertyDefinitionKV;
import org.eclipse.winery.common.propertydefinitionkv.PropertyDefinitionKVList;
import org.eclipse.winery.common.propertydefinitionkv.WinerysPropertiesDefinition;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions;
import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TCapabilityDefinition;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TEntityType;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.model.tosca.TNodeTemplate.Capabilities;
import org.eclipse.winery.model.tosca.TNodeTemplate.Requirements;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TPlan;
import org.eclipse.winery.model.tosca.TPlans;
import org.eclipse.winery.model.tosca.TRelationshipTemplate;
import org.eclipse.winery.model.tosca.TRelationshipTemplate.SourceElement;
import org.eclipse.winery.model.tosca.TRelationshipTemplate.TargetElement;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.model.tosca.TRequirement;
import org.eclipse.winery.model.tosca.TRequirementDefinition;
import org.eclipse.winery.model.tosca.TServiceTemplate;
import org.eclipse.winery.model.tosca.TTopologyTemplate;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class ModelUtilities {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ModelUtilities.class);
	
	
	/**
	 * This is a special method for Winery. Winery allows to define a property
	 * definition by specifying name/type values. Instead of parsing the
	 * extensible elements returned TDefinitions, this method is a convenience
	 * method to access this information
	 * 
	 * @param t the entitytype to read the properties definition from
	 * @return a WinerysPropertiesDefinition object, which includes a map of
	 *         name/type-pairs denoting the associated property definitions. A
	 *         default element name and namespace is added if it is not defined
	 *         in the underlying XML. null if no Winery specific KV properties
	 *         are defined for the given entity type
	 */
	public static WinerysPropertiesDefinition getWinerysPropertiesDefinition(TEntityType et) {
		// similar implementation as org.eclipse.winery.repository.resources.entitytypes.properties.PropertiesDefinitionResource.getListFromEntityType(TEntityType)
		WinerysPropertiesDefinition res = null;
		for (Object o : et.getAny()) {
			if (o instanceof WinerysPropertiesDefinition) {
				res = (WinerysPropertiesDefinition) o;
			}
		}
		
		if (res != null) {
			// we put defaults if elementname and namespace have not been set
			
			if (res.getElementName() == null) {
				res.setElementName("Properties");
			}
			
			if (res.getNamespace() == null) {
				// we use the targetnamespace of the original element
				String ns = et.getTargetNamespace();
				if (!ns.endsWith("/")) {
					ns += "/";
				}
				ns += "propertiesdefinition/winery";
				res.setNamespace(ns);
			}
		}
		
		return res;
	}
	
	/**
	 * This is a special method for Winery. Winery allows to define a property
	 * by specifying name/value values. Instead of parsing the XML contained in
	 * TNodeType, this method is a convenience method to access this information
	 * 
	 * The return type "Properties" is used because of the key/value properties.
	 * 
	 * @param template the node template to get the associated properties
	 */
	public static Properties getPropertiesKV(TEntityTemplate template) {
		Properties properties = new Properties();
		org.eclipse.winery.model.tosca.TEntityTemplate.Properties tprops = template.getProperties();
		if (tprops != null) {
			// no checking for validity, just reading
			Element el = (Element) tprops.getAny();
			if (el == null) {
				// somehow invalid .tosca. We return empty properties instead of throwing a NPE
				return properties;
			}
			NodeList childNodes = el.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				if (item instanceof Element) {
					String key = item.getLocalName();
					String value = item.getTextContent();
					properties.put(key, value);
				}
			}
		}
		return properties;
	}
	
	/**
	 * This is a special method for Winery. Winery allows to define a property
	 * by specifying name/value values. We convert the given Properties to XML.
	 * 
	 * @param wpd the Winery's properties definition of the type of the given
	 *            template (i.e., wpd =
	 *            getWinerysPropertiesDefinition(template.getType()))
	 * @param template the node template to set the associated properties
	 */
	public static void setPropertiesKV(WinerysPropertiesDefinition wpd, TEntityTemplate template, Properties properties) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ModelUtilities.logger.debug(e.getMessage(), e);
			throw new IllegalStateException("Could not instantiate document builder", e);
		}
		Document doc = db.newDocument();
		
		Element root = doc.createElementNS(wpd.getNamespace(), wpd.getElementName());
		doc.appendChild(root);
		
		// we produce the serialization in the same order the XSD would be generated (because of the usage of xsd:sequence)
		for (PropertyDefinitionKV prop : wpd.getPropertyDefinitionKVList()) {
			// we always write the element tag as the XSD forces that
			Element element = doc.createElementNS(wpd.getNamespace(), prop.getKey());
			root.appendChild(element);
			String value = properties.getProperty(prop.getKey());
			if (value != null) {
				Text text = doc.createTextNode(value);
				element.appendChild(text);
			}
		}
		
		org.eclipse.winery.model.tosca.TEntityTemplate.Properties tprops = new org.eclipse.winery.model.tosca.TEntityTemplate.Properties();
		tprops.setAny(doc.getDocumentElement());
		template.setProperties(tprops);
	}
	
	/**
	 * Generates a XSD when Winery's K/V properties are used. This method is put
	 * here instead of WinerysPropertiesDefinitionResource to avoid generating
	 * the subresource
	 * 
	 * public because of the usage by TOSCAEXportUtil
	 * 
	 * @return empty Document, if Winery's Properties Definition is not fully
	 *         filled (e.g., no wrapping element defined)
	 */
	public static Document getWinerysPropertiesDefinitionXSDAsDocument(WinerysPropertiesDefinition wpd) {
		/*
		 * This is a quick hack: an XML schema container is created for each
		 * element. Smarter solution: create a hash from namespace to XML schema
		 * element and re-use that for each new element
		* Drawback of "smarter" solution: not a single XSD file any more
		 */
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ModelUtilities.logger.debug(e.getMessage(), e);
			throw new IllegalStateException("Could not instantiate document builder", e);
		}
		Document doc = docBuilder.newDocument();
		
		if (!ModelUtilities.allRequiredFieldsNonNull(wpd)) {
			// wpd not fully filled -> valid XSD cannot be provided
			// fallback: add comment and return "empty" document
			Comment comment = doc.createComment("Required fields are missing in Winery's key/value properties definition.");
			doc.appendChild(comment);
			return doc;
		}
		
		// create XSD schema container
		Element schemaElement = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
		doc.appendChild(schemaElement);
		schemaElement.setAttribute("elementFormDefault", "qualified");
		schemaElement.setAttribute("attributeFormDefault", "unqualified");
		schemaElement.setAttribute("targetNamespace", wpd.getNamespace());
		
		// create XSD element itself
		Element el = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element");
		schemaElement.appendChild(el);
		el.setAttribute("name", wpd.getElementName());
		Element el2 = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");
		el.appendChild(el2);
		el = el2;
		el2 = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "sequence");
		el.appendChild(el2);
		el = el2;
		
		// currently, "xsd" is a hardcoded prefix in the type definition
		el.setAttribute("xmlns:xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		for (PropertyDefinitionKV prop : wpd.getPropertyDefinitionKVList()) {
			el2 = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element");
			el.appendChild(el2);
			el2.setAttribute("name", prop.getKey());
			// prop.getType has the prefix included
			el2.setAttribute("type", prop.getType());
		}
		
		return doc;
	}
	
	/**
	 * Removes an existing Winery's Properties definition. If no such definition
	 * exists, the TEntityType is not modified
	 */
	public static void removeWinerysPropertiesDefinition(TEntityType et) {
		for (Iterator<Object> iterator = et.getAny().iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof WinerysPropertiesDefinition) {
				iterator.remove();
				break;
			}
		}
	}
	
	public static void replaceWinerysPropertiesDefinition(TEntityType et, WinerysPropertiesDefinition wpd) {
		ModelUtilities.removeWinerysPropertiesDefinition(et);
		et.getAny().add(wpd);
	}
	
	public static String getBorderColor(TNodeType nt) {
		String borderColor = nt.getOtherAttributes().get(QNames.QNAME_BORDER_COLOR);
		if (borderColor == null) {
			borderColor = Util.getColor(nt.getName());
		}
		return borderColor;
	}
	
	public static String getColor(TRelationshipType rt) {
		String color = rt.getOtherAttributes().get(QNames.QNAME_COLOR);
		if (color == null) {
			color = Util.getColor(rt.getName());
		}
		return color;
	}
	
	/**
	 * Returns the Properties. If no properties exist, the element is created
	 * 
	 * @return
	 */
	public static org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties getProperties(TBoundaryDefinitions defs) {
		org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties properties = defs.getProperties();
		if (properties == null) {
			properties = new org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties();
			defs.setProperties(properties);
		}
		return properties;
	}
	
	/**
	 * Special method to get the name of an extensible element as the TOSCA
	 * specification does not have a separate super type for elements with a
	 * name
	 * 
	 * {@link
	 * org.eclipse.winery.common.Util.instanceSupportsNameAttribute(Class<?
	 * extends TOSCAComponentId>)} is related
	 * 
	 * @param e the extensible element offering a name attribute (besides an id
	 *            attribute)
	 * @return the name of the extensible element
	 * @throws IllegalStateException if e does not offer the method "getName"
	 */
	public static String getName(TExtensibleElements e) {
		Method method;
		Object res;
		try {
			method = e.getClass().getMethod("getName");
			res = method.invoke(e);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return (String) res;
	}
	
	/**
	 * Returns the name of the given element. If the name does not exist or is
	 * empty, the id is returned
	 * 
	 * {@see getName}
	 * 
	 * @return the name if there is a name field, if not, the id is returned. In
	 *         case there is a Name field,
	 */
	public static String getNameWithIdFallBack(TExtensibleElements ci) {
		Method method;
		String res = null;
		try {
			method = ci.getClass().getMethod("getName");
			res = (String) method.invoke(ci);
		} catch (Exception e) {
		}
		if (StringUtils.isEmpty(res)) {
			try {
				method = ci.getClass().getMethod("getId");
				res = (String) method.invoke(ci);
			} catch (Exception e2) {
				throw new IllegalStateException(e2);
			}
		}
		return res;
	}
	
	/**
	 * Special method to set the name of an extensible element as the TOSCA
	 * specification does not have a separate super type for elements with a
	 * name
	 * 
	 * @param e the extensible element offering a name attribute (besides an id
	 *            attribute)
	 * @param name the new name
	 * @throws IllegalStateException if e does not offer the method "getName"
	 */
	public static void setName(TExtensibleElements e, String name) {
		Method method;
		try {
			method = e.getClass().getMethod("setName", String.class);
			method.invoke(e, name);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	public static boolean allRequiredFieldsNonNull(WinerysPropertiesDefinition wpd) {
		boolean valid = wpd.getNamespace() != null;
		valid = valid && (wpd.getElementName() != null);
		if (valid) {
			PropertyDefinitionKVList propertyDefinitionKVList = wpd.getPropertyDefinitionKVList();
			valid = (propertyDefinitionKVList != null);
			if (valid) {
				for (PropertyDefinitionKV def : propertyDefinitionKVList) {
					valid = valid && (def.getKey() != null);
					valid = valid && (def.getType() != null);
				}
			}
		}
		return valid;
	}
	
	/**
	 * @return null if no explicit left is set
	 */
	public static String getLeft(TNodeTemplate nodeTemplate) {
		Map<QName, String> otherAttributes = nodeTemplate.getOtherAttributes();
		String left = otherAttributes.get(new QName(Namespaces.TOSCA_WINERY_EXTENSIONS_NAMESPACE, "x"));
		return left;
	}
	
	/**
	 * @return null if no explicit left is set
	 */
	public static String getTop(TNodeTemplate nodeTemplate) {
		Map<QName, String> otherAttributes = nodeTemplate.getOtherAttributes();
		String top = otherAttributes.get(new QName(Namespaces.TOSCA_WINERY_EXTENSIONS_NAMESPACE, "y"));
		return top;
	}
	
	/**
	 * locates targetObjectRef inside a topology template
	 * 
	 * @param topologyTemplate the topology template to search in
	 * @param targetObjectRef the object ref as String
	 * 
	 * @return null if not found, otherwise the entity template in the topology
	 */
	public static TEntityTemplate findNodeTemplateOrRequirementOfNodeTemplateOrCapabilityOfNodeTemplateOrRelationshipTemplate(TTopologyTemplate topologyTemplate, String targetObjectRef) {
		// We cannot use XMLs id pointing capabilities as we work on the Java model
		// Other option: modify the stored XML directly. This is more error prune than walking through the whole topology
		for (TEntityTemplate t : topologyTemplate.getNodeTemplateOrRelationshipTemplate()) {
			if (t instanceof TNodeTemplate) {
				if (t.getId().equals(targetObjectRef)) {
					return t;
				}
				TNodeTemplate nt = (TNodeTemplate) t;
				
				Requirements requirements = nt.getRequirements();
				if (requirements != null) {
					for (TRequirement req : requirements.getRequirement()) {
						if (req.getId().equals(targetObjectRef)) {
							return req;
						}
					}
				}
				
				Capabilities capabilities = nt.getCapabilities();
				if (capabilities != null) {
					for (TCapability cap : capabilities.getCapability()) {
						if (cap.getId().equals(targetObjectRef)) {
							return cap;
						}
					}
				}
				
			} else {
				assert (t instanceof TRelationshipTemplate);
				if (t.getId().equals(targetObjectRef)) {
					return t;
				}
			}
		}
		
		// no return hit inside the loop: nothing was found
		return null;
	}
	
	/**
	 * Returns the id of the given element
	 * 
	 * The TOSCA specification does NOT always put an id field. In the case of
	 * EntityTypes and EntityTypeImplementations, there is no id, but a name
	 * field
	 * 
	 * This method abstracts from that fact.
	 */
	public static String getId(TExtensibleElements ci) {
		Method method;
		Object res;
		try {
			method = ci.getClass().getMethod("getId");
			res = method.invoke(ci);
		} catch (Exception e) {
			// If no "getId" method is there, we try "getName"
			try {
				method = ci.getClass().getMethod("getName");
				res = method.invoke(ci);
			} catch (Exception e2) {
				throw new IllegalStateException(e2);
			}
		}
		return (String) res;
	}
	
	/**
	 * Resolves a given id as requirement in the given ServiceTemplate
	 * 
	 * @return null if not found
	 */
	public static TRequirement resolveRequirement(TServiceTemplate serviceTemplate, String reference) {
		TRequirement resolved = null;
		for (TEntityTemplate tmpl : serviceTemplate.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate()) {
			if (tmpl instanceof TNodeTemplate) {
				TNodeTemplate n = (TNodeTemplate) tmpl;
				Requirements requirements = n.getRequirements();
				if (requirements != null) {
					for (TRequirement req : n.getRequirements().getRequirement()) {
						if (req.getId().equals(reference)) {
							resolved = req;
						}
					}
				}
			}
		}
		return resolved;
	}
	
	public static TCapability resolveCapability(TServiceTemplate serviceTemplate, String reference) {
		TCapability resolved = null;
		for (TEntityTemplate tmpl : serviceTemplate.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate()) {
			if (tmpl instanceof TNodeTemplate) {
				TNodeTemplate n = (TNodeTemplate) tmpl;
				Capabilities capabilities = n.getCapabilities();
				if (capabilities != null) {
					for (TCapability cap : n.getCapabilities().getCapability()) {
						if (cap.getId().equals(reference)) {
							resolved = cap;
						}
					}
				}
			}
		}
		return resolved;
	}
	
	public static TNodeTemplate resolveNodeTemplate(TServiceTemplate serviceTemplate, String reference) {
		TNodeTemplate resolved = null;
		for (TEntityTemplate tmpl : serviceTemplate.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate()) {
			if (tmpl instanceof TNodeTemplate) {
				TNodeTemplate n = (TNodeTemplate) tmpl;
				if (n.getId().equals(reference)) {
					resolved = n;
				}
			}
		}
		return resolved;
	}
	
	public static TRelationshipTemplate resolveRelationshipTemplate(TServiceTemplate serviceTemplate, String reference) {
		TRelationshipTemplate resolved = null;
		for (TEntityTemplate tmpl : serviceTemplate.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate()) {
			if (tmpl instanceof TRelationshipTemplate) {
				TRelationshipTemplate n = (TRelationshipTemplate) tmpl;
				if (n.getId().equals(reference)) {
					resolved = n;
				}
			}
		}
		return resolved;
	}
	
	public static TPlan resolvePlan(TServiceTemplate serviceTemplate, String reference) {
		TPlan resolved = null;
		TPlans plans = serviceTemplate.getPlans();
		if (plans == null) {
			return null;
		}
		for (TPlan p : plans.getPlan()) {
			if (p.getId().equals(reference)) {
				resolved = p;
			}
		}
		return resolved;
	}

	/**
	 * Sets the x coordinate of a {@link TNodeTemplate}.
	 *
	 * @param nodeTemplate
	 * 			 the nodeTemplate to be altered
	 * @param coordinate
	 * 			 the value of the coordinate to be set
	 * @return
	 * 			 the altered {@link TNodeTemplate}
	 */
	public static TNodeTemplate setLeft(TNodeTemplate nodeTemplate, String coordinate) {

		Map<QName, String> otherNodeTemplateAttributes = nodeTemplate.getOtherAttributes();
		otherNodeTemplateAttributes.put(new QName(Namespaces.TOSCA_WINERY_EXTENSIONS_NAMESPACE, "x"), coordinate);

		return nodeTemplate;
	}

	/**
	 * Sets the y coordinate of a {@link TNodeTemplate}.
	 *
	 * @param nodeTemplate
	 * 			 the nodeTemplate to be altered
	 * @param coordinate
	 * 			 the value of the coordinate to be set
	 * @return
	 * 			 the altered {@link TNodeTemplate}
	 */
	public static TNodeTemplate setTop(TNodeTemplate nodeTemplate, String coordinate) {

		Map<QName, String> otherNodeTemplateAttributes = nodeTemplate.getOtherAttributes();
		otherNodeTemplateAttributes.put(new QName(Namespaces.TOSCA_WINERY_EXTENSIONS_NAMESPACE, "y"), coordinate);

		return nodeTemplate;

	}

	/**
	 * This method instantiates a {@link TNodeTemplate} for a given {@link TNodeType}.
	 *
	 * @param nodeType
	 *            the {@link TNodeType} used for the {@link TNodeTemplate} instantiation.
	 *
	 * @return the instantiated {@link TNodeTemplate}
	 */
	public static TNodeTemplate instantiateNodeTemplate(TNodeType nodeType) {

		TNodeTemplate nodeTemplate = new TNodeTemplate();

		nodeTemplate.setId(UUID.randomUUID().toString());
		nodeTemplate.setName(nodeType.getName());
		nodeTemplate.setType(new QName(nodeType.getTargetNamespace(), nodeType.getName()));

		// add capabilities to the NodeTemplate
		if (nodeType.getCapabilityDefinitions() != null) {
			for (TCapabilityDefinition cd : nodeType.getCapabilityDefinitions().getCapabilityDefinition()) {
				TCapability capa = new TCapability();
				capa.setId(UUID.randomUUID().toString());
				capa.setName(cd.getCapabilityType().getLocalPart());
				capa.setType(new QName(cd.getCapabilityType().getNamespaceURI(), cd.getCapabilityType().getLocalPart()));
				nodeTemplate.setCapabilities(new Capabilities());
				nodeTemplate.getCapabilities().getCapability().add(capa);
			}
		}

		// add requirements
		if (nodeType.getRequirementDefinitions() != null && nodeType.getRequirementDefinitions().getRequirementDefinition() != null) {
			Requirements requirementsNode = new Requirements();
			nodeTemplate.setRequirements(requirementsNode);
			for (TRequirementDefinition definition : nodeType.getRequirementDefinitions().getRequirementDefinition()) {
				TRequirement newRequirement = new TRequirement();
				newRequirement.setName(definition.getName());
				newRequirement.setId(definition.getName());
				newRequirement.setType(definition.getRequirementType());
				nodeTemplate.getRequirements().getRequirement().add(newRequirement);
			}
		}

		return nodeTemplate;
	}

	/**
	 * This method instantiates a {@link TRelationshipTemplate} for a given {@link TRelationshipType}.
	 *
	 * @param nodeType
	 *            the {@link TRelationshipType} used for the {@link TRelationshipTemplate} instantiation.
	 * @param sourceNodeTemplate
	 *            the source {@link TNodeTemplate} of the connection
	 * @param targetNodeTemplate
	 *            the target {@link TNodeTemplate} of the connection
	 *
	 * @return the instantiated {@link TRelationshipTemplate}
	 */
	public static TRelationshipTemplate instantiateRelationshipTemplate(TRelationshipType relationshipType, TNodeTemplate sourceNodeTemplate, TNodeTemplate targetNodeTemplate) {

		TRelationshipTemplate relationshipTemplate = new TRelationshipTemplate();
		relationshipTemplate.setId(UUID.randomUUID().toString());
		relationshipTemplate.setName(relationshipType.getName());
		relationshipTemplate.setType(new QName(relationshipType.getTargetNamespace(), relationshipType.getName()));

		// connect the NodeTemplates
		SourceElement source = new SourceElement();
		source.setRef(sourceNodeTemplate);
		relationshipTemplate.setSourceElement(source);
		TargetElement target = new TargetElement();
		target.setRef(targetNodeTemplate);
		relationshipTemplate.setTargetElement(target);

		return relationshipTemplate;
	}
}
