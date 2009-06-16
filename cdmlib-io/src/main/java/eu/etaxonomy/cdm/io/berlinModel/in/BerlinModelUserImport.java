/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.User;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelUserImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelUserImport.class);

	private static int modCount = 100;
	private static final String dbTableName = "webAuthorisation";
	private static final String pluralString = "Users";
	
	public BerlinModelUserImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for "+pluralString+" not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	protected boolean doInvoke(BerlinModelImportState state){
		
		MapWrapper<User> userMap = (MapWrapper<User>)state.getStore(ICdmIO.USER_STORE);
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("start make "+pluralString+" ...");
		boolean success = true ;
		
		
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM "+dbTableName+" " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		String namespace = dbTableName;
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				try{
					if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
					
					//
					int authorisationId = rs.getInt("AuthorisationId");
					String username = rs.getString("Username");
					String pwd = rs.getString("Password");
					
					if (username != null){
						username = username.trim();
					}
					User user = User.NewInstance(username, pwd);
					
					Person person = Person.NewInstance();
					user.setPerson(person);
					
					dbAttrName = "RealName";
					cdmAttrName = "TitleCache";
					success &= ImportHelper.addStringValue(rs, person, dbAttrName, cdmAttrName);
	
	
					userMap.put(username, user);
					state.putUser(username, user);
				}catch(Exception ex){
					logger.error(ex.getMessage());
					ex.printStackTrace();
					success = false;
				}
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
		logger.info("save " + i + " "+pluralString + " ...");
		getUserService().saveAll(userMap.objects());

		logger.info("end make "+pluralString+" ..." + getSuccessString(success));;
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoUser();
	}

}
