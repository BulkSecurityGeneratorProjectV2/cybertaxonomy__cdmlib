// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ISectionBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 16.02.2010
 *
 */
public final class PesiTransformer {
	private static final Logger logger = Logger.getLogger(PesiTransformer.class);

	// References
	public static int REF_ARTICLE_IN_PERIODICAL = 1;
	public static int REF_PART_OF_OTHER = 2;
	public static int REF_BOOK = 3;
	public static int REF_DATABASE = 4;
	public static int REF_INFORMAL = 5;
	public static int REF_NOT_APPLICABLE = 6;
	public static int REF_WEBSITE = 7;
	public static int REF_PUBLISHED = 8;
	public static int REF_JOURNAL = 9;
	public static int REF_UNRESOLVED = 10;
	public static int REF_PUBLICATION = 11;

	public static String REF_STR_ARTICLE_IN_PERIODICAL = "Article in periodical";
	public static String REF_STR_PART_OF_OTHER = "Part of other";
	public static String REF_STR_BOOK = "Book";
	public static String REF_STR_DATABASE = "Database";
	public static String REF_STR_INFORMAL = "Informal";
	public static String REF_STR_NOT_APPLICABLE = "Not applicable";
	public static String REF_STR_WEBSITE = "Website";
	public static String REF_STR_PUBLISHED = "Published";
	public static String REF_STR_JOURNAL = "Journal";
	public static String REF_STR_UNRESOLVED = "Unresolved";
	public static String REF_STR_PUBLICATION = "Publication";
	
	// NameStatus
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
	public static int NAME_ST_LEGITIMATE = 20; // PESI specific from here
	public static int NAME_ST_COMB_INED = 21;
	public static int NAME_ST_COMB_AND_STAT_INED = 22;
	public static int NAME_ST_NOM_AND_ORTH_CONS = 23;
	public static int NAME_ST_NOM_NOV_INED = 24;
	public static int NAME_ST_SP_NOV_INED = 25;
	public static int NAME_ST_ALTERNATE_REPRESENTATION = 26;
	public static int NAME_ST_TEMPORARY_NAME = 27;
	public static int NAME_ST_SPECIES_INQUIRENDA = 28;

	public static String NAME_ST_STR_NOM_INVAL = "Nom. Inval.";
	public static String NAME_ST_STR_NOM_ILLEG = "Nom. Illeg.";
	public static String NAME_ST_STR_NOM_NUD = "Nom. Nud.";
	public static String NAME_ST_STR_NOM_REJ = "Nom. Rej.";
	public static String NAME_ST_STR_NOM_REJ_PROP = "Nom. Rej. Prop.";
	public static String NAME_ST_STR_NOM_UTIQUE_REJ = "Nom. Utique Rej.";
	public static String NAME_ST_STR_NOM_UTIQUE_REJ_PROP = "Nom. Utique Rej. Prop.";
	public static String NAME_ST_STR_NOM_CONS = "Nom. Cons.";
	public static String NAME_ST_STR_NOM_CONS_PROP = "Nom. Cons. Prop.";
	public static String NAME_ST_STR_ORTH_CONS = "Orth. Cons.";
	public static String NAME_ST_STR_ORTH_CONS_PROP = "Orth. Cons. Prop.";
	public static String NAME_ST_STR_NOM_SUPERFL = "Nom. Superfl.";
	public static String NAME_ST_STR_NOM_AMBIG = "Nom. Ambig.";
	public static String NAME_ST_STR_NOM_PROVIS = "Nom. Provis.";
	public static String NAME_ST_STR_NOM_DUB = "Nom. Dub.";
	public static String NAME_ST_STR_NOM_NOV = "Nom. Nov.";
	public static String NAME_ST_STR_NOM_CONFUS = "Nom. Confus.";
	public static String NAME_ST_STR_NOM_ALTERN = "Nom. Altern.";
	public static String NAME_ST_STR_COMB_INVAL = "Comb. Inval.";
	public static String NAME_ST_STR_LEGITIMATE = "Legitim"; 
	public static String NAME_ST_STR_COMB_INED = "Comb. Ined."; // PESI specific from here
	public static String NAME_ST_STR_COMB_AND_STAT_INED = "Comb. & Stat. Ined.";
	public static String NAME_ST_STR_NOM_AND_ORTH_CONS = "Nom. & Orth. Cons.";
	public static String NAME_ST_STR_NOM_NOV_INED = "Nom. Nov. Ined.";
	public static String NAME_ST_STR_SP_NOV_INED = "Sp. Nov. Ined.";
	public static String NAME_ST_STR_ALTERNATE_REPRESENTATION = "Alternate Representation";
	public static String NAME_ST_STR_TEMPORARY_NAME = "Temporary Name";
	public static String NAME_ST_STR_SPECIES_INQUIRENDA = "Species Inquirenda";

	// TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;
	
	public static String T_STATUS_STR_ACCEPTED = "Accepted";
	public static String T_STATUS_STR_SYNONYM = "Synonym";
	public static String T_STATUS_STR_PARTIAL_SYN = "Partial Synonym";
	public static String T_STATUS_STR_PRO_PARTE_SYN = "Pro Parte Synonym";
	public static String T_STATUS_STR_UNRESOLVED = "Unresolved";
	public static String T_STATUS_STR_ORPHANED = "Orphaned";
	
	// TypeDesginationStatus
	public static int TYPE_BY_ORIGINAL_DESIGNATION = 1;
	public static int TYPE_BY_SUBSEQUENT_DESIGNATION = 2;
	public static int TYPE_BY_MONOTYPY = 3;
	
	public static String TYPE_STR_BY_ORIGINAL_DESIGNATION = "Type by original designation";
	public static String TYPE_STR_BY_SUBSEQUENT_DESIGNATION = "Type by subsequent designation";
	public static String TYPE_STR_BY_MONOTYPY = "Type by monotypy";
	
	// RelTaxonQualifier
	public static int IS_BASIONYM_FOR = 1;
	public static int IS_LATER_HOMONYM_OF = 2;
	public static int IS_REPLACED_SYNONYM_FOR = 3;
	public static int IS_VALIDATION_OF = 4;
	public static int IS_LATER_VALIDATION_OF = 5;
	public static int IS_TYPE_OF = 6;
	public static int IS_CONSERVED_TYPE_OF = 7;
	public static int IS_REJECTED_TYPE_OF = 8;
	public static int IS_FIRST_PARENT_OF = 9;
	public static int IS_SECOND_PARENT_OF = 10;
	public static int IS_FEMALE_PARENT_OF = 11;
	public static int IS_MALE_PARENT_OF = 12;
	public static int IS_CONSERVED_AGAINST = 13;
	public static int IS_REJECTED_IN_FAVOUR_OF = 14;
	public static int IS_TREATED_AS_LATER_HOMONYM_OF = 15;
	public static int IS_ORTHOGRAPHIC_VARIANT_OF = 16;
	public static int IS_ALTERNATIVE_NAME_FOR = 17;
	public static int HAS_SAME_TYPE_AS = 18;
	public static int IS_LECTOTYPE_OF = 61;
	public static int TYPE_NOT_DESIGNATED = 62;
	public static int IS_TAXONOMICALLY_INCLUDED_IN = 101;
	public static int IS_SYNONYM_OF = 102;
	public static int IS_MISAPPLIED_NAME_FOR = 103;
	public static int IS_PRO_PARTE_SYNONYM_OF = 104;
	public static int IS_PARTIAL_SYNONYM_OF = 105;
	public static int IS_HETEROTYPIC_SYNONYM_OF = 106;
	public static int IS_HOMOTYPIC_SYNONYM_OF = 107;
	public static int IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF = 201;
	public static int IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF = 202;
	public static int IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF = 203;
	public static int IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF = 204;
	public static int IS_INFERRED_EPITHET_FOR = 301;
	public static int IS_INFERRED_GENUS_FOR = 302;
	public static int IS_POTENTIAL_COMBINATION_FOR = 303;

	public static String STR_IS_BASIONYM_FOR = "is basionym for";
	public static String STR_IS_LATER_HOMONYM_OF = "is later homonym of";
	public static String STR_IS_REPLACED_SYNONYM_FOR = "is replaced synonym for";
	public static String STR_IS_VALIDATION_OF = "is validation of";
	public static String STR_IS_LATER_VALIDATION_OF = "is later validation of";
	public static String STR_IS_TYPE_OF = "is type of";
	public static String STR_IS_CONSERVED_TYPE_OF = "is conserved type of";
	public static String STR_IS_REJECTED_TYPE_OF = "is rejected type of";
	public static String STR_IS_FIRST_PARENT_OF = "is first parent of";
	public static String STR_IS_SECOND_PARENT_OF = "is second parent of";
	public static String STR_IS_FEMALE_PARENT_OF = "is female parent of";
	public static String STR_IS_MALE_PARENT_OF = "is male parent of";
	public static String STR_IS_CONSERVED_AGAINST = "is conserved against";
	public static String STR_IS_REJECTED_IN_FAVOUR_OF = "is rejected in favour of";
	public static String STR_IS_TREATED_AS_LATER_HOMONYM_OF = "is treated as later homonym of";
	public static String STR_IS_ORTHOGRAPHIC_VARIANT_OF = "is orthographic variant of";
	public static String STR_IS_ALTERNATIVE_NAME_FOR = "is alternative name for";
	public static String STR_HAS_SAME_TYPE_AS = "has same type as";
	public static String STR_IS_LECTOTYPE_OF = "is lectotype of";
	public static String STR_TYPE_NOT_DESIGNATED = "type not designated";
	public static String STR_IS_TAXONOMICALLY_INCLUDED_IN  = "is taxonomically included in";
	public static String STR_IS_SYNONYM_OF = "is synonym of";
	public static String STR_IS_MISAPPLIED_NAME_FOR = "is misapplied name for";
	public static String STR_IS_PRO_PARTE_SYNONYM_OF = "is pro parte synonym of";
	public static String STR_IS_PARTIAL_SYNONYM_OF = "is partial synonym of";
	public static String STR_IS_HETEROTYPIC_SYNONYM_OF = "is heterotypic synonym of";
	public static String STR_IS_HOMOTYPIC_SYNONYM_OF = "is homotypic synonym of";
	public static String STR_IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF = "is pro parte and homotypic synonym of";
	public static String STR_IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF = "is pro parte and heterotypic synonym of";
	public static String STR_IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF = "is partial and homotypic synonym of";
	public static String STR_IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF = "is partial and heterotypic synonym of";
	public static String STR_IS_INFERRED_EPITHET_FOR = "is inferred epithet for";
	public static String STR_IS_INFERRED_GENUS_FOR = "is inferred genus for";
	public static String STR_IS_POTENTIAL_COMBINATION_FOR = "is potential combination for";

	// Kingdoms
	public static int KINGDOM_NULL = 0;
	public static int KINGDOM_ANIMALIA = 2;
	public static int KINGDOM_PLANTAE = 3;
	public static int KINGDOM_FUNGI = 4;
	public static int KINGDOM_PROTOZOA = 5;
	public static int KINGDOM_BACTERIA = 6;
	public static int KINGDOM_CHROMISTA = 7;
	
	public static String STR_KINGDOM_NULL = "NULL";
	public static String STR_KINGDOM_ANIMALIA = "Animalia";
	public static String STR_KINGDOM_PLANTAE = "Plantae";
	public static String STR_KINGDOM_FUNGI = "Fungi";
	public static String STR_KINGDOM_PROTOZOA = "Protozoa";
	public static String STR_KINGDOM_BACTERIA = "Bacteria";
	public static String STR_KINGDOM_CHROMISTA = "Chromista";

	// Animalia Ranks
	public static int Animalia_Kingdom = 10;
	public static int Animalia_Subkingdom = 20;
	public static int Animalia_Superphylum = 23;
	public static int Animalia_Phylum = 30;
	public static int Animalia_Subphylum = 40;
	public static int Animalia_Infraphylum = 45;
	public static int Animalia_Superclass = 50;
	public static int Animalia_Class = 60;
	public static int Animalia_Subclass = 70;
	public static int Animalia_Infraclass = 80;
	public static int Animalia_Superorder = 90;
	public static int Animalia_Order = 100;
	public static int Animalia_Suborder = 110;
	public static int Animalia_Infraorder = 120;
	public static int Animalia_Section = 121;
	public static int Animalia_Subsection = 122;
	public static int Animalia_Superfamily = 130;
	public static int Animalia_Family = 140;
	public static int Animalia_Subfamily = 150;
	public static int Animalia_Tribe = 160;
	public static int Animalia_Subtribe = 170;
	public static int Animalia_Genus = 180;
	public static int Animalia_Subgenus = 190;
	public static int Animalia_Species =220;
	public static int Animalia_Subspecies = 230;
	public static int Animalia_Natio = 235;
	public static int Animalia_Variety = 240;
	public static int Animalia_Subvariety = 250;
	public static int Animalia_Forma = 260;

	public static String Animalia_STR_Kingdom = "Kingdom";
	public static String Animalia_STR_Subkingdom = "Subkingdom";
	public static String Animalia_STR_Superphylum = "Superphylum";
	public static String Animalia_STR_Phylum = "Phylum";
	public static String Animalia_STR_Subphylum = "Subphylum";
	public static String Animalia_STR_Infraphylum = "Infraphylum";
	public static String Animalia_STR_Superclass = "Superclass";
	public static String Animalia_STR_Class = "Class";
	public static String Animalia_STR_Subclass = "Subclass";
	public static String Animalia_STR_Infraclass = "Infraclass";
	public static String Animalia_STR_Superorder = "Superorder";
	public static String Animalia_STR_Order = "Order";
	public static String Animalia_STR_Suborder = "Suborder";
	public static String Animalia_STR_Infraorder = "Infraorder";
	public static String Animalia_STR_Section = "Section";
	public static String Animalia_STR_Subsection = "Subsection";
	public static String Animalia_STR_Superfamily = "Superfamily";
	public static String Animalia_STR_Family = "Family";
	public static String Animalia_STR_Subfamily = "Subfamily";
	public static String Animalia_STR_Tribe = "Tribe";
	public static String Animalia_STR_Subtribe = "Subtribe";
	public static String Animalia_STR_Genus = "Genus";
	public static String Animalia_STR_Subgenus = "Subgenus";
	public static String Animalia_STR_Species = "Species";
	public static String Animalia_STR_Subspecies = "Subspecies";
	public static String Animalia_STR_Natio = "Natio";
	public static String Animalia_STR_Variety = "Variety";
	public static String Animalia_STR_Subvariety = "Subvariety";
	public static String Animalia_STR_Forma = "Forma";

	// Plantae Ranks
	public static int Plantae_Kingdom = 10;
	public static int Plantae_Subkingdom = 20;
	public static int Plantae_Division = 30;
	public static int Plantae_Subdivision = 40;
	public static int Plantae_Class = 60;
	public static int Plantae_Subclass = 70;
	public static int Plantae_Order = 100;
	public static int Plantae_Suborder = 110;
	public static int Plantae_Family = 140;
	public static int Plantae_Subfamily = 150;
	public static int Plantae_Tribe	= 160;
	public static int Plantae_Subtribe = 170;
	public static int Plantae_Genus = 180;
	public static int Plantae_Subgenus = 190;
	public static int Plantae_Section = 200;
	public static int Plantae_Subsection = 210;
	public static int Plantae_Series = 212;
	public static int Plantae_Subseries	= 214;
	public static int Plantae_Aggregate	= 216;
	public static int Plantae_Coll_Species = 218;
	public static int Plantae_Species = 220;
	public static int Plantae_Subspecies = 230;
	public static int Plantae_Proles = 232;
	public static int Plantae_Race = 234;
	public static int Plantae_Convarietas = 236;
	public static int Plantae_Variety = 240;
	public static int Plantae_Subvariety = 250;
	public static int Plantae_Forma	= 260;
	public static int Plantae_Subforma = 270;
	public static int Plantae_Forma_spec = 275;
	public static int Plantae_Taxa_infragen = 280;
	public static int Plantae_Taxa_infraspec = 285;
	
