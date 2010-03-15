package eu.etaxonomy.cdm.app.pesi.merging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.pesi.FaunaEuropaeaSources;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaImportConfigurator;
import eu.etaxonomy.cdm.io.pesi.merging.FaunaEuErmsMerging;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameComparator;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateProxyHelperExtended;

public class FaunaEuErmsMergingActivator {

	static final ICdmDataSource faunaEuropaeaSource = CdmDestinations.cdm_test_patricia();
	static final ICdmDataSource ermsSource = CdmDestinations.cdm_test_andreasM();
	
	//TODO hole aus beiden DB alle TaxonNameBases
	
	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.VALIDATE, false);

		return appCtrInit;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		FaunaEuErmsMergingActivator sc = new FaunaEuErmsMergingActivator();
		
		CdmApplicationController appCtrFaunaEu = sc.initDb(faunaEuropaeaSource);
		String sFileName = "c:\\test.csv";
		//CdmApplicationController appCtrErms = sc.initDb(ermsSource);
		List<String> propertyPaths = new ArrayList<String>();
		
		propertyPaths.add("sources");
		propertyPaths.add("taxonBases.*");
		propertyPaths.add("taxonBases.relationsFromThisTaxon");
		propertyPaths.add("taxonBases.taxonNodes.parent.taxon.name.*");
		//propertyPaths.add("taxa.relationsFromThisTaxon");
		//propertyPaths.add("taxa.taxonNodes.parent.taxon.name.*");
		//propertyPaths.add("taxonBases.taxonNodes.parent.taxon.relationsFromThisTaxon");
		/*propertyPaths.add("taxa");
		propertyPaths.add("taxa.taxonNodes.*");
		//propertyPaths.add("taxonBases.taxonNodes.$");
		propertyPaths.add("taxa.taxonNodes.parentNode.*");
		propertyPaths.add("taxa.taxonNodes.parentNode.taxon.*");
		propertyPaths.add("taxa.taxonNodes.parent.taxon.name.*");
		propertyPaths.add("hybridParentRelations");
		propertyPaths.add("hybridParentRelations.relatedFrom");
		propertyPaths.add("hybridParentRelations.relatedTo");*/
		
		/*get all Taxa of faunaEuropaea and Erms	
		List<TaxonBase> taxaOfFaunaEu = appCtrFaunaEu.getTaxonService().list(null, null, 0, null, null);
		List<TaxonBase> taxaOfErms = appCtrErms.getTaxonService().list(TaxonBase.class, null, 0, null, null);
		TaxonComparator taxComp = new TaxonComparator();
		Collections.sort(taxaOfFaunaEu, taxComp);
		Collections.sort(taxaOfErms, taxComp);
		*/
		List<TaxonNameBase> namesOfIdenticalTaxa = appCtrFaunaEu.getTaxonService().findIdenticalTaxonNames(propertyPaths);
		//System.err.println("first name: " + namesOfIdenticalTaxa.get(0) + " " + namesOfIdenticalTaxa.size());
		TaxonNameBase zooName = (TaxonNameBase)namesOfIdenticalTaxa.get(0);
		System.err.println(zooName + " nr of taxa " + namesOfIdenticalTaxa.size());
		TaxonNameComparator taxComp = new TaxonNameComparator();
		Collections.sort(namesOfIdenticalTaxa,taxComp);
		System.err.println(namesOfIdenticalTaxa.get(0) + " - " + namesOfIdenticalTaxa.get(1) + " - " + namesOfIdenticalTaxa.get(2));
		List<FaunaEuErmsMerging> mergingObjects = new ArrayList<FaunaEuErmsMerging>();
		FaunaEuErmsMerging mergeObject;
		TaxonNameBase faunaEuTaxName;
		TaxonNameBase ermsTaxName;
				
		mergingObjects= sc.createMergeObjects(namesOfIdenticalTaxa);
		
		sc.writeSameNamesdifferentAuthorToCsv(mergingObjects, sFileName + "1");
		sc.writeSameNamesdifferentStatusToCsv(mergingObjects, sFileName + "2");
		sc.writeSameNamesToCsVFile(mergingObjects, sFileName + "3");
		//sc.writeSameNamesdifferentPhylumToCsv(mergingObjects, sFileName + "3");
		
		
		System.out.println("End merging Fauna Europaea and Erms");
	}
	
	private boolean writeSameNamesToCsVFile(
			List<FaunaEuErmsMerging> mergingObjects, String string) {
	    try{
		FileWriter writer = new FileWriter(string);
	
	    //create Header
	    writer.append("same names but different phylum");
	    writer.append('\n');
		writer.append("id in Fauna Europaea");
		writer.append(';');
		writer.append("name");
		writer.append(';');
		writer.append("author");
		writer.append(';');
		writer.append("rank");
		writer.append(';');
		writer.append("state");
		writer.append(';');
		writer.append("phylum");
		writer.append(';');
		writer.append("parent");
		writer.append(';');
		writer.append("parent rank");
		writer.append(';');
		
		writer.append("id in Erms");
		writer.append(';');
		writer.append("name");
		writer.append(';');
		writer.append("author");
		writer.append(';');
		writer.append("rank");
		writer.append(';');
		writer.append("state");
		writer.append(';');
		writer.append("phylum");
		writer.append(';');
		writer.append("parent");
		writer.append(';');
		writer.append("parent rank");
		writer.append('\n');
		for (FaunaEuErmsMerging merging : mergingObjects){
	    	//TODO
			writeCsvLine(writer, merging) ;
			
		}
		writer.flush();
		writer.close();
	}
	catch(IOException e)
	{
	 return false;
	} 
	return true;
	}


	private boolean writeSameNamesdifferentPhylumToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		    writer.append("same names but different phylum");
		    writer.append('\n');
			writer.append("id in Fauna Europaea");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append(';');
			
			writer.append("id in Erms");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append('\n');
		    
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	//TODO
				if (!merging.getPhylumInErms().equals(merging.getPhylumInFaunaEu())){
					writeCsvLine(writer, merging) ;
				}
			}
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private boolean writeSameNamesdifferentRankToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		    writer.append("same names but different rank");
		    writer.append('\n');
			writer.append("id in Fauna Europaea");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append(';');
			
			writer.append("id in Erms");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append('\n');
			
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	
				if (!merging.getRankInErms().equals(merging.getRankInFaunaEu())){
					writeCsvLine(writer, merging);
				}
			}
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private boolean writeSameNamesdifferentStatusToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		    writer.append("same names but different status");
		    writer.append('\n');
			writer.append("id in Fauna Europaea");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append(';');
			
			writer.append("id in Erms");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append('\n');
		    
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	
				if (merging.isStatInErms()^merging.isStatInFaunaEu()){
					 writeCsvLine(writer, merging);
				}
			}
			
 
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private boolean writeSameNamesdifferentAuthorToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		    writer.append("same names but different authors");
		    writer.append('\n');
			writer.append("id in Fauna Europaea");
			writer.append(',');
			writer.append("name");
			writer.append(',');
			writer.append("author");
			writer.append(',');
			writer.append("rank");
			writer.append(',');
			writer.append("state");
			writer.append(',');
			writer.append("phylum");
			writer.append(',');
			
			writer.append("id in Erms");
			writer.append(',');
			writer.append("name");
			writer.append(',');
			writer.append("author");
			writer.append(',');
			writer.append("rank");
			writer.append(',');
			writer.append("state");
			writer.append(',');
			writer.append("phylum");
			writer.append('\n');
		    
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	
				if (!merging.getAuthorInErms().equals(merging.getAuthorInFaunaEu())){
					 writeCsvLine(writer, merging);
				}
			}
			
 
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private void writeCsvLine(FileWriter writer, FaunaEuErmsMerging merging) throws IOException{
		writer.append(merging.getIdInFaunaEu());
		writer.append(';');
		writer.append(merging.getNameCacheInFaunaEu());
		writer.append(';');
		writer.append(merging.getAuthorInFaunaEu());
		writer.append(';');
		writer.append(merging.getRankInFaunaEu());
		writer.append(';');
		if (merging.isStatInFaunaEu()){
			writer.append("accepted");
		}else{
			writer.append("synonym");
		}
		writer.append(';');
		writer.append(merging.getPhylumInFaunaEu());
		writer.append(';');
		writer.append(merging.getParentStringInFaunaEu());
		writer.append(';');
		writer.append(merging.getParentRankStringInFaunaEu());
		writer.append(';');
		
		writer.append(merging.getIdInErms());
		writer.append(';');
		writer.append(merging.getNameCacheInErms());
		writer.append(';');
		writer.append(merging.getAuthorInErms());
		writer.append(';');
		writer.append(merging.getRankInErms());
		writer.append(';');
		if (merging.isStatInErms()){
			writer.append("accepted");
		}else{
			writer.append("synonym");
		}
		
		writer.append(';');
		writer.append(merging.getPhylumInErms());
		writer.append(';');
		writer.append(merging.getParentStringInErms());
		writer.append(';');
		writer.append(merging.getParentRankStringInErms());
		writer.append('\n');
	}
	
	
	private List<FaunaEuErmsMerging> createMergeObjects(List<TaxonNameBase> names){
		
		List<FaunaEuErmsMerging> merge = new ArrayList<FaunaEuErmsMerging>();
		ZoologicalName zooName, zooName2;
		FaunaEuErmsMerging mergeObject;
		String idInSource1;
		for (int i = 0; i<names.size(); i=i+2){
			zooName = (ZoologicalName)names.get(i);
			zooName2 = (ZoologicalName)names.get(i+1);
			mergeObject = new FaunaEuErmsMerging();
			//TODO:�berpr�fen, ob die beiden Namen identisch sind und aus unterschiedlichen DB kommen
			
			Iterator sources = zooName.getSources().iterator();
			if (sources.hasNext()){
				IdentifiableSource source = (IdentifiableSource)sources.next();
				idInSource1 = source.getIdInSource();
				mergeObject.setIdInErms(idInSource1);
			}
			sources = zooName2.getSources().iterator();
			if (sources.hasNext()){
				IdentifiableSource source = (IdentifiableSource)sources.next();
				idInSource1 = source.getIdInSource();
				mergeObject.setIdInFaunaEu(idInSource1);
			}
			
			mergeObject.setNameCacheInErms(zooName.getNameCache());
			mergeObject.setNameCacheInFaunaEu(zooName2.getNameCache());
			
			mergeObject.setAuthorInErms(zooName.getAuthorshipCache());
			mergeObject.setAuthorInFaunaEu(zooName2.getAuthorshipCache());
			Set<Taxon> taxa = zooName.getTaxa();
			if (!taxa.isEmpty()){
				mergeObject.setStatInErms(true);
				Iterator taxaIterator = taxa.iterator();
				Taxon taxon = null;
				while (taxaIterator.hasNext()){
					taxon = (Taxon) taxaIterator.next();
					if (!taxon.isMisappliedName()){
						break;
					}
				}
				Set<TaxonNode> nodes = taxon.getTaxonNodes();
				Iterator taxonNodeIterator = nodes.iterator();
				TaxonNode node, parentNode = null;
				while (taxonNodeIterator.hasNext()){
					node = (TaxonNode)taxonNodeIterator.next();
					if (!node.isTopmostNode()){
						parentNode = (TaxonNode)node.getParent();
					}
				}
				//TODO: �ndern mit erweitertem Initializer..
				if (parentNode != null){
					ZoologicalName test = HibernateProxyHelper.deproxy(parentNode.getTaxon().getName(), ZoologicalName.class);
					String parentNameCache = test.getNameCache();
					mergeObject.setParentStringInErms(parentNameCache);
					mergeObject.setParentRankStringInErms(test.getRank().getLabel());
					System.err.println("parentName: " + parentNameCache);
				}
			}else{
				mergeObject.setStatInErms(false);
			}
			taxa = zooName2.getTaxa();
			if (!taxa.isEmpty()){
				mergeObject.setStatInFaunaEu(true);
				Iterator taxaIterator = taxa.iterator();
				Taxon taxon = null;
				while (taxaIterator.hasNext()){
					taxon = (Taxon) taxaIterator.next();
					if (!taxon.isMisappliedName()){
						break;
					}
				}
				Set<TaxonNode> nodes = taxon.getTaxonNodes();
				Iterator taxonNodeIterator = nodes.iterator();
				TaxonNode node, parentNode = null;
				while (taxonNodeIterator.hasNext()){
					node = (TaxonNode)taxonNodeIterator.next();
					if (!node.isTopmostNode()){
						parentNode = (TaxonNode)node.getParent();
					}
				}
				//TODO: �ndern mit erweitertem Initializer..
				if (parentNode != null){
					ZoologicalName test2 = (ZoologicalName)parentNode.getTaxon().getName();
					String parentNameCache = test2.getNameCache();
					mergeObject.setParentStringInFaunaEu(parentNameCache);
					mergeObject.setParentRankStringInFaunaEu(test2.getRank().getLabel());
					System.err.println("parentName: " + parentNameCache);
				}
			}else{
				mergeObject.setStatInErms(false);
			}
			taxa = zooName2.getTaxa();
			if (!taxa.isEmpty()){
				mergeObject.setStatInFaunaEu(true);
			}else{
				mergeObject.setStatInFaunaEu(false);
				
			}
			
			mergeObject.setRankInErms(zooName.getRank().getLabel());
			mergeObject.setRankInFaunaEu(zooName2.getRank().getLabel());
			
			//TODO:Phyllum des TaxonNames ermitteln..(Funktion von Marc??)
			/*
			 * mergeObject.setPhylumInErms(phylumInErms);
			 * mergeObject.setPhylumInFaunaEu(phylumInFaunaEu);
			 * 
			 */
			
			
			//set parent informations
			
			
			/*
			Set<HybridRelationship> parentRelations = zooName.getParentRelationships();
			Iterator parentIterator = parentRelations.iterator();
			HybridRelationship parentRel;
			ZoologicalName parentName;
			while (parentIterator.hasNext()){
				parentRel = (HybridRelationship)parentIterator.next();
				parentName = (ZoologicalName)parentRel.getParentName();
				mergeObject.setParentRankStringInErms(parentName.getRank().getLabel());
				mergeObject.setParentStringInErms(parentName.getNameCache());
			}
			
			parentRelations = zooName2.getParentRelationships();
			parentIterator = parentRelations.iterator();
		
			while (parentIterator.hasNext()){
				parentRel = (HybridRelationship)parentIterator.next();
				parentName = (ZoologicalName)parentRel.getParentName();
				mergeObject.setParentRankStringInFaunaEu(parentName.getRank().getLabel());
				mergeObject.setParentStringInFaunaEu(parentName.getNameCache());
			}*/
			merge.add(mergeObject);
		}
		
		return merge;
		
		
	}
	
}
