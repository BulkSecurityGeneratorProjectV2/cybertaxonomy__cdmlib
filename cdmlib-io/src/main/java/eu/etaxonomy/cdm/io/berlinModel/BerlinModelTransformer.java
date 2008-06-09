package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public final class BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(BerlinModelTransformer.class);
 
	//REFERENCES
	public static int REF_ARTICLE = 1;
	public static int REF_PART_OF_OTHER_TITLE = 2;
	public static int REF_BOOK = 3;
	public static int REF_DATABASE = 4;
	public static int REF_INFORMAL = 5;
	public static int REF_NOT_APPLICABLE = 6;
	public static int REF_WEBSITE = 7;
	public static int REF_CD = 8;
	public static int REF_JOURNAL = 9;
	public static int REF_UNKNOWN = 10;

	
	//NameStatus
	public static int NAME_ST_NOM_INVAL = 1;
	public static int NAME_ST_NOM_ILLEG = 2;
	public static int NAME_ST_NOM_NUD = 3;
	public static int NAME_ST_NOM_REJ = 4;
	public static int NAME_ST_NOM_REJ_PROP = 5;
	public static int NAME_ST_NOM_UTIQUE_REJ = 6;
	public static int NAME_ST_NOM_UTIQUE_REJ_PROP = 7;
	public static int NAME_ST_NOM_CONS = 8;
	public static int NAME_ST_NOM_CONS_PROP = 9;
	public static int NAME_ST_ORTH_CONS = 10;
	public static int NAME_ST_ORTH_CONS_PROP = 11;
	public static int NAME_ST_NOM_SUPERFL = 12;
	public static int NAME_ST_NOM_AMBIG = 13;
	public static int NAME_ST_NOM_PROVIS = 14;
	public static int NAME_ST_NOM_DUB = 15;
	public static int NAME_ST_NOM_NOV = 16;
	public static int NAME_ST_NOM_CONFUS = 17;
	public static int NAME_ST_NOM_ALTERN = 18;
	public static int NAME_ST_COMB_INVAL = 19;
	
	
	//NameRelationShip
	public static int NAME_REL_IS_BASIONYM_FOR = 1;
	public static int NAME_REL_IS_LATER_HOMONYM_OF = 2;
	public static int NAME_REL_IS_REPLACED_SYNONYM_FOR = 3;
	public static int NAME_REL_IS_VALIDATION_OF = 4;
	public static int NAME_REL_IS_LATER_VALIDATION_OF = 5;
	public static int NAME_REL_IS_TYPE_OF = 6;
	public static int NAME_REL_IS_CONSERVED_TYPE_OF =7;
	public static int NAME_REL_IS_REJECTED_TYPE_OF = 8;
	public static int NAME_REL_IS_FIRST_PARENT_OF = 9;
	public static int NAME_REL_IS_SECOND_PARENT_OF = 10;
	public static int NAME_REL_IS_FEMALE_PARENT_OF = 11;
	public static int NAME_REL_IS_MALE_PARENT_OF = 12;
	public static int NAME_REL_IS_CONSERVED_AGAINST =13;
	public static int NAME_REL_IS_REJECTED_IN_FAVOUR_OF = 14;
	public static int NAME_REL_IS_TREATED_AS_LATER_HOMONYM_OF = 15;
	public static int NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF = 16;
	public static int NAME_REL_HAS_SAME_TYPE_AS = 18;
	public static int NAME_REL_IS_LECTOTYPE_OF = 61;
	public static int NAME_REL_TYPE_NOT_DESIGNATED = 62;
	
	//NameFacts
	public static String NAME_FACT_PROTOLOGUE = "Protologue";
	public static String NAME_FACT_ALSO_PUBLISHED_IN = "Also published in";
	
	//TYPES
	public static int NAME_TYPE_HOLOTYPE = 1;
	public static int NAME_TYPE_LECTOTYPE = 2;
	public static int NAME_TYPE_NEOTYPE = 3;
	public static int NAME_TYPE_EPITYPE = 4;
	public static int NAME_TYPE_ISO_LECTOTYPE = 5;
	public static int NAME_TYPE_ISO_NEOTYPE = 6;
	public static int NAME_TYPE_ISO_TYPE = 7;
	public static int NAME_TYPE_PARA_NEOTYPE = 8;
	public static int NAME_TYPE_PARA_TYPE = 9;
	public static int NAME_TYPE_SECOND_LECTOTYPE = 10;
	public static int NAME_TYPE_SECOND_NEOTYPE = 11;
	public static int NAME_TYPE_SYNTYPE = 12;
	public static int NAME_TYPE_ICONOTYPE = 21;
	public static int NAME_TYPE_PHOTOTYPE = 22;
	
	//TaxonRelationShip
	public static int TAX_REL_IS_INCLUDED_IN = 1;
	public static int TAX_REL_IS_SYNONYM_OF = 2;
	public static int TAX_REL_IS_MISAPPLIED_NAME_OF = 3;
	public static int TAX_REL_IS_HETEROTYPIC_SYNONYM_OF = 6;
	public static int TAX_REL_IS_HOMOTYPIC_SYNONYM_OF = 7;
	

	//TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;
	
	
	//Facts
	public static int FACT_DESCRIPTION = 1;
	public static int FACT_GROWTH_FORM = 2;
	public static int FACT_HARDINESS = 3;
	public static int FACT_ECOLOGY = 4;
	public static int FACT_PHENOLOGY = 5;
	public static int FACT_KARYOLOGY = 6;
	public static int FACT_ILLUSTRATION = 7;
	public static int FACT_IDENTIFICATION = 8;
	public static int FACT_OBSERVATION = 9;
	public static int FACT_DISTIRBUTION_EM = 10;
	public static int FACT_DISTIRBUTION_WORLD = 11;
	
	/** Creates an cdm-Rank by the berlinModel rankId
	 * @param doubt doubtfulFalg
	 * @return "true" if doubt = "a"
	 */
	public static Rank rankId2Rank (int rankId) throws UnknownCdmTypeException{
		switch (rankId){
			case 1: return Rank.KINGDOM();
			case 3: return Rank.SUBKINGDOM();
			case 5: return Rank.PHYLUM();
			case 7: return Rank.SUBPHYLUM();
			case 8: return Rank.DIVISION();
			case 9: return Rank.SUBDIVISION();
			case 10: return Rank.CLASS();
			case 13: return Rank.SUBCLASS();
			case 16: return Rank.SUPERORDER();
			case 18: return Rank.ORDER();
			case 19: return Rank.SUBORDER();
			case 20: return Rank.FAMILY();
			case 25: return Rank.SUBFAMILY();
			case 30: return Rank.TRIBE();
			case 35: return Rank.SUBTRIBE();
			case 40: return Rank.GENUS();
			case 42: return Rank.SUBGENUS();
			case 45: return Rank.SECTION();
			case 47: return Rank.SUBSECTION();
			case 50: return Rank.SERIES();
			case 52: return Rank.SUBSERIES();
			case 58: return Rank.SPECIESAGGREGATE();
			//FIXME
			//case 59: return Rank.SPECIESAGGREGATE();
			case 60: return Rank.SPECIES();
			case 65: return Rank.SUBSPECIES();
			case 68: return Rank.CONVAR();
			case 70: return Rank.VARIETY();
			case 73: return Rank.SUBVARIETY();
			case 80: return Rank.FORM();
			case 82: return Rank.SUBFORM();
			case 84: return Rank.SPECIALFORM();
			case 98: return Rank.INFRAGENERICTAXON();
			case 99: return Rank.INFRASPECIFICTAXON();
			default: {
				throw new UnknownCdmTypeException("Unknown Rank id" + Integer.valueOf(rankId).toString());
			}
		}		
	}
		
	
}