	public static String Plantae_STR_Kingdom = "Kingdom";
	public static String Plantae_STR_Subkingdom = "Subkingdom";
	public static String Plantae_STR_Division = "Division";
	public static String Plantae_STR_Subdivision = "Subdivision";
	public static String Plantae_STR_Class = "Class";
	public static String Plantae_STR_Subclass = "Subclass";
	public static String Plantae_STR_Order = "Order";
	public static String Plantae_STR_Suborder = "Suborder";
	public static String Plantae_STR_Family = "Family";
	public static String Plantae_STR_Subfamily = "Subfamily";
	public static String Plantae_STR_Tribe	= "Tribe";
	public static String Plantae_STR_Subtribe = "Subtribe";
	public static String Plantae_STR_Genus = "Genus";
	public static String Plantae_STR_Subgenus = "Subgenus";
	public static String Plantae_STR_Section = "Section";
	public static String Plantae_STR_Subsection = "Subsection";
	public static String Plantae_STR_Series = "Series";
	public static String Plantae_STR_Subseries	= "Subseries";
	public static String Plantae_STR_Aggregate	= "Aggregate";
	public static String Plantae_STR_Coll_Species = "Coll. Species";
	public static String Plantae_STR_Species = "Species";
	public static String Plantae_STR_Subspecies = "Subspecies";
	public static String Plantae_STR_Proles = "Proles";
	public static String Plantae_STR_Race = "Race";
	public static String Plantae_STR_Convarietas = "Convarietas";
	public static String Plantae_STR_Variety = "Variety";
	public static String Plantae_STR_Subvariety = "Subvariety";
	public static String Plantae_STR_Forma	= "Forma";
	public static String Plantae_STR_Subforma = "Subforma";
	public static String Plantae_STR_Forma_spec = "Forma spec.";
	public static String Plantae_STR_Taxa_infragen = "Taxa infragen.";
	public static String Plantae_STR_Taxa_infraspec = "Taxa infraspec.";
	
	// Fungi Ranks
	public static int Fungi_Kingdom = 10;
	public static int Fungi_Subkingdom = 20;
	public static int Fungi_Division = 30;
	public static int Fungi_Subdivision = 40;
	public static int Fungi_Class	= 60;
	public static int Fungi_Subclass = 70;
	public static int Fungi_Order	= 100;
	public static int Fungi_Suborder = 110;
	public static int Fungi_Family = 140;
	public static int Fungi_Subfamily = 150;
	public static int Fungi_Tribe = 160;
	public static int Fungi_Subtribe = 170;
	public static int Fungi_Genus = 180;
	public static int Fungi_Subgenus = 190;
	public static int Fungi_Section = 200;
	public static int Fungi_Subsection = 210;
	public static int Fungi_Species = 220;
	public static int Fungi_Subspecies = 230;
	public static int Fungi_Variety = 240;
	public static int Fungi_Subvariety = 250;
	public static int Fungi_Forma	= 260;
	public static int Fungi_Subforma = 270;
	
	//Protozoa Ranks
	public static int Protozoa_Kingdom = 10;
	public static int Protozoa_Subkingdom = 20;
	public static int Protozoa_Phylum = 30;
	public static int Protozoa_Subphylum = 40;
	public static int Protozoa_Superclass = 50;
	public static int Protozoa_Class	= 60;
	public static int Protozoa_Subclass = 70;
	public static int Protozoa_Infraclass = 80;
	public static int Protozoa_Superorder = 90;
	public static int Protozoa_Order	= 100;
	public static int Protozoa_Suborder = 110;
	public static int Protozoa_Infraorder = 120;
	public static int Protozoa_Superfamily = 130;
	public static int Protozoa_Family = 140;
	public static int Protozoa_Subfamily = 150;
	public static int Protozoa_Tribe	= 160;
	public static int Protozoa_Subtribe = 170;
	public static int Protozoa_Genus	= 180;
	public static int Protozoa_Subgenus = 190;
	public static int Protozoa_Species = 220;
	public static int Protozoa_Subspecies = 230;
	public static int Protozoa_Variety = 240;
	public static int Protozoa_Forma	= 260;
	
	// Bacteria Ranks
	public static int Bacteria_Kingdom = 10;
	public static int Bacteria_Subkingdom = 20;
	public static int Bacteria_Phylum = 30;
	public static int Bacteria_Subphylum	= 40;
	public static int Bacteria_Superclass = 50;
	public static int Bacteria_Class	= 60;
	public static int Bacteria_Subclass = 70;
	public static int Bacteria_Infraclass = 80;
	public static int Bacteria_Superorder = 90;
	public static int Bacteria_Order	= 100;
	public static int Bacteria_Suborder = 110;
	public static int Bacteria_Infraorder = 120;
	public static int Bacteria_Superfamily = 130;
	public static int Bacteria_Family = 140;
	public static int Bacteria_Subfamily	= 150;
	public static int Bacteria_Tribe	= 160;
	public static int Bacteria_Subtribe = 170;
	public static int Bacteria_Genus	= 180;
	public static int Bacteria_Subgenus = 190;
	public static int Bacteria_Species = 220;
	public static int Bacteria_Subspecies = 230;
	public static int Bacteria_Variety = 240;
	public static int Bacteria_Forma	= 260;
	
	public static String STR_BACTERIA_KINGDOM = "Kingdom";
	public static String STR_BACTERIA_SUBKINGDOM = "Subkingdom";
	public static String STR_BACTERIA_PHYLUM = "Phylum";
	public static String STR_BACTERIA_SUBPHYLUM = "Subphylum";
	public static String STR_BACTERIA_SUPERCLASS = "Superclass";
	public static String STR_BACTERIA_CLASS = "Class";
	public static String STR_BACTERIA_SUBCLASS = "Subclass";
	public static String STR_BACTERIA_INFRACLASS = "Infraclass";
	public static String STR_BACTERIA_SUPERORDER = "Superorder";
	public static String STR_BACTERIA_ORDER = "Order";
	public static String STR_BACTERIA_SUBORDER = "Suborder";
	public static String STR_BACTERIA_INFRAORDER = "Infraorder";
	public static String STR_BACTERIA_SUPERFAMILY = "Superfamily";
	public static String STR_BACTERIA_FAMILY = "Family";
	public static String STR_BACTERIA_SUBFAMILY = "Subfamily";
	public static String STR_BACTERIA_TRIBE = "Tribe";
	public static String STR_BACTERIA_SUBTRIBE = "Subtribe";
	public static String STR_BACTERIA_GENUS = "Genus";
	public static String STR_BACTERIA_SUBGENUS = "Subgenus";
	public static String STR_BACTERIA_SPECIES = "Species";
	public static String STR_BACTERIA_SUBSPECIES = "Subspecies";
	public static String STR_BACTERIA_VARIETY = "Variety";
	public static String STR_BACTERIA_FORMA = "Forma";
	
	// Chromista Ranks
	public static int Chromista_Kingdom = 10;
	public static int Chromista_Subkingdom = 20;
	public static int Chromista_Infrakingdom = 25;
	public static int Chromista_Phylum = 30;
	public static int Chromista_Subphylum = 40;
	public static int Chromista_Superclass = 50;
	public static int Chromista_Class = 60;
	public static int Chromista_Subclass = 70;
	public static int Chromista_Infraclass = 80;
	public static int Chromista_Superorder = 90;
	public static int Chromista_Order = 100;
	public static int Chromista_Suborder = 110;
	public static int Chromista_Infraorder = 120;
	public static int Chromista_Superfamily	= 130;
	public static int Chromista_Family = 140;
	public static int Chromista_Subfamily = 150;
	public static int Chromista_Tribe = 160;
	public static int Chromista_Subtribe = 170;
	public static int Chromista_Genus = 180;
	public static int Chromista_Subgenus = 190;
	public static int Chromista_Section = 200;
	public static int Chromista_Subsection = 210;
	public static int Chromista_Species	= 220;
	public static int Chromista_Subspecies = 230;
	public static int Chromista_Variety	= 240;
	public static int Chromista_Subvariety = 250;
	public static int Chromista_Forma = 260;
	
	// NoteCategory
	public static int NoteCategory_description = 1;
	public static int NoteCategory_ecology = 4;
	public static int NoteCategory_phenology	= 5;
	public static int NoteCategory_general_distribution_euromed = 10;
	public static int NoteCategory_general_distribution_world = 11;
	public static int NoteCategory_Common_names = 12;
	public static int NoteCategory_Occurrence = 13;
	public static int NoteCategory_Maps =14;
	public static int NoteCategory_Link_to_maps = 20;
	public static int NoteCategory_Link_to_images = 21;
	public static int NoteCategory_Link_to_taxonomy = 22;
	public static int NoteCategory_Link_to_general_information = 23;
	public static int NoteCategory_undefined_link = 24;
	public static int NoteCategory_Editor_Braces = 249;
	public static int NoteCategory_Editor_Brackets = 250;
	public static int NoteCategory_Editor_Parenthesis = 251;
	public static int NoteCategory_Inedited = 252;
	public static int NoteCategory_Comments_on_editing_process = 253;
	public static int NoteCategory_Publication_date = 254;
	public static int NoteCategory_Morphology = 255;
	public static int NoteCategory_Acknowledgments = 257;
	public static int NoteCategory_Original_publication = 258;
	public static int NoteCategory_Type_locality	= 259;
	public static int NoteCategory_Environment = 260;
	public static int NoteCategory_Spelling = 261;
	public static int NoteCategory_Systematics = 262;
	public static int NoteCategory_Remark = 263;
	public static int NoteCategory_Date_of_publication = 264;
	public static int NoteCategory_Additional_information = 266;
	public static int NoteCategory_Status = 267;
	public static int NoteCategory_Nomenclature = 268;
	public static int NoteCategory_Homonymy = 269;
	public static int NoteCategory_Taxonomy = 270;
	public static int NoteCategory_Taxonomic_status = 272;
	public static int NoteCategory_Authority	= 273;
	public static int NoteCategory_Identification = 274;
	public static int NoteCategory_Validity = 275;
	public static int NoteCategory_Classification = 276;
	public static int NoteCategory_Distribution = 278;
	public static int NoteCategory_Synonymy = 279;
	public static int NoteCategory_Habitat = 280;
	public static int NoteCategory_Biology = 281;
	public static int NoteCategory_Diagnosis	= 282;
	public static int NoteCategory_Host = 283;
	public static int NoteCategory_Note = 284;
	public static int NoteCategory_Rank = 285;
	public static int NoteCategory_Taxonomic_Remark = 286;
	public static int NoteCategory_Taxonomic_Remarks = 287;

	
	public static String NoteCategory_STR_description = "description";
	public static String NoteCategory_STR_ecology = "ecology";
	public static String NoteCategory_STR_phenology	= "phenology";
	public static String NoteCategory_STR_general_distribution_euromed = "general distribution (Euro+Med)";
	public static String NoteCategory_STR_general_distribution_world = "general distribution (world)";
	public static String NoteCategory_STR_Common_names = "Common names";
	public static String NoteCategory_STR_Occurrence = "Occurrence";
	public static String NoteCategory_STR_Maps = "Maps";
	public static String NoteCategory_STR_Link_to_maps = "Link to maps";
	public static String NoteCategory_STR_Link_to_images = "Link to images";
	public static String NoteCategory_STR_Link_to_taxonomy = "Link to taxonomy";
	public static String NoteCategory_STR_Link_to_general_information = "Link to general information";
	public static String NoteCategory_STR_undefined_link = "undefined link";
	public static String NoteCategory_STR_Editor_Braces = "Editor_Braces";
	public static String NoteCategory_STR_Editor_Brackets = "Editor_Brackets";
	public static String NoteCategory_STR_Editor_Parenthesis = "Editor_Parenthesis";
	public static String NoteCategory_STR_Inedited = "Inedited";
	public static String NoteCategory_STR_Comments_on_editing_process = "Comments on editing process";
	public static String NoteCategory_STR_Publication_date = "Publication date";
	public static String NoteCategory_STR_Morphology = "Morphology";
	public static String NoteCategory_STR_Acknowledgments = "Acknowledgments";
	public static String NoteCategory_STR_Original_publication = "Original publication";
	public static String NoteCategory_STR_Type_locality	= "Type locality";
	public static String NoteCategory_STR_Environment = "Environment";
	public static String NoteCategory_STR_Spelling = "Spelling";
	public static String NoteCategory_STR_Systematics = "Systematics";
	public static String NoteCategory_STR_Remark = "Remark";
	public static String NoteCategory_STR_Date_of_publication = "Date of publication";
	public static String NoteCategory_STR_Additional_information = "Additional information";
	public static String NoteCategory_STR_Status = "Status";
	public static String NoteCategory_STR_Nomenclature = "Nomenclature";
	public static String NoteCategory_STR_Homonymy = "Homonymy";
	public static String NoteCategory_STR_Taxonomy = "Taxonomy";
	public static String NoteCategory_STR_Taxonomic_status = "Taxonomic status";
	public static String NoteCategory_STR_Authority	= "Authority";
	public static String NoteCategory_STR_Identification = "Identification";
	public static String NoteCategory_STR_Validity = "Validity";
	public static String NoteCategory_STR_Classification = "Classification";
	public static String NoteCategory_STR_Distribution = "Distribution";
	public static String NoteCategory_STR_Synonymy = "Synonymy";
	public static String NoteCategory_STR_Habitat = "Habitat";
	public static String NoteCategory_STR_Biology = "Biology";
	public static String NoteCategory_STR_Diagnosis	= "Diagnosis";
	public static String NoteCategory_STR_Host = "Host";
	public static String NoteCategory_STR_Note = "Note";
	public static String NoteCategory_STR_Rank = "Rank";
	public static String NoteCategory_STR_Taxonomic_Remark = "Taxonomic Remark";
	public static String NoteCategory_STR_Taxonomic_Remarks = "Taxonomic Remarks";
	
	
	// Language
	public static int Language_Albanian = 1;
	public static int Language_Arabic = 2;
	public static int Language_Armenian = 3;
	public static int Language_Azerbaijan = 4;
	public static int Language_Belarusian = 5;
	public static int Language_Bulgarian = 6;
	public static int Language_Catalan = 7;
	public static int Language_Croat = 8;
	public static int Language_Czech = 9;
	public static int Language_Danish = 10;
	public static int Language_Dutch = 11;
	public static int Language_English = 12;
	public static int Language_Euskera = 13;
	public static int Language_Estonian = 14;
	public static int Language_Finnish = 15;
	public static int Language_French = 16;
	public static int Language_Georgian = 17;
	public static int Language_German = 18;
	public static int Language_Greek = 19;
	public static int Language_Hungarian = 20;
	public static int Language_Icelandic = 21;
	public static int Language_Irish_Gaelic = 22;
	public static int Language_Israel_Hebrew = 23;
	public static int Language_Italian = 24;
	public static int Language_Latvian = 25;
	public static int Language_Lithuanian = 26;
	public static int Language_Macedonian = 27;
	public static int Language_Maltese = 28;
	public static int Language_Moldovian = 29;
	public static int Language_Norwegian = 30;
	public static int Language_Polish = 31;
	public static int Language_Portuguese = 32;
	public static int Language_Roumanian = 33;
	public static int Language_Russian = 34;
	public static int Language_Russian_Caucasian = 35;
	public static int Language_Russian_Altaic_kalmyk_oirat = 36;
	public static int Language_Russian_Altaic_karachay_balkar = 37;
	public static int Language_Russian_Altaic_kumyk = 38;
	public static int Language_Russian_Altaic_nogai = 39;
	public static int Language_Russian_Altaic_north_azerbaijani = 40;
	public static int Language_Russian_Indo_european_russian = 41;
	public static int Language_Russian_Indo_european_kalmyk_oirat = 42;
	public static int Language_Russian_Indo_european_osetin = 43;
	public static int Language_Russian_North_caucasian_abaza = 44;
	public static int Language_Russian_North_caucasian_adyghe = 45;
	public static int Language_Russian_North_caucasian_chechen = 46;
	public static int Language_Russian_North_caucasian_kabardian = 47;
	public static int Language_Russian_North_caucasian_lak = 48;
	public static int Language_Russian_North_caucasian_avar = 49;
	public static int Language_Russian_North_caucasian_in = 50;
	public static int Language_Russian_Uralic_chuvash = 51;
	public static int Language_Russian_Uralic_udmurt = 52;
	public static int Language_Serbian = 53;
	public static int Language_Slovak = 54;
	public static int Language_Slovene = 55;
	public static int Language_Spanish_Castillian = 56;
	public static int Language_Swedish = 57;
	public static int Language_Turkish = 58;
	public static int Language_Ukraine = 59;
	public static int Language_Welsh = 60;
	public static int Language_Corsican = 61;

