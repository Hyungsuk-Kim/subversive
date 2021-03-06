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

package org.eclipse.team.svn.ui.wizard.createpatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.SavePatchInWorkspacePanel;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourcePathVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Select patch file wizard page
 * 
 * @author Alexander Gurov
 */
public class SelectPatchFilePage extends AbstractVerifiedWizardPage {
	protected Text fileNameField;
	protected Text wsPathField;
	protected Button browseButton;
	protected Button browseWSButton;
	protected Combo charsetField;
	
	protected String proposedName;
	
	protected String charset;
	protected String fileName;
	protected IFile file;
	protected int writeMode;
	
	protected CheckboxTreeViewer changeViewer;
	protected IResource []roots;
	protected Object []initialSelection;
	protected Object []realSelection;

	public SelectPatchFilePage(String proposedName, IResource []roots) {
		super(
			SelectPatchFilePage.class.getName(), 
			SVNUIMessages.SelectPatchFilePage_Title, 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif")); //$NON-NLS-1$
		this.setDescription(SVNUIMessages.SelectPatchFilePage_Description);
		this.proposedName = proposedName + ".patch"; //$NON-NLS-1$
		this.writeMode = CreatePatchWizard.WRITE_TO_CLIPBOARD;
		// filter out nested project's resources if there are more than 1 entry for the physical resource
		if (roots != null) {
			LinkedHashMap<IResource, Boolean> resourceSet = new LinkedHashMap<IResource, Boolean>();
			for (IResource resource : roots) {
				resourceSet.put(resource, Boolean.FALSE);
			}
			for (IResource resource : roots) {
				IPath path = FileUtility.getResourcePath(resource);
				for (IResource checkAgainst : roots) {
					if (resource != checkAgainst && path.isPrefixOf(FileUtility.getResourcePath(checkAgainst))) {
						resourceSet.put(checkAgainst, Boolean.TRUE);
					}
				}
			}
			ArrayList<IResource> filteredRoots = new ArrayList<IResource>();
			for (Map.Entry<IResource, Boolean> entry : resourceSet.entrySet()) {
				if (!entry.getValue().booleanValue()) {
					filteredRoots.add(entry.getKey());
				}
			}
			this.roots = filteredRoots.toArray(new IResource[filteredRoots.size()]);
		}
		try {
			File tmp = File.createTempFile("patch", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			tmp.delete();
			SelectPatchFilePage.this.fileName = tmp.getAbsolutePath();
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getCharset() {
		return this.charset;
	}
	
	public boolean isRecursive() {
		if (this.initialSelection == null) {
			return true;
		}
		HashSet result = new HashSet(Arrays.asList(this.initialSelection));
		result.removeAll(Arrays.asList(this.realSelection));
		return result.isEmpty();
	}
	
	public IResource []getSelection() {
		return Arrays.asList(this.realSelection).toArray(new IResource[this.realSelection.length]);
	}

	public IFile getFile() {
		return this.file;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public int getWriteMode() {
		return this.writeMode;
	}

	protected Composite createControlImpl(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Composite saveTo = composite;
		
		if (this.roots != null) {
			Group saveToImpl = new Group(composite, SWT.NONE);
			layout = new GridLayout();
			saveToImpl.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			saveToImpl.setLayoutData(data);
			saveToImpl.setText(SVNUIMessages.SelectPatchFilePage_SaveTo);
			saveTo = saveToImpl;
		}
		
		Button saveToClipboard = new Button(saveTo, SWT.RADIO);
		saveToClipboard.setText(SVNUIMessages.SelectPatchFilePage_SaveToClipboard);
		data = new GridData(GridData.FILL_HORIZONTAL);
		saveToClipboard.setLayoutData(data);
		saveToClipboard.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				if (button.getSelection()) {
					SelectPatchFilePage.this.fileNameField.setEnabled(false);
					SelectPatchFilePage.this.browseButton.setEnabled(false);
					SelectPatchFilePage.this.browseWSButton.setEnabled(false);
					SelectPatchFilePage.this.charsetField.setEnabled(true);
					try {
						SelectPatchFilePage.this.fileName = File.createTempFile("patch", ".tmp").getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
					} 
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					SelectPatchFilePage.this.writeMode = CreatePatchWizard.WRITE_TO_CLIPBOARD;
					SelectPatchFilePage.this.validateContent();
				}
			}
		});

		this.charsetField = new Combo(saveTo, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.charsetField.setLayoutData(data);
		String sysEnc = System.getProperty("file.encoding"); //$NON-NLS-1$
		String []comboItems = "UTF-8".equalsIgnoreCase(sysEnc) ? new String[] {sysEnc} :  new String[] {sysEnc, "UTF-8"}; //$NON-NLS-1$ //$NON-NLS-2$
		this.charset = comboItems[0];
		this.charsetField.setItems(comboItems);
		this.charsetField.select(0);
		this.charsetField.setVisibleItemCount(comboItems.length);
		this.charsetField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SelectPatchFilePage.this.charset = SelectPatchFilePage.this.charsetField.getText();
			}
		});
		this.charsetField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SelectPatchFilePage.this.charset = SelectPatchFilePage.this.charsetField.getText();
			}
		});
		
		final Button saveOnFileSystem = new Button(saveTo, SWT.RADIO);
		saveOnFileSystem.setText(SVNUIMessages.SelectPatchFilePage_SaveInFS);
		data = new GridData(GridData.FILL_HORIZONTAL);
		saveOnFileSystem.setLayoutData(data);
		saveOnFileSystem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				if (button.getSelection()) {
					SelectPatchFilePage.this.fileNameField.setEnabled(true);
					SelectPatchFilePage.this.browseButton.setEnabled(true);
					SelectPatchFilePage.this.browseWSButton.setEnabled(false);
					SelectPatchFilePage.this.charsetField.setEnabled(false);
					SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.fileNameField.getText();
					SelectPatchFilePage.this.writeMode = CreatePatchWizard.WRITE_TO_EXTERNAL_FILE;
					SelectPatchFilePage.this.validateContent();
				}
			}
		});

		Composite fsComposite = new Composite(saveTo, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		fsComposite.setLayoutData(data);
		
		this.fileNameField = new Text(fsComposite, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.fileNameField.setLayoutData(data);
		this.fileNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.fileNameField.getText();
			}
		});
		CompositeVerifier cVerifier = new CompositeVerifier();
		String name = SVNUIMessages.SelectPatchFilePage_SaveInFS_Verifier;
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new ResourcePathVerifier(name));
		cVerifier.add(new AbstractFormattedVerifier(name) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		        String text = this.getText(input);
		        if (new File(text).exists()) {
		            return SVNUIMessages.format(SVNUIMessages.SelectPatchFilePage_SaveInFS_Verifier_Warning, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		});
		this.attachTo(this.fileNameField, new AbstractVerifierProxy(cVerifier) {
			protected boolean isVerificationEnabled(Control input) {
				return saveOnFileSystem.getSelection();
			}
		});

		this.browseButton = new Button(fsComposite, SWT.PUSH);
		this.browseButton.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browseButton);
		this.browseButton.setLayoutData(data);
		this.browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog dlg = new FileDialog(SelectPatchFilePage.this.getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
				dlg.setText(SVNUIMessages.SelectPatchFilePage_SavePatchAs);
				dlg.setFileName(SelectPatchFilePage.this.proposedName);
				dlg.setFilterExtensions(new String[] {"*.patch", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
				String file = dlg.open();
				if (file != null) {
					SelectPatchFilePage.this.fileName = file;
					SelectPatchFilePage.this.fileNameField.setText(file);
					SelectPatchFilePage.this.validateContent();
				}			
			}
		});			
		
		final Button saveInWorkspace = new Button(saveTo, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL);
		saveInWorkspace.setLayoutData(data);
		saveInWorkspace.setText(SVNUIMessages.SelectPatchFilePage_SaveInWS);
		saveInWorkspace.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Button button = (Button)event.widget;
				if (button.getSelection()) {
					SelectPatchFilePage.this.fileNameField.setEnabled(false);
					SelectPatchFilePage.this.browseButton.setEnabled(false);
					SelectPatchFilePage.this.browseWSButton.setEnabled(true);
					SelectPatchFilePage.this.charsetField.setEnabled(false);
					SelectPatchFilePage.this.fileName = SelectPatchFilePage.this.file == null ? null : FileUtility.getWorkingCopyPath(SelectPatchFilePage.this.file);
					SelectPatchFilePage.this.writeMode = CreatePatchWizard.WRITE_TO_WORKSPACE_FILE;
					SelectPatchFilePage.this.validateContent();
				}
			}
		});
		
		Composite wsComposite = new Composite(saveTo, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		wsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		wsComposite.setLayoutData(data);
		
		this.wsPathField = new Text(wsComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.wsPathField.setLayoutData(data);
		cVerifier = new CompositeVerifier();
		name = SVNUIMessages.SelectPatchFilePage_SaveInWS_Verifier;
		cVerifier.add(new NonEmptyFieldVerifier(name));
		cVerifier.add(new AbstractFormattedVerifier(name) {
		    protected String getErrorMessageImpl(Control input) {
		        return null;
		    }
		    protected String getWarningMessageImpl(Control input) {
		        if (SelectPatchFilePage.this.file != null && SelectPatchFilePage.this.file.isAccessible()) {
		            return SVNUIMessages.format(SVNUIMessages.SelectPatchFilePage_SaveInWS_Verifier_Warning, new String[] {AbstractFormattedVerifier.FIELD_NAME});
		        }
		        return null;
		    }
		});
		this.attachTo(this.wsPathField, new AbstractVerifierProxy(cVerifier) {
			protected boolean isVerificationEnabled(Control input) {
				return saveInWorkspace.getSelection();
			}
		});

		this.browseWSButton = new Button(wsComposite, SWT.PUSH);
		this.browseWSButton.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browseWSButton);
		this.browseWSButton.setLayoutData(data);
		this.browseWSButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IProject proposedDestination = SelectPatchFilePage.this.roots.length > 0 ? SelectPatchFilePage.this.roots[0].getProject() : null;
				SavePatchInWorkspacePanel panel = new SavePatchInWorkspacePanel(SelectPatchFilePage.this.proposedName, proposedDestination);
				DefaultDialog dlg = new DefaultDialog(SelectPatchFilePage.this.getShell(), panel);
				if (dlg.open() == 0) {
					SelectPatchFilePage.this.file = panel.getFile();
					SelectPatchFilePage.this.fileName = FileUtility.getWorkingCopyPath(SelectPatchFilePage.this.file);
					SelectPatchFilePage.this.wsPathField.setText(SelectPatchFilePage.this.file.getFullPath().toString());
					SelectPatchFilePage.this.validateContent();
				}			
			}
		});			
		
		if (this.roots != null) {
			Label label = new Label(composite, SWT.NONE);
			data = new GridData();
			label.setLayoutData(data);
			label.setText(SVNUIMessages.SelectPatchFilePage_Changes);
			
			this.changeViewer = new CheckboxTreeViewer(composite, SWT.BORDER);
			data = new GridData(GridData.FILL_BOTH);
			data.heightHint = 200;
			data.widthHint = 600;
			this.changeViewer.getControl().setLayoutData(data);
			this.changeViewer.setContentProvider(new WorkbenchContentProvider() {
				public Object[] getChildren(Object element) {
					if (element instanceof IProject || element instanceof IFolder) {
						try {
							Object[] result = SVNRemoteStorage.instance().getRegisteredChildren((IContainer)element);
							return result != null ? result : new Object[0];
						}
						catch (Exception e) {
							// do nothing
						}
					}
					Object[] result = super.getChildren(element);
					return result != null ? result : new Object[0];
				}
			}); 
			this.changeViewer.setLabelProvider(new WorkbenchLabelProvider());
			this.changeViewer.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					SelectPatchFilePage.this.changeViewer.getControl().setRedraw(false);
					
					IResource resource = (IResource)event.getElement();
					HashSet grayed = new HashSet();
					grayed.addAll(Arrays.asList(SelectPatchFilePage.this.changeViewer.getGrayedElements()));
					if (event.getChecked()) {
						if (resource.getType() != IResource.FILE) {
							SelectPatchFilePage.this.changeViewer.setSubtreeChecked(resource, true);
							IPath path = resource.getFullPath();
							for (int i = 0; i < SelectPatchFilePage.this.initialSelection.length; i++) {
								IResource current = (IResource)SelectPatchFilePage.this.initialSelection[i];
								if (path.isPrefixOf(current.getFullPath())) {
									grayed.remove(current);
								}
							}
						}
						while ((resource = resource.getParent()).getType() != IResource.ROOT) {
							boolean hasUnchecked = false;
							IPath path = resource.getFullPath();
							for (int i = 0; i < SelectPatchFilePage.this.initialSelection.length; i++) {
								IResource current = (IResource)SelectPatchFilePage.this.initialSelection[i];
								if (path.isPrefixOf(current.getFullPath()) && !current.equals(resource)) {
									hasUnchecked |= !SelectPatchFilePage.this.changeViewer.getChecked(current);
								}
							}
							if (!hasUnchecked) {
								grayed.remove(resource);
								SelectPatchFilePage.this.changeViewer.setChecked(resource, true);
							}
						}
					}
					else {
						if (resource.getType() != IResource.FILE) {
							SelectPatchFilePage.this.changeViewer.setSubtreeChecked(resource, false);
						}
						grayed.addAll(Arrays.asList(FileUtility.getPathNodes(resource)));
					}
					SelectPatchFilePage.this.changeViewer.setGrayedElements(grayed.toArray());
					SelectPatchFilePage.this.realSelection = SelectPatchFilePage.this.changeViewer.getCheckedElements();
					
					SelectPatchFilePage.this.changeViewer.getControl().setRedraw(true);
				}
			});
			this.changeViewer.addFilter(new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (element instanceof IResource) {
						IResource resource = (IResource)element;
						IPath resourcePath = resource.getFullPath();
						for (int i = 0; i < SelectPatchFilePage.this.roots.length; i++) {
							IPath rootPath = SelectPatchFilePage.this.roots[i].getFullPath();
							if ((rootPath.isPrefixOf(resourcePath) || resourcePath.isPrefixOf(rootPath)) && 
								FileUtility.checkForResourcesPresenceRecursive(new IResource[] {resource}, IStateFilter.SF_ANY_CHANGE)) {
								return true;
							}
						}
					}
					return false;
				}
			});
			this.changeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
			this.changeViewer.expandAll();
			TreeItem []items = this.changeViewer.getTree().getItems();
			for (int i = 0; i < items.length; i++) {
				this.changeViewer.setSubtreeChecked(items[i].getData(), true);
			}
			this.realSelection = this.initialSelection = this.changeViewer.getCheckedElements();
		}

		this.fileNameField.setEnabled(false);
		this.browseButton.setEnabled(false);
		this.browseWSButton.setEnabled(false);
		saveToClipboard.setSelection(true);
		
//		Setting context help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.team.svn.help.patchFileContext"); //$NON-NLS-1$
		
		return composite;
	}

}
