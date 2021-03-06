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

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Correct file path verifier
 * 
 * @author Sergiy Logvin
 */
public class ResourcePathVerifier extends AbstractFormattedVerifier {
    protected static String ERROR_MESSAGE;
        
    public ResourcePathVerifier(String fieldName) {
        super(fieldName);
        ResourcePathVerifier.ERROR_MESSAGE = SVNUIMessages.format(SVNUIMessages.Verifier_ResourcePath, new String[] {AbstractFormattedVerifier.FIELD_NAME});
    }
    
    protected String getErrorMessageImpl(Control input) {
        String text = this.getText(input);
        Path path = new Path(text);
        if (!path.isValidPath(text)) {
            return ResourcePathVerifier.ERROR_MESSAGE;
        }
        return null;
    }

    protected String getWarningMessageImpl(Control input) {
        return null;
    }

}
