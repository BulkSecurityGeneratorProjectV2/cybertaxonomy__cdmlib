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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * Maps text data to a database string field. (Only handles one language)
 * @author a.mueller
 * @since 06.02.2012
 */
public class DbTextDataMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbTextDataMapper.class);

	private final Language language;

	public static DbTextDataMapper NewInstance(Language language, String dbAttributeString){
		return new DbTextDataMapper(language, dbAttributeString, null);
	}

	public static DbTextDataMapper NewInstance(Language language, String dbAttributeString, String defaultValue){
		return new DbTextDataMapper(language, dbAttributeString, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbTextDataMapper(Language language, String dbAttributeString, Object defaultValue) {
		super("multiLanguageText", dbAttributeString, defaultValue);
		if (language == null){
			language = Language.DEFAULT();
		}
		this.language  = language;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(TextData.class)){
			TextData textData = CdmBase.deproxy(cdmBase, TextData.class);
			LanguageString langString = textData.getMultilanguageText().get(language);
			if (langString != null){
				return langString.getText();
			}else{
				return null;
			}
		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type TextData, but was " + cdmBase.getClass());
		}
	}

	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}
