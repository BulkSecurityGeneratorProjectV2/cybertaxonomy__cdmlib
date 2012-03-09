// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifDescriptionCsv2CdmConverter extends ConverterBase<DwcaImportState>  implements IConverter<CsvStreamItem, IReader<CdmBase>, String>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifDescriptionCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";
	
	/**
	 * @param state
	 */
	public GbifDescriptionCsv2CdmConverter(DwcaImportState state) {
		super();
		this.state = state;
	}

	public IReader<MappedCdmBase> map(CsvStreamItem item ){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csv = item.map;
		Reference<?> sourceReference = null;
		String sourceReferecenDetail = null;
		
		Taxon taxon = getTaxon(csv);
		if (taxon != null){
			MappedCdmBase  mcb = new MappedCdmBase(item.term, csv.get(CORE_ID), taxon);
			resultList.add(mcb);
		}
		String message = "Not yet implemented"; 
		fireWarningEvent(message, item, 15);
		return new ListReader<MappedCdmBase>(resultList);
	}

	
	@Override
	public String getSourceId(CsvStreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}
	
	private Taxon getTaxon(Map<String, String> csv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(){
		return this.getClass().getName();
	}

}
