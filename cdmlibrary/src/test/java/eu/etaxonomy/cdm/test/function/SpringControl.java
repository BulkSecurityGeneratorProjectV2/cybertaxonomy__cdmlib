/* just for testing */


package eu.etaxonomy.cdm.test.function;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.*;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.DatabaseServiceHibernateImpl;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.NameServiceImpl;
import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.*;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;

import java.io.InputStream;
import java.util.*;



public class SpringControl {
	static Logger logger = Logger.getLogger(SpringControl.class);
	
	public void testBeanFactory (){
		String fileName = "editCdm.spring.cfg.xml";
		ClassPathResource cpr = new ClassPathResource(fileName);
		
		XmlBeanFactory  bf = new XmlBeanFactory(cpr);
		ITaxonNameDao tnDao = (ITaxonNameDao)bf.getBean("tnDao");
		TaxonNameBase tn = tnDao.findById(1);
		List<TaxonNameBase> tnList = tnDao.list(1000, 0);
		
		logger.warn(tn.getUuid());
	}
	
	public void testAppContext(){
		String fileName = "editCdm.spring.cfg.xml";
		
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(fileName);
		appContext.registerShutdownHook();
		
		String[] o = appContext.getBeanDefinitionNames();
		for (int i= 0; i<o.length;i++){
			System.out.println(o[i]);
		}
		
		ITaxonNameDao tnDao = (ITaxonNameDao) appContext.getBean( "tnDao" );
		TaxonNameBase tn = tnDao.findById(1);
		List<TaxonNameBase> tnList = tnDao.list(1000, 0);
		for (TaxonNameBase tn2: tnList){
			System.out.print(tn2.getUuid()+";");
		}
		appContext.close();
	}
	
	public void testAppController(){
		
		CdmApplicationController appCtr = new CdmApplicationController();
		DatabaseServiceHibernateImpl dbsi = (DatabaseServiceHibernateImpl)appCtr.getDatabaseService();
		//dbsi.fillTerms();
		
		
		logger.info("Create name objects...");
		NonViralName tn = new NonViralName(Rank.SPECIES());
		BotanicalName tn3 = new BotanicalName(Rank.SPECIES());
		
		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setUninomial("tn1-Genus1");
		tn3.setUninomial("tn3-genus");
		tn3.getUninomial();
		
		logger.info("Create new Author agent...");
		Agent team= new Agent();
		team.addPropertyChangeListener(listener);
		team.setTitleCache("AuthorAgent1");
		tn.setCombinationAuthorTeam(team);
		
		logger.info("Save objects ...");
		appCtr.getAgentService().saveAgent(team);
		appCtr.getNameService().saveTaxonName(tn);
		appCtr.getNameService().saveTaxonName(tn3);

		// load objects
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().getAllNames(1000, 0);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}
		appCtr.close();
	}

	public void testTermApi(){
		CdmApplicationController appCtr = new CdmApplicationController();
		ITermService ts = (ITermService)appCtr.getTermService();
		//DefinedTermBase dt = ts.getTermByUri("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//logger.warn(dt.toString());
		List<DefinedTermBase> dts = ts.listTerms();
		for (DefinedTermBase d: dts){
			logger.warn(d.toString());
		}
	}

	private void test(){
		System.out.println("Start");
		SpringControl sc = new SpringControl();
    	//testBeanFactory();
    	//testAppContext();
		testTermApi();
    	//testAppController();
    	System.out.println("\nEnd");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		SpringControl sc = new SpringControl();
    	sc.test();
	}

}
