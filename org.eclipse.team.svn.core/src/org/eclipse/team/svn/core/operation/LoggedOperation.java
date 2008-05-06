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

package org.eclipse.team.svn.core.operation;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorCancelException;

/**
 * Logged operation allow us to safely write to log and show error messages
 * 
 * @author Alexander Gurov
 */
public class LoggedOperation implements IActionOperation {
	protected IActionOperation op;

	public LoggedOperation(IActionOperation op) {
		this.op = op;
	}

	final public IActionOperation run(IProgressMonitor monitor) {
		IStatus status = this.op.run(monitor).getStatus();
		if (status.getSeverity() != IStatus.OK) {
			this.handleError(status);
		}
		return this.op;
	}
	
	public int getOperationWeight() {
		return IActionOperation.DEFAULT_WEIGHT;
	}
	
	public IConsoleStream getConsoleStream() {
		return this.op.getConsoleStream();
	}
	
	public void setConsoleStream(IConsoleStream stream) {
		this.op.setConsoleStream(stream);
	}

	public ISchedulingRule getSchedulingRule() {
		return this.op.getSchedulingRule();
	}
	
	public String getOperationName() {
		return this.op.getOperationName();
	}
	
	public String getId() {
		return this.op.getId();
	}
	
	public final IStatus getStatus() {
		return this.op.getStatus();
	}
	
	public int getExecutionState() {
		return this.op.getExecutionState();
	}
	
	public static void reportError(String where, Throwable t) {
		String errMessage = SVNTeamPlugin.instance().getResource("Operation.Error.LogHeader", new String[] {where});
	    MultiStatus status = new MultiStatus(SVNTeamPlugin.NATURE_ID, IStatus.OK, errMessage, null);
		Status st = 
			new Status(
					IStatus.ERROR, 
					SVNTeamPlugin.NATURE_ID, 
					IStatus.OK, 
					status.getMessage() + ": " + t.getMessage(), 
					t);
		status.merge(st);
		LoggedOperation.logError(status);
	}
	
	protected void handleError(IStatus errorStatus) {
		if (!errorStatus.isMultiStatus()) {
			Throwable ex = errorStatus.getException();
			if (!(ex instanceof SVNConnectorCancelException) && !(ex instanceof ActivityCancelledException)) {
				LoggedOperation.logError(errorStatus);
			}
			return;
        }
		
		IStatus []children = errorStatus.getChildren();
		ArrayList <IStatus>statusesWithoutCancel = new ArrayList<IStatus>(); 
        for (int i = 0; i < children.length; i++) {
            Throwable exception = children[i].getException();
        	if (!(exception instanceof SVNConnectorCancelException) && !(exception instanceof ActivityCancelledException)) {
        		statusesWithoutCancel.add(children[i]);
            }
        }
        if (statusesWithoutCancel.size() > 0) {
		    IStatus newStatus = new MultiStatus(errorStatus.getPlugin(), 
		    		errorStatus.getCode(), 
		    		statusesWithoutCancel.toArray(new IStatus[statusesWithoutCancel.size()]),
		    		errorStatus.getMessage(),
		    		errorStatus.getException());
		    LoggedOperation.logError(newStatus);
        }
	}
	
	protected static void logError(IStatus errorStatus) {
		SVNTeamPlugin.instance().getLog().log(errorStatus);
	}

}
