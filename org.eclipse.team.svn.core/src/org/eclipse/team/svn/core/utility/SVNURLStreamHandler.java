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

package org.eclipse.team.svn.core.utility;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.eclipse.team.svn.core.SVNMessages;

/**
 * SVN-specific URL stream handler
 * 
 * @author Alexander Gurov
 */
public class SVNURLStreamHandler extends URLStreamHandler {
	protected URL url;
	
	public SVNURLStreamHandler() {
	}
	
	public URL getURL() {
		return this.url;
	}

    protected URLConnection openConnection(URL u) throws IOException {
        return null;
    }
    
    public void setHost(String host) {
    	this.setURL(this.url, this.url.getProtocol(), host, this.url.getPort(), this.url.getAuthority(), this.url.getUserInfo(), this.url.getPath(), this.url.getQuery(), this.url.getRef());
    }
    
    protected void parseURL(URL u, String spec, int start, int limit) {
    	String protocol = u.getProtocol();
        if (!protocol.equals("file") && //$NON-NLS-1$
    		!protocol.equals("svn") && //$NON-NLS-1$
            !protocol.equals("http") && //$NON-NLS-1$
            !protocol.equals("https") && //$NON-NLS-1$
            !protocol.equals("svn+ssh")) { //$NON-NLS-1$
    		String errMessage = SVNMessages.formatErrorString("Error_UnknownProtocol", new String[] {protocol}); //$NON-NLS-1$
            throw new RuntimeException(errMessage);
        }
    	this.url = u;
        super.parseURL(u, spec, start, limit);
    }
    
}
