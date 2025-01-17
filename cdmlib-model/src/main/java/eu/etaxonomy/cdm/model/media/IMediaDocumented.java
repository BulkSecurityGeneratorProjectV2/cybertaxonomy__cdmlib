/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.media;

import java.util.Set;


/**
 * Interface for all objects having {@link Media} attached
 * @author a.mueller
 *
 */
public interface IMediaDocumented {
	public Set<Media> getMedia();
}
