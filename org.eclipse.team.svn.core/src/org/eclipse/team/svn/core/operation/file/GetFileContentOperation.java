/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.file;

import java.io.File;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * This operation retrieve a content of the file in selected revision
 * 
 * @author Alexander Gurov
 */
public class GetFileContentOperation extends AbstractFileOperation {
	public static final int DEFAULT_BUFFER_SIZE = 2048;
	
	protected SVNRevision revision;
	protected SVNRevision pegRevision;
	protected int bufferSize;
	protected OutputStream target;

	public GetFileContentOperation(File file, SVNRevision revision, SVNRevision pegRevision, OutputStream target) {
		this(file, revision, pegRevision, GetFileContentOperation.DEFAULT_BUFFER_SIZE, target);
	}
	
	public GetFileContentOperation(File file, SVNRevision revision, SVNRevision pegRevision, int bufferSize, OutputStream target) {
		super("Operation_GetFileContent", SVNMessages.class, new File[] {file}); //$NON-NLS-1$
		this.revision = revision;
		this.pegRevision = pegRevision;
		this.bufferSize = bufferSize;
		this.target = target;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		
	    IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(file, false);
		ISVNConnector proxy = remote.getRepositoryLocation().acquireSVNProxy();
		try {
			SVNRevision.Kind kind = this.revision.getKind();
			if (kind == Kind.BASE || kind == Kind.WORKING) {
				proxy.streamFileContent(new SVNEntryRevisionReference(file.getAbsolutePath(), null, this.revision), this.bufferSize, this.target, new SVNProgressMonitor(this, monitor, null));
			}
			else {
				proxy.streamFileContent(SVNUtility.getEntryRevisionReference(remote), this.bufferSize, this.target, new SVNProgressMonitor(this, monitor, null));
			}
		}
		finally {
		    remote.getRepositoryLocation().releaseSVNProxy(proxy);
		}
	}

}
