/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author AM
 *
 */
public abstract class NameCacheStrategyBase<T extends TaxonNameBase> extends StrategyBase implements INameCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(NameCacheStrategyBase.class);

	final static UUID uuid = UUID.fromString("817ae5b5-3ac2-414b-a134-a9ae86cba040");

	/**
	 * 
	 */
	public NameCacheStrategyBase() {
		super();
	}
	

	/**
	 * Generates and returns the "full name cache" (including scientific name, author teams and eventually year).
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public abstract String getTitleCache(T name);


	
	public abstract List<Object> getTaggedName(T taxonNameBase);



}
