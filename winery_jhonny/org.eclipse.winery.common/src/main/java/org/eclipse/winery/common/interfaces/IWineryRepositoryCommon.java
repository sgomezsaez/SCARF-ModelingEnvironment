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
package org.eclipse.winery.common.interfaces;

import java.io.IOException;

import org.eclipse.winery.common.ids.GenericId;

/**
 * Enables access to the winery repository via Ids defined in package
 * {@link org.eclipse.winery.common.ids}
 * 
 * Methods are moved from
 * {@link org.eclipse.winery.repository.backend.IGenericRepository} to here as
 * soon there is an implementation for them. The ultimate goal is to eliminate
 * IGenericRepository
 * 
 * These methods are shared between {@link IWineryRepository} and
 * {@link org.eclipse.winery.repository.backend.IRepository}
 */
public interface IWineryRepositoryCommon {
	
	/**
	 * Deletes the TOSCA element <b>and all sub elements</b> referenced by the
	 * given id from the repository
	 * 
	 * We assume that each id is a directory
	 * 
	 * @param id
	 */
	public void forceDelete(GenericId id) throws IOException;
	
}
