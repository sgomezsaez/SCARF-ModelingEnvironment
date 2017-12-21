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
package org.eclipse.winery.repository.export;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;

import org.eclipse.winery.common.ids.definitions.TOSCAComponentId;

/**
 * Holds the state of ids regarding the export <br />
 * 
 * Required as we do not know at the entry point (usually a service template),
 * which other components are linked <br />
 * 
 * Users can call flagAsExportRequired more than once for the same id. If an id
 * is already exported, it is not flagged as exported again
 */
public class ExportedState {
	
	private final Collection<TOSCAComponentId> exported = new HashSet<>();
	private final Queue<TOSCAComponentId> exportRequired = new ArrayDeque<>();
	
	
	/**
	 * @return the first tosca component id to be exported, null if no more
	 *         elements are in the queue
	 */
	public TOSCAComponentId pop() {
		return this.exportRequired.poll();
	}
	
	public void flagAsExported(TOSCAComponentId id) {
		this.exportRequired.remove(id);
		this.exported.add(id);
	}
	
	/**
	 * Flags the given id as required for export, if not already exported
	 * 
	 * @param id the id to flag
	 */
	public void flagAsExportRequired(TOSCAComponentId id) {
		if (!this.exported.contains(id)) {
			this.exportRequired.add(id);
		}
	}
	
	public void flagAsExportRequired(Collection<TOSCAComponentId> ids) {
		for (TOSCAComponentId id : ids) {
			if ((!this.exported.contains(id)) && (!this.exportRequired.contains(id))) {
				this.exportRequired.add(id);
			}
		}
	}
}
