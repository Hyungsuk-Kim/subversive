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

package org.eclipse.team.svn.ui.wizard.shareproject;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.ArrayStructuredContentProvider;
import org.eclipse.team.svn.ui.utility.ColumnedViewerComparator;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Select repository location wizard page
 * 
 * @author Alexander Gurov
 */
public class SelectRepositoryLocationPage extends AbstractVerifiedWizardPage {
	protected boolean useExistingLocation;
	protected IRepositoryLocation location;
	protected TableViewer repositoriesView;
	protected IRepositoryLocation []repositories;
	protected boolean importProject;

	public SelectRepositoryLocationPage(IRepositoryLocation []repositories) {
		this(repositories, false);
	}
	
	public SelectRepositoryLocationPage(IRepositoryLocation []repositories, boolean importProject) {
		super(
				SelectRepositoryLocationPage.class.getName(), 
				SVNUIMessages.getString("SelectRepositoryLocationPage_Title" + SelectRepositoryLocationPage.getNationalizationSuffix(importProject)),  //$NON-NLS-1$
				SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
			
			this.setDescription(SVNUIMessages.SelectRepositoryLocationPage_Description);
			this.repositories = repositories;
			this.useExistingLocation = true;
			this.location = this.repositories[0];
			this.importProject = importProject;
	}

	protected Composite createControlImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		this.initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label description = new Label(composite, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = this.convertHeightInCharsToPixels(2);
		description.setLayoutData(data);
		description.setText(SVNUIMessages.getString("SelectRepositoryLocationPage_Hint" + SelectRepositoryLocationPage.getNationalizationSuffix(this.importProject))); //$NON-NLS-1$
		
		Button addLocationButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		addLocationButton.setText(SVNUIMessages.SelectRepositoryLocationPage_AddLocation); 
		addLocationButton.setSelection(false);
		
		Button useExistingLocationButton = new Button(composite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		useExistingLocationButton.setLayoutData(data);
		useExistingLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button)e.widget;
				SelectRepositoryLocationPage.this.repositoriesView.getTable().setEnabled(
					SelectRepositoryLocationPage.this.useExistingLocation = button.getSelection());
				SelectRepositoryLocationPage.this.setPageComplete(true);
			}
		});
		useExistingLocationButton.setText(SVNUIMessages.SelectRepositoryLocationPage_UseLocation); 
		useExistingLocationButton.setSelection(true);
		
		Table table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableLayout tLayout = new TableLayout();
		tLayout.addColumnData(new ColumnWeightData(30, true));
		tLayout.addColumnData(new ColumnWeightData(70, true));
		table.setLayout(tLayout);

		this.repositoriesView = new TableViewer(table);
		
		ColumnedViewerComparator comparator = new ColumnedViewerComparator(this.repositoriesView) {
			public int compareImpl(Viewer viewer, Object row1, Object row2) {
				IRepositoryLocation location1 = (IRepositoryLocation)row1;
            	IRepositoryLocation location2 = (IRepositoryLocation)row2;
            	if (this.column == 0) {
            		return ColumnedViewerComparator.compare(location1.getLabel(), location2.getLabel());
            	}
        		return ColumnedViewerComparator.compare(location1.getUrl(), location2.getUrl());
			}
		};
		
		TableColumn col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText("Label");
		col.addSelectionListener(comparator);

		col = new TableColumn(table, SWT.LEFT);
		col.setResizable(true);
		col.setText("URL");
		col.addSelectionListener(comparator);

		this.repositoriesView.getTable().setSortDirection(SWT.UP);
		this.repositoriesView.getTable().setSortColumn(this.repositoriesView.getTable().getColumn(0));
		
		this.repositoriesView.setContentProvider(new ArrayStructuredContentProvider());
		this.repositoriesView.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				IRepositoryLocation location = (IRepositoryLocation)element;
				if (columnIndex == 0) {
					return location.getLabel();
				}
				return location.getUrlAsIs();
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return true;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		this.repositoriesView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)SelectRepositoryLocationPage.this.repositoriesView.getSelection();
				SelectRepositoryLocationPage.this.location = (IRepositoryLocation)selection.getFirstElement();
				SelectRepositoryLocationPage.this.setPageComplete(true);
			}
		});
		
		this.repositoriesView.setInput(this.repositories);
		this.repositoriesView.getTable().select(0);
		IStructuredSelection selection = (IStructuredSelection)this.repositoriesView.getSelection();
		this.location = (IRepositoryLocation)selection.getFirstElement();
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.reposLocationContext"); //$NON-NLS-1$
		
		return composite;
	}

	public boolean useExistingLocation() {
		return this.useExistingLocation;
	}
	
	public IRepositoryLocation getRepositoryLocation() {
		return this.useExistingLocation() ? this.location : null;
	}
	
	protected static String getNationalizationSuffix(boolean importProject) {
		return "." + (importProject ? "Import" : "Share"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
