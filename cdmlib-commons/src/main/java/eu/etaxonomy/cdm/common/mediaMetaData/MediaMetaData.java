// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.common.mediaMetaData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

/**
 * 
 * @author n.hoffmann
 * @created 13.11.2008
 * @version 1.0
 */
public abstract class MediaMetaData {
	private static Logger logger = Logger.getLogger(MediaMetaData.class);
	protected String formatName, mimeType;
	HashMap<String, String> metaData;
	
	
	public abstract void readMetaData(URI mediaUri, Integer timeOut);
 
	public Map<String, String> getMetaData() {
		
		return metaData;
	}

 
	


		
	
}