	public static String STR_LANGUAGE_ALBANIAN = "Albanian";
	public static String STR_LANGUAGE_ARABIC = "Arabic";
	public static String STR_LANGUAGE_ARMENIAN = "Armenian";
	public static String STR_LANGUAGE_AZERBAIJAN = "Azerbaijan";
	public static String STR_LANGUAGE_BELARUSIAN = "Belarusian";
	public static String STR_LANGUAGE_BULGARIAN = "Bulgarian";
	public static String STR_LANGUAGE_CATALAN = "Catalan";
	public static String STR_LANGUAGE_CROAT = "Croat";
	public static String STR_LANGUAGE_CZECH = "Czech";
	public static String STR_LANGUAGE_DANISH = "Danish";
	public static String STR_LANGUAGE_DUTCH = "Dutch";
	public static String STR_LANGUAGE_ENGLISH = "English";
	public static String STR_LANGUAGE_EUSKERA = "Euskera";
	public static String STR_LANGUAGE_ESTONIAN = "Estonian";
	public static String STR_LANGUAGE_FINNISH = "Finnish";
	public static String STR_LANGUAGE_FRENCH = "French";
	public static String STR_LANGUAGE_GEORGIAN = "Georgian";
	public static String STR_LANGUAGE_GERMAN = "German";
	public static String STR_LANGUAGE_GREEK = "Greek";
	public static String STR_LANGUAGE_HUNGARIAN = "Hungarian";
	public static String STR_LANGUAGE_ICELANDIC = "Icelandic";
	public static String STR_LANGUAGE_IRISH_GAELIC = "Irish Gaelic";
	public static String STR_LANGUAGE_ISRAEL_HEBREW = "Israel (Hebrew)";
	public static String STR_LANGUAGE_ITALIAN = "Italian";
	public static String STR_LANGUAGE_LATVIAN = "Latvian";
	public static String STR_LANGUAGE_LITHUANIAN = "Lithuanian";
	public static String STR_LANGUAGE_MACEDONIAN = "Macedonian";
	public static String STR_LANGUAGE_MALTESE = "Maltese";
	public static String STR_LANGUAGE_MOLDOVIAN = "Moldovian";
	public static String STR_LANGUAGE_NORWEGIAN = "Norwegian";
	public static String STR_LANGUAGE_POLISH = "Polish";
	public static String STR_LANGUAGE_PORTUGUESE = "Portuguese";
	public static String STR_LANGUAGE_ROUMANIAN = "Roumanian";
	public static String STR_LANGUAGE_RUSSIAN = "Russian";
	public static String STR_LANGUAGE_RUSSIAN_CAUCASIAN = "Russian Caucasian";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_KALMYK_OIRAT = "Russian (Altaic, kalmyk-oirat)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_KARACHAY_BALKAR = "Russian (Altaic, karachay-balkar)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_KUMYK = "Russian (Altaic, kumyk)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_NOGAI = "Russian (Altaic, nogai)";
	public static String STR_LANGUAGE_RUSSIAN_ALTAIC_NORTH_AZERBAIJANI = "Russian (Altaic, north azerbaijani)";
	public static String STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_RUSSIAN = "Russian (Indo-european, russian)";
	public static String STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_KALMYK_OIRAT = "Russian (Indo-european, kalmyk-oirat)";
	public static String STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_OSETIN = "Russian (Indo-european, osetin)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ABAZA = "Russian (North caucasian, abaza)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ADYGHE = "Russian (North caucasian, adyghe)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_CHECHEN = "Russian (North caucasian, chechen)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_KABARDIAN = "Russian (North caucasian, kabardian)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_LAK = "Russian (North caucasian, lak)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_AVAR = "Russian (North caucasian, avar)";
	public static String STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_IN = "Russian (North caucasian, in)";
	public static String STR_LANGUAGE_RUSSIAN_URALIC_CHUVASH = "Russian (Uralic, chuvash)";
	public static String STR_LANGUAGE_RUSSIAN_URALIC_UDMURT = "Russian (Uralic, udmurt)";
	public static String STR_LANGUAGE_SERBIAN = "Serbian";
	public static String STR_LANGUAGE_SLOVAK = "Slovak";
	public static String STR_LANGUAGE_SLOVENE = "Slovene";
	public static String STR_LANGUAGE_SPANISH_CASTILLIAN = "Spanish, Castillian";
	public static String STR_LANGUAGE_SWEDISH = "Swedish";
	public static String STR_LANGUAGE_TURKISH = "Turkish";
	public static String STR_LANGUAGE_UKRAINE = "Ukraine";
	public static String STR_LANGUAGE_WELSH = "Welsh";
	public static String STR_LANGUAGE_CORSICAN = "Corsican";

	
	// FossilStatus
	public static int FOSSILSTATUS_RECENT_ONLY = 1;
	public static int FOSSILSTATUS_FOSSIL_ONLY = 2;
	public static int FOSSILSTATUS_RECENT_FOSSIL = 3;
	
	public static String STR_FOSSILSTATUS_RECENT_ONLY = "recent only";
	public static String STR_FOSSILSTATUS_FOSSIL_ONLY = "fossil only";
	public static String STR_FOSSILSTATUS_RECENT_FOSSIL = "recent + fossil";

	// SourceUse
	public static int ORIGINAL_DESCRIPTION = 1;
	public static int BASIS_OF_RECORD = 2;
	public static int ADDITIONAL_SOURCE = 3;
	public static int SOURCE_OF_SYNONYMY = 4;
	public static int REDESCRIPTION = 5;
	public static int NEW_COMBINATION_REFERENCE = 6;
	public static int STATUS_SOURCE = 7;
	public static int NOMENCLATURAL_REFERENCE = 8;
	
	public static String STR_ORIGINAL_DESCRIPTION = "original description";
	public static String STR_BASIS_OF_RECORD = "basis of record";
	public static String STR_ADDITIONAL_SOURCE = "additional source";
	public static String STR_SOURCE_OF_SYNONYMY = "source of synonymy";
	public static String STR_REDESCRIPTION = "redescription";
	public static String STR_NEW_COMBINATION_REFERENCE = "new combination reference";
	public static String STR_STATUS_SOURCE = "status source";
	public static String STR_NOMENCLATURAL_REFERENCE = "nomenclatural reference";

	// Erms Area
	public static int ERMS_EUROPEAN_MARINE_WATERS = 7788;
	public static int ERMS_MEDITERRANEAN_SEA = 7789;
	public static int ERMS_WHITE_SEA = 7791;
	public static int ERMS_NORTH_SEA = 7792;
	public static int ERMS_BALTIC_SEA = 7793;
	public static int ERMS_BLACK_SEA = 7794;
	public static int ERMS_BARENTS_SEA = 7795;
	public static int ERMS_CASPIAN_SEA = 7796;
	public static int ERMS_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = 7799;
	public static int ERMS_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = 7802;
	public static int ERMS_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = 7805;
	public static int ERMS_ENGLISH_CHANNEL = 7818;
	public static int ERMS_ADRIATIC_SEA = 7821;
	public static int ERMS_BISCAY_BAY = 7831;
	public static int ERMS_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = 7839;
	public static int ERMS_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = 7862;
	public static int ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = 7869;
	public static int ERMS_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = 7902;
	public static int ERMS_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = 7939;
	public static int ERMS_TIRRENO_SEA = 7946;
	public static int ERMS_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = 7964;
	public static int ERMS_IRISH_EXCLUSIVE_ECONOMIC_ZONE = 7974;
	public static int ERMS_IRISH_SEA = 7975;
	public static int ERMS_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = 7978;
	public static int ERMS_NORWEGIAN_SEA = 7980;
	public static int ERMS_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = 8027;
	public static int ERMS_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = 8050;
	public static int ERMS_SKAGERRAK = 8072;
	public static int ERMS_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = 8143;
	public static int ERMS_WADDEN_SEA = 8155;
	public static int ERMS_BELT_SEA = 8203;
	public static int ERMS_MARMARA_SEA = 8205;
	public static int ERMS_SEA_OF_AZOV = 8837;
	public static int ERMS_AEGEAN_SEA = 9146;
	public static int ERMS_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = 9178;
	public static int ERMS_SOUTH_BALTIC_PROPER = 9903;
	public static int ERMS_BALTIC_PROPER = 9904;
	public static int ERMS_NORTH_BALTIC_PROPER = 9905;
	public static int ERMS_ARCHIPELAGO_SEA = 9908;
	public static int ERMS_BOTHNIAN_SEA = 9909;
	public static int ERMS_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = 10515;
	public static int ERMS_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = 10528;
	public static int ERMS_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = 10529;
	public static int ERMS_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = 10564;
	public static int ERMS_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = 10574;
	public static int ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = 10659;
	public static int ERMS_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = 10708;
	public static int ERMS_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = 10778;
	public static int ERMS_BALEAR_SEA = 10779;
	public static int ERMS_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = 10782;
	public static int ERMS_DANISH_EXCLUSIVE_ECONOMIC_ZONE = 11039;

	public static String STR_ERMS_EUROPEAN_MARINE_WATERS = "European Marine Waters";
	public static String STR_ERMS_MEDITERRANEAN_SEA = "Mediterranean Sea";
	public static String STR_ERMS_WHITE_SEA = "White Sea";
	public static String STR_ERMS_NORTH_SEA = "North Sea";
	public static String STR_ERMS_BALTIC_SEA = "Baltic Sea";
	public static String STR_ERMS_BLACK_SEA = "Black Sea";
	public static String STR_ERMS_BARENTS_SEA = "Barents Sea";
	public static String STR_ERMS_CASPIAN_SEA = "Caspian Sea";
	public static String STR_ERMS_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = "Portuguese Exclusive Economic Zone";
	public static String STR_ERMS_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Belgian Exclusive Economic Zone";
	public static String STR_ERMS_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = "French Exclusive Economic Zone";
	public static String STR_ERMS_ENGLISH_CHANNEL = "English Channel";
	public static String STR_ERMS_ADRIATIC_SEA = "Adriatic Sea";
	public static String STR_ERMS_BISCAY_BAY = "Biscay Bay";
	public static String STR_ERMS_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = "Dutch Exclusive Economic Zone";
	public static String STR_ERMS_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = "United Kingdom Exclusive Economic Zone";
	public static String STR_ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = "Spanish Exclusive Economic Zone";
	public static String STR_ERMS_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = "Egyptian Exclusive Economic Zone";
	public static String STR_ERMS_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = "Grecian Exclusive Economic Zone";
	public static String STR_ERMS_TIRRENO_SEA = "Tirreno Sea";
	public static String STR_ERMS_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = "Icelandic Exclusive Economic Zone";
	public static String STR_ERMS_IRISH_EXCLUSIVE_ECONOMIC_ZONE = "Irish Exclusive economic Zone";
	public static String STR_ERMS_IRISH_SEA = "Irish Sea";
	public static String STR_ERMS_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = "Italian Exclusive Economic Zone";
	public static String STR_ERMS_NORWEGIAN_SEA = "Norwegian Sea";
	public static String STR_ERMS_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = "Moroccan Exclusive Economic Zone";
	public static String STR_ERMS_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Norwegian Exclusive Economic Zone";
	public static String STR_ERMS_SKAGERRAK = "Skagerrak";
	public static String STR_ERMS_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = "Tunisian Exclusive Economic Zone";
	public static String STR_ERMS_WADDEN_SEA = "Wadden Sea";
	public static String STR_ERMS_BELT_SEA = "Belt Sea";
	public static String STR_ERMS_MARMARA_SEA = "Marmara Sea";
	public static String STR_ERMS_SEA_OF_AZOV = "Sea of Azov";
	public static String STR_ERMS_AEGEAN_SEA = "Aegean Sea";
	public static String STR_ERMS_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = "Bulgarian Exclusive Economic Zone";
	public static String STR_ERMS_SOUTH_BALTIC_PROPER = "South Baltic proper";
	public static String STR_ERMS_BALTIC_PROPER = "Baltic Proper";
	public static String STR_ERMS_NORTH_BALTIC_PROPER = "North Baltic proper";
	public static String STR_ERMS_ARCHIPELAGO_SEA = "Archipelago Sea";
	public static String STR_ERMS_BOTHNIAN_SEA = "Bothnian Sea";
	public static String STR_ERMS_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = "German Exclusive Economic Zone";
	public static String STR_ERMS_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = "Swedish Exclusive Economic Zone";
	public static String STR_ERMS_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = "Ukrainian Exclusive Economic Zone";
	public static String STR_ERMS_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = "Madeiran Exclusive Economic Zone";
	public static String STR_ERMS_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = "Lebanese Exclusive Economic Zone";
	public static String STR_ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = "Spanish Exclusive Economic Zone [Mediterranean part]";
	public static String STR_ERMS_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = "Estonian Exclusive Economic Zone";
	public static String STR_ERMS_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = "Croatian Exclusive Economic Zone";
	public static String STR_ERMS_BALEAR_SEA = "Balear Sea";
	public static String STR_ERMS_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = "Turkish Exclusive Economic Zone";
	public static String STR_ERMS_DANISH_EXCLUSIVE_ECONOMIC_ZONE = "Danish Exclusive Economic Zone";

	
	// PESI Area
	public static int PESI_EAST_AEGEAN_ISLANDS = 1;
	public static int PESI_GREEK_EAST_AEGEAN_ISLANDS = 2;
	public static int PESI_TURKISH_EAST_AEGEAN_ISLANDS = 3;
	public static int PESI_AUSTRIA_WITH_LIECHTENSTEIN = 5;
	public static int PESI_AUSTRIA = 6;
	public static int PESI_LIECHTENSTEIN = 7;
	public static int PESI_AZORES = 8;
	public static int PESI_CORVO = 9;
	public static int PESI_FAIAL = 10;
	public static int PESI_GRACIOSA = 11;
	public static int PESI_S�O_JORGE = 12;
	public static int PESI_FLORES = 13;
	public static int PESI_S�O_MIGUEL = 14;
	public static int PESI_PICO = 15;
	public static int PESI_SANTA_MARIA = 16;
	public static int PESI_TERCEIRA = 17;
	public static int PESI_BELGIUM_WITH_LUXEMBOURG = 18;
	public static int PESI_BELGIUM = 19;
	public static int PESI_LUXEMBOURG = 20;
	public static int PESI_BOSNIA_HERZEGOVINA = 21;
	public static int PESI_BALEARES = 22;
	public static int PESI_IBIZA_WITH_FORMENTERA = 23;
	public static int PESI_MALLORCA = 24;
	public static int PESI_MENORCA = 25;
	public static int PESI_GREAT_BRITAIN = 26;
	public static int PESI_BALTIC_STATES_ESTONIA_LATVIA_LITHUANIA_AND_KALININGRAD_REGION = 27;
	public static int PESI_BULGARIA = 28;
	public static int PESI_BELARUS = 29;
	public static int PESI_CANARY_ISLANDS = 30;
	public static int PESI_GRAN_CANARIA = 31;
	public static int PESI_FUERTEVENTURA_WITH_LOBOS = 32;
	public static int PESI_GOMERA = 33;
	public static int PESI_HIERRO = 34;
	public static int PESI_LANZAROTE_WITH_GRACIOSA = 35;
	public static int PESI_LA_PALMA = 36;
	public static int PESI_TENERIFE = 37;
	public static int PESI_MONTENEGRO = 38;
	public static int PESI_CORSE = 39;
	public static int PESI_CRETE_WITH_KARPATHOS,_KASOS_AND_GAVDHOS = 40;
	public static int PESI_CZECH_REPUBLIC = 41;
	public static int PESI_CROATIA = 42;
	public static int PESI_CYPRUS = 43;
	public static int PESI_FORMER_CZECHOSLOVAKIA = 44;
	public static int PESI_DENMARK_WITH_BORNHOLM = 45;
	public static int PESI_ESTONIA = 46;
	public static int PESI_FAROE_ISLANDS = 47;
	public static int PESI_FINLAND_WITH_AHVENANMAA = 48;
	public static int PESI_FRANCE = 49;
	public static int PESI_CHANNEL_ISLANDS = 50;
	public static int PESI_FRENCH_MAINLAND = 51;
	public static int PESI_MONACO = 52;
	public static int PESI_GERMANY = 53;
	public static int PESI_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS = 54;
	public static int PESI_IRELAND = 55;
	public static int PESI_REPUBLIC_OF_IRELAND = 56;
	public static int PESI_NORTHERN_IRELAND = 57;
	public static int PESI_SWITZERLAND = 58;
	public static int PESI_NETHERLANDS = 59;
	public static int PESI_SPAIN = 60;
	public static int PESI_ANDORRA = 61;
	public static int PESI_GIBRALTAR = 62;
	public static int PESI_KINGDOM_OF_SPAIN = 63;
	public static int PESI_HUNGARY = 64;
	public static int PESI_ICELAND = 65;
	public static int PESI_ITALY = 66;
	public static int PESI_ITALIAN_MAINLAND = 67;
	public static int PESI_SAN_MARINO = 68;
	public static int PESI_FORMER_JUGOSLAVIA = 69;
	public static int PESI_LATVIA = 70;
	public static int PESI_LITHUANIA = 71;
	public static int PESI_PORTUGUESE_MAINLAND = 72;
	public static int PESI_MADEIRA_ARCHIPELAGO = 73;
	public static int PESI_DESERTAS = 74;
	public static int PESI_MADEIRA = 75;
	public static int PESI_PORTO_SANTO = 76;
	public static int PESI_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA = 77;
	public static int PESI_MOLDOVA = 78;
	public static int PESI_NORWEGIAN_MAINLAND = 79;
	public static int PESI_POLAND = 80;
	public static int PESI_THE_RUSSIAN_FEDERATION = 81;
	public static int PESI_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND = 82;
	public static int PESI_CENTRAL_EUROPEAN_RUSSIA = 83;
	public static int PESI_EASTERN_EUROPEAN_RUSSIA = 84;
	public static int PESI_KALININGRAD = 85;
	public static int PESI_NORTHERN_EUROPEAN_RUSSIA = 86;
	public static int PESI_NORTHWEST_EUROPEAN_RUSSIA = 87;
	public static int PESI_SOUTH_EUROPEAN_RUSSIA = 88;
	public static int PESI_ROMANIA = 89;
	public static int PESI_FORMER_USSR = 90;
	public static int PESI_RUSSIA_BALTIC = 91;
	public static int PESI_RUSSIA_CENTRAL = 92;
	public static int PESI_RUSSIA_SOUTHEAST = 93;
	public static int PESI_RUSSIA_NORTHERN = 94;
	public static int PESI_RUSSIA_SOUTHWEST = 95;
	public static int PESI_SARDEGNA = 96;
	public static int PESI_SVALBARD_WITH_BJORNOYA_AND_JAN_MAYEN = 97;
	public static int PESI_SELVAGENS_ISLANDS = 98;
	public static int PESI_SICILY_WITH_MALTA = 99;
	public static int PESI_MALTA = 100;
	public static int PESI_SICILY = 101;
	public static int PESI_SLOVAKIA = 102;
	public static int PESI_SLOVENIA = 103;
	public static int PESI_SERBIA_WITH_MONTENEGRO = 104;
	public static int PESI_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO = 105;
	public static int PESI_SWEDEN = 106;
	public static int PESI_EUROPEAN_TURKEY = 107;
	public static int PESI_UKRAINE_INCLUDING_CRIMEA = 108;
	public static int PESI_CRIMEA = 109;
	public static int PESI_UKRAINE = 110;
	public static int PESI_GREEK_MAINLAND = 111;
	public static int PESI_CRETE = 112;
	public static int PESI_DODECANESE_ISLANDS = 113;
	public static int PESI_CYCLADES_ISLANDS = 114;
	public static int PESI_NORTH_AEGEAN_ISLANDS = 115;
	public static int PESI_VATICAN_CITY = 116;
	public static int PESI_FRANZ_JOSEF_LAND = 117;
	public static int PESI_NOVAYA_ZEMLYA = 118;
	public static int PESI_AZERBAIJAN_INCLUDING_NAKHICHEVAN = 119;
	public static int PESI_AZERBAIJAN = 120;
	public static int PESI_NAKHICHEVAN = 121;
	public static int PESI_ALGERIA = 122;
	public static int PESI_ARMENIA = 123;
	public static int PESI_CAUCASUS_REGION = 124;
	public static int PESI_EGYPT = 125;
	public static int PESI_GEORGIA = 126;
	public static int PESI_ISRAEL_JORDAN = 127;
	public static int PESI_ISRAEL = 128;
	public static int PESI_JORDAN = 129;
	public static int PESI_LEBANON = 130;
	public static int PESI_LIBYA = 131;
	public static int PESI_LEBANON_SYRIA = 132;
	public static int PESI_MOROCCO = 133;
	public static int PESI_NORTH_CAUCASUS = 134;
	public static int PESI_SINAI = 135;
	public static int PESI_SYRIA = 136;
	public static int PESI_TUNISIA = 137;
	public static int PESI_ASIATIC_TURKEY = 138;
	public static int PESI_TURKEY = 139;
	public static int PESI_NORTHERN_AFRICA = 140;
	public static int PESI_AFRO_TROPICAL_REGION = 141;
	public static int PESI_AUSTRALIAN_REGION = 142;
	public static int PESI_EAST_PALAEARCTIC = 143;
	public static int PESI_NEARCTIC_REGION = 144;
	public static int PESI_NEOTROPICAL_REGION = 145;
	public static int PESI_NEAR_EAST = 146;
	public static int PESI_ORIENTAL_REGION = 147;
	public static int PESI_EUROPEAN_MARINE_WATERS = 148;
	public static int PESI_MEDITERRANEAN_SEA = 149;
	public static int PESI_WHITE_SEA = 150;
	public static int PESI_NORTH_SEA = 151;
	public static int PESI_BALTIC_SEA = 152;
	public static int PESI_BLACK_SEA = 153;
	public static int PESI_BARENTS_SEA = 154;
	public static int PESI_CASPIAN_SEA = 155;
	public static int PESI_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = 156;
	public static int PESI_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = 157;
	public static int PESI_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = 158;
	public static int PESI_ENGLISH_CHANNEL = 159;
	public static int PESI_ADRIATIC_SEA = 160;
	public static int PESI_BISCAY_BAY = 161;
	public static int PESI_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = 162;
	public static int PESI_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = 163;
	public static int PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = 164;
	public static int PESI_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = 165;
	public static int PESI_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = 166;
	public static int PESI_TIRRENO_SEA = 167;
	public static int PESI_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = 168;
	public static int PESI_IRISH_EXCLUSIVE_ECONOMIC_ZONE = 169;
	public static int PESI_IRISH_SEA = 170;
	public static int PESI_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = 171;
	public static int PESI_NORWEGIAN_SEA = 172;
	public static int PESI_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = 173;
	public static int PESI_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = 174;
	public static int PESI_SKAGERRAK = 175;
	public static int PESI_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = 176;
	public static int PESI_WADDEN_SEA = 177;
	public static int PESI_BELT_SEA = 178;
	public static int PESI_MARMARA_SEA = 179;
	public static int PESI_SEA_OF_AZOV = 180;
	public static int PESI_AEGEAN_SEA = 181;
	public static int PESI_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = 182;
	public static int PESI_SOUTH_BALTIC_PROPER = 183;
	public static int PESI_BALTIC_PROPER = 184;
	public static int PESI_NORTH_BALTIC_PROPER = 185;
	public static int PESI_ARCHIPELAGO_SEA = 186;
	public static int PESI_BOTHNIAN_SEA = 187;
	public static int PESI_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = 188;
	public static int PESI_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = 189;
	public static int PESI_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = 190;
	public static int PESI_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = 191;
	public static int PESI_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = 192;
	public static int PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = 193;
	public static int PESI_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = 194;
	public static int PESI_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = 195;
	public static int PESI_BALEAR_SEA = 196;
	public static int PESI_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = 197;
	public static int PESI_DANISH_EXCLUSIVE_ECONOMIC_ZONE = 198;

