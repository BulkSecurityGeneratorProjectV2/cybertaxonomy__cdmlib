/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;



import eu.etaxonomy.cdm.model.common.ILoadableTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import au.com.bytecode.opencsv.CSVWriter;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * +/- current ISO codes. year given with each entry
 * http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html
 * http://www.davros.org/misc/iso3166.txt
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:02
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WaterbodyOrCountry", propOrder = {
//    "validPeriod",
    "continents"
})
@XmlRootElement(name = "WaterbodyOrCountry")
@Entity
public class WaterbodyOrCountry extends NamedArea {
//public class WaterbodyOrCountry extends DefinedTermBase<WaterbodyOrCountry> {

	private static final Logger logger = Logger.getLogger(WaterbodyOrCountry.class);
	/**
	 * 2 character ISO 3166 Country codes
	 */
	@XmlAttribute(name = "iso3166_A2")
	private char[] iso3166_A2 = new char[2];
	
//    @XmlElement(name = "ValidPeriod")
//	private TimePeriod validPeriod;
	
    @XmlElementWrapper(name = "Continents")
    @XmlElement(name = "Continent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<Continent> continents = new HashSet<Continent>();
	
	private static final UUID uuidAfghanistan = UUID.fromString("974ce01a-5bce-4be8-b728-a46869354960");
	private static final UUID uuidAlbaniaPeoplesSocialistRepublicof = UUID.fromString("238a6a93-8857-4fd6-af9e-6437c90817ac");
	private static final UUID uuidAlgeriaPeoplesDemocraticRepublicof = UUID.fromString("a14b38ac-e963-4c1a-85c2-de1f17f8c72a");
	private static final UUID uuidAmericanSamoa = UUID.fromString("4a071803-88aa-4367-9707-bb1f24ad4386");
	private static final UUID uuidAndorraPrincipalityof = UUID.fromString("7efd738f-33a1-4969-9d49-552571ffe935");
	private static final UUID uuidAngolaRepublicof = UUID.fromString("c48ca5e4-154a-46d6-af29-f722486bedba");
	private static final UUID uuidAnguilla = UUID.fromString("4a3b7f0d-0ff5-4691-a232-a2dc43ad4c56");
	private static final UUID uuidAntarcticaSouthOf60 = UUID.fromString("36aea55c-5d4c-4015-bb70-f15d9280c805");
	private static final UUID uuidAntiguaandBarbuda = UUID.fromString("fe425b94-f0e2-4e20-9e08-f28d53016347");
	private static final UUID uuidArgentinaArgentineRepublic = UUID.fromString("ee0a4820-914d-424c-8133-57efb3028741");
	private static final UUID uuidArmenia = UUID.fromString("7c685229-ce21-4dfd-a2c7-0932003f14ef");
	private static final UUID uuidAruba = UUID.fromString("f5a9fc99-52d5-4a54-9859-edede22cb39d");
	private static final UUID uuidAustraliaCommonwealthof = UUID.fromString("c22658e2-b1a9-4f4c-9ccd-affe0255efc8");
	private static final UUID uuidAustriaRepublicof = UUID.fromString("dfeb9102-7101-41cb-9449-bf5eae83cb5b");
	private static final UUID uuidAzerbaijanRepublicof = UUID.fromString("5189a180-f4ef-4a8a-9e90-36977c351960");
	private static final UUID uuidBahamasCommonwealthofthe = UUID.fromString("8b6851bf-b82e-4114-a99f-9b40ce0f3b2c");
	private static final UUID uuidBahrainKingdomof = UUID.fromString("7f7e8c06-a804-4efa-b02f-7679f929a760");
	private static final UUID uuidBangladeshPeoplesRepublicof = UUID.fromString("89752d76-d03a-46e1-9763-cc089f8a8e53");
	private static final UUID uuidBarbados = UUID.fromString("c870ad88-4393-4e76-a37d-39656c5d7ff2");
	private static final UUID uuidBelarus = UUID.fromString("66872923-5ae7-48be-b669-d9a2b7e4663c");
	private static final UUID uuidBelgiumKingdomof = UUID.fromString("fa27fe27-4966-4381-a341-3535f2b4309e");
	private static final UUID uuidBelize = UUID.fromString("6c3eeed7-00eb-4aa3-8e3c-2d8bc25f3338");
	private static final UUID uuidBeninPeoplesRepublicof = UUID.fromString("e6875306-892c-43d0-9aaa-9ac26e5d6551");
	private static final UUID uuidBermuda = UUID.fromString("88f4017e-27dc-4828-a2d7-0cf0637f1a7b");
	private static final UUID uuidBhutanKingdomof = UUID.fromString("35d9b61f-15d6-453d-8b01-8c786da241b3");
	private static final UUID uuidBoliviaRepublicof = UUID.fromString("8a18a774-0072-4678-8746-43de9ee066c4");
	private static final UUID uuidBosniaandHerzegovina = UUID.fromString("368be113-c0f2-444c-939c-65b544d19702");
	private static final UUID uuidBotswanaRepublicof = UUID.fromString("e00464af-d38e-4cd5-b5fe-50b27eace4ee");
	private static final UUID uuidBouvetIsland = UUID.fromString("65fa17a7-efa7-4be5-9d51-8b261c5217b7");
	private static final UUID uuidBrazilFederativeRepublicof = UUID.fromString("dccbe7f8-d5e3-48e5-bcbb-96886eb7108a");
	private static final UUID uuidBritishIndianOceanTerritory = UUID.fromString("996f912c-971f-40cb-88a4-1575226415b9");
	private static final UUID uuidBritishVirginIslands = UUID.fromString("5b71a5a2-0551-4563-b0aa-8aa259b90979");
	private static final UUID uuidBruneiDarussalam = UUID.fromString("7e6247b5-4145-454b-ad51-b60809a8a939");
	private static final UUID uuidBulgariaPeoplesRepublicof = UUID.fromString("51ddedf0-4646-46ba-9840-ab5513eec455");
	private static final UUID uuidBurkinaFaso = UUID.fromString("c4b22384-e26f-4a44-b641-64208f72ea25");
	private static final UUID uuidBurundiRepublicof = UUID.fromString("0d584b61-15b9-41fa-8cec-242f1f094417");
	private static final UUID uuidCambodiaKingdomof = UUID.fromString("485a4988-a3dd-43b8-9c18-e0351618056a");
	private static final UUID uuidCameroonUnitedRepublicof = UUID.fromString("30ba95a0-a951-46a0-aa67-e539475d4386");
	private static final UUID uuidCanada = UUID.fromString("5dc3dc6f-3816-44b3-b661-a4cf1528bae7");
	private static final UUID uuidCapeVerdeRepublicof = UUID.fromString("083ff0fc-9eea-4f1b-80c0-f203bd2890b8");
	private static final UUID uuidCaymanIslands = UUID.fromString("23264b59-fcc9-47a0-9f69-30a98757c121");
	private static final UUID uuidCentralAfricanRepublic = UUID.fromString("40d7ffa6-11cc-417c-adf7-f4acc03cca20");
	private static final UUID uuidChadRepublicof = UUID.fromString("d1ea5922-6bd2-4c63-b49a-259207c584a4");
	private static final UUID uuidChileRepublicof = UUID.fromString("9c41644f-4946-4586-b2a4-c8ec33dbe68b");
	private static final UUID uuidChinaPeoplesRepublicof = UUID.fromString("e0ed33bb-4afe-4994-81f3-b5f91655ff62");
	private static final UUID uuidChristmasIsland = UUID.fromString("e785a72e-2b51-42b9-bea0-888924906b3e");
	private static final UUID uuidCocosIslands = UUID.fromString("0994e57b-a0fa-4597-9098-8815235e9053");
	private static final UUID uuidColombiaRepublicof = UUID.fromString("cd334393-328c-4fb7-9600-bdca44c224d6");
	private static final UUID uuidComorosUnionofthe = UUID.fromString("3b52601e-e85f-415c-bc36-acc45717107f");
	private static final UUID uuidCongoDemocraticRepublicof = UUID.fromString("5a70a5b8-7264-48f1-b552-6fde52ae43f7");
	private static final UUID uuidCongoPeoplesRepublicof = UUID.fromString("5c0a6d1d-f5c1-4c92-b3cd-9a1c0cd0d9dc");
	private static final UUID uuidCookIslands = UUID.fromString("72f5df8d-ff1c-44af-9444-e368d770f36f");
	private static final UUID uuidCostaRicaRepublicof = UUID.fromString("aca508c7-2d49-4760-83cb-93b6ccce6751");
	private static final UUID uuidCoteDIvoireIvoryCoastRepublicofthe = UUID.fromString("5a6673d7-1580-4470-974c-b36c4584247f");
	private static final UUID uuidCubaRepublicof = UUID.fromString("229f0575-9035-4738-8741-f131cad59107");
	private static final UUID uuidCyprusRepublicof = UUID.fromString("4b13d6b8-7eca-4d42-8172-f2018051ca19");
	private static final UUID uuidCzechRepublic = UUID.fromString("56ee8c08-506d-4c27-9c31-db5344356ea3");
	private static final UUID uuidDenmarkKingdomof = UUID.fromString("dbf70b64-a47e-4339-ae07-828f9ff2b7d8");
	private static final UUID uuidDjiboutiRepublicof = UUID.fromString("8c80ca2b-e6e6-46bc-9f35-978a1a078a55");
	private static final UUID uuidDominicaCommonwealthof = UUID.fromString("c8ef3805-69dd-4e84-ab69-c813252910dd");
	private static final UUID uuidDominicanRepublic = UUID.fromString("1c2084e4-38cc-41d1-9d33-0360fed7c55d");
	private static final UUID uuidEcuadorRepublicof = UUID.fromString("e396160a-3554-4da8-ad40-cd7137c021d7");
	private static final UUID uuidEgyptArabRepublicof = UUID.fromString("3c4a2a5a-d3d7-4c82-a28f-2feaa7050c04");
	private static final UUID uuidElSalvadorRepublicof = UUID.fromString("2706e84c-a57d-40ab-aee4-dce25fe89211");
	private static final UUID uuidEquatorialGuineaRepublicof = UUID.fromString("7d0cee2b-086a-465e-afc3-0216bff7fd19");
	private static final UUID uuidEritrea = UUID.fromString("8394a73d-a0c6-481c-8e86-e05705891fac");
	private static final UUID uuidEstonia = UUID.fromString("b442614f-5bfa-4583-b87b-7c7c856015f1");
	private static final UUID uuidEthiopia = UUID.fromString("8866fa09-8ee2-4957-ad86-4e622085ef40");
	private static final UUID uuidFaeroeIslands = UUID.fromString("0b2933ea-cee6-4611-b52b-09d6fcdbcf9d");
	private static final UUID uuidFalklandIslands = UUID.fromString("8c667c52-70b6-447a-b4f2-dfa2d759d5f6");
	private static final UUID uuidFijiRepublicoftheFijiIslands = UUID.fromString("8a83a1e5-b648-4cea-86cd-7affaea817a7");
	private static final UUID uuidFinlandRepublicof = UUID.fromString("47bbb4b3-6f18-46f9-9eb6-6ec92c41fe84");
	private static final UUID uuidFranceFrenchRepublic = UUID.fromString("4c49d9d3-6bc3-481a-93c6-c8156cba25fe");
	private static final UUID uuidFrenchGuiana = UUID.fromString("38ba5ec2-913b-4894-a5bf-d55f3bd9d7a0");
	private static final UUID uuidFrenchPolynesia = UUID.fromString("7dadc5d4-d4e8-4ad6-bfa4-e8498a706778");
	private static final UUID uuidFrenchSouthernTerritories = UUID.fromString("590663d7-1b7e-4088-9407-2a589eb73fd4");
	private static final UUID uuidGabonGaboneseRepublic = UUID.fromString("d285a9f8-4349-4428-a848-c9aa45c4c8ab");
	private static final UUID uuidGambiaRepublicofthe = UUID.fromString("3dcc7fea-7785-4254-9947-f724e27a76fc");
	private static final UUID uuidGeorgia = UUID.fromString("af3f8bd9-1f5e-42cf-a0cc-f9199ab1bb89");
	private static final UUID uuidGermany = UUID.fromString("cbe7ce69-2952-4309-85dd-0d7d4a4830a1");
	private static final UUID uuidGhanaRepublicof = UUID.fromString("d4cf6c57-98ee-43b8-8d92-b510371dd151");
	private static final UUID uuidGibraltar = UUID.fromString("46764ae0-2d8d-461e-89d0-a1953edef02f");
	private static final UUID uuidGreeceHellenicRepublic = UUID.fromString("5b7c78d1-f068-4c4d-b2c9-9ac075b7169a");
	private static final UUID uuidGreenland = UUID.fromString("34bbe398-e0da-40bd-b16b-34a2e9fd3cc2");
	private static final UUID uuidGrenada = UUID.fromString("dda637e3-7742-4faf-bc05-e5d2c2d86a52");
	private static final UUID uuidGuadaloupe = UUID.fromString("2559330d-f79b-4273-b6db-e47abce1de6c");
	private static final UUID uuidGuam = UUID.fromString("264c71d7-91ef-4a5e-9ae6-49aac2a6ba3a");
	private static final UUID uuidGuatemalaRepublicof = UUID.fromString("54040dec-6f42-48cc-93d8-8b283b23e530");
	private static final UUID uuidGuineaRevolutionaryPeoplesRepcof = UUID.fromString("1b3cf756-b0c2-4e14-88af-d260b937d01f");
	private static final UUID uuidGuineaBissauRepublicof = UUID.fromString("2dbf1dc1-7428-4284-9090-8785a30f4e71");
	private static final UUID uuidGuyanaRepublicof = UUID.fromString("9cbe3428-0cfe-420e-a88e-eac196a16a37");
	private static final UUID uuidHaitiRepublicof = UUID.fromString("f1071b42-0247-4c4d-92a5-8bdf18099c50");
	private static final UUID uuidHeardandMcDonaldIslands = UUID.fromString("646a16d4-4a1f-47a0-a475-a19c605e04e0");
	private static final UUID uuidVaticanCityState = UUID.fromString("afebd310-0c8d-4601-b025-a06a1d195035");
	private static final UUID uuidHondurasRepublicof = UUID.fromString("c6684b89-3ea6-4922-9148-d74ff3ee33fd");
	private static final UUID uuidHongKongSpecialAdministrativeRegionofChina = UUID.fromString("5aa1c98c-9efd-443f-9c10-708f175d5cea");
	private static final UUID uuidHrvatska = UUID.fromString("a3acb45e-39ec-476b-bff2-7ff7e0383f7e");
	private static final UUID uuidHungaryHungarianPeoplesRepublic = UUID.fromString("4d8b56d0-ab74-437f-98e0-3b88ebaa8c89");
	private static final UUID uuidIcelandRepublicof = UUID.fromString("c7bf91f8-024c-4c04-9c0b-856a27b2d0ca");
	private static final UUID uuidIndiaRepublicof = UUID.fromString("a0b872f9-fc04-440d-ace3-edce8ea75e0b");
	private static final UUID uuidIndonesiaRepublicof = UUID.fromString("96eb663a-61b1-4a44-9017-0c4b1ea024d6");
	private static final UUID uuidIranIslamicRepublicof = UUID.fromString("14f148e0-a9cf-428d-a244-a9917aae974d");
	private static final UUID uuidIraqRepublicof = UUID.fromString("daf3de07-b1b8-47fa-8207-7e237ea30b7f");
	private static final UUID uuidIreland = UUID.fromString("376f61f8-6234-4e61-bc5e-d0d76393cfa0");
	private static final UUID uuidIsraelStateof = UUID.fromString("4c61dc3f-978d-4df9-9bd9-65089ee01dae");
	private static final UUID uuidItalyItalianRepublic = UUID.fromString("9404a588-503b-4033-acf5-ee4a47337ed0");
	private static final UUID uuidJamaica = UUID.fromString("528bede6-26db-47e6-b6cb-32f77ab5fef7");
	private static final UUID uuidJapan = UUID.fromString("a8be059a-6f1a-45aa-8019-f6bc3b81c691");
	private static final UUID uuidJordanHashemiteKingdomof = UUID.fromString("533b9709-1f97-43e6-8e12-68e116675c64");
	private static final UUID uuidKazakhstanRepublicof = UUID.fromString("3047567d-997d-491a-b0bc-d4b287f76fab");
	private static final UUID uuidKenyaRepublicof = UUID.fromString("9410b793-43fa-4205-bd24-5f92d392667f");
	private static final UUID uuidKiribatiRepublicof = UUID.fromString("d46f42ec-a520-49d8-ac87-cc8bccc91516");
	private static final UUID uuidKoreaDemocraticPeoplesRepublicof = UUID.fromString("0f2068a7-e284-417d-87ec-691c1e64c13c");
	private static final UUID uuidKoreaRepublicof = UUID.fromString("f81e0bbb-8984-431e-9962-de590a989fd3");
	private static final UUID uuidKuwaitStateof = UUID.fromString("00451db7-4f5a-4e5d-a6fe-955a8af306a0");
	private static final UUID uuidKyrgyzRepublic = UUID.fromString("fc3cb838-98f0-46b4-a5fe-5efafc121e95");
	private static final UUID uuidLaoPeoplesDemocraticRepublic = UUID.fromString("83b736b4-5049-4301-b370-ba19e7aa0403");
	private static final UUID uuidLatvia = UUID.fromString("c24a316c-cec1-47c2-a777-296ce67ce11a");
	private static final UUID uuidLebanonLebaneseRepublic = UUID.fromString("425b9cd2-0056-484a-9f77-5449215c65ba");
	private static final UUID uuidLesothoKingdomof = UUID.fromString("fbbbc46c-ed8f-45f5-87bc-062a7ee7ffdf");
	private static final UUID uuidLiberiaRepublicof = UUID.fromString("f40126ab-4cbe-409e-8f61-8911280e0857");
	private static final UUID uuidLibyanArabJamahiriya = UUID.fromString("b9115908-2937-45e3-8fb3-009136b013af");
	private static final UUID uuidLiechtensteinPrincipalityof = UUID.fromString("1bb6cf13-1286-40c8-bff8-1a18ef65e213");
	private static final UUID uuidLithuania = UUID.fromString("3a2a0f69-92b1-45ab-baa8-47cf48e7272b");
	private static final UUID uuidLuxembourgGrandDuchyof = UUID.fromString("5c481573-3d28-4c2c-87e1-acee4ccc64f1");
	private static final UUID uuidMacaoSpecialAdministrativeRegionofChina = UUID.fromString("927f5ae3-8d26-4794-9e5d-95cf9e0dfd03");
	private static final UUID uuidMacedoniatheformerYugoslavRepublicof = UUID.fromString("1cf135bb-cac7-4ba9-82dc-319ee41984c5");
	private static final UUID uuidMadagascarRepublicof = UUID.fromString("116be5e1-861e-4283-8689-f527a923b9d3");
	private static final UUID uuidMalawiRepublicof = UUID.fromString("61b41230-6365-433f-9454-5fea029f0e02");
	private static final UUID uuidMalaysia = UUID.fromString("5650de95-a90c-45c1-92bf-85d9b91911dd");
	private static final UUID uuidMaldivesRepublicof = UUID.fromString("5b932d64-3ca6-4691-881f-8b48bd2f3f15");
	private static final UUID uuidMaliRepublicof = UUID.fromString("2e201266-8535-4437-8870-a1d63745ec3d");
	private static final UUID uuidMaltaRepublicof = UUID.fromString("0ee9727a-36cb-40cb-9e65-cd4646c09d63");
	private static final UUID uuidMarshallIslands = UUID.fromString("2c507bb4-de73-4e3f-98ce-26bd2b0c016a");
	private static final UUID uuidMartinique = UUID.fromString("93ec114a-0486-4325-bef8-d1b5dea89419");
	private static final UUID uuidMauritaniaIslamicRepublicof = UUID.fromString("dfd0aaf0-4a73-4d41-a6d7-9cdfd01f4c40");
	private static final UUID uuidMauritius = UUID.fromString("719daa07-1dce-4473-8c40-b0efd644028c");
	private static final UUID uuidMayotte = UUID.fromString("48116e69-19a9-4169-9952-4ca46c586fa2");
	private static final UUID uuidMexicoUnitedMexicanStates = UUID.fromString("4ba4809b-3fa8-496d-a74d-80843a4740c8");
	private static final UUID uuidMicronesiaFederatedStatesof = UUID.fromString("70a91b6f-f196-4051-afdb-4e9aeaca490d");
	private static final UUID uuidMoldovaRepublicof = UUID.fromString("500f43b9-47c4-4c2a-af58-80adbc40c5f3");
	private static final UUID uuidMonacoPrincipalityof = UUID.fromString("4ef4c6cb-e02c-41a3-8d5f-74e8ae09ca71");
	private static final UUID uuidMongoliaMongolianPeoplesRepublic = UUID.fromString("8b7ebb83-9998-4efd-b97c-f1b7d3a7151f");
	private static final UUID uuidMontserrat = UUID.fromString("cd64d76f-6f2b-4e44-8d31-a2765100257b");
	private static final UUID uuidMoroccoKingdomof = UUID.fromString("d9c048d5-3220-439d-8af4-2a8ec3036e5b");
	private static final UUID uuidMozambiquePeoplesRepublicof = UUID.fromString("9f2b714e-6159-401b-9108-5d0b9413f6c8");
	private static final UUID uuidMyanmar = UUID.fromString("fd07e660-b3d6-46e7-bf7d-ec984e573c60");
	private static final UUID uuidNamibia = UUID.fromString("2c361180-c71c-4de0-8a98-0ff5a71bccaa");
	private static final UUID uuidNauruRepublicof = UUID.fromString("35d8c1ce-a2e9-43d6-9afe-582278a53d34");
	private static final UUID uuidNepalKingdomof = UUID.fromString("fa46cd94-68f9-4d0d-98a2-27dc6589658f");
	private static final UUID uuidNetherlandsAntilles = UUID.fromString("802d28f4-442f-47da-8e16-eb08d4de21b4");
	private static final UUID uuidNetherlandsKingdomofthe = UUID.fromString("5880f989-f10d-4a9c-aae8-4e6c7b212dd8");
	private static final UUID uuidNewCaledonia = UUID.fromString("587f11ed-27de-4751-9d04-b04f13f3f67c");
	private static final UUID uuidNewZealand = UUID.fromString("322c12c9-7b5a-4343-9861-23c93bbe62b4");
	private static final UUID uuidNicaraguaRepublicof = UUID.fromString("290da724-674d-4c99-8630-cb237162ae0a");
	private static final UUID uuidNigerRepublicofthe = UUID.fromString("1804792f-cccd-4f14-9e63-5c241bfd8429");
	private static final UUID uuidNigeriaFederalRepublicof = UUID.fromString("6dae052c-7477-485a-9d2c-63760991f9d8");
	private static final UUID uuidNiueRepublicof = UUID.fromString("e804fe1d-8246-481b-a293-d3c0b71d6abd");
	private static final UUID uuidNorfolkIsland = UUID.fromString("3d5afd71-90d7-459f-ade1-c8b65cbc7fe1");
	private static final UUID uuidNorthernMarianaIslands = UUID.fromString("43471298-1133-473e-b9b3-9152c5955177");
	private static final UUID uuidNorwayKingdomof = UUID.fromString("e136efdf-82bb-4528-be5c-881acd8315cb");
	private static final UUID uuidOmanSultanateof = UUID.fromString("36f43aca-3302-4abd-a7e3-f65ff050a087");
	private static final UUID uuidPakistanIslamicRepublicof = UUID.fromString("d42712ec-45aa-4811-9029-d38e5a607345");
	private static final UUID uuidPalau = UUID.fromString("02f4bc12-bc36-447b-b08c-e74e8fe25678");
	private static final UUID uuidPalestinianTerritoryOccupied = UUID.fromString("41f45c19-6910-470e-86fb-a3f426b8ca9c");
	private static final UUID uuidPanamaRepublicof = UUID.fromString("fd2ac965-bdb4-484a-9e4a-250f26aad030");
	private static final UUID uuidPapuaNewGuinea = UUID.fromString("3bc710b1-8b46-48e3-bdcd-54f64ca018cc");
	private static final UUID uuidParaguayRepublicof = UUID.fromString("e99f321f-664a-4a4b-90a9-1bdc98ea35f6");
	private static final UUID uuidPeruRepublicof = UUID.fromString("e4d92c3e-0f91-41d8-b10e-58c78b4c55ea");
	private static final UUID uuidPhilippinesRepublicofthe = UUID.fromString("8547697c-d80f-4531-b092-4c9fde373d7b");
	private static final UUID uuidPitcairnIsland = UUID.fromString("c3abd7ab-c953-4c0c-8bc1-e32f4a49775a");
	private static final UUID uuidPolandPolishPeoplesRepublic = UUID.fromString("579f8a7a-7fa5-4783-a8ec-cdc527781411");
	private static final UUID uuidPortugalPortugueseRepublic = UUID.fromString("f47bd6f5-c82b-4932-81ce-40345748536b");
	private static final UUID uuidPuertoRico = UUID.fromString("6471bdcc-b4cc-4a07-b946-dd15be7eec41");
	private static final UUID uuidQatarStateof = UUID.fromString("710d68a7-4a02-4d70-bbc8-22b904893429");
	private static final UUID uuidReunion = UUID.fromString("d85d98f6-3f09-44b0-a39b-0e2b6bf4746c");
	private static final UUID uuidRomaniaSocialistRepublicof = UUID.fromString("7d7c8221-4123-4ba2-88ef-25e7f10aafbc");
	private static final UUID uuidRussianFederation = UUID.fromString("504292b5-053a-4c6a-a690-db031ac02fc0");
	private static final UUID uuidRwandaRwandeseRepublic = UUID.fromString("27c2cc85-7c54-4356-b713-836c15f2da4e");
	private static final UUID uuidStHelena = UUID.fromString("626ec513-fddb-41f3-ab36-2ae2190a1bc1");
	private static final UUID uuidStKittsandNevis = UUID.fromString("777d19e2-d5e8-48e2-9a0f-cd95097e4e75");
	private static final UUID uuidStLucia = UUID.fromString("a3a55f1c-ea50-43df-b141-e8543eb20ebb");
	private static final UUID uuidStPierreandMiquelon = UUID.fromString("34f97908-18c5-4f67-b411-1b905161a330");
	private static final UUID uuidStVincentandtheGrenadines = UUID.fromString("dfe67a34-6a3a-4a56-8f90-3c007360f105");
	private static final UUID uuidSamoaIndependentStateof = UUID.fromString("7ad3f6bd-5e8a-467b-a481-1a523066b0e7");
	private static final UUID uuidSanMarinoRepublicof = UUID.fromString("e0c3ad69-a078-424f-a7d4-81025d190c91");
	private static final UUID uuidSaoTomeandPrincipeDemocraticRepublicof = UUID.fromString("a5369890-7a96-46bf-b91c-2c47d86660dd");
	private static final UUID uuidSaudiArabiaKingdomof = UUID.fromString("62fe4794-7fb0-4520-9493-b9150436393e");
	private static final UUID uuidSenegalRepublicof = UUID.fromString("e106a448-1205-4515-96f4-758e98176342");
	private static final UUID uuidSerbiaandMontenegro = UUID.fromString("94430436-eb97-4048-bf57-270c42b73fd3");
	private static final UUID uuidSeychellesRepublicof = UUID.fromString("3bb44fb7-0976-4e3d-94b9-439763b53711");
	private static final UUID uuidSierraLeoneRepublicof = UUID.fromString("88e731a7-5c80-4f29-8cf0-54acf70d6277");
	private static final UUID uuidSingaporeRepublicof = UUID.fromString("e063b480-c834-4e39-b7a9-74fc578c637b");
	private static final UUID uuidSlovakia = UUID.fromString("0349b9b5-865d-46ea-9750-ab71962d5106");
	private static final UUID uuidSlovenia = UUID.fromString("526b3fb4-08fc-4238-aa8b-e3217fae7214");
	private static final UUID uuidSolomonIslands = UUID.fromString("fc915f15-b2cf-40a7-8268-7c1f2744295a");
	private static final UUID uuidSomaliaSomaliRepublic = UUID.fromString("e8591331-3b75-4569-90a6-4aca1d1d9a53");
	private static final UUID uuidSouthAfricaRepublicof = UUID.fromString("508c9fcb-1b6c-4225-8e31-262a4df64a85");
	private static final UUID uuidSouthGeorgiaandtheSouthSandwichIslands = UUID.fromString("bf34dad1-63d1-4859-8818-da369616c470");
	private static final UUID uuidSpainSpanishState = UUID.fromString("e4d6474b-d903-4850-b51e-389f546b7601");
	private static final UUID uuidSriLankaDemocraticSocialistRepublicof = UUID.fromString("c7e74d0e-5c0d-4e3f-a19b-e072abbf0b92");
	private static final UUID uuidSudanDemocraticRepublicofthe = UUID.fromString("a47a922b-fa61-4164-8f6d-7cf2ba33ca8c");
	private static final UUID uuidSurinameRepublicof = UUID.fromString("6268a5c7-df0e-4230-8681-966798383dc4");
	private static final UUID uuidSvalbardJanMayenIslands = UUID.fromString("e47f9fe5-54c7-4c61-8c74-abc514749e41");
	private static final UUID uuidSwazilandKingdomof = UUID.fromString("bb006073-0088-4adf-9482-01e598bc3fd3");
	private static final UUID uuidSwedenKingdomof = UUID.fromString("8272e206-cb6f-499c-a1d9-7c581f5947c5");
	private static final UUID uuidSwitzerlandSwissConfederation = UUID.fromString("dd79f943-8237-4710-bc5f-acc1ea1a2dd8");
	private static final UUID uuidSyrianArabRepublic = UUID.fromString("f92c3ca4-3468-40b6-b387-d4677fca86d9");
	private static final UUID uuidTaiwanProvinceofChina = UUID.fromString("0fffb0e5-81b9-40be-be69-9aff204f51c4");
	private static final UUID uuidTajikistan = UUID.fromString("b78e4b96-6095-4316-bc4c-6bdec5593622");
	private static final UUID uuidTanzaniaUnitedRepublicof = UUID.fromString("8a519200-784a-495a-b0da-b3277913b880");
	private static final UUID uuidThailandKingdomof = UUID.fromString("6c35d8b5-a75b-4f17-8869-04cad4535bd8");
	private static final UUID uuidTimorLesteDemocraticRepublicof = UUID.fromString("77f9e6b5-a363-454c-996b-34aec2f10f99");
	private static final UUID uuidTogoTogoleseRepublic = UUID.fromString("75f15dd5-9998-4937-9a2c-b440798a6695");
	private static final UUID uuidTokelauIslands = UUID.fromString("b301d428-6936-4538-b5d3-778534b779e6");
	private static final UUID uuidTongaKingdomof = UUID.fromString("0abdcd01-09ff-42a8-b8ba-10458dca5ba9");
	private static final UUID uuidTrinidadandTobagoRepublicof = UUID.fromString("20ed7f03-1263-47fd-a4df-26fab6daae75");
	private static final UUID uuidTunisiaRepublicof = UUID.fromString("e121e4d7-e1aa-4f2e-9b9e-33f5109460d7");
	private static final UUID uuidTurkeyRepublicof = UUID.fromString("f7c15c55-d0b3-4eda-8961-582d5071df78");
	private static final UUID uuidTurkmenistan = UUID.fromString("442c0439-cf39-4c5a-96de-a99fe1a476cf");
	private static final UUID uuidTurksandCaicosIslands = UUID.fromString("d6c83f2f-5130-477a-994e-daa08b70352f");
	private static final UUID uuidTuvalu = UUID.fromString("30745e37-22c6-4b92-b955-85cb23f0526f");
	private static final UUID uuidUSVirginIslands = UUID.fromString("b5f9a299-41ea-414b-83d5-91518f64a481");
	private static final UUID uuidUgandaRepublicof = UUID.fromString("e74c11af-3a4e-4d13-9c2a-2e57d2954111");
	private static final UUID uuidUkraine = UUID.fromString("c44e49c7-a447-466d-ae4f-d290ab03ff18");
	private static final UUID uuidUnitedArabEmirates = UUID.fromString("a5b5e8ce-66c8-4ca0-a31b-473c90876108");
	private static final UUID uuidUnitedKingdomofGreatBritainAndNorthernIreland = UUID.fromString("5364e352-926f-4e07-9abb-2deea19346ec");
	private static final UUID uuidUnitedStatesMinorOutlyingIslands = UUID.fromString("4e88114b-e278-4816-ba7d-7bc17098c407");
	private static final UUID uuidUnitedStatesofAmerica = UUID.fromString("d9dacd9e-dd04-4641-957a-589bdb9fe5fb");
	private static final UUID uuidUruguayEasternRepublicof = UUID.fromString("baf46f00-7b05-4d88-b1cf-ce922f3ba262");
	private static final UUID uuidUzbekistan = UUID.fromString("86ebc56d-8b06-4bb1-a0f9-b15626c02fbd");
	private static final UUID uuidVanuatu = UUID.fromString("b4e16ad0-3cb7-4809-a5ae-9a143595c2a4");
	private static final UUID uuidVenezuelaBolivarianRepublicof = UUID.fromString("e8099497-0e51-41ca-85d7-d23b730d9c1a");
	private static final UUID uuidVietNamSocialistRepublicof = UUID.fromString("f9295319-572e-4c3d-9962-176a7802750b");
	private static final UUID uuidWallisandFutunaIslands = UUID.fromString("b4844963-f140-41b3-935d-58fd14df5878");
	private static final UUID uuidWesternSahara = UUID.fromString("fa9e1eb4-ee4c-4b13-82dd-ec42a8b7e627");
	private static final UUID uuidYemen = UUID.fromString("713e1840-ff18-4a96-bc32-3da2b048c77d");
	private static final UUID uuidZambiaRepublicof = UUID.fromString("90318040-d346-4c8f-be69-fa8ade0b12d9");
	private static final UUID uuidZimbabwe = UUID.fromString("aa96ca19-46ab-40ad-a494-e4842f13eb4c");
	private static final UUID uuidArcticOcean = UUID.fromString("aa96ca19-46ab-6365-af29-e4842f13eb4c");
	private static final UUID uuidAtlanticOcean = UUID.fromString("36aea55c-46ab-6365-af29-e4842f13eb4c");
	private static final UUID uuidPacificOcean = UUID.fromString("36aea55c-892c-6365-af29-e4842f13eb4c");
	private static final UUID uuidIndianOcean = UUID.fromString("36aea55c-892c-4114-af29-d4b287f76fab");
	private static final UUID uuidSouthernOcean = UUID.fromString("aa96ca19-892c-4114-af29-d4b287f76fab");
	private static final UUID uuidMediterraneanSea = UUID.fromString("aa96ca19-892c-4114-a494-d4b287f76fab");
	private static final UUID uuidBlackSea = UUID.fromString("d4cf6c57-892c-4114-bf57-96886eb7108a");
	private static final UUID uuidCaspianSea = UUID.fromString("d4cf6c57-892c-c953-a494-96886eb7108a");
	private static final UUID uuidRedSea = UUID.fromString("aa96ca19-46ab-c953-a494-96886eb7108a");
	private static final UUID uuidPersianGulf = UUID.fromString("aa96ca19-46ab-4114-a494-96886eb7108a");

	
	/*
	 * 
uuidAfghanistan
uuidAlbaniaPeoplesSocialistRepublicof
uuidAlgeriaPeoplesDemocraticRepublicof
uuidAmericanSamoa
uuidAndorraPrincipalityof
uuidAngolaRepublicof
uuidAnguilla
uuidAntarctica(theterritorySouthof60degS)
uuidAntiguaandBarbuda
uuidArgentinaArgentineRepublic
uuidArmenia
uuidAruba
uuidAustraliaCommonwealthof
uuidAustriaRepublicof
uuidAzerbaijanRepublicof
uuidBahamasCommonwealthofthe
uuidBahrainKingdomof
uuidBangladeshPeoplesRepublicof
uuidBarbados
uuidBelarus
uuidBelgiumKingdomof
uuidBelize
uuidBeninPeoplesRepublicof
uuidBermuda
uuidBhutanKingdomof
uuidBoliviaRepublicof
uuidBosniaandHerzegovina
uuidBotswanaRepublicof
uuidBouvetIsland(Bouvetoya)
uuidBrazilFederativeRepublicof
uuidBritishIndianOceanTerritory(ChagosArchipelago)
uuidBritishVirginIslands
uuidBruneiDarussalam
uuidBulgariaPeoplesRepublicof
uuidBurkinaFaso
uuidBurundiRepublicof
uuidCambodiaKingdomof
uuidCameroonUnitedRepublicof
uuidCanada
uuidCapeVerdeRepublicof
uuidCaymanIslands
uuidCentralAfricanRepublic
uuidChadRepublicof
uuidChileRepublicof
uuidChinaPeoplesRepublicof
uuidChristmasIsland
uuidCocos(Keeling)Islands
uuidColombiaRepublicof
uuidComorosUnionofthe
uuidCongoDemocraticRepublicof
uuidCongoPeoplesRepublicof
uuidCookIslands
uuidCostaRicaRepublicof
uuidCoteDIvoireIvoryCoastRepublicofthe
uuidCubaRepublicof
uuidCyprusRepublicof
uuidCzechRepublic
uuidDenmarkKingdomof
uuidDjiboutiRepublicof
uuidDominicaCommonwealthof
uuidDominicanRepublic
uuidEcuadorRepublicof
uuidEgyptArabRepublicof
uuidElSalvadorRepublicof
uuidEquatorialGuineaRepublicof
uuidEritrea
uuidEstonia
uuidEthiopia
uuidFaeroeIslands
uuidFalklandIslands(Malvinas)
uuidFijiRepublicoftheFijiIslands
uuidFinlandRepublicof
uuidFranceFrenchRepublic
uuidFrenchGuiana
uuidFrenchPolynesia
uuidFrenchSouthernTerritories
uuidGabonGaboneseRepublic
uuidGambiaRepublicofthe
uuidGeorgia
uuidGermany
uuidGhanaRepublicof
uuidGibraltar
uuidGreeceHellenicRepublic
uuidGreenland
uuidGrenada
uuidGuadaloupe
uuidGuam
uuidGuatemalaRepublicof
uuidGuineaRevolutionaryPeoplesRepcof
uuidGuinea-BissauRepublicof
uuidGuyanaRepublicof
uuidHaitiRepublicof
uuidHeardandMcDonaldIslands
uuidHolySee(VaticanCityState)
uuidHondurasRepublicof
uuidHongKongSpecialAdministrativeRegionofChina
uuidHrvatska(Croatia)
uuidHungaryHungarianPeoplesRepublic
uuidIcelandRepublicof
uuidIndiaRepublicof
uuidIndonesiaRepublicof
uuidIranIslamicRepublicof
uuidIraqRepublicof
uuidIreland
uuidIsraelStateof
uuidItalyItalianRepublic
uuidJamaica
uuidJapan
uuidJordanHashemiteKingdomof
uuidKazakhstanRepublicof
uuidKenyaRepublicof
uuidKiribatiRepublicof
uuidKoreaDemocraticPeoplesRepublicof
uuidKoreaRepublicof
uuidKuwaitStateof
uuidKyrgyzRepublic
uuidLaoPeoplesDemocraticRepublic
uuidLatvia
uuidLebanonLebaneseRepublic
uuidLesothoKingdomof
uuidLiberiaRepublicof
uuidLibyanArabJamahiriya
uuidLiechtensteinPrincipalityof
uuidLithuania
uuidLuxembourgGrandDuchyof
uuidMacaoSpecialAdministrativeRegionofChina
uuidMacedoniatheformerYugoslavRepublicof
uuidMadagascarRepublicof
uuidMalawiRepublicof
uuidMalaysia
uuidMaldivesRepublicof
uuidMaliRepublicof
uuidMaltaRepublicof
uuidMarshallIslands
uuidMartinique
uuidMauritaniaIslamicRepublicof
uuidMauritius
uuidMayotte
uuidMexicoUnitedMexicanStates
uuidMicronesiaFederatedStatesof
uuidMoldovaRepublicof
uuidMonacoPrincipalityof
uuidMongoliaMongolianPeoplesRepublic
uuidMontserrat
uuidMoroccoKingdomof
uuidMozambiquePeoplesRepublicof
uuidMyanmar
uuidNamibia
uuidNauruRepublicof
uuidNepalKingdomof
uuidNetherlandsAntilles
uuidNetherlandsKingdomofthe
uuidNewCaledonia
uuidNewZealand
uuidNicaraguaRepublicof
uuidNigerRepublicofthe
uuidNigeriaFederalRepublicof
uuidNiueRepublicof
uuidNorfolkIsland
uuidNorthernMarianaIslands
uuidNorwayKingdomof
uuidOmanSultanateof
uuidPakistanIslamicRepublicof
uuidPalau
uuidPalestinianTerritoryOccupied
uuidPanamaRepublicof
uuidPapuaNewGuinea
uuidParaguayRepublicof
uuidPeruRepublicof
uuidPhilippinesRepublicofthe
uuidPitcairnIsland
uuidPolandPolishPeoplesRepublic
uuidPortugalPortugueseRepublic
uuidPuertoRico
uuidQatarStateof
uuidReunion
uuidRomaniaSocialistRepublicof
uuidRussianFederation
uuidRwandaRwandeseRepublic
uuidSt.Helena
uuidSt.KittsandNevis
uuidSt.Lucia
uuidSt.PierreandMiquelon
uuidSt.VincentandtheGrenadines
uuidSamoaIndependentStateof
uuidSanMarinoRepublicof
uuidSaoTomeandPrincipeDemocraticRepublicof
uuidSaudiArabiaKingdomof
uuidSenegalRepublicof
uuidSerbiaandMontenegro
uuidSeychellesRepublicof
uuidSierraLeoneRepublicof
uuidSingaporeRepublicof
uuidSlovakia(SlovakRepublic)
uuidSlovenia
uuidSolomonIslands
uuidSomaliaSomaliRepublic
uuidSouthAfricaRepublicof
uuidSouthGeorgiaandtheSouthSandwichIslands
uuidSpainSpanishState
uuidSriLankaDemocraticSocialistRepublicof
uuidSudanDemocraticRepublicofthe
uuidSurinameRepublicof
uuidSvalbard&JanMayenIslands
uuidSwazilandKingdomof
uuidSwedenKingdomof
uuidSwitzerlandSwissConfederation
uuidSyrianArabRepublic
uuidTaiwanProvinceofChina
uuidTajikistan
uuidTanzaniaUnitedRepublicof
uuidThailandKingdomof
uuidTimor-LesteDemocraticRepublicof
uuidTogoTogoleseRepublic
uuidTokelau(TokelauIslands)
uuidTongaKingdomof
uuidTrinidadandTobagoRepublicof
uuidTunisiaRepublicof
uuidTurkeyRepublicof
uuidTurkmenistan
uuidTurksandCaicosIslands
uuidTuvalu
uuidUSVirginIslands
uuidUgandaRepublicof
uuidUkraine
uuidUnitedArabEmirates
uuidUnitedKingdomofGreatBritain&N.Ireland
uuidUnitedStatesMinorOutlyingIslands
uuidUnitedStatesofAmerica
uuidUruguayEasternRepublicof
uuidUzbekistan
uuidVanuatu
uuidVenezuelaBolivarianRepublicof
uuidVietNamSocialistRepublicof
uuidWallisandFutunaIslands
uuidWesternSahara
uuidYemen
uuidZambiaRepublicof
uuidZimbabwe
uuidArcticOcean
uuidAtlanticOcean
uuidPacificOcean
uuidIndianOcean
uuidSouthernOcean
uuidMediterraneanSea
uuidBlackSea
uuidCaspianSea
uuidRedSea
uuidPersianGulf

	 */

	public static final WaterbodyOrCountry ARGENTINA_ARGENTINE_REPUBLIC(){
		return getByUuid(uuidArgentinaArgentineRepublic);
	}
	
	
	public static final WaterbodyOrCountry getByUuid(UUID uuid){
		return (WaterbodyOrCountry) findByUuid(uuid);
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static WaterbodyOrCountry NewInstance(){
		return new WaterbodyOrCountry();
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static WaterbodyOrCountry NewInstance(String term, String label, String labelAbbrev){
		return new WaterbodyOrCountry(term, label, labelAbbrev);
	}
	

	public WaterbodyOrCountry() {
		super();
	}
	public WaterbodyOrCountry(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	@ManyToMany
    @JoinTable(
        name="DefinedTermBase_Continent"
    )
//	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Continent> getContinents() {
		return continents;
	}

	protected void setContinents(Set<Continent> continents) {
		this.continents = continents;
	}
	public void addContinents(Continent continent) {
		this.continents.add(continent);
	}
	public void removeContinents(Continent continent) {
		this.continents.remove(continent);
	}

	/**
	 * Get 2 character ISO 3166 Country code
	 * @return  a String representation of the ISO 3166 code
	 */
	//TODO create userDefinedType ?
	@Column(length=2)
	public String getIso3166_A2(){
		return String.valueOf(iso3166_A2);
	}

	/**
	 * Set 2 character ISO 3166 Country code 
	 * @param iso3166_A2  a String representation of the ISO 3166 code
	 */
	public void setIso3166_A2(String iso3166_A2){
		this.iso3166_A2 = iso3166_A2.toCharArray();
	}

//	public TimePeriod getValidPeriod(){
//		return this.validPeriod;
//	}
//
//	/**
//	 * 
//	 * @param validPeriod    validPeriod
//	 */
//	public void setValidPeriod(TimePeriod validPeriod){
//		this.validPeriod = validPeriod;
//	}

	
	@Override 
	public ILoadableTerm readCsvLine(List csvLine) {
	//public ILoadableTerm readCsvLine(List<String> csvLine) {
		ILoadableTerm result;
		// read UUID, URI, english label+description
		result = super.readCsvLine(csvLine);
		// iso codes extra
		this.iso3166_A2 = ((String)csvLine.get(4)).trim().toCharArray();
		//this.iso3166_A2=csvLine.get(4).trim().toCharArray();
		String[] continentList;
		String tmp = ((String)csvLine.get(5)).trim().toString();
		//String tmp = csvLine.get(5).trim().toString();
		if (tmp.length()>2){
			tmp=tmp.substring(1, tmp.length()-1);

			continentList=tmp.split(",");
			for (int i=0;i<continentList.length;i++){
				// 3b69f979-408c-4080-b573-0ad78a315610
				logger.debug("continent: "+continentList[i]);
//				System.out.println("continent: "+continentList[i]);
				Continent conti = new Continent();
				this.addContinents(conti.getByUuid(UUID.fromString(continentList[i])));
			}
		}
		return result;
	}
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[6];
		line[0] = getUuid().toString();
		line[1] = getUri();
		line[2] = getLabel(Language.ENGLISH());
		line[3] = getDescription();
		line[4] = this.getIso3166_A2().toString();
		line[5] = this.getContinents().toString();
		writer.writeNext(line);
	}	
}