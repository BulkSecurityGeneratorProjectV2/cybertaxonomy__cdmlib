/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;



/**
 * @author a.mueller
 *
 */
@Service
@Transactional
public class AgentServiceImpl extends IdentifiableServiceBase<AgentBase,IAgentDao> implements IAgentService {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);
	

	/**
	 * Constructor
	 */
	public AgentServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load AgentService Bean"); }
	}

	public List<AgentBase> findAgentsByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}

	public AgentBase getAgentByUuid(UUID uuid) {
		return dao.findByUuid(uuid);
	}

	public UUID saveAgent(AgentBase agent) {
		return super.saveCdmObject(agent);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, AgentBase> saveAgentAll(Collection<? extends AgentBase> agentCollection){
		return saveCdmObjectAll(agentCollection);
	}

	
	public List<AgentBase> getAllAgents(int limit, int start){
		return dao.list(limit, start);
	}
	
	public List<Institution> searchInstitutionByCode(String code) {
		return dao.getInstitutionByCode(code);
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
		
	}

	@Autowired
	protected void setDao(IAgentDao dao) {
		assert dao != null;
		this.dao = dao;
	}

	public Pager<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countInstitutionalMemberships(person);
		
		List<InstitutionalMembership> results = new ArrayList<InstitutionalMembership>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getInstitutionalMemberships(person, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<InstitutionalMembership>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Person> getMembers(Team team, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countMembers(team);
			
		List<Person> results = new ArrayList<Person>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getMembers(team, pageSize, pageNumber); 
		}
			
		return new DefaultPagerImpl<Person>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Address> getAddresses(AgentBase agent, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countAddresses(agent);
		
		List<Address> results = new ArrayList<Address>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getAddresses(agent, pageSize, pageNumber); 
		}
			
		return new DefaultPagerImpl<Address>(pageNumber, numberOfResults, pageSize, results);
	}

	
}
