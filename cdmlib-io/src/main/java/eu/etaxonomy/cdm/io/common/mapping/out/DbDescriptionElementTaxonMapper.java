/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbDescriptionElementTaxonMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DbDescriptionElementTaxonMapper.class);

	private boolean isCache = false;
	private boolean cacheIsNameTitleCache = false;

	public static DbDescriptionElementTaxonMapper NewInstance(String dbAttributeString){
		return new DbDescriptionElementTaxonMapper(dbAttributeString, false, false, null);
	}

	public static DbDescriptionElementTaxonMapper NewInstance(String dbAttributeString, boolean isCache, boolean cacheIsNameTitleCache, Object defaultValue){
		return new DbDescriptionElementTaxonMapper(dbAttributeString, isCache, cacheIsNameTitleCache, defaultValue);
	}

	protected DbDescriptionElementTaxonMapper(String dbAttributeString, boolean isCache, boolean cacheIsNameTitleCache, Object defaultValue) {
		super("inDescription.taxon", dbAttributeString, defaultValue);
		this.isCache = isCache;
		this.cacheIsNameTitleCache = cacheIsNameTitleCache;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		Object result = null;
		if (cdmBase.isInstanceOf(DescriptionElementBase.class)){
			DescriptionElementBase element = CdmBase.deproxy(cdmBase, DescriptionElementBase.class);
			DescriptionBase<?> inDescription = element.getInDescription();
			if (inDescription != null ){
				if (inDescription.isInstanceOf(TaxonDescription.class)) {
					TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
					Taxon taxon = taxonDescription.getTaxon();
					if (isCache){
						if (cacheIsNameTitleCache && taxon.getName() != null){
							return taxon.getName().getTitleCache();
						}else{
							return taxon.getTitleCache();
						}
					}else{
						result = getState().getDbId(taxon);
					}
				}else if (inDescription.isInstanceOf(TaxonNameDescription.class)){
					TaxonNameDescription nameDescription = CdmBase.deproxy(inDescription, TaxonNameDescription.class);
					TaxonName taxonName = nameDescription.getTaxonName();
					if (isCache){
						return taxonName.getTitleCache();
					}else{
						result = getState().getDbId(taxonName);
					}
				}else{
					throw new ClassCastException("Description of type "+ inDescription.getClass().getName() + " not handled yet");
				}
			}else{
				throw new ClassCastException("DescriptionElement has no description "+ element.getUuid());
			}
			return result;
		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type DescriptionElementBase, but was " + cdmBase.getClass());
		}
	}

	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}

	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return  Integer.class;
		}
	}
}