	public static String STR_PESI_EAST_AEGEAN_ISLANDS = "East Aegean Islands";
	public static String STR_PESI_GREEK_EAST_AEGEAN_ISLANDS = "Greek East Aegean Islands";
	public static String STR_PESI_TURKISH_EAST_AEGEAN_ISLANDS = "Turkish East Aegean Islands";
	public static String STR_PESI_ALBANIA = "Albania";
	public static String STR_PESI_AUSTRIA_WITH_LIECHTENSTEIN = "Austria with Liechtenstein";
	public static String STR_PESI_AUSTRIA = "Austria";
	public static String STR_PESI_LIECHTENSTEIN = "Liechtenstein";
	public static String STR_PESI_AZORES = "Azores";
	public static String STR_PESI_CORVO = "Corvo";
	public static String STR_PESI_FAIAL = "Faial";
	public static String STR_PESI_GRACIOSA = "Graciosa";
	public static String STR_PESI_S�O_JORGE = "S�o Jorge";
	public static String STR_PESI_FLORES = "Flores";
	public static String STR_PESI_S�O_MIGUEL = "S�o Miguel";
	public static String STR_PESI_PICO = "Pico";
	public static String STR_PESI_SANTA_MARIA = "Santa Maria";
	public static String STR_PESI_TERCEIRA = "Terceira";
	public static String STR_PESI_BELGIUM_WITH_LUXEMBOURG = "Belgium with Luxembourg";
	public static String STR_PESI_BELGIUM = "Belgium";
	public static String STR_PESI_LUXEMBOURG = "Luxembourg";
	public static String STR_PESI_BOSNIA_HERZEGOVINA = "Bosnia-Herzegovina";
	public static String STR_PESI_BALEARES = "Baleares";
	public static String STR_PESI_IBIZA_WITH_FORMENTERA = "Ibiza with Formentera";
	public static String STR_PESI_MALLORCA = "Mallorca";
	public static String STR_PESI_MENORCA = "Menorca";
	public static String STR_PESI_GREAT_BRITAIN = "Great Britain";
	public static String STR_PESI_BALTIC_STATES_ESTONIA_LATVIA_LITHUANIA_AND_KALININGRAD_REGION = "Baltic states (Estonia, Latvia, Lithuania) and Kaliningrad region";
	public static String STR_PESI_BULGARIA = "Bulgaria";
	public static String STR_PESI_BELARUS = "Belarus";
	public static String STR_PESI_CANARY_ISLANDS = "Canary Islands";
	public static String STR_PESI_GRAN_CANARIA = "Gran Canaria";
	public static String STR_PESI_FUERTEVENTURA_WITH_LOBOS = "Fuerteventura with Lobos";
	public static String STR_PESI_GOMERA = "Gomera";
	public static String STR_PESI_HIERRO = "Hierro";
	public static String STR_PESI_LANZAROTE_WITH_GRACIOSA = "Lanzarote with Graciosa";
	public static String STR_PESI_LA_PALMA = "La Palma";
	public static String STR_PESI_TENERIFE = "Tenerife";
	public static String STR_PESI_MONTENEGRO = "Montenegro";
	public static String STR_PESI_CORSE = "Corse";
	public static String STR_PESI_CRETE_WITH_KARPATHOS_KASOS_AND_GAVDHOS = "Crete with Karpathos, Kasos & Gavdhos";
	public static String STR_PESI_CZECH_REPUBLIC = "Czech Republic";
	public static String STR_PESI_CROATIA = "Croatia";
	public static String STR_PESI_CYPRUS = "Cyprus";
	public static String STR_PESI_FORMER_CZECHOSLOVAKIA = "Former Czechoslovakia";
	public static String STR_PESI_DENMARK_WITH_BORNHOLM = "Denmark with Bornholm";
	public static String STR_PESI_ESTONIA = "Estonia";
	public static String STR_PESI_FAROE_ISLANDS = "Faroe Islands";
	public static String STR_PESI_FINLAND_WITH_AHVENANMAA = "Finland with Ahvenanmaa";
	public static String STR_PESI_FRANCE = "France";
	public static String STR_PESI_CHANNEL_ISLANDS = "Channel Islands";
	public static String STR_PESI_FRENCH_MAINLAND = "French mainland";
	public static String STR_PESI_MONACO = "Monaco";
	public static String STR_PESI_GERMANY = "Germany";
	public static String STR_PESI_GREECE_WITH_CYCLADES_AND_MORE_ISLANDS = "Greece with Cyclades and more islands";
	public static String STR_PESI_IRELAND = "Ireland";
	public static String STR_PESI_REPUBLIC_OF_IRELAND = "Republic of Ireland";
	public static String STR_PESI_NORTHERN_IRELAND = "Northern Ireland";
	public static String STR_PESI_SWITZERLAND = "Switzerland";
	public static String STR_PESI_NETHERLANDS = "Netherlands";
	public static String STR_PESI_SPAIN = "Spain";
	public static String STR_PESI_ANDORRA = "Andorra";
	public static String STR_PESI_GIBRALTAR = "Gibraltar";
	public static String STR_PESI_KINGDOM_OF_SPAIN = "Kingdom of Spain";
	public static String STR_PESI_HUNGARY = "Hungary";
	public static String STR_PESI_ICELAND = "Iceland";
	public static String STR_PESI_ITALY = "Italy";
	public static String STR_PESI_ITALIAN_MAINLAND = "Italian mainland";
	public static String STR_PESI_SAN_MARINO = "San Marino";
	public static String STR_PESI_FORMER_JUGOSLAVIA = "Former Jugoslavia";
	public static String STR_PESI_LATVIA = "Latvia";
	public static String STR_PESI_LITHUANIA = "Lithuania";
	public static String STR_PESI_PORTUGUESE_MAINLAND = "Portuguese mainland";
	public static String STR_PESI_MADEIRA_ARCHIPELAGO = "Madeira";
	public static String STR_PESI_DESERTAS = "Desertas";
	public static String STR_PESI_MADEIRA = "Madeira";
	public static String STR_PESI_PORTO_SANTO = "Porto Santo";
	public static String STR_PESI_THE_FORMER_JUGOSLAV_REPUBLIC_OF_MAKEDONIJA = "The former Jugoslav Republic of Makedonija";
	public static String STR_PESI_MOLDOVA = "Moldova";
	public static String STR_PESI_NORWEGIAN_MAINLAND = "Norwegian mainland";
	public static String STR_PESI_POLAND = "Poland";
	public static String STR_PESI_THE_RUSSIAN_FEDERATION = "The Russian Federation";
	public static String STR_PESI_NOVAYA_ZEMLYA_AND_FRANZ_JOSEPH_LAND = "Novaya Zemlya & Franz-Joseph Land";
	public static String STR_PESI_CENTRAL_EUROPEAN_RUSSIA = "Central European Russia";
	public static String STR_PESI_EASTERN_EUROPEAN_RUSSIA = "Eastern European Russia";
	public static String STR_PESI_KALININGRAD = "Kaliningrad";
	public static String STR_PESI_NORTHERN_EUROPEAN_RUSSIA = "Northern European Russia";
	public static String STR_PESI_NORTHWEST_EUROPEAN_RUSSIA = "Northwest European Russia";
	public static String STR_PESI_SOUTH_EUROPEAN_RUSSIA = "South European Russia";
	public static String STR_PESI_ROMANIA = "Romania";
	public static String STR_PESI_FORMER_USSR = "Former USSR";
	public static String STR_PESI_RUSSIA_BALTIC = "Russia Baltic";
	public static String STR_PESI_RUSSIA_CENTRAL = "Russia Central";
	public static String STR_PESI_RUSSIA_SOUTHEAST = "Russia Southeast";
	public static String STR_PESI_RUSSIA_NORTHERN = "Russia Northern";
	public static String STR_PESI_RUSSIA_SOUTHWEST = "Russia Southwest";
	public static String STR_PESI_SARDEGNA = "Sardegna";
	public static String STR_PESI_SVALBARD_WITH_BJ�RN�YA_AND_JAN_MAYEN = "Svalbard with Bj�rn�ya and Jan Mayen";
	public static String STR_PESI_SELVAGENS_ISLANDS = "Selvagens Islands";
	public static String STR_PESI_SICILY_WITH_MALTA = "Sicily with Malta";
	public static String STR_PESI_MALTA = "Malta";
	public static String STR_PESI_SICILY = "Sicily";
	public static String STR_PESI_SLOVAKIA = "Slovakia";
	public static String STR_PESI_SLOVENIA = "Slovenia";
	public static String STR_PESI_SERBIA_WITH_MONTENEGRO = "Serbia with Montenegro";
	public static String STR_PESI_SERBIA_INCLUDING_VOJVODINA_AND_WITH_KOSOVO = "Serbia including Vojvodina and with Kosovo";
	public static String STR_PESI_SWEDEN = "Sweden";
	public static String STR_PESI_EUROPEAN_TURKEY = "European Turkey";
	public static String STR_PESI_UKRAINE_INCLUDING_CRIMEA = "Ukraine including Crimea";
	public static String STR_PESI_CRIMEA = "Crimea";
	public static String STR_PESI_UKRAINE = "Ukraine";
	public static String STR_PESI_GREEK_MAINLAND = "Greek mainland";
	public static String STR_PESI_CRETE = "Crete";
	public static String STR_PESI_DODECANESE_ISLANDS = "Dodecanese Islands";
	public static String STR_PESI_CYCLADES_ISLANDS = "Cyclades Islands";
	public static String STR_PESI_NORTH_AEGEAN_ISLANDS = "North Aegean Islands";
	public static String STR_PESI_VATICAN_CITY = "Vatican City";
	public static String STR_PESI_FRANZ_JOSEF_LAND = "Franz Josef Land";
	public static String STR_PESI_NOVAYA_ZEMLYA = "Novaya Zemlya";
	public static String STR_PESI_AZERBAIJAN_INCLUDING_NAKHICHEVAN = "Azerbaijan including Nakhichevan";
	public static String STR_PESI_AZERBAIJAN = "Azerbaijan";
	public static String STR_PESI_NAKHICHEVAN = "Nakhichevan";
	public static String STR_PESI_ALGERIA = "Algeria";
	public static String STR_PESI_ARMENIA = "Armenia";
	public static String STR_PESI_CAUCASUS_REGION = "Caucasus region";
	public static String STR_PESI_EGYPT = "Egypt";
	public static String STR_PESI_GEORGIA = "Georgia";
	public static String STR_PESI_ISRAEL_JORDAN = "Israel-Jordan";
	public static String STR_PESI_ISRAEL = "Israel";
	public static String STR_PESI_JORDAN = "Jordan";
	public static String STR_PESI_LEBANON = "Lebanon";
	public static String STR_PESI_LIBYA = "Libya";
	public static String STR_PESI_LEBANON_SYRIA = "Lebanon-Syria";
	public static String STR_PESI_MOROCCO = "Morocco";
	public static String STR_PESI_NORTH_CAUCASUS = "North Caucasus";
	public static String STR_PESI_SINAI = "Sinai";
	public static String STR_PESI_SYRIA = "Syria";
	public static String STR_PESI_TUNISIA = "Tunisia";
	public static String STR_PESI_ASIATIC_TURKEY = "Asiatic Turkey";
	public static String STR_PESI_TURKEY = "Turkey";
	public static String STR_PESI_NORTHERN_AFRICA = "Northern Africa";
	public static String STR_PESI_AFRO_TROPICAL_REGION = "Afro-tropical region";
	public static String STR_PESI_AUSTRALIAN_REGION = "Australian region";
	public static String STR_PESI_EAST_PALAEARCTIC = "East Palaearctic";
	public static String STR_PESI_NEARCTIC_REGION = "Nearctic region";
	public static String STR_PESI_NEOTROPICAL_REGION = "Neotropical region";
	public static String STR_PESI_NEAR_EAST = "Near East";
	public static String STR_PESI_ORIENTAL_REGION = "Oriental region";
	public static String STR_PESI_EUROPEAN_MARINE_WATERS = "European Marine Waters";
	public static String STR_PESI_MEDITERRANEAN_SEA = "Mediterranean Sea";
	public static String STR_PESI_WHITE_SEA = "White Sea";
	public static String STR_PESI_NORTH_SEA = "North Sea";
	public static String STR_PESI_BALTIC_SEA = "Baltic Sea";
	public static String STR_PESI_BLACK_SEA = "Black Sea";
	public static String STR_PESI_BARENTS_SEA = "Barents Sea";
	public static String STR_PESI_CASPIAN_SEA = "Caspian Sea";
	public static String STR_PESI_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE = "Portuguese Exclusive Economic Zone";
	public static String STR_PESI_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Belgian Exclusive Economic Zone";
	public static String STR_PESI_FRENCH_EXCLUSIVE_ECONOMIC_ZONE = "French Exclusive Economic Zone";
	public static String STR_PESI_ENGLISH_CHANNEL = "English Channel";
	public static String STR_PESI_ADRIATIC_SEA = "Adriatic Sea";
	public static String STR_PESI_BISCAY_BAY = "Biscay Bay";
	public static String STR_PESI_DUTCH_EXCLUSIVE_ECONOMIC_ZONE = "Dutch Exclusive Economic Zone";
	public static String STR_PESI_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE = "United Kingdom Exclusive Economic Zone";
	public static String STR_PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE = "Spanish Exclusive Economic Zone";
	public static String STR_PESI_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE = "Egyptian Exclusive Economic Zone";
	public static String STR_PESI_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE = "Grecian Exclusive Economic Zone";
	public static String STR_PESI_TIRRENO_SEA = "Tirreno Sea";
	public static String STR_PESI_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE = "Icelandic Exclusive Economic Zone";
	public static String STR_PESI_IRISH_EXCLUSIVE_ECONOMIC_ZONE = "Irish Exclusive economic Zone";
	public static String STR_PESI_IRISH_SEA = "Irish Sea";
	public static String STR_PESI_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE = "Italian Exclusive Economic Zone";
	public static String STR_PESI_NORWEGIAN_SEA = "Norwegian Sea";
	public static String STR_PESI_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE = "Moroccan Exclusive Economic Zone";
	public static String STR_PESI_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE = "Norwegian Exclusive Economic Zone";
	public static String STR_PESI_SKAGERRAK = "Skagerrak";
	public static String STR_PESI_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE = "Tunisian Exclusive Economic Zone";
	public static String STR_PESI_WADDEN_SEA = "Wadden Sea";
	public static String STR_PESI_BELT_SEA = "Belt Sea";
	public static String STR_PESI_MARMARA_SEA = "Marmara Sea";
	public static String STR_PESI_SEA_OF_AZOV = "Sea of Azov";
	public static String STR_PESI_AEGEAN_SEA = "Aegean Sea";
	public static String STR_PESI_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE = "Bulgarian Exclusive Economic Zone";
	public static String STR_PESI_SOUTH_BALTIC_PROPER = "South Baltic proper";
	public static String STR_PESI_BALTIC_PROPER = "Baltic Proper";
	public static String STR_PESI_NORTH_BALTIC_PROPER = "North Baltic proper";
	public static String STR_PESI_ARCHIPELAGO_SEA = "Archipelago Sea";
	public static String STR_PESI_BOTHNIAN_SEA = "Bothnian Sea";
	public static String STR_PESI_GERMAN_EXCLUSIVE_ECONOMIC_ZONE = "German Exclusive Economic Zone";
	public static String STR_PESI_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE = "Swedish Exclusive Economic Zone";
	public static String STR_PESI_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE = "Ukrainian Exclusive Economic Zone";
	public static String STR_PESI_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE = "Madeiran Exclusive Economic Zone";
	public static String STR_PESI_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE = "Lebanese Exclusive Economic Zone";
	public static String STR_PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART = "Spanish Exclusive Economic Zone [Mediterranean part]";
	public static String STR_PESI_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE = "Estonian Exclusive Economic Zone";
	public static String STR_PESI_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE = "Croatian Exclusive Economic Zone";
	public static String STR_PESI_BALEAR_SEA = "Balear Sea";
	public static String STR_PESI_TURKISH_EXCLUSIVE_ECONOMIC_ZONE = "Turkish Exclusive Economic Zone";
	public static String STR_PESI_DANISH_EXCLUSIVE_ECONOMIC_ZONE = "Danish Exclusive Economic Zone";

	
	// OccurrenceStatus
	public static int STATUS_PRESENT = 1;
	public static int STATUS_ABSENT = 2;
	public static int STATUS_NATIVE = 3;
	public static int STATUS_INTRODUCED = 4;
	public static int STATUS_NATURALISED = 5;
	public static int STATUS_INVASIVE = 6;
	public static int STATUS_MANAGED = 7;
	public static int STATUS_DOUBTFUL = 8;

