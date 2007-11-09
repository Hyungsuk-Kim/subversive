/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.client.ISVNClient;
import org.eclipse.team.svn.core.client.SVNEntryInfo;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.AbstractSVNStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;


/**
 * SVN storage provider based on java.io.File
 * 
 * @author Alexander Gurov
 */
public class SVNFileStorage extends AbstractSVNStorage implements IFileStorage {
	public static final String STATE_INFO_FILE_NAME = ".externalSVNRepositories";
	
	private static SVNFileStorage instance = new SVNFileStorage();
	
	public static SVNFileStorage instance() {
		return SVNFileStorage.instance;
	}
    
	public void initialize(IPath stateInfoLocation) throws Exception {
		this.initializeImpl(stateInfoLocation, SVNFileStorage.STATE_INFO_FILE_NAME);
	}
	
	public IRepositoryResource asRepositoryResource(File file, boolean allowsNull) {
		// check if this resource is placed in working copy
		File wcRoot = file;
		SVNEntryInfo info = null;
		ISVNClient proxy = CoreExtensionsManager.instance().getSVNClientWrapperFactory().newInstance();
		try {
			while (info == null) {
				if (wcRoot == null) {
					// no WC found
					if (allowsNull) {
						return null;
					}
					throw new RuntimeException(file.getAbsolutePath() + " is not under version control");
				}
				info = SVNUtility.getSVNInfo(wcRoot, proxy);
				if (info == null) {
					wcRoot = wcRoot.getParentFile();
				}
			}
		}
		finally {
			proxy.dispose();
		}
		
		String wcUrl = SVNUtility.decodeURL(info.url);
		String rootUrl = SVNUtility.decodeURL(info.reposRootUrl);
		IRepositoryLocation location = this.findLocation(wcUrl, rootUrl);
		
		if (wcRoot != file) {
			wcUrl += file.getAbsolutePath().substring(wcRoot.getAbsolutePath().length());
		}
		
		return file.isFile() ? (IRepositoryResource)location.asRepositoryFile(wcUrl, allowsNull) : location.asRepositoryContainer(wcUrl, allowsNull);
	}
	
	protected IRepositoryLocation findLocation(String resourceUrl, String rootUrl) {
		Path url = new Path(resourceUrl);
		IRepositoryLocation []locations = this.getRepositoryLocations();
		for (int i = 0; i < locations.length; i++) {
			if (new Path(locations[i].getUrl()).isPrefixOf(url)) {
				return locations[i];
			}
		}
		for (int i = 0; i < locations.length; i++) {
			if (locations[i].getRepositoryRootUrl().equals(rootUrl)) {
				return locations[i];
			}
		}
		IRepositoryLocation location = this.newRepositoryLocation(";" + rootUrl);
		this.addRepositoryLocation(location);
		return location;
	}

	private SVNFileStorage() {
		super();
	}

	protected IRepositoryLocation wrapLocationIfRequired(IRepositoryLocation location, String url, boolean isFile) {
		return location;
	}

}
