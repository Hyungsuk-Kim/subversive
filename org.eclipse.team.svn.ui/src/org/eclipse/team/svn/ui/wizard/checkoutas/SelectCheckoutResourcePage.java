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

package org.eclipse.team.svn.ui.wizard.checkoutas;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * "Import from SVN" wizard resource selection page
 * 
 * @author Alexander Gurov
 */
public class SelectCheckoutResourcePage extends AbstractVerifiedWizardPage {

	protected RepositoryResourceSelectionComposite selectComposite;	
	
	protected IRepositoryResource baseResource;
	protected IRepositoryResource selectedResource;
	
	public SelectCheckoutResourcePage() {
		super(
			SelectCheckoutResourcePage.class.getName(), 
			SVNUIMessages.SelectCheckoutResourcePage_Title, 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
		this.setDescription(SVNUIMessages.SelectCheckoutResourcePage_Description);		
	}
	
	public void setRepositoryLocation(IRepositoryLocation location) {		
		this.selectedResource = this.baseResource = location.getRoot();
		this.selectComposite.setBaseResource(this.baseResource);
	}
	
	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;		
	}		
	
	protected Composite createControlImpl(Composite parent) {
		GridData data = null;
		GridLayout layout = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 4;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);	
		
		this.selectComposite = new RepositoryResourceSelectionComposite(
				composite, SWT.NONE, this, "selectCheckoutUrl", this.baseResource, true,  //$NON-NLS-1$
				SVNUIMessages.SelectRepositoryResourcePage_Select_Title,
				SVNUIMessages.SelectRepositoryResourcePage_Select_Description, RepositoryResourceSelectionComposite.MODE_DEFAULT, RepositoryResourceSelectionComposite.TEXT_BASE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 550;
		this.selectComposite.setLayoutData(data);
		
		//Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.selectCheckResourcesContext"); //$NON-NLS-1$
		
		return composite;
	}
	
	public IWizardPage getNextPage() {
		this.selectedResource = this.selectComposite.getSelectedResource();
		this.selectComposite.saveHistory();
		return super.getNextPage();
	}
}
