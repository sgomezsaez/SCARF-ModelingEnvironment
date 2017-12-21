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
package org.eclipse.winery.repository.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;

import javax.ws.rs.core.MediaType;

import org.eclipse.winery.common.RepositoryFileReference;
import org.eclipse.winery.common.ids.GenericId;
import org.eclipse.winery.common.ids.Namespace;
import org.eclipse.winery.common.ids.definitions.TOSCAComponentId;
import org.eclipse.winery.common.ids.elements.TOSCAElementId;
import org.eclipse.winery.common.interfaces.IWineryRepositoryCommon;

/**
 * Enables access to the winery repository via Ids defined in package
 * {@link org.eclipse.winery.common.ids}
 * 
 * In contrast to {@link org.eclipse.winery.repository.backend.IRepository},
 * this is NOT dependent on a particular storage format for the properties.
 * These two classes exist to make the need for reengineering explicit.
 * 
 * This is a first attempt to offer methods via GenericId. It might happen, that
 * methods, where GenericIds make sense, are simply added to "IWineryRepository"
 * instead of being added here.
 * 
 * The ultimate goal is to get rid of this class and to have
 * IWineryRepositoryCommon only.
 * 
 * Currently, this class is used internally only
 */
interface IGenericRepository extends IWineryRepositoryCommon {
	
	/**
	 * Flags the given TOSCA element as existing. The resources itself create
	 * appropriate data files.
	 * 
	 * Pre-Condition: !exists(id)<br/>
	 * Post-Condition: exists(id)
	 * 
	 * Typically, the given TOSCA element is created if a configuration is asked
	 * for
	 * 
	 * @param id
	 * @return
	 */
	public boolean flagAsExisting(GenericId id);
	
	/**
	 * Checks whether the associated TOSA element exists
	 * 
	 * @param id the id to check
	 * @return true iff the TOSCA element belonging to the given ID exists
	 */
	public boolean exists(GenericId id);
	
	/**
	 * Deletes the referenced object from the repository
	 * 
	 * @param ref
	 */
	public void forceDelete(RepositoryFileReference ref) throws IOException;
	
	/**
	 * @param ref reference to check
	 * @return true if the file associated with the given reference exists
	 */
	public boolean exists(RepositoryFileReference ref);
	
	/**
	 * Puts the given content to the given file. Replaces existing content.
	 * 
	 * If the parent of the reference does not exist, it is created.
	 * 
	 * @param ref the reference to the file. Must not be null.
	 * @param content the content to put into the file. Must not be null.
	 * @param mediaType the media type of the file. Must not be null.
	 * 
	 * @throws IOException if something goes wrong
	 */
	public void putContentToFile(RepositoryFileReference ref, String content, MediaType mediaType) throws IOException;
	
	/**
	 * Puts the given content to the given file. Replaces existing content.
	 * 
	 * If the parent of the reference does not exist, it is created.
	 * 
	 * @param ref the reference to the file
	 * @param content the content to put into the file
	 * @throws IOException if something goes wrong
	 */
	public void putContentToFile(RepositoryFileReference ref, InputStream inputStream, MediaType mediaType) throws IOException;
	
	/**
	 * Creates an opened inputStream of the contents referenced by ref. The
	 * stream has to be closed by the caller.
	 * 
	 * @param ref the reference to the file
	 * @return an inputstream
	 * @throws IOException if something goes wrong
	 */
	public InputStream newInputStream(RepositoryFileReference ref) throws IOException;
	
	/**
	 * Returns the size of the file referenced by ref
	 * 
	 * @param ref a refernce to the file stored in the repository
	 * @return the size in bytes
	 * @throws IOException if something goes wrong
	 */
	long getSize(RepositoryFileReference ref) throws IOException;
	
	/**
	 * Returns the last modification time of the entry.
	 * 
	 * @param ref the reference to the file
	 * @return the time of the last modification
	 * @throws IOException if something goes wrong
	 */
	FileTime getLastModifiedTime(RepositoryFileReference ref) throws IOException;
	
	/**
	 * Returns the mimetype belonging to the reference.
	 * 
	 * @param ref the reference to the file
	 * @return the mimetype as string
	 * @throws IOException if something goes wrong
	 * @throws IllegalStateException if an internal error occurs, which is not
	 *             an IOException
	 */
	String getMimeType(RepositoryFileReference ref) throws IOException;
	
	/**
	 * @return the last change date of the file belonging to the given
	 *         reference. NULL if the associated file does not exist.
	 */
	Date getLastUpdate(RepositoryFileReference ref);
	
	/**
	 * Returns all components available of the given id type
	 * 
	 * @param idClass class of the Ids to search for
	 * @return empty set if no ids are available
	 */
	public <T extends TOSCAComponentId> SortedSet<T> getAllTOSCAComponentIds(Class<T> idClass);
	
	/**
	 * Returns the set of <em>all</em> ids nested in the given reference
	 * 
	 * The generated Ids are linked as child to the id associated to the given
	 * reference
	 * 
	 * Required for getting plans nested in a service template: plans are nested
	 * below the PlansOfOneServiceTemplateId
	 * 
	 * @param ref a reference to the TOSCA element to be checked. The path
	 *            belonging to this element is checked.
	 * @param idClass
	 * @return the set of Ids nested in the given reference. Empty set if there
	 *         are no or the reference itself does not exist.
	 */
	public <T extends TOSCAElementId> SortedSet<T> getNestedIds(GenericId ref, Class<T> idClass);
	
	/**
	 * Returns the set of files nested in the given reference
	 */
	public SortedSet<RepositoryFileReference> getContainedFiles(GenericId id);
	
	/**
	 * Returns all namespaces used by all known TOSCA components
	 */
	public Collection<Namespace> getUsedNamespaces();
	
}
