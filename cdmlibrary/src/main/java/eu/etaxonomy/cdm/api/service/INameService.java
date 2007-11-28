package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;


public interface INameService extends IIdentifiableEntityService<TaxonNameBase> {

	public abstract TaxonNameBase getTaxonNameByUuid(String uuid);

	public abstract String saveTaxonName(TaxonNameBase taxonName);

	public abstract List<TaxonNameBase> getAllNames(int limit, int start);

	public abstract List<TaxonNameBase> getNamesByName(String name);

	public abstract TermVocabulary getRankEnumeration();
}