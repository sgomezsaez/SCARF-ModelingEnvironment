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
package org.eclipse.winery.repository.resources.imports.genericimports;

import org.eclipse.winery.common.ids.definitions.imports.GenericImportId;
import org.eclipse.winery.repository.resources.AbstractComponentsResource;

/**
 * Manages a certain kind of imports without special treatments
 */
public class GenericImportsResource extends AbstractComponentsResource<GenericImportResource> {
	
	private String type;
	
	
	/**
	 * @param id the (decoded) id, e.g., http://schemas.xmlsoap.org/wsdl/
	 */
	public GenericImportsResource(String id) {
		this.type = id;
	}
	
	@Override
	public GenericImportResource getComponentInstaceResource(String namespace, String id, boolean encoded) {
		GenericImportId iId = new GenericImportId(namespace, id, encoded, this.type);
		return new GenericImportResource(iId);
	}
	
}
