/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.update.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.MarkAsMergedOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.operation.ClearUpdateStatusesOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.update.UpdateSyncInfo;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.utility.UnacceptableOperationNotificator;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view <mark as merged> action implementation
 * 
 * @author Alexander Gurov
 */
public class MarkAsMergedAction extends AbstractSynchronizeModelAction {

	public MarkAsMergedAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                if (super.select(info)) {
                    UpdateSyncInfo sync = (UpdateSyncInfo)info;
                    boolean localIsFile = sync.getLocalResource().getResource() instanceof IFile;
                    boolean remoteIsFile = ((ResourceVariant)sync.getRemote()).getResource() instanceof ILocalFile;
                    return 
                    	!IStateFilter.SF_OBSTRUCTED.accept(sync.getLocal(), sync.getLocalResource().getStatus(), sync.getLocalResource().getChangeMask()) &&
                    	localIsFile == remoteIsFile;
                }
                return false;
            }
        };
	}

	protected IActionOperation execute(FilteredSynchronizeModelOperation operation) {
		IResource []resources = UnacceptableOperationNotificator.shrinkResourcesWithNotOnRespositoryParents(operation.getShell(), operation.getSelectedResources());
		if (resources == null || resources.length == 0) {
			return null;
		}
		
		MarkAsMergedOperation mainOp = new MarkAsMergedOperation(resources, false, null);

		CompositeOperation op = new CompositeOperation(mainOp.getId());

		op.add(mainOp);
		op.add(new ClearUpdateStatusesOperation(resources));
		op.add(new RefreshResourcesOperation(FileUtility.getParents(resources, false)));

		return op;
	}

}
