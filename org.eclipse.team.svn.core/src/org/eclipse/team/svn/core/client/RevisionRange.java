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

package org.eclipse.team.svn.core.client;

/**
 * The revision range container
 * 
 * The JavaHL API's is the only way to interact between SVN and Java-based tools. At the same time JavaHL client library
 * is not EPL compatible and we won't to pin plug-in with concrete client implementation. So, the only way to do this is
 * providing our own client interface which will be covered by concrete client implementation.
 * 
 * @author Alexander Gurov
 */
public class RevisionRange {
	/**
	 * The "from" revision object
	 */
	public final Revision from;

	/**
	 * The "to" revision object
	 */
	public final Revision to;

	/**
	 * The {@link RevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * @param from
	 *            the "from" revision object. Greater or equals to zero.
	 * @param to
	 *            the "to" revision object. Greater or equals to zero.
	 * @throws IllegalArgumentException
	 *             if from or to contains negative value
	 */
	public RevisionRange(long from, long to) {
		this.from = Revision.fromNumber(from);
		this.to = Revision.fromNumber(to);
	}

	/**
	 * The {@link RevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * @param from
	 *            the "from" revision object. Cannot be <code>null</code>.
	 * @param to
	 *            the "to" revision object Cannot be <code>null</code>.
	 * @throws NullPointerException
	 *             if one of arguments (or both) is null
	 */
	public RevisionRange(Revision from, Revision to) {
		if (from == null) {
			throw new NullPointerException("The \"from\" field cannot be initialized with null");
		}
		if (to == null) {
			throw new NullPointerException("The \"to\" field cannot be initialized with null");
		}
		this.from = from;
		this.to = to;
	}

	/**
	 * The {@link RevisionRange} instance could be initialized only once because all fields are final
	 * 
	 * Accepts a string in one of these forms:
	 * 
	 * {revision} the "from" and "to" fields will be initialized with the same value
	 * 
	 * {revision}-{revision} the first revision will be set into "from" object and the second into the "to" object
	 * 
	 * @param revisionElement
	 *            revision range or single revision
	 * @throws NumberFormatException
	 *             if the string does not contain a parsable <code>long</code>.
	 */
	public RevisionRange(String revisionElement) {
		int hyphen = revisionElement.indexOf('-');
		if (hyphen > 0) {
			this.from = Revision.fromNumber(Long.parseLong(revisionElement.substring(0, hyphen)));
			this.to = Revision.fromNumber(Long.parseLong(revisionElement.substring(hyphen + 1)));
		}
		else {
			this.to = this.from = Revision.fromNumber(Long.parseLong(revisionElement.trim()));
		}
	}

	public String toString() {
		if (this.from.equals(this.to)) {
			return this.from.toString();
		}
		return this.from.toString() + '-' + this.to.toString();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.from.hashCode();
		result = prime * result + this.to.hashCode();
		return result;
	}

	public boolean equals(Object range) {
		if (this == range) {
			return true;
		}
		if (!(range instanceof RevisionRange)) {
			return false;
		}

		RevisionRange other = (RevisionRange) range;
		return this.from.equals(other.from) && this.to.equals(other.to);
	}

}
