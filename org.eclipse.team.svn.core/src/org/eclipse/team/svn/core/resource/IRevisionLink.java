/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.resource;

/**
 * Repository revision link
 * 
 * @author Igor Burilo
 */
public interface IRevisionLink {

	public void setComment(String comment);
	public String getComment();	
	public IRepositoryResource getRepositoryResource();
}
