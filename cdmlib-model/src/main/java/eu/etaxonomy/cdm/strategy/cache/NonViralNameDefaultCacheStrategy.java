/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;


/**
 * This class is a default implementation for the INonViralNameCacheStrategy<T extends NonViralName> interface.
 * The method actually implements a cache strategy for botanical names so no method has to be overwritten by
 * a subclass for botanic names.
 * Where differing from this Default BotanicNameCacheStrategy other subclasses should overwrite the existing methods
 * e.g. a CacheStrategy for zoological names should overwrite getAuthorAndExAuthor
 * @author a.mueller
 */
/**
 * @author AM
 *
 * @param <T>
 */
public class NonViralNameDefaultCacheStrategy<T extends NonViralName> extends NameCacheStrategyBase<T> implements INonViralNameCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");
	
	protected String NameAuthorSeperator = " ";
	protected String BasionymStart = "(";
	protected String BasionymEnd = ")";
	protected String ExAuthorSeperator = " ex. ";
	protected CharSequence BasionymAuthorCombinationAuthorSeperator = " ";
	
	public  UUID getUuid(){
		return uuid;
	}

	
	/**
	 * Factory method
	 * @return NonViralNameDefaultCacheStrategy A new instance of  NonViralNameDefaultCacheStrategy
	 */
	public static NonViralNameDefaultCacheStrategy NewInstance(){
		return new NonViralNameDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	protected NonViralNameDefaultCacheStrategy(){
		super();
	}

/* **************** GETTER / SETTER **************************************/
	
	/**
	 * String that seperates the NameCache part from the AuthorCache part
	 * @return
	 */
	public String getNameAuthorSeperator() {
		return NameAuthorSeperator;
	}


	public void setNameAuthorSeperator(String nameAuthorSeperator) {
		NameAuthorSeperator = nameAuthorSeperator;
	}


	/**
	 * String the basionym author part starts with e.g. '('.
	 * This should correspond with the {@link NonViralNameDefaultCacheStrategy#getBasionymEnd() basionymEnd} attribute
	 * @return
	 */
	public String getBasionymStart() {
		return BasionymStart;
	}


	public void setBasionymStart(String basionymStart) {
		BasionymStart = basionymStart;
	}


	/**
	 * String the basionym author part ends with e.g. ')'.
	 * This should correspond with the {@link NonViralNameDefaultCacheStrategy#getBasionymStart() basionymStart} attribute
	 * @return
	 */
	public String getBasionymEnd() {
		return BasionymEnd;
	}


	public void setBasionymEnd(String basionymEnd) {
		BasionymEnd = basionymEnd;
	}


	/**
	 * String to seperate ex author from author.
	 * @return
	 */
	public String getExAuthorSeperator() {
		return ExAuthorSeperator;
	}


	public void setExAuthorSeperator(String exAuthorSeperator) {
		ExAuthorSeperator = exAuthorSeperator;
	}


	/**
	 * String that seperates the basionym/original_combination author part from the combination author part
	 * @return
	 */
	public CharSequence getBasionymAuthorCombinationAuthorSeperator() {
		return BasionymAuthorCombinationAuthorSeperator;
	}


	public void setBasionymAuthorCombinationAuthorSeperator(
			CharSequence basionymAuthorCombinationAuthorSeperator) {
		BasionymAuthorCombinationAuthorSeperator = basionymAuthorCombinationAuthorSeperator;
	}

	
//** *****************************************************************************************/
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// just for testing
	@Override
	public String getTitleCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		String result = "";
		if (isAutonym(nonViralName)){
			String speciesPart = getSpeciesNameCache(nonViralName);
			//TODO should this include basionym authors and ex authors
			INomenclaturalAuthor author = nonViralName.getCombinationAuthorTeam();
			String authorPart = "";
			if (author != null){
				authorPart = CdmUtils.Nz(author.getNomenclaturalTitle());
			}
			String infraSpeciesPart = (CdmUtils.Nz(nonViralName.getInfraSpecificEpithet()));
			result = CdmUtils.concat(" ", new String[]{speciesPart, authorPart, infraSpeciesPart});
			result = result.trim().replace("null", "");
		}else{
			String nameCache = CdmUtils.Nz(getNameCache(nonViralName));
			String authorCache = CdmUtils.Nz(getAuthorshipCache(nonViralName));
			result = CdmUtils.concat(NameAuthorSeperator, nameCache, authorCache);
		}
		return result;
	}
	
	
	/**
	 * Generates and returns the "name cache" (only scientific name without author teams and year).
	 * @see eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getNameCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		String result;
		Rank rank = nonViralName.getRank();
		
		if (rank == null){
			result = getRanklessNameCache(nonViralName);
		}else if (rank.isInfraSpecific()){
			result = getInfraSpeciesNameCache(nonViralName);
		}else if (rank.isSpecies()){
			result = getSpeciesNameCache(nonViralName);
		}else if (rank.isInfraGeneric()){
			result = getInfraGenusNameCache(nonViralName);
		}else if (rank.isGenus()){
			result = getGenusOrUninomialNameCache(nonViralName);
		}else if (rank.isSupraGeneric()){
			result = getGenusOrUninomialNameCache(nonViralName);
		}else{ 
			logger.warn("Name Strategy for Name (UUID: " + nonViralName.getUuid() +  ") not yet implemented");
			result = "";
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy#getAuthorCache(eu.etaxonomy.cdm.model.name.NonViralName)
	 */
	public String getAuthorshipCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		String result = "";
		INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorTeam();
		INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorTeam();
		INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorTeam();
		INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorTeam();
		String basionymPart = "";
		String authorPart = "";
		//basionym
		if (basionymAuthor != null || exBasionymAuthor != null){
			basionymPart = BasionymStart + getAuthorAndExAuthor(basionymAuthor, exBasionymAuthor) + BasionymEnd;
		}
		if (combinationAuthor != null || exCombinationAuthor != null){
			authorPart = getAuthorAndExAuthor(combinationAuthor, exCombinationAuthor);
		}
		result = CdmUtils.concat(BasionymAuthorCombinationAuthorSeperator, basionymPart, authorPart);
		return result;
	}
	
	/**
	 * Returns the AuthorCache part for a combination of an author and an ex author. This applies on combination authors
	 * as well as on basionym/orginal combination authors.
	 * @param author the author
	 * @param exAuthor the ex-author
	 * @return
	 */
	protected String getAuthorAndExAuthor(INomenclaturalAuthor author, INomenclaturalAuthor exAuthor){
		String result = "";
		String authorString = "";
		String exAuthorString = "";
		if (author != null){
			authorString = CdmUtils.Nz(author.getNomenclaturalTitle());
		}
		if (exAuthor != null){
			exAuthorString = CdmUtils.Nz(exAuthor.getNomenclaturalTitle());
		}
		if (exAuthorString.length() > 0 ){
			exAuthorString = ExAuthorSeperator + exAuthorString;
		}
		result = authorString + exAuthorString;
		return result;
 
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTaggedName(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	public List<Object> getTaggedName(T nvn) {
		List<Object> tags = new ArrayList<Object>();
		tags.add(nvn.getGenusOrUninomial());
		if (nvn.isSpecies() || nvn.isInfraSpecific()){
			tags.add(nvn.getSpecificEpithet());			
		}
		if (nvn.isInfraSpecific()){
			tags.add(nvn.getRank());			
			tags.add(nvn.getInfraSpecificEpithet());			
		}
		Team at = Team.NewInstance();
		at.setProtectedTitleCache(true);
		at.setTitleCache(nvn.getAuthorshipCache());
		tags.add(at);			
		tags.add(nvn.getNomenclaturalReference());			
		return tags;
	}
	

	/************** PRIVATES ****************/
		
		protected String getRanklessNameCache(NonViralName nonViralName){
			String result = "";
			result = (result + (nonViralName.getGenusOrUninomial())).trim().replace("null", "-");
			result += " " + (CdmUtils.Nz(nonViralName.getSpecificEpithet())).trim();
			result += " " + (CdmUtils.Nz(nonViralName.getInfraSpecificEpithet())).trim();
			result = result.trim().replace("null", "-");
			//result += " (rankless)";
			return result;			
		}
	
	
		protected String getGenusOrUninomialNameCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			return result;
		}
		
		protected String getInfraGenusNameCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " (" + (CdmUtils.Nz(nonViralName.getInfraGenericEpithet()) + ")").trim().replace("null", "");
			return result;
		}

		
		protected String getSpeciesNameCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " " + CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim().replace("null", "");
			return result;
		}
		
		
		protected String getInfraSpeciesNameCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " " + (CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim()).replace("null", "");
			if (! isAutonym(nonViralName)){
				result += " " + (nonViralName.getRank().getAbbreviation()).trim().replace("null", "");
			}
			result += " " + (CdmUtils.Nz(nonViralName.getInfraSpecificEpithet())).trim().replace("null", "");
			return result;
		}
		
		
		/**
		 * @param name
		 * @return true, if name has Rank, Rank is below species and species epithet equals infraSpeciesEpithtet, else false
		 */
		protected boolean isAutonym(NonViralName nonViralName){
			if (nonViralName.getRank() != null && nonViralName.getRank().isInfraSpecific() && nonViralName.getSpecificEpithet() != null && nonViralName.getSpecificEpithet().trim().equals(nonViralName.getInfraSpecificEpithet().trim())){
				return true;
			}else{
				return false;
			}
		}
	
}
