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
package org.eclipse.winery.repository.resources._support.collections.withid;

import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.winery.repository.resources._support.IPersistable;
import org.eclipse.winery.repository.resources._support.collections.EntityCollectionResource;
import org.eclipse.winery.repository.resources._support.collections.IIdDetermination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EntityWithIdCollectionResource<EntityResourceT extends EntityWithIdResource<EntityT>, EntityT> extends EntityCollectionResource<EntityResourceT, EntityT> {
	
	private static final Logger logger = LoggerFactory.getLogger(EntityWithIdCollectionResource.class);
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityWithIdCollectionResource(Class<EntityResourceT> entityResourceTClazz, Class<EntityT> entityTClazz, List<EntityT> list, IPersistable res) {
		super(entityResourceTClazz, entityTClazz, list, res);
	}
	
	/**
	 * Each CollectionResource has to implement the id getting by itself as
	 * TOSCA XSD does not provide a general purpose id fetching mechanism
	 */
	@Override
	public abstract String getId(EntityT entity);
	
	@Override
	protected EntityResourceT getEntityResourceInstance(EntityT entity, int idx) {
		Constructor<EntityResourceT> constructor;
		try {
			constructor = this.entityResourceTClazz.getConstructor(IIdDetermination.class, this.entityTClazz, int.class, List.class, this.res.getClass());
		} catch (Exception e) {
			try {
				constructor = this.entityResourceTClazz.getConstructor(IIdDetermination.class, this.entityTClazz, int.class, List.class, IPersistable.class);
			} catch (Exception e2) {
				EntityWithIdCollectionResource.logger.debug("Could not get constructor", e);
				EntityWithIdCollectionResource.logger.debug("res.getClass() was {}", this.res.getClass());
				throw new IllegalStateException(e2);
			}
		}
		EntityResourceT newInstance;
		try {
			newInstance = constructor.newInstance(this, entity, idx, this.list, this.res);
		} catch (Exception e) {
			EntityWithIdCollectionResource.logger.debug("Could not instantiate class", e);
			throw new IllegalStateException(e);
		}
		return newInstance;
	}
	
}