	public static String STR_STATUS_PRESENT = "Present";
	public static String STR_STATUS_ABSENT = "Absent";
	public static String STR_STATUS_NATIVE = "Native";
	public static String STR_STATUS_INTRODUCED = "Introduced";
	public static String STR_STATUS_NATURALISED = "Naturalised";
	public static String STR_STATUS_INVASIVE = "Invasive";
	public static String STR_STATUS_MANAGED = "Managed";
	public static String STR_STATUS_DOUBTFUL = "Doubtful";

	
	// QualityStatus
	public static int CHECKED_BY_TAXONOMIC_EDITOR_INCLUDED_IN_ERMS_1_1 = 0;
	public static int ADDED_BY_DATABASE_MANAGEMENT_TEAM = 2;
	public static int CHECKED_BY_TAXONOMIC_EDITOR = 3;
	public static int EDITED_BY_DATABASE_MANAGEMENT_TEAM = 4;

	public static String STR_CHECKED_BY_TAXONOMIC_EDITOR_INCLUDED_IN_ERMS_1_1 = "Checked by Taxonomic Editor: included in ERMS 1.1";
	public static String STR_ADDED_BY_DATABASE_MANAGEMENT_TEAM = "Added by Database Management Team";
	public static String STR_CHECKED_BY_TAXONOMIC_EDITOR = "Checked by Taxonomic Editor";
	public static String STR_EDITED_BY_DATABASE_MANAGEMENT_TEAM = "Edited by Database Management Team";

	/**
	 * 
	 * @param qualityStatus
	 * @return
	 */
	public static Integer qualityStatus2QualityStatusFk(String qualityStatus) {
		Integer result = null;
		if (qualityStatus == null) {
			logger.error("The given QualityStatus is NULL.");
			return null;
		}
		if (qualityStatus.equals(STR_CHECKED_BY_TAXONOMIC_EDITOR_INCLUDED_IN_ERMS_1_1)) {
			return CHECKED_BY_TAXONOMIC_EDITOR_INCLUDED_IN_ERMS_1_1;
		} else if (qualityStatus.equals(STR_ADDED_BY_DATABASE_MANAGEMENT_TEAM)) {
			return ADDED_BY_DATABASE_MANAGEMENT_TEAM;
		} else if (qualityStatus.equals(STR_CHECKED_BY_TAXONOMIC_EDITOR)) {
			return CHECKED_BY_TAXONOMIC_EDITOR;
		} else if (qualityStatus.equals(STR_EDITED_BY_DATABASE_MANAGEMENT_TEAM)) {
			return EDITED_BY_DATABASE_MANAGEMENT_TEAM;
		}
		return result;
	}
	
	
	/**
	 * Returns the OccurrenceStatusCache for a given PresenceAbsenceTerm.
	 * @param term
	 * @return
	 * @throws UnknownCdmTypeException 
	 */
	public static String presenceAbsenceTerm2OccurrenceStatusCache(PresenceAbsenceTermBase<?> term) {
		String result = STR_STATUS_PRESENT; // TODO: What should be returned if a PresenceTerm/AbsenceTerm could not be translated to a datawarehouse occurrence status id?
		if (term == null) {
			logger.error("The given PresenceAbsenceTerm is NULL.");
			return null;
		}
		if (term.isInstanceOf(PresenceTerm.class)) {
			PresenceTerm presenceTerm = CdmBase.deproxy(term, PresenceTerm.class);
			if (presenceTerm.equals(PresenceTerm.PRESENT())) {
				result = STR_STATUS_PRESENT;
			} else if (presenceTerm.equals(PresenceTerm.NATIVE())) {
				result = STR_STATUS_NATIVE;
			} else if (presenceTerm.equals(PresenceTerm.INTRODUCED())) {
				result = STR_STATUS_INTRODUCED;
			} else if (presenceTerm.equals(PresenceTerm.NATURALISED())) {
				result = STR_STATUS_NATURALISED;
			} else if (presenceTerm.equals(PresenceTerm.INVASIVE())) {
				result = STR_STATUS_INVASIVE;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STR_STATUS_MANAGED;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STR_STATUS_DOUBTFUL;
			} else {
				logger.error("PresenceTerm could not be translated to datawarehouse occurrence status id: " + presenceTerm.getLabel());
			}
		} else if (term.isInstanceOf(AbsenceTerm.class)) {
			AbsenceTerm absenceTerm = CdmBase.deproxy(term, AbsenceTerm.class);
			if (absenceTerm.equals(AbsenceTerm.ABSENT())) {
				result = STR_STATUS_ABSENT;
			} else {
				logger.error("AbsenceTerm could not be translated to datawarehouse occurrence status id: " + absenceTerm.getLabel());
			}
//			result = STR_STATUS_ABSENT; // or just like this?
		}
		return result;
	}

	/**
	 * Returns the OccurrenceStatusId for a given PresenceAbsenceTerm.
	 * @param term
	 * @return
	 * @throws UnknownCdmTypeException 
	 */
	public static Integer presenceAbsenceTerm2OccurrenceStatusId(PresenceAbsenceTermBase<?> term) {
		Integer result = STATUS_PRESENT; // TODO: What should be returned if a PresenceTerm/AbsenceTerm could not be translated to a datawarehouse occurrence status id?
		if (term == null) {
			logger.error("The given PresenceAbsenceTerm is NULL.");
//			return null;
			return result; // TODO: It crashes otherwise because the OccurrenceStatusId must not be NULL.
		}
		if (term.isInstanceOf(PresenceTerm.class)) {
			PresenceTerm presenceTerm = CdmBase.deproxy(term, PresenceTerm.class);
			if (presenceTerm.equals(PresenceTerm.PRESENT())) {
				result = STATUS_PRESENT;
			} else if (presenceTerm.equals(PresenceTerm.NATIVE())) {
				result = STATUS_NATIVE;
			} else if (presenceTerm.equals(PresenceTerm.INTRODUCED())) {
				result = STATUS_INTRODUCED;
			} else if (presenceTerm.equals(PresenceTerm.NATURALISED())) {
				result = STATUS_NATURALISED;
			} else if (presenceTerm.equals(PresenceTerm.INVASIVE())) {
				result = STATUS_INVASIVE;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STATUS_MANAGED;
//			} else if (presenceTerm.equals(PresenceTerm.)) {
//				result = STATUS_DOUBTFUL;
			} else {
				logger.error("PresenceTerm could not be translated to datawarehouse occurrence status id: " + presenceTerm.getLabel());
			}
		} else if (term.isInstanceOf(AbsenceTerm.class)) {
			AbsenceTerm absenceTerm = CdmBase.deproxy(term, AbsenceTerm.class);
			if (absenceTerm.equals(AbsenceTerm.ABSENT())) {
				result = STATUS_ABSENT;
			} else {
				logger.error("AbsenceTerm could not be translated to datawarehouse occurrence status id: " + absenceTerm.getLabel());
			}
//			result = STATUS_ABSENT; // or just like this?
		}
		return result;
	}
	
