/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.action.remote.management;

import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRevisionLink;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.repository.model.RepositoryRevision;

/**
 * Discard revision links on the repository location
 * 
 * @author Alexander Gurov
 */
public class DiscardRevisionLinksAction extends AbstractRepositoryTeamAction {

	public DiscardRevisionLinksAction() {
		super();
	}

	public void runImpl(IAction action) {
		final RepositoryRevision []revisions = ((RepositoryRevision [])this.getAdaptedSelection(RepositoryRevision.class));
		DiscardConfirmationDialog dialog = new DiscardConfirmationDialog(this.getShell(), revisions.length == 1, DiscardConfirmationDialog.MSG_LINK);
		if (dialog.open() == 0) {
			HashSet<IRepositoryLocation> locations = new HashSet<IRepositoryLocation>();
			for (int i = 0; i < revisions.length; i++) {
				locations.add(revisions[i].getRevisionLink().getRepositoryResource().getRepositoryLocation());
			}
			AbstractActionOperation mainOp = new AbstractActionOperation("Operation_RemoveRevisionLinks", SVNUIMessages.class) { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					for (int i = 0; i < revisions.length; i++) {
						final IRevisionLink link = revisions[i].getRevisionLink();
						this.protectStep(new IUnprotectedOperation() {
							public void run(IProgressMonitor monitor) throws Exception {
								IRepositoryLocation location = link.getRepositoryResource().getRepositoryLocation();
								location.removeRevisionLink(link);
							}
						}, monitor, revisions.length);
					}
				}
			};
			CompositeOperation op = new CompositeOperation(mainOp.getId(), mainOp.getMessagesClass());
			op.add(mainOp);
			op.add(new SaveRepositoryLocationsOperation());
			op.add(new RefreshRepositoryLocationsOperation(locations.toArray(new IRepositoryLocation[locations.size()]), true));
			
			this.runBusy(op);
		}
	}
	
	public boolean isEnabled() {
		return this.getAdaptedSelection(RepositoryRevision.class).length > 0;
	}

}
