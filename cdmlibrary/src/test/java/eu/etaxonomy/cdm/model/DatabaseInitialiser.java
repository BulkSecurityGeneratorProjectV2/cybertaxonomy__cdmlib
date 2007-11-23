package eu.etaxonomy.cdm.model;

import java.util.Random;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class DatabaseInitialiser {
	private static final Logger logger = Logger.getLogger(DatabaseInitialiser.class);

	private static SessionFactory factory;
	private static ClassPathXmlApplicationContext applicationContext;

	public DatabaseInitialiser() {
		applicationContext = new ClassPathXmlApplicationContext("appInitContext.xml");
		factory = (SessionFactory)applicationContext.getBean("sessionFactory");
	}

	public static Integer insertTaxon(String speciesname){
		logger.info("Populate database with a taxon");
		Taxon tax = new Taxon();
		TaxonNameBase nm = new BotanicalName(Rank.SPECIES());
		nm.setNameCache(speciesname);
		nm.setTitleCache(speciesname);
		tax.setName(nm);
		save(tax);
		return tax.getId();
	}

	private static void save(Object obj){
		Session s = factory.openSession();
		Transaction tx = s.beginTransaction();
		s.saveOrUpdate(obj);
		tx.commit();
		s.close();		
	}

	public static void main(String[] args) {
		DatabaseInitialiser dbInit = new DatabaseInitialiser();
		dbInit.insertTaxon("Abies alba");
		dbInit.insertTaxon("Polygala vulgaris");
	}
}