	/**
	 * Returns the PESI Area Cache for an ERMS Area.
	 * @param area
	 * @return
	 */
	public static String area2AreaCache(NamedArea ermsArea) {
		if (ermsArea == null) {
			logger.error("The given NamedArea is NULL.");
			return null;
			}
		// cdm_test_andreas2
		String result = null;
		String areaName = ermsArea.getRepresentation(Language.DEFAULT()).getLabel();
		if (areaName.equalsIgnoreCase(STR_ERMS_EUROPEAN_MARINE_WATERS)) { result = STR_PESI_EUROPEAN_MARINE_WATERS;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MEDITERRANEAN_SEA)) { result = STR_PESI_MEDITERRANEAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_WHITE_SEA)) { result = STR_PESI_WHITE_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORTH_SEA)) { result = STR_PESI_NORTH_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BALTIC_SEA)) { result = STR_PESI_BALTIC_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BLACK_SEA)) { result = STR_PESI_BLACK_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BARENTS_SEA)) { result = STR_PESI_BARENTS_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_CASPIAN_SEA)) { result = STR_PESI_CASPIAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_FRENCH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_FRENCH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ENGLISH_CHANNEL)) { result = STR_PESI_ENGLISH_CHANNEL;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ADRIATIC_SEA)) { result = STR_PESI_ADRIATIC_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BISCAY_BAY)) { result = STR_PESI_BISCAY_BAY;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_DUTCH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_DUTCH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_TIRRENO_SEA)) { result = STR_PESI_TIRRENO_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_IRISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_IRISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_IRISH_SEA)) { result = STR_PESI_IRISH_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORWEGIAN_SEA)) { result = STR_PESI_NORWEGIAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SKAGERRAK)) { result = STR_PESI_SKAGERRAK;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_WADDEN_SEA)) { result = STR_PESI_WADDEN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BELT_SEA)) { result = STR_PESI_BELT_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MARMARA_SEA)) { result = STR_PESI_MARMARA_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SEA_OF_AZOV)) { result = STR_PESI_SEA_OF_AZOV;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_AEGEAN_SEA)) { result = STR_PESI_AEGEAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SOUTH_BALTIC_PROPER)) { result = STR_PESI_SOUTH_BALTIC_PROPER;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BALTIC_PROPER)) { result = STR_PESI_BALTIC_PROPER;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORTH_BALTIC_PROPER)) { result = STR_PESI_NORTH_BALTIC_PROPER;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ARCHIPELAGO_SEA)) { result = STR_PESI_ARCHIPELAGO_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BOTHNIAN_SEA)) { result = STR_PESI_BOTHNIAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_GERMAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_GERMAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART)) { result = STR_PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BALEAR_SEA)) { result = STR_PESI_BALEAR_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_TURKISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_TURKISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_DANISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = STR_PESI_DANISH_EXCLUSIVE_ECONOMIC_ZONE;
			} else {
			// Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		return result;
		}
		
	/**
	 * Returns the PESI Area Identifier for an ERMS Area.
	 * @param area
	 * @return
	 */
	public static Integer area2AreaId(NamedArea ermsArea) {
		if (ermsArea == null) {
			logger.error("The given NamedArea is NULL.");
			return null;
			}
		Integer result = null;
		String areaName = ermsArea.getRepresentation(Language.DEFAULT()).getLabel();
		if (areaName.equalsIgnoreCase(STR_ERMS_EUROPEAN_MARINE_WATERS)) { result = PESI_EUROPEAN_MARINE_WATERS;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MEDITERRANEAN_SEA)) { result = PESI_MEDITERRANEAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_WHITE_SEA)) { result = PESI_WHITE_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORTH_SEA)) { result = PESI_NORTH_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BALTIC_SEA)) { result = PESI_BALTIC_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BLACK_SEA)) { result = PESI_BLACK_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BARENTS_SEA)) { result = PESI_BARENTS_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_CASPIAN_SEA)) { result = PESI_CASPIAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_PORTUGUESE_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_BELGIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_FRENCH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_FRENCH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ENGLISH_CHANNEL)) { result = PESI_ENGLISH_CHANNEL;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ADRIATIC_SEA)) { result = PESI_ADRIATIC_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BISCAY_BAY)) { result = PESI_BISCAY_BAY;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_DUTCH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_DUTCH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_UNITED_KINGDOM_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_EGYPTIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_GRECIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_TIRRENO_SEA)) { result = PESI_TIRRENO_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_ICELANDIC_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_IRISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_IRISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_IRISH_SEA)) { result = PESI_IRISH_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_ITALIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORWEGIAN_SEA)) { result = PESI_NORWEGIAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_MOROCCAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_NORWEGIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SKAGERRAK)) { result = PESI_SKAGERRAK;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_TUNISIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_WADDEN_SEA)) { result = PESI_WADDEN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BELT_SEA)) { result = PESI_BELT_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MARMARA_SEA)) { result = PESI_MARMARA_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SEA_OF_AZOV)) { result = PESI_SEA_OF_AZOV;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_AEGEAN_SEA)) { result = PESI_AEGEAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_BULGARIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SOUTH_BALTIC_PROPER)) { result = PESI_SOUTH_BALTIC_PROPER;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BALTIC_PROPER)) { result = PESI_BALTIC_PROPER;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_NORTH_BALTIC_PROPER)) { result = PESI_NORTH_BALTIC_PROPER;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ARCHIPELAGO_SEA)) { result = PESI_ARCHIPELAGO_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BOTHNIAN_SEA)) { result = PESI_BOTHNIAN_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_GERMAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_GERMAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_SWEDISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_UKRAINIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_MADEIRAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_LEBANESE_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART)) { result = PESI_SPANISH_EXCLUSIVE_ECONOMIC_ZONE_MEDITERRANEAN_PART;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_ESTONIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_CROATIAN_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_BALEAR_SEA)) { result = PESI_BALEAR_SEA;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_TURKISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_TURKISH_EXCLUSIVE_ECONOMIC_ZONE;
		} else if (areaName.equalsIgnoreCase(STR_ERMS_DANISH_EXCLUSIVE_ECONOMIC_ZONE)) { result = PESI_DANISH_EXCLUSIVE_ECONOMIC_ZONE;
			} else {
			// Actually the export has to stop here because AreaFk's are not allowed to be NULL.
			}
		return result;
		}

	/**
	 * Returns the PESI SourceUseId for a given CDM sourceUseId.
	 * @param sourceUseId
	 * @return
	 */
	public static Integer sourceUseIdSourceUseId(Integer sourceUseId) {
		// TODO: CDM sourceUseId and PESI sourceUseId are equal for now.
		Integer result = null;
		switch (sourceUseId) {
			case 3: return ADDITIONAL_SOURCE;
			case 4: return SOURCE_OF_SYNONYMY;
			case 8: return NOMENCLATURAL_REFERENCE;
		}
		return result;
	}
	
	/**
	 * Returns the SourceUseCache for a tiven sourceUseId.
	 * @param sourceUseId
	 * @return
	 */
	public static String sourceUseId2SourceUseCache(Integer sourceUseId) {
		// TODO: CDM sourceUseId and PESI sourceUseId are equal for now.
		String result = null;
		switch (sourceUseId) {
			case 3: return STR_ADDITIONAL_SOURCE;
			case 4: return STR_SOURCE_OF_SYNONYMY;
			case 8: return STR_NOMENCLATURAL_REFERENCE;
		}
		return result;
	}
	
	/**
	 * Returns the FossilStatusId to a given FossilStatus.
	 * @param fossilStatus
	 * @return
	 */
	public static Integer fossilStatus2FossilStatusId(String fossilStatus) {
		if (fossilStatus == null) {
			logger.error("The given FossilStatus is NULL.");
			return null;
		} else if (fossilStatus.equals(STR_FOSSILSTATUS_RECENT_ONLY)) {
			return FOSSILSTATUS_RECENT_ONLY;
		} else if (fossilStatus.equals(STR_FOSSILSTATUS_FOSSIL_ONLY)) {
			return FOSSILSTATUS_FOSSIL_ONLY;
		} else if (fossilStatus.equals(STR_FOSSILSTATUS_RECENT_FOSSIL)) {
			return FOSSILSTATUS_RECENT_FOSSIL;
		} else {
			logger.warn("Fossilstatus unknown: " + fossilStatus);
			return null;
	}
	}
	
	/**
	 * Returns the LanguageCache to a given Language.
	 * @param language
	 * @return
	 */
	public static String language2LanguageCache(Language language) {
		if (language == null ) {
			logger.error("The given Language is NULL.");
			return null;
		}
		if (language.equals(Language.ALBANIAN())) {
			return STR_LANGUAGE_ALBANIAN;
		} else if (language.equals(Language.ARABIC())) {
			return STR_LANGUAGE_ARABIC;
		} else if (language.equals(Language.ARMENIAN())) {
			return STR_LANGUAGE_ARMENIAN;
		} else if (language.equals(Language.AZERBAIJANI())) {
			return STR_LANGUAGE_AZERBAIJAN;
		} else if (language.equals(Language.BELORUSSIAN())) {
			return STR_LANGUAGE_BELARUSIAN;
		} else if (language.equals(Language.BULGARIAN())) {
			return STR_LANGUAGE_BULGARIAN;
		} else if (language.equals(Language.CATALAN_VALENCIAN())) {
			return STR_LANGUAGE_CATALAN;
		} else if (language.equals(Language.CROATIAN())) {
			return STR_LANGUAGE_CROAT;
		} else if (language.equals(Language.CZECH())) {
			return STR_LANGUAGE_CZECH;
		} else if (language.equals(Language.DANISH())) {
			return STR_LANGUAGE_DANISH;
		} else if (language.equals(Language.DUTCH_MIDDLE())) {
			return STR_LANGUAGE_DUTCH;
		} else if (language.equals(Language.ENGLISH())) {
			return STR_LANGUAGE_ENGLISH;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_EUSKERA;
		} else if (language.equals(Language.ESTONIAN())) {
			return STR_LANGUAGE_ESTONIAN;
		} else if (language.equals(Language.FINNISH())) {
			return STR_LANGUAGE_FINNISH;
		} else if (language.equals(Language.FRENCH())) {
			return STR_LANGUAGE_FRENCH;
		} else if (language.equals(Language.GEORGIAN())) {
			return STR_LANGUAGE_GEORGIAN;
		} else if (language.equals(Language.GERMAN())) {
			return STR_LANGUAGE_GERMAN;
		} else if (language.equals(Language.GREEK_MODERN())) {
			return STR_LANGUAGE_GREEK;
		} else if (language.equals(Language.HUNGARIAN())) {
			return STR_LANGUAGE_HUNGARIAN;
		} else if (language.equals(Language.ICELANDIC())) {
			return STR_LANGUAGE_ICELANDIC;
		} else if (language.equals(Language.IRISH())) {
			return STR_LANGUAGE_IRISH_GAELIC;
		} else if (language.equals(Language.HEBREW())) {
			return STR_LANGUAGE_ISRAEL_HEBREW;
		} else if (language.equals(Language.ITALIAN())) {
			return STR_LANGUAGE_ITALIAN;
		} else if (language.equals(Language.LATVIAN())) {
			return STR_LANGUAGE_LATVIAN;
		} else if (language.equals(Language.LITHUANIAN())) {
			return STR_LANGUAGE_LITHUANIAN;
		} else if (language.equals(Language.MACEDONIAN())) {
			return STR_LANGUAGE_MACEDONIAN;
		} else if (language.equals(Language.MALTESE())) {
			return STR_LANGUAGE_MALTESE;
		} else if (language.equals(Language.MOLDAVIAN())) {
			return STR_LANGUAGE_MOLDOVIAN;
		} else if (language.equals(Language.NORWEGIAN())) {
			return STR_LANGUAGE_NORWEGIAN;
		} else if (language.equals(Language.POLISH())) {
			return STR_LANGUAGE_POLISH;
		} else if (language.equals(Language.PORTUGUESE())) {
			return STR_LANGUAGE_PORTUGUESE;
		} else if (language.equals(Language.ROMANIAN())) {
			return STR_LANGUAGE_ROUMANIAN;
		} else if (language.equals(Language.RUSSIAN())) {
			return STR_LANGUAGE_RUSSIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_CAUCASIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_KALMYK_OIRAT;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_KARACHAY_BALKAR;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_KUMYK;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_NOGAI;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_ALTAIC_NORTH_AZERBAIJANI;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_RUSSIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_KALMYK_OIRAT;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_INDO_EUROPEAN_OSETIN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ABAZA;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_ADYGHE;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_CHECHEN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_KABARDIAN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_LAK;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_AVAR;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_NORTH_CAUCASIAN_IN;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_URALIC_CHUVASH;
//		} else if (language.equals(Language.)) {
//			return STR_LANGUAGE_RUSSIAN_URALIC_UDMURT;
		} else if (language.equals(Language.SERBIAN())) {
			return STR_LANGUAGE_SERBIAN;
		} else if (language.equals(Language.SLOVAK())) {
			return STR_LANGUAGE_SLOVAK;
		} else if (language.equals(Language.SLOVENIAN())) {
			return STR_LANGUAGE_SLOVENE;
		} else if (language.equals(Language.SPANISH_CATALAN())) {
			return STR_LANGUAGE_SPANISH_CASTILLIAN;
		} else if (language.equals(Language.SWEDISH())) {
			return STR_LANGUAGE_SWEDISH;
		} else if (language.equals(Language.TURKISH())) {
			return STR_LANGUAGE_TURKISH;
		} else if (language.equals(Language.UKRAINIAN())) {
			return STR_LANGUAGE_UKRAINE;
		} else if (language.equals(Language.WELSH())) {
			return STR_LANGUAGE_WELSH;
		} else if (language.equals(Language.CORSICAN())) {
			return STR_LANGUAGE_CORSICAN;
		} else {
			logger.debug("Unknown Language: " + language.getTitleCache());
			return null;
		}
	}
	
	/**
	 * Returns the identifier of the given Language.
	 * @param language
	 * @return
	 */
	public static Integer language2LanguageId(Language language) {
		if (language == null ) {
			logger.error("The given Language is NULL.");
			return null;
		}
		if (language.equals(Language.ALBANIAN())) {
			return Language_Albanian;
		} else if (language.equals(Language.ARABIC())) {
			return Language_Arabic;
		} else if (language.equals(Language.ARMENIAN())) {
			return Language_Armenian;
		} else if (language.equals(Language.AZERBAIJANI())) {
			return Language_Azerbaijan;
		} else if (language.equals(Language.BELORUSSIAN())) {
			return Language_Belarusian;
		} else if (language.equals(Language.BULGARIAN())) {
			return Language_Bulgarian;
		} else if (language.equals(Language.CATALAN_VALENCIAN())) {
			return Language_Catalan;
		} else if (language.equals(Language.CROATIAN())) {
			return Language_Croat;
		} else if (language.equals(Language.CZECH())) {
			return Language_Czech;
		} else if (language.equals(Language.DANISH())) {
			return Language_Danish;
		} else if (language.equals(Language.DUTCH_MIDDLE())) {
			return Language_Dutch;
		} else if (language.equals(Language.ENGLISH())) {
			return Language_English;
//		} else if (language.equals(Language.)) {
//			return Language_Euskera;
		} else if (language.equals(Language.ESTONIAN())) {
			return Language_Estonian;
		} else if (language.equals(Language.FINNISH())) {
			return Language_Finnish;
		} else if (language.equals(Language.FRENCH())) {
			return Language_French;
		} else if (language.equals(Language.GEORGIAN())) {
			return Language_Georgian;
		} else if (language.equals(Language.GERMAN())) {
			return Language_German;
		} else if (language.equals(Language.GREEK_MODERN())) {
			return Language_Greek;
		} else if (language.equals(Language.HUNGARIAN())) {
			return Language_Hungarian;
		} else if (language.equals(Language.ICELANDIC())) {
			return Language_Icelandic;
		} else if (language.equals(Language.IRISH())) {
			return Language_Irish_Gaelic;
		} else if (language.equals(Language.HEBREW())) {
			return Language_Israel_Hebrew;
		} else if (language.equals(Language.ITALIAN())) {
			return Language_Italian;
		} else if (language.equals(Language.LATVIAN())) {
			return Language_Latvian;
		} else if (language.equals(Language.LITHUANIAN())) {
			return Language_Lithuanian;
		} else if (language.equals(Language.MACEDONIAN())) {
			return Language_Macedonian;
		} else if (language.equals(Language.MALTESE())) {
			return Language_Maltese;
		} else if (language.equals(Language.MOLDAVIAN())) {
			return Language_Moldovian;
		} else if (language.equals(Language.NORWEGIAN())) {
			return Language_Norwegian;
		} else if (language.equals(Language.POLISH())) {
			return Language_Polish;
		} else if (language.equals(Language.PORTUGUESE())) {
			return Language_Portuguese;
		} else if (language.equals(Language.ROMANIAN())) {
			return Language_Roumanian;
		} else if (language.equals(Language.RUSSIAN())) {
			return Language_Russian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Caucasian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_kalmyk_oirat;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_karachay_balkar;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_kumyk;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_nogai;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Altaic_north_azerbaijani;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Indo_european_russian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Indo_european_kalmyk_oirat;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Indo_european_osetin;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_abaza;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_adyghe;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_chechen;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_kabardian;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_lak;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_avar;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_North_caucasian_in;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Uralic_chuvash;
//		} else if (language.equals(Language.)) {
//			return Language_Russian_Uralic_udmurt;
		} else if (language.equals(Language.SERBIAN())) {
			return Language_Serbian;
		} else if (language.equals(Language.SLOVAK())) {
			return Language_Slovak;
		} else if (language.equals(Language.SLOVENIAN())) {
			return Language_Slovene;
		} else if (language.equals(Language.SPANISH_CATALAN())) {
			return Language_Spanish_Castillian;
		} else if (language.equals(Language.SWEDISH())) {
			return Language_Swedish;
		} else if (language.equals(Language.TURKISH())) {
			return Language_Turkish;
		} else if (language.equals(Language.UKRAINIAN())) {
			return Language_Ukraine;
		} else if (language.equals(Language.WELSH())) {
			return Language_Welsh;
		} else if (language.equals(Language.CORSICAN())) {
			return Language_Corsican;
		} else {
			logger.debug("Unknown Language: " + language.getTitleCache());
			return null;
		}
	}
	
	/**
	 * Returns the NodeCategoryCache for a given TextData.
	 * @param feature
	 * @return
	 */
	public static String textData2NodeCategoryCache(Feature feature) {
		if (feature == null) {
			logger.error("The given Feature is NULL.");
			return null;
		}
		if (feature.equals(Feature.DESCRIPTION())) {
			return NoteCategory_STR_description;
		} else if (feature.equals(Feature.ECOLOGY())) {
			return NoteCategory_STR_ecology;
		} else if (feature.equals(Feature.PHENOLOGY())) {
			return NoteCategory_STR_phenology;
		} else if (feature.equals(Feature.COMMON_NAME())) {
			return NoteCategory_STR_Common_names;
		} else if (feature.equals(Feature.OCCURRENCE())) {
			return NoteCategory_STR_Occurrence;
//		} else if (feature.equals(Feature.CITATION())) {
//			return;
			
			// TODO: Unknown NodeCategories
//			NoteCategory_general_distribution_euromed = 10;
//			NoteCategory_general_distribution_world = 11;
//			NoteCategory_Common_names = 12;
//			NoteCategory_Maps =14;
//			NoteCategory_Link_to_maps = 20;
//			NoteCategory_Link_to_images = 21;
//			NoteCategory_Link_to_taxonomy = 22;
//			NoteCategory_Link_to_general_information = 23;
//			NoteCategory_undefined_link = 24;
//			NoteCategory_Editor_Braces = 249;
//			NoteCategory_Editor_Brackets = 250;
//			NoteCategory_Editor_Parenthesis = 251;
//			NoteCategory_Inedited = 252;
//			NoteCategory_Comments_on_editing_process = 253;
//			NoteCategory_Publication_date = 254;
//			NoteCategory_Morphology = 255;
//			NoteCategory_Acknowledgments = 257;
//			NoteCategory_Original_publication = 258;
//			NoteCategory_Type_locality	= 259;
//			NoteCategory_Environment = 260;
//			NoteCategory_Spelling = 261;
//			NoteCategory_Systematics = 262;
//			NoteCategory_Remark = 263;
//			NoteCategory_Date_of_publication = 264;
//			NoteCategory_Additional_information = 266;
//			NoteCategory_Status = 267;
//			NoteCategory_Nomenclature = 268;
//			NoteCategory_Homonymy = 269;
//			NoteCategory_Taxonomy = 270;
//			NoteCategory_Taxonomic_status = 272;
//			NoteCategory_Authority	= 273;
//			NoteCategory_Identification = 274;
//			NoteCategory_Validity = 275;
//			NoteCategory_Classification = 276;
//			NoteCategory_Distribution = 278;
//			NoteCategory_Synonymy = 279;
//			NoteCategory_Habitat = 280;
//			NoteCategory_Biology = 281;
//			NoteCategory_Diagnosis	= 282;
//			NoteCategory_Host = 283;
//			NoteCategory_Note = 284;
//			NoteCategory_Rank = 285;
//			NoteCategory_Taxonomic_Remark = 286;
//			NoteCategory_Taxonomic_Remarks = 287;

 		} else {
			logger.debug("Unknown Feature: " + feature.getTitleCache());
			return null;
		}
	}

	/**
	 * Returns the NodeCategoryFk for a given TextData.
	 * @param feature
	 * @return
	 */
	public static Integer textData2NodeCategoryFk(Feature feature) {
		if (feature == null) {
			logger.error("The given Feature is NULL.");
			return null;
		}
		if (feature.equals(Feature.DESCRIPTION())) {
			return NoteCategory_description;
		} else if (feature.equals(Feature.ECOLOGY())) {
			return NoteCategory_ecology;
		} else if (feature.equals(Feature.PHENOLOGY())) {
			return NoteCategory_phenology;
		} else if (feature.equals(Feature.COMMON_NAME())) {
			return NoteCategory_Common_names;
		} else if (feature.equals(Feature.OCCURRENCE())) {
			return NoteCategory_Occurrence;
//		} else if (feature.equals(Feature.CITATION())) {
//			return;
			
			// TODO: Unknown NodeCategories
//			NoteCategory_general_distribution_euromed = 10;
//			NoteCategory_general_distribution_world = 11;
//			NoteCategory_Common_names = 12;
//			NoteCategory_Maps =14;
//			NoteCategory_Link_to_maps = 20;
//			NoteCategory_Link_to_images = 21;
//			NoteCategory_Link_to_taxonomy = 22;
//			NoteCategory_Link_to_general_information = 23;
//			NoteCategory_undefined_link = 24;
//			NoteCategory_Editor_Braces = 249;
//			NoteCategory_Editor_Brackets = 250;
//			NoteCategory_Editor_Parenthesis = 251;
//			NoteCategory_Inedited = 252;
//			NoteCategory_Comments_on_editing_process = 253;
//			NoteCategory_Publication_date = 254;
//			NoteCategory_Morphology = 255;
//			NoteCategory_Acknowledgments = 257;
//			NoteCategory_Original_publication = 258;
//			NoteCategory_Type_locality	= 259;
//			NoteCategory_Environment = 260;
//			NoteCategory_Spelling = 261;
//			NoteCategory_Systematics = 262;
//			NoteCategory_Remark = 263;
//			NoteCategory_Date_of_publication = 264;
//			NoteCategory_Additional_information = 266;
//			NoteCategory_Status = 267;
//			NoteCategory_Nomenclature = 268;
//			NoteCategory_Homonymy = 269;
//			NoteCategory_Taxonomy = 270;
//			NoteCategory_Taxonomic_status = 272;
//			NoteCategory_Authority	= 273;
//			NoteCategory_Identification = 274;
//			NoteCategory_Validity = 275;
//			NoteCategory_Classification = 276;
//			NoteCategory_Distribution = 278;
//			NoteCategory_Synonymy = 279;
//			NoteCategory_Habitat = 280;
//			NoteCategory_Biology = 281;
//			NoteCategory_Diagnosis	= 282;
//			NoteCategory_Host = 283;
//			NoteCategory_Note = 284;
//			NoteCategory_Rank = 285;
//			NoteCategory_Taxonomic_Remark = 286;
//			NoteCategory_Taxonomic_Remarks = 287;

		}else{
			logger.debug("Unknown Feature: " + feature.getTitleCache());
			return null;
		}
	}

	/**
	 * Returns the string representation for a given rank.
	 * @param rank
	 * @param pesiKingdomId
	 * @return
	 */
	public static String rank2RankCache(Rank rank, Integer pesiKingdomId) {
		String result = null;
		if (rank == null) {
			logger.error("Rank is NULL. RankCache can not be determined.");
			return null;
		}
		
		// We differentiate between Animalia and Plantae only for now.
		if (pesiKingdomId == KINGDOM_ANIMALIA) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Animalia_STR_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Animalia_STR_Subkingdom;
			} else if (rank.equals(Rank.SUPERPHYLUM())) {
				result = Animalia_STR_Superphylum;
			} else if (rank.equals(Rank.PHYLUM())) {
				result = Animalia_STR_Phylum;
			} else if (rank.equals(Rank.SUBPHYLUM())) {
				result = Animalia_STR_Subphylum;
			} else if (rank.equals(Rank.INFRAPHYLUM())) {
				result = Animalia_STR_Infraphylum;
			} else if (rank.equals(Rank.SUPERCLASS())) {
				result = Animalia_STR_Superclass;
			} else if (rank.equals(Rank.CLASS())) {
				result = Animalia_STR_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Animalia_STR_Subclass;
			} else if (rank.equals(Rank.INFRACLASS())) {
				result = Animalia_STR_Infraclass;
			} else if (rank.equals(Rank.SUPERORDER())) {
				result = Animalia_STR_Superorder;
			} else if (rank.equals(Rank.ORDER())) {
				result = Animalia_STR_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Animalia_STR_Suborder;
			} else if (rank.equals(Rank.INFRAORDER())) {
				result = Animalia_STR_Infraorder;
			} else if (rank.equals(Rank.SECTION_ZOOLOGY())) {
				result = Animalia_STR_Section;
			} else if (rank.equals(Rank.SUBSECTION_ZOOLOGY())) {
				result = Animalia_STR_Subsection;
			} else if (rank.equals(Rank.SUPERFAMILY())) {
				result = Animalia_STR_Superfamily;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Animalia_STR_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Animalia_STR_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Animalia_STR_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Animalia_STR_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Animalia_STR_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Animalia_STR_Subgenus;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Animalia_STR_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Animalia_STR_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Animalia_STR_Natio;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Animalia_STR_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Animalia_STR_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Animalia_STR_Forma;
			}
		} else if (pesiKingdomId == KINGDOM_PLANTAE) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Plantae_STR_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Plantae_STR_Subkingdom;
			} else if (rank.equals(Rank.DIVISION())) {
				result = Plantae_STR_Division;
			} else if (rank.equals(Rank.SUBDIVISION())) {
				result = Plantae_STR_Subdivision;
			} else if (rank.equals(Rank.CLASS())) {
				result = Plantae_STR_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Plantae_STR_Subclass;
			} else if (rank.equals(Rank.ORDER())) {
				result = Plantae_STR_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Plantae_STR_Suborder;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Plantae_STR_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Plantae_STR_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Plantae_STR_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Plantae_STR_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Plantae_STR_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Plantae_STR_Subgenus;
			} else if (rank.equals(Rank.SECTION_BOTANY())) {
				result = Plantae_STR_Section;
			} else if (rank.equals(Rank.SUBSECTION_BOTANY())) {
				result = Plantae_STR_Subsection;
			} else if (rank.equals(Rank.SERIES())) {
				result = Plantae_STR_Series;
			} else if (rank.equals(Rank.SUBSERIES())) {
				result = Plantae_STR_Subseries;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Aggregate;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Coll_Species;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Plantae_STR_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Plantae_STR_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Proles;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Race;
			} else if (rank.equals(Rank.CONVAR())) {
				result = Plantae_STR_Convarietas;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Plantae_STR_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Plantae_STR_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Plantae_STR_Forma;
			} else if (rank.equals(Rank.SUBFORM())) {
				result = Plantae_STR_Subforma;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Forma_spec;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Taxa_infragen;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_STR_Taxa_infraspec;
			}
		} else {
			//TODO Exception
			logger.warn("Rank not yet supported in CDM: "+ rank.getLabel());
		}
		return result;
	}
	
	/**
	 * Returns the identifier of a PESI specific kingdom for a given CDM nomenclatural code.
	 * @param nomenclaturalCode
	 * @return KINGDOM_ANIMALIA for NomenclaturalCode.ICZN, KINGDOM_PLANTAE for NomenclaturalCode.ICBN
	 */
	public static Integer nomenClaturalCode2Kingdom(NomenclaturalCode nomenclaturalCode) {
		Integer result = null;
		// TODO: This needs to be refined. For now we differentiate between Animalia and Plantae only.
		if (nomenclaturalCode != null) {
		if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
			result = KINGDOM_ANIMALIA;
		} else if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)) {
			result = KINGDOM_PLANTAE;
			} else if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
				result = KINGDOM_BACTERIA;
		}
		} else {
			logger.error("The given NomenclaturalCode is NULL.");
		}
		return result;
	}
	
	/**
	 * Returns the RankId for a Rank.
	 * @param rank
	 * @return
	 */
	public static Integer rank2RankId (Rank rank, Integer pesiKingdomId) {
		Integer result = null;
		if (rank == null) {
			logger.error("Rank is NULL. RankId can not be determined.");
			return null;
		}
		
		// We differentiate between Animalia and Plantae only for now.
		if (pesiKingdomId == KINGDOM_ANIMALIA) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Animalia_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Animalia_Subkingdom;
			} else if (rank.equals(Rank.SUPERPHYLUM())) {
				result = Animalia_Superphylum;
			} else if (rank.equals(Rank.PHYLUM())) {
				result = Animalia_Phylum;
			} else if (rank.equals(Rank.SUBPHYLUM())) {
				result = Animalia_Subphylum;
			} else if (rank.equals(Rank.INFRAPHYLUM())) {
				result = Animalia_Infraphylum;
			} else if (rank.equals(Rank.SUPERCLASS())) {
				result = Animalia_Superclass;
			} else if (rank.equals(Rank.CLASS())) {
				result = Animalia_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Animalia_Subclass;
			} else if (rank.equals(Rank.INFRACLASS())) {
				result = Animalia_Infraclass;
			} else if (rank.equals(Rank.SUPERORDER())) {
				result = Animalia_Superorder;
			} else if (rank.equals(Rank.ORDER())) {
				result = Animalia_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Animalia_Suborder;
			} else if (rank.equals(Rank.INFRAORDER())) {
				result = Animalia_Infraorder;
			} else if (rank.equals(Rank.SECTION_ZOOLOGY())) {
				result = Animalia_Section;
			} else if (rank.equals(Rank.SUBSECTION_ZOOLOGY())) {
				result = Animalia_Subsection;
			} else if (rank.equals(Rank.SUPERFAMILY())) {
				result = Animalia_Superfamily;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Animalia_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Animalia_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Animalia_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Animalia_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Animalia_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Animalia_Subgenus;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Animalia_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Animalia_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Animalia_Natio;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Animalia_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Animalia_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Animalia_Forma;
			} else {
				logger.warn("Rank '" + rank.getTitleCache() + "' unknown for Kingdom '" + STR_KINGDOM_ANIMALIA + "'.");
			}
		} else if (pesiKingdomId == KINGDOM_PLANTAE) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Plantae_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Plantae_Subkingdom;
			} else if (rank.equals(Rank.DIVISION())) {
				result = Plantae_Division;
			} else if (rank.equals(Rank.SUBDIVISION())) {
				result = Plantae_Subdivision;
			} else if (rank.equals(Rank.CLASS())) {
				result = Plantae_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Plantae_Subclass;
			} else if (rank.equals(Rank.ORDER())) {
				result = Plantae_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Plantae_Suborder;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Plantae_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Plantae_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Plantae_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Plantae_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Plantae_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Plantae_Subgenus;
			} else if (rank.equals(Rank.SECTION_BOTANY())) {
				result = Plantae_Section;
			} else if (rank.equals(Rank.SUBSECTION_BOTANY())) {
				result = Plantae_Subsection;
			} else if (rank.equals(Rank.SERIES())) {
				result = Plantae_Series;
			} else if (rank.equals(Rank.SUBSERIES())) {
				result = Plantae_Subseries;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Aggregate;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Coll_Species;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Plantae_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Plantae_Subspecies;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Proles;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Race;
			} else if (rank.equals(Rank.CONVAR())) {
				result = Plantae_Convarietas;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Plantae_Variety;
			} else if (rank.equals(Rank.SUBVARIETY())) {
				result = Plantae_Subvariety;
			} else if (rank.equals(Rank.FORM())) {
				result = Plantae_Forma;
			} else if (rank.equals(Rank.SUBFORM())) {
				result = Plantae_Subforma;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Forma_spec;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Taxa_infragen;
//			} else if (rank.equals(Rank.)) { // not yet specified
//				result = Plantae_Taxa_infraspec;
			} else {
				logger.warn("Rank '" + rank.getTitleCache() + "' unknown for Kingdom '" + STR_KINGDOM_PLANTAE + "'.");
			}
		} else if (pesiKingdomId == KINGDOM_BACTERIA) {
			if (rank.equals(Rank.KINGDOM())) {
				result = Bacteria_Kingdom;
			} else if (rank.equals(Rank.SUBKINGDOM())) {
				result = Bacteria_Subkingdom;
			} else if (rank.equals(Rank.PHYLUM())) {
				result = Bacteria_Phylum;
			} else if (rank.equals(Rank.SUBPHYLUM())) {
				result = Bacteria_Subphylum;
			} else if (rank.equals(Rank.SUPERCLASS())) {
				result = Bacteria_Superclass;
			} else if (rank.equals(Rank.CLASS())) {
				result = Bacteria_Class;
			} else if (rank.equals(Rank.SUBCLASS())) {
				result = Bacteria_Subclass;
			} else if (rank.equals(Rank.INFRACLASS())) {
				result = Bacteria_Infraclass;
			} else if (rank.equals(Rank.SUPERORDER())) {
				result = Bacteria_Superorder;
			} else if (rank.equals(Rank.ORDER())) {
				result = Bacteria_Order;
			} else if (rank.equals(Rank.SUBORDER())) {
				result = Bacteria_Suborder;
			} else if (rank.equals(Rank.INFRAORDER())) {
				result = Bacteria_Infraorder;
			} else if (rank.equals(Rank.SUPERFAMILY())) {
				result = Bacteria_Superfamily;
			} else if (rank.equals(Rank.FAMILY())) {
				result = Bacteria_Family;
			} else if (rank.equals(Rank.SUBFAMILY())) {
				result = Bacteria_Subfamily;
			} else if (rank.equals(Rank.TRIBE())) {
				result = Bacteria_Tribe;
			} else if (rank.equals(Rank.SUBTRIBE())) {
				result = Bacteria_Subtribe;
			} else if (rank.equals(Rank.GENUS())) {
				result = Bacteria_Genus;
			} else if (rank.equals(Rank.SUBGENUS())) {
				result = Bacteria_Subgenus;
			} else if (rank.equals(Rank.SPECIES())) {
				result = Bacteria_Species;
			} else if (rank.equals(Rank.SUBSPECIES())) {
				result = Bacteria_Subspecies;
			} else if (rank.equals(Rank.VARIETY())) {
				result = Bacteria_Variety;
			} else if (rank.equals(Rank.FORM())) {
				result = Bacteria_Forma;
		} else {
				logger.warn("Rank '" + rank.getTitleCache() + "' unknown for Kingdom '" + STR_KINGDOM_BACTERIA + "'.");
			}
		} else {
			//TODO Exception
			logger.warn("Rank not yet supported in CDM: "+ rank.getLabel());
		}
		return result;
	}

	/**
	 * 
	 * @param nameTypeDesignationStatus
	 * @return
	 */
	public static Integer nameTypeDesignationStatus2TypeDesignationStatusId(NameTypeDesignationStatus nameTypeDesignationStatus) {
		if (nameTypeDesignationStatus == null) {
			logger.error("The given NameTypeDesignationStatus is NULL.");
			return null;
		}
		if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION())) {
			return TYPE_BY_ORIGINAL_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION())) {
			return TYPE_BY_SUBSEQUENT_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.MONOTYPY())) {
			return TYPE_BY_MONOTYPY;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Name Type Designation Status not yet supported in PESI: "+ nameTypeDesignationStatus.getLabel());
			return null;
		}

	}

	/**
	 * 
	 * @param nameTypeDesignationStatus
	 * @return
	 */
	public static String nameTypeDesignationStatus2TypeDesignationStatusCache(NameTypeDesignationStatus nameTypeDesignationStatus) {
		if (nameTypeDesignationStatus == null) {
			logger.error("The given NameTypeDesignationStatus is NULL.");
			return null;
		}
		if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION())) {
			return TYPE_STR_BY_ORIGINAL_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION())) {
			return TYPE_STR_BY_SUBSEQUENT_DESIGNATION;
		} else if (nameTypeDesignationStatus.equals(NameTypeDesignationStatus.MONOTYPY())) {
			return TYPE_STR_BY_MONOTYPY;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Name Type Designation Status not yet supported in PESI: "+ nameTypeDesignationStatus.getLabel());
			return null;
		}

	}

	/**
	 * 
	 * @param taxonBase
	 * @return
	 */
	public static Integer taxonBase2statusFk (TaxonBase<?> taxonBase){
		if (taxonBase == null) {
			logger.error("The given Taxon is NULL.");
			return null;
		}
		if (taxonBase.isInstanceOf(Taxon.class)){
			return T_STATUS_ACCEPTED;
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			return T_STATUS_SYNONYM;
		}else{
			logger.warn("Unknown ");
			return T_STATUS_UNRESOLVED;
		}
		//TODO 
//		public static int T_STATUS_PARTIAL_SYN = 3;
//		public static int T_STATUS_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_UNRESOLVED = 5;
//		public static int T_STATUS_ORPHANED = 6;
	}

	/**
	 * 
	 * @param taxonBase
	 * @return
	 */
	public static String taxonBase2statusCache (TaxonBase<?> taxonBase){
		if (taxonBase == null) {
			logger.error("The given Taxon is NULL.");
			return null;
		}
		if (taxonBase.isInstanceOf(Taxon.class)){
			return T_STATUS_STR_ACCEPTED;
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			return T_STATUS_STR_SYNONYM;
		}else{
			logger.warn("Unknown ");
			return T_STATUS_STR_UNRESOLVED;
		}
		//TODO 
//		public static int T_STATUS_STR_PARTIAL_SYN = 3;
//		public static int T_STATUS_STR_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_STR_UNRESOLVED = 5;
//		public static int T_STATUS_STR_ORPHANED = 6;
	}
		
	/**
	 * Returns the {@link SourceCategory SourceCategory} representation of the given {@link ReferenceType ReferenceType} in PESI.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The {@link SourceCategory SourceCategory} representation in PESI.
	 */
	public static Integer reference2SourceCategoryFK(ReferenceBase<?> reference) {
		if (reference == null){
			logger.error("The given Reference is NULL.");
			return null;
		} else if (reference.getType().equals(ReferenceType.Article)) {
			return REF_ARTICLE_IN_PERIODICAL;
		} else if (reference instanceof ISectionBase) {
			return REF_PART_OF_OTHER;
		} else if (reference.getType().equals(ReferenceType.Book)) {
			return REF_BOOK;
		} else if (reference.getType().equals(ReferenceType.Database)) {
			return REF_DATABASE;
		} else if (reference.getType().equals(ReferenceType.WebPage)) {
			return REF_WEBSITE;
		} else if (reference.getType().equals(ReferenceType.CdDvd)) {
			return REF_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Journal)) {
			return REF_JOURNAL;
		} else if (reference.getType().equals(ReferenceType.Generic)) {
			return REF_UNRESOLVED;
		} else if (reference.getType().equals(ReferenceType.PrintSeries)) {
			return REF_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Proceedings)) {
			return REF_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Patent)) {
			return REF_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.PersonalCommunication)) {
			return REF_INFORMAL;
		} else if (reference.getType().equals(ReferenceType.Report)) {
			return REF_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Thesis)) {
			return REF_NOT_APPLICABLE;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Reference type not yet supported in PESI: "+ reference.getClass().getSimpleName());
			return null;
		}
	}
	
	/**
	 * Returns the {@link SourceCategoryCache SourceCategoryCache}.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The {@link SourceCategoryCache SourceCategoryCache}.
	 */
	public static String getSourceCategoryCache(ReferenceBase<?> reference) {
		if (reference == null){
			logger.error("The given Reference is NULL.");
			return null;
		} else if (reference.getType().equals(ReferenceType.Article)) {
			return REF_STR_ARTICLE_IN_PERIODICAL;
		} else if (reference instanceof ISectionBase) {
			return REF_STR_PART_OF_OTHER;
		} else if (reference.getType().equals(ReferenceType.Book)) {
			return REF_STR_BOOK;
		} else if (reference.getType().equals(ReferenceType.Database)) {
			return REF_STR_DATABASE;
		} else if (reference.getType().equals(ReferenceType.WebPage)) {
			return REF_STR_WEBSITE;
		} else if (reference.getType().equals(ReferenceType.CdDvd)) {
			return REF_STR_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Journal)) {
			return REF_STR_JOURNAL;
		} else if (reference.getType().equals(ReferenceType.Generic)) {
			return REF_STR_UNRESOLVED;
		} else if (reference.getType().equals(ReferenceType.PrintSeries)) {
			return REF_STR_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Proceedings)) {
			return REF_STR_PUBLISHED;
		} else if (reference.getType().equals(ReferenceType.Patent)) {
			return REF_STR_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.PersonalCommunication)) {
			return REF_STR_INFORMAL;
		} else if (reference.getType().equals(ReferenceType.Report)) {
			return REF_STR_NOT_APPLICABLE;
		} else if (reference.getType().equals(ReferenceType.Thesis)) {
			return REF_STR_NOT_APPLICABLE;
		} else {
			//TODO Figure out a way to handle this gracefully.
			logger.warn("Reference type not yet supported in PESI: "+ reference.getClass().getSimpleName());
			return null;
		}
	}

	/**
	 * 
	 * @param status
	 * @return
	 */
	public static String nomStatus2NomStatusCache(NomenclaturalStatusType status) {
		if (status == null){
			logger.error("The given NomenclaturalStatusType is NULL.");
			return null;
		}
		if (status.equals(NomenclaturalStatusType.INVALID())) {return NAME_ST_STR_NOM_INVAL;
		}else if (status.equals(NomenclaturalStatusType.ILLEGITIMATE())) {return NAME_ST_STR_NOM_ILLEG;
		}else if (status.equals(NomenclaturalStatusType.NUDUM())) {return NAME_ST_STR_NOM_NUD;
		}else if (status.equals(NomenclaturalStatusType.REJECTED())) {return NAME_ST_STR_NOM_REJ;
		}else if (status.equals(NomenclaturalStatusType.REJECTED_PROP())) {return NAME_ST_STR_NOM_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED())) {return NAME_ST_STR_NOM_UTIQUE_REJ;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED_PROP())) {return NAME_ST_STR_NOM_UTIQUE_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.CONSERVED())) {return NAME_ST_STR_NOM_CONS;
	
		}else if (status.equals(NomenclaturalStatusType.CONSERVED_PROP())) {return NAME_ST_STR_NOM_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED())) {return NAME_ST_STR_ORTH_CONS;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP())) {return NAME_ST_STR_ORTH_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.SUPERFLUOUS())) {return NAME_ST_STR_NOM_SUPERFL;
		}else if (status.equals(NomenclaturalStatusType.AMBIGUOUS())) {return NAME_ST_STR_NOM_AMBIG;
		}else if (status.equals(NomenclaturalStatusType.PROVISIONAL())) {return NAME_ST_STR_NOM_PROVIS;
		}else if (status.equals(NomenclaturalStatusType.DOUBTFUL())) {return NAME_ST_STR_NOM_DUB;
		}else if (status.equals(NomenclaturalStatusType.NOVUM())) {return NAME_ST_STR_NOM_NOV;
	
		}else if (status.equals(NomenclaturalStatusType.CONFUSUM())) {return NAME_ST_STR_NOM_CONFUS;
		}else if (status.equals(NomenclaturalStatusType.ALTERNATIVE())) {return NAME_ST_STR_NOM_ALTERN;
		}else if (status.equals(NomenclaturalStatusType.COMBINATION_INVALID())) {return NAME_ST_STR_COMB_INVAL;
		}else if (status.equals(NomenclaturalStatusType.LEGITIMATE())) {return NAME_ST_STR_LEGITIMATE;
		
		// The following are non-existent in CDM
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_COMB_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_COMB_AND_STAT_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_NOM_AND_ORTH_CONS;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_NOM_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_SP_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_ALTERNATE_REPRESENTATION;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_TEMPORARY_NAME;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_STR_SPECIES_INQUIRENDA;

		//TODO
		}else {
			//TODO Exception
			logger.warn("NomStatus type not yet supported by PESI export: "+ status);
		return null;
	}
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 */
	public static Integer nomStatus2nomStatusFk (NomenclaturalStatusType status){
		if (status == null){
			logger.error("The given NomenclaturalStatusType is NULL.");
			return null;
		}
		if (status.equals(NomenclaturalStatusType.INVALID())) {return NAME_ST_NOM_INVAL;
		}else if (status.equals(NomenclaturalStatusType.ILLEGITIMATE())) {return NAME_ST_NOM_ILLEG;
		}else if (status.equals(NomenclaturalStatusType.NUDUM())) {return NAME_ST_NOM_NUD;
		}else if (status.equals(NomenclaturalStatusType.REJECTED())) {return NAME_ST_NOM_REJ;
		}else if (status.equals(NomenclaturalStatusType.REJECTED_PROP())) {return NAME_ST_NOM_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED())) {return NAME_ST_NOM_UTIQUE_REJ;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED_PROP())) {return NAME_ST_NOM_UTIQUE_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.CONSERVED())) {return NAME_ST_NOM_CONS;
		
		}else if (status.equals(NomenclaturalStatusType.CONSERVED_PROP())) {return NAME_ST_NOM_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED())) {return NAME_ST_ORTH_CONS;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP())) {return NAME_ST_ORTH_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.SUPERFLUOUS())) {return NAME_ST_NOM_SUPERFL;
		}else if (status.equals(NomenclaturalStatusType.AMBIGUOUS())) {return NAME_ST_NOM_AMBIG;
		}else if (status.equals(NomenclaturalStatusType.PROVISIONAL())) {return NAME_ST_NOM_PROVIS;
		}else if (status.equals(NomenclaturalStatusType.DOUBTFUL())) {return NAME_ST_NOM_DUB;
		}else if (status.equals(NomenclaturalStatusType.NOVUM())) {return NAME_ST_NOM_NOV;
		
		}else if (status.equals(NomenclaturalStatusType.CONFUSUM())) {return NAME_ST_NOM_CONFUS;
		}else if (status.equals(NomenclaturalStatusType.ALTERNATIVE())) {return NAME_ST_NOM_ALTERN;
		}else if (status.equals(NomenclaturalStatusType.COMBINATION_INVALID())) {return NAME_ST_COMB_INVAL;
		}else if (status.equals(NomenclaturalStatusType.LEGITIMATE())) {return NAME_ST_LEGITIMATE;
		
		// The following are non-existent in CDM
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_COMB_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_COMB_AND_STAT_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_NOM_AND_ORTH_CONS;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_NOM_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_SP_NOV_INED;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_ALTERNATE_REPRESENTATION;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_TEMPORARY_NAME;
//		}else if (status.equals(NomenclaturalStatusType.)) {return NAME_ST_SPECIES_INQUIRENDA;

		//TODO
		}else {
			//TODO Exception
			logger.warn("NomStatus type not yet supported by PESI export: "+ status);
			return null;
		}
	}
	
	/**
	 * Returns the RelTaxonQualifierCache for a given taxonRelation.
	 * @param relation
	 * @return
	 */
	public static String taxonRelation2RelTaxonQualifierCache(RelationshipBase<?,?,?> relation){
		if (relation == null) {
			logger.error("The given Relationship is NULL.");
			return null;
		}
		RelationshipTermBase<?> type = relation.getType();
		if (type.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			return STR_IS_TAXONOMICALLY_INCLUDED_IN;
		} else if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return STR_IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			return STR_IS_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			return STR_IS_HOMOTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			return STR_IS_HETEROTYPIC_SYNONYM_OF;
		}

		// The following have no equivalent attribute in CDM
