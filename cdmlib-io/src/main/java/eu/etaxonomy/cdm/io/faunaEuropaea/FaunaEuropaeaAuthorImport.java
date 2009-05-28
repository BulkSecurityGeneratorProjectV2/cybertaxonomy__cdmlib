/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaAuthorImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaAuthorImport.class);

	private static int modCount = 1000;

	public FaunaEuropaeaAuthorImport(){
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("No checking for Authors not implemented");
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 

		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		Source source = fauEuConfig.getSource();
		
		String namespace = "AuthorTeam";
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making Authors ..."); }
		
		try {

			String strQuery = 
				" SELECT *  " +
				" FROM author " ;
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			while (rs.next()) {

				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("Authors handled: " + (i-1)); 
					}
				}

				int authorId = rs.getInt("aut_id");
				String authorName = rs.getString("aut_name");

				TeamOrPersonBase<Team> author = null;

				try {
					author = Team.NewInstance();
					author.setTitleCache(authorName);

					ImportHelper.setOriginalSource(author, fauEuConfig.getSourceReference(), authorId, namespace);

					if (!authorStore.containsId(authorId)) {
						if (author == null) {
							logger.warn("Reference is null");
						}
						authorStore.put(authorId, author);
					} else {
						logger.warn("Reference with duplicated aut_id (" + authorId + 
						") not imported.");
					}
				} catch (Exception e) {
					logger.warn("An exception occurred when creating author with id " + authorId + 
					". Author could not be saved.");
				}
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving authors ..."); }

			// save authors
			getAgentService().saveAgentAll(authorStore.objects());

			if(logger.isInfoEnabled()) { logger.info("End making authors ..."); }

			return true;

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return !config.isDoAuthors();
	}

}
