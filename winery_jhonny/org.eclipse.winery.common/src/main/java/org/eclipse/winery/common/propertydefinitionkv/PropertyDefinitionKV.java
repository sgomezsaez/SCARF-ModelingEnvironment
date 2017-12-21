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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PropertyDefinition")
public class PropertyDefinitionKV {
	
	private String key;
	private String type;
	
	
	public PropertyDefinitionKV() {
		super();
	}
	
	public PropertyDefinitionKV(String key, String type) {
		super();
		this.setKey(key);
		this.setType(type);
	}
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(String key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		this.key = key;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return this.key.hashCode();
	}
}