//		IS_BASIONYM_FOR
//		IS_LATER_HOMONYM_OF
//		IS_REPLACED_SYNONYM_FOR
//		IS_VALIDATION_OF
//		IS_LATER_VALIDATION_OF
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_FIRST_PARENT_OF
//		IS_SECOND_PARENT_OF
//		IS_FEMALE_PARENT_OF
//		IS_MALE_PARENT_OF
//		IS_CONSERVED_AGAINST
//		IS_REJECTED_IN_FAVOUR_OF
//		IS_TREATED_AS_LATER_HOMONYM_OF
//		IS_ORTHOGRAPHIC_VARIANT_OF
//		IS_ALTERNATIVE_NAME_FOR
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED
//		IS_PRO_PARTE_SYNONYM_OF
//		IS_PARTIAL_SYNONYM_OF
//		IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF
//		IS_INFERRED_EPITHET_FOR
//		IS_INFERRED_GENUS_FOR
//		IS_POTENTIAL_COMBINATION_FOR

		return null;
	}
	
	/**
	 * Returns the RelTaxonQualifierFk for a TaxonRelation.
	 * @param relation
	 * @return
	 */
	public static Integer taxonRelation2RelTaxonQualifierFk(RelationshipBase<?,?,?> relation) {
		if (relation == null) {
			logger.error("The given Relationship is NULL.");
			return null;
		}
		RelationshipTermBase<?> type = relation.getType();
		if (type.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
			return IS_TAXONOMICALLY_INCLUDED_IN;
		} else if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
			return IS_MISAPPLIED_NAME_FOR;
		} else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {
			return IS_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
			return IS_HOMOTYPIC_SYNONYM_OF;
		} else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {
			return IS_HETEROTYPIC_SYNONYM_OF;
		}

		// The following have no equivalent attribute in CDM
