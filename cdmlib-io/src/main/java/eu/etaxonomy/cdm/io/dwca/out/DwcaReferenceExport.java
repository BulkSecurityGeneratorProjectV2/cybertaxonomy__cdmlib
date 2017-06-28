/**
d* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaReferenceExport extends DwcaExportBase {

    private static final long serialVersionUID = -8334741499089219441L;

    private static final Logger logger = Logger.getLogger(DwcaReferenceExport.class);

	protected static final String fileName = "reference.txt";
	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Reference";

	/**
	 * Constructor
	 */
	public DwcaReferenceExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 *
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected void doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);

		DwcaTaxOutputFile file = DwcaTaxOutputFile.REFERENCE;

		try {
			DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

            List<TaxonNode> allNodes = allNodes(state);
			for (TaxonNode node : allNodes){

			    //sec
				DwcaReferenceRecord record = new DwcaReferenceRecord(metaRecord, config);
				Taxon taxon = CdmBase.deproxy(node.getTaxon());
				Reference sec = taxon.getSec();
				if (sec != null && ! state.recordExists(file, sec)){
					handleReference(state, record, sec, taxon);
					PrintWriter writer = createPrintWriter(state, file);
                    record.write(state, writer);
					state.addExistingRecord(file, sec);
				}

				//nomRef
				record = new DwcaReferenceRecord(metaRecord, config);
				INomenclaturalReference nomRefI = taxon.getName().getNomenclaturalReference();
				Reference nomRef = CdmBase.deproxy(nomRefI, Reference.class);
				if (nomRef != null && ! state.recordExists(file, nomRef)){
					handleReference(state, record, nomRef, taxon);
					PrintWriter writer = createPrintWriter(state, file);
		            record.write(state, writer);
					state.addExistingRecord(file, nomRef);
				}

                flushWriter(state, file);

			}
		} catch (Exception e) {
	          String message = "Unexpected exception: " + e.getMessage();
	          state.getResult().addException (e, message, "DwcaReferenceExport.doInvoke()");
		} finally{
			closeWriter(file, state);
		}
		commitTransaction(txStatus);
		return;
	}

	private void handleReference(DwcaTaxExportState state, DwcaReferenceRecord record, Reference reference, Taxon taxon) {

		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());

		record.setISBN_ISSN(StringUtils.isNotBlank(reference.getIsbn())? reference.getIsbn(): reference.getIssn());
		record.setUri(reference.getUri());
		record.setDoi(reference.getDoiString());
		record.setLsid(reference.getLsid());
		//TODO microreference
		record.setBibliographicCitation(reference.getTitleCache());
		record.setTitle(reference.getTitle());
		record.setCreator(reference.getAuthorship());
		record.setDate(reference.getDatePublished());
		record.setSource(reference.getInReference()==null?null:reference.getInReference().getTitleCache());

		//FIXME abstracts, remarks, notes
		record.setDescription(reference.getReferenceAbstract());
		//FIXME
		record.setSubject(null);

		//TODO missing, why ISO639-1 better 639-3
		record.setLanguage(null);
		record.setRights(reference.getRights());
		//TODO
		record.setTaxonRemarks(null);
		//TODO
		record.setType(null);
	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoReferences();
	}

}
