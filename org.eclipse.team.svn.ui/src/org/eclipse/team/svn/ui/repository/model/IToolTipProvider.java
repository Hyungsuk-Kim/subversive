/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexey Mikoyan - Initial implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.model;

/**
 * Implementors of this interface will provide interested parts with
 * tooltip message
 *
 * @author Alexey Mikoyan
 *
 */
public interface IToolTipProvider {
	public String getToolTipMessage(String formatString);
}
