/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:53
 */
public class ImageFile extends MediaInstance {
	static Logger logger = Logger.getLogger(ImageFile.class);

	//image height in pixel
	@Description("image height in pixel")
	private int height;
	//image width in pixel
	@Description("image width in pixel")
	private int width;

	public int getHeight(){
		return height;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHeight(int newVal){
		height = newVal;
	}

	public int getWidth(){
		return width;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setWidth(int newVal){
		width = newVal;
	}

}