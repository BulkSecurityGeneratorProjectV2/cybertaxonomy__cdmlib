package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


/**
 * @author a.mueller
 *
 */
public interface ITaxonNameParser<T extends TaxonNameBase> extends IStrategy {
	

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials. 
	 */
	public T parseFullName(String fullName);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @param rank
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseFullName(String fullName, Rank rank);

	/**
 	 * TODO What happens with existing data in nameToBeFilled 
	 * Parses the taxonname String and fills the result into the existing TaxonNameBase nameToBeFilled. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * No change is done to nameToBeFilled if fullName == null.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @param rank
	 * @param nameToBeFilled The TaxonNameBaseToBeFilled
	 */
	public void parseFullName(BotanicalName nameToBeFilled, String fullName, Rank rank);

	
	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase without Author, Year, Reference etc.
	 * @param rank
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials  
	 */
	public T parseSimpleName(String simpleName, Rank rank);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase without Author, Year, Reference etc.
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseSimpleName(String simpleName);
	

	
	
}