//		IS_BASIONYM_FOR
//		IS_LATER_HOMONYM_OF
//		IS_REPLACED_SYNONYM_FOR
//		IS_VALIDATION_OF
//		IS_LATER_VALIDATION_OF
//		IS_TYPE_OF
//		IS_CONSERVED_TYPE_OF
//		IS_REJECTED_TYPE_OF
//		IS_FIRST_PARENT_OF
//		IS_SECOND_PARENT_OF
//		IS_FEMALE_PARENT_OF
//		IS_MALE_PARENT_OF
//		IS_CONSERVED_AGAINST
//		IS_REJECTED_IN_FAVOUR_OF
//		IS_TREATED_AS_LATER_HOMONYM_OF
//		IS_ORTHOGRAPHIC_VARIANT_OF
//		IS_ALTERNATIVE_NAME_FOR
//		HAS_SAME_TYPE_AS
//		IS_LECTOTYPE_OF
//		TYPE_NOT_DESIGNATED
//		IS_PRO_PARTE_SYNONYM_OF
//		IS_PARTIAL_SYNONYM_OF
//		IS_PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HOMOTYPIC_SYNONYM_OF
//		IS_PARTIAL_AND_HETEROTYPIC_SYNONYM_OF
//		IS_INFERRED_EPITHET_FOR
//		IS_INFERRED_GENUS_FOR
//		IS_POTENTIAL_COMBINATION_FOR

		return null;
	}
}
