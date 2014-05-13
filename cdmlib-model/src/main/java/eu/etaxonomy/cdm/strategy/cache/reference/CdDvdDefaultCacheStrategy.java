/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.Reference;

public class CdDvdDefaultCacheStrategy extends NomRefDefaultCacheStrategyBase implements INomenclaturalReferenceCacheStrategy {
	private static final Logger logger = Logger.getLogger(CdDvdDefaultCacheStrategy.class);
	
	private String prefixEdition = "ed.";
	private String prefixSeries = "ser.";
	private String prefixVolume = "vol.";
	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("68076ca5-d517-489c-8ae2-01d3c38cc788");
	
	@Override
	protected UUID getUuid() {
		return uuid; 
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static CdDvdDefaultCacheStrategy NewInstance(){
		return new CdDvdDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private CdDvdDefaultCacheStrategy(){
		super();
	}
	
	@Override
	protected String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev){
		if (ref == null){
			return null;
		}
		String nomRefCache = "";
		//TODO
		String titel = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);
//		String publisher = CdmUtils.Nz(nomenclaturalReference.getPublisher());
		
		boolean needsComma = false;
		//titelAbbrev
		String titelAbbrevPart = "";
		if (titel.length() > 0){
			nomRefCache = titel + blank; 
		}
//		//publisher
//		String publisherPart = "";
//		if (!"".equals(publisher)){
//			publisherPart = publisher;
//			needsComma = true;
//		}
//		nomRefCache += publisherPart;

		
		//delete .
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}
		return nomRefCache.trim();
	}
	
	private boolean isNumeric(String string){
		if (string == null){
			return false;
		}
		try {
			Double.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
	}

}
