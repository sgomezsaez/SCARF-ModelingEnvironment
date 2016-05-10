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
package org.eclipse.winery.repository.resources;

import org.eclipse.winery.common.ids.Namespace;
import org.eclipse.winery.repository.TestWithRepositoryConnection;

public abstract class TestResource extends TestWithRepositoryConnection {
	
	protected static final String TESTNS = "http://www.example.org/winery/test/resources";
	protected static final Namespace NS = new Namespace(TestResource.TESTNS, false);
	
}
