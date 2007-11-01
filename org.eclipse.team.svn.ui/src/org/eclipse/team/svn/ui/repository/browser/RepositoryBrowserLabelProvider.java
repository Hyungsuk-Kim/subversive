/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.browser;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.compare.CompareUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.Lock;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveWorkingDirectory;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;

/**
 * Repository browser label provider
 *
 * @author Sergiy Logvin
 */
public class RepositoryBrowserLabelProvider implements ITableLabelProvider {
	protected RepositoryBrowserTableViewer tableViewer;
	protected Map images;
	
	protected static String hasProps;
	protected static String noAuthor;
	protected static String noDate;

	public RepositoryBrowserLabelProvider(RepositoryBrowserTableViewer tableViewer) {
		this.tableViewer = tableViewer;
		this.images = new HashMap();
		RepositoryBrowserLabelProvider.noAuthor = SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor");
		RepositoryBrowserLabelProvider.noDate = SVNTeamPlugin.instance().getResource("SVNInfo.NoDate");
		RepositoryBrowserLabelProvider.hasProps = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.HasProps");
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == tableViewer.getNameColumnIndex()) {
			ImageDescriptor iDescr = null;
			if (element instanceof RepositoryResource) {
				iDescr = ((RepositoryResource)element).getImageDescriptor(null);
			}
			else if (element instanceof RepositoryFictiveNode) {
				iDescr = ((RepositoryFictiveNode)element).getImageDescriptor(null);
			}
			if (iDescr != null) {
				Image img = (Image)this.images.get(iDescr);
				if (img == null) {
					this.images.put(iDescr, img = iDescr.createImage());
					CompareUI.disposeOnShutdown(img);
				}
				return img;
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof RepositoryResource) {
			return this.getColumnTextForElement(element, columnIndex);
		}
		else if (element instanceof RepositoryFictiveNode) {
			if (columnIndex == tableViewer.getNameColumnIndex()) {
				return ((RepositoryFictiveNode)element).getLabel(null);
			}
			if (element instanceof RepositoryFictiveWorkingDirectory) {
				return this.getColumnTextForElement(((RepositoryFictiveWorkingDirectory)element).getAssociatedDirectory(), columnIndex);
			}
		}
		return "";		
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {								
	}

	protected String getColumnTextForElement(Object element, int columnIndex) {
		DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		if (element instanceof RepositoryResource) {
			IRepositoryResource resource = ((RepositoryResource)element).getRepositoryResource();
			IRepositoryResource.Information resourceInfo = resource.getInfo();
			if (columnIndex == tableViewer.getNameColumnIndex()) {
				return ((RepositoryResource)element).getLabel();
			}
			else if (columnIndex == tableViewer.getRevisionColumnIndex()) {
				String revision = "";
				try {
					if (resource.isInfoCached()) {
						revision = String.valueOf(((RepositoryResource)element).getRevision());
					}
					else {
						revision = SVNTeamUIPlugin.instance().getResource(RepositoryPending.PENDING);
					}
				}
				catch (Exception ex) {
					LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.GetColumnText"), ex);
				}
				return revision;
			}
			else if (resourceInfo != null) {			
				if (columnIndex == tableViewer.getDateColumnIndex()) {
					return (resourceInfo.lastChangedDate != 0) ? dateTimeFormat.format(new Date(resourceInfo.lastChangedDate)) : RepositoryBrowserLabelProvider.noDate;
				}
				else if (columnIndex == tableViewer.getAuthorColumnIndex()) {
					String author = resourceInfo.lastAuthor;
					return (author != null) ? author : RepositoryBrowserLabelProvider.noAuthor;
				}
				else if (columnIndex == tableViewer.getLockOwnerColumnIndex()) {
					Lock lock = resourceInfo.lock;
					String lockOwner = (lock == null) ? "" : lock.owner;
					return lockOwner;
				}
				else if (columnIndex == tableViewer.getSizeColumnIndex()) {
					long size = resourceInfo.fileSize;
					return (resource instanceof IRepositoryFile) ? String.valueOf(size) : "";
				}
				else if (columnIndex == tableViewer.getPropertiesColumnIndex()) {
					boolean hasProps = resourceInfo.hasProperties;
					return (hasProps) ? RepositoryBrowserLabelProvider.hasProps : "";
				}
			}
		}
		return "";
	}
	
}
