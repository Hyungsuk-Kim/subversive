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

package org.eclipse.team.svn.ui.wizard;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.verifier.AbstractVerificationKeyListener;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * Verified WizardPage implementation provides validation abilities
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractVerifiedWizardPage extends WizardPage implements IValidationManager {
    private VerificationKeyListener changeListener;
    
    public AbstractVerifiedWizardPage(String pageName) {
        super(pageName);
        this.changeListener = new VerificationKeyListener();
    }

    public AbstractVerifiedWizardPage(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
        this.changeListener = new VerificationKeyListener();
    }

	public void createControl(Composite parent) {
		this.setControl(this.createControlImpl(parent));
		this.addListeners();
	}
	
	public boolean isFilledRight() {
		return this.changeListener.isFilledRight();
	}

	public void attachTo(Control cmp, AbstractVerifier verifier) {
		this.changeListener.attachTo(cmp, verifier);
	}
	
	public void addListeners() {
		this.changeListener.addListeners();		
		this.validateContent();
		this.setMessage(this.getDescription(), IMessageProvider.NONE);
	}
	
	public void detachFrom(Control cmp) {
		this.changeListener.detachFrom(cmp);
	}
		
	public void detachAll() {
		this.changeListener.detachAll();
	}
	
	public void validateContent() {
		this.changeListener.validateContent();
	}
	
	public boolean validateControl(Control cmp) {
		return this.changeListener.validateControl(cmp);
	}
	
    public void setPageComplete(boolean complete) {
        super.setPageComplete(complete && this.isFilledRight() && this.isPageCompleteImpl());
    }

    public boolean isPageComplete() {
    	if (this.getContainer().getCurrentPage() == this) {
        	return super.isPageComplete();
    	}
    	return true;
    }
    
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			this.getControl().setFocus();
		}
	}
	
    public void setMessage(String newMessage, int newType) {
    	if (newType == IMessageProvider.WARNING) {
        	//NOTE Eclipse workaround: all warnings are rendered as animated but old message does not cleared. So, old error still visible after warning is shown.
            AbstractVerifiedWizardPage.this.setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        	//NOTE Eclipse workaround: clear error message before setting warning message
            AbstractVerifiedWizardPage.this.setErrorMessage(null);
        	super.setMessage(newMessage, newType);
    	}
    	else if (newType == IMessageProvider.ERROR) {
        	//NOTE Eclipse workaround: all warnings are rendered as animated but old message does not cleared. So, old error still visible after warning is shown.
            AbstractVerifiedWizardPage.this.setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        	//NOTE Eclipse workaround: error will be rendered as animated only when setErrorMessage() is used.
            AbstractVerifiedWizardPage.this.setErrorMessage(newMessage);
    	}
    	else {
        	//NOTE Eclipse workaround: clear error message before setting default message
            AbstractVerifiedWizardPage.this.setErrorMessage(null);
        	super.setMessage(newMessage, newType);
    	}
    }
    
    protected abstract Composite createControlImpl(Composite parent);
    
	protected boolean isPageCompleteImpl() {
	    return true;
	}
	
    protected class VerificationKeyListener extends AbstractVerificationKeyListener {
        public VerificationKeyListener() {
            super();
        }
        
        public void hasError(String errorReason) {
            AbstractVerifiedWizardPage.this.setMessage(errorReason, IMessageProvider.ERROR);
			this.handleButtons();
        }

        public void hasWarning(String warningReason) {
            AbstractVerifiedWizardPage.this.setMessage(warningReason, IMessageProvider.WARNING);
			this.handleButtons();
        }

        public void hasNoError() {
            AbstractVerifiedWizardPage.this.setMessage(AbstractVerifiedWizardPage.this.getDescription(), IMessageProvider.NONE);
			this.handleButtons();
        }

        protected void handleButtons() {
            AbstractVerifiedWizardPage.this.setPageComplete(this.isFilledRight());
        }
        
    }
    
}
