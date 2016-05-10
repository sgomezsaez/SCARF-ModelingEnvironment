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
package org.eclipse.winery.common.propertydefinitionkv;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.winery.common.constants.Namespaces;

/**
 * This is Winery's main extension element for a key/value based properties
 * definition
 */
@XmlRootElement(name = "PropertiesDefinition")
public class WinerysPropertiesDefinition {
	
	private String namespace;
	private String elementName;
	private PropertyDefinitionKVList propertyDefinitionKVList;
	private Boolean isDerivedFromXSD = Boolean.FALSE;
	
	
	@XmlAttribute(name = "namespace")
	public String getNamespace() {
		return this.namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	@XmlAttribute(name = "elementname")
	public String getElementName() {
		return this.elementName;
	}
	
	public void setElementName(String localName) {
		this.elementName = localName;
	}
	
	@XmlElement(name = "properties")
	public PropertyDefinitionKVList getPropertyDefinitionKVList() {
		return this.propertyDefinitionKVList;
	}
	
	public void setPropertyDefinitionKVList(PropertyDefinitionKVList propertyDefinitionKVList) {
		this.propertyDefinitionKVList = propertyDefinitionKVList;
	}
	
	/**
	 * @return null if not derived from XSD, "Boolean.TRUE" otherwise. This
	 *         leads JAXB to write the attribute only if derivedFromXSD is true
	 */
	@XmlAttribute(name = "derivedFromXSD", namespace = Namespaces.TOSCA_WINERY_EXTENSIONS_NAMESPACE)
	public Boolean getIsDerivedFromXSD() {
		if ((this.isDerivedFromXSD != null) && (this.isDerivedFromXSD)) {
			return Boolean.TRUE;
		} else {
			return null;
		}
	}
	
	public void setIsDerivedFromXSD(Boolean isDerivedFromXSD) {
		this.isDerivedFromXSD = isDerivedFromXSD;
	}
	
}
