/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.verifier;

import java.io.File;

import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Existing file verifier
 * 
 * @author Sergiy Logvin
 */
public class ExistingResourceVerifier extends AbstractFormattedVerifier {
	protected static String ERROR_MESSAGE_DOES_NOT_EXIST;
	protected static String ERROR_MESSAGE_IS_NOT_A_FILE;
	protected static String ERROR_MESSAGE_IS_NOT_A_DIRECTORY;
	
    protected boolean checkNodeType;
    protected boolean files;
        
    public ExistingResourceVerifier(String fieldName) {
        super(fieldName);
        this.init();
        this.checkNodeType = false;
    }
    
    public ExistingResourceVerifier(String fieldName, boolean files) {
        super(fieldName);
        this.init();
        this.files = files;
        this.checkNodeType = true;
    }
    
    protected String getErrorMessageImpl(Control input) {
    	File currentFile = new File(this.getText(input));
        if (!currentFile.exists()) {
            return ExistingResourceVerifier.ERROR_MESSAGE_DOES_NOT_EXIST;
        }
        else if (this.checkNodeType && this.files && !currentFile.isFile()) {
        	return ExistingResourceVerifier.ERROR_MESSAGE_IS_NOT_A_FILE;
        }
        else if (this.checkNodeType && !this.files && !currentFile.isDirectory()) {
        	return ExistingResourceVerifier.ERROR_MESSAGE_IS_NOT_A_DIRECTORY;
        }
        return null;
    }

    protected String getWarningMessageImpl(Control input) {
        return null;
    }

    private void init() {
    	ExistingResourceVerifier.ERROR_MESSAGE_DOES_NOT_EXIST = SVNUIMessages.format(SVNUIMessages.Verifier_ExistingResource_NotExists, new String[] {AbstractFormattedVerifier.FIELD_NAME});
    	ExistingResourceVerifier.ERROR_MESSAGE_IS_NOT_A_FILE = SVNUIMessages.format(SVNUIMessages.Verifier_ExistingResource_IsNotAFile, new String[] {AbstractFormattedVerifier.FIELD_NAME});
    	ExistingResourceVerifier.ERROR_MESSAGE_IS_NOT_A_DIRECTORY = SVNUIMessages.format(SVNUIMessages.Verifier_ExistingResource_IsNotADir, new String[] {AbstractFormattedVerifier.FIELD_NAME});
    }
}
