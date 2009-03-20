/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.agent;

import java.util.List;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public interface IAgentDao extends IIdentifiableDao<AgentBase> {
	
	public List<Institution> getInstitutionByCode(String code);
	
//  TODO Currently Contact is a property of Person or Institution, but according 
//	to http://rs.tdwg.org/ontology/voc/Team, teams should have a Contact too - so
//  implementation of these methods is dependent upon a bit of refactoring in cdmlib-model
//	List<Address> getAddresses(Agent agent, Integer pageSize, Integer pageNumber);
//  int countAddresses(Agent agent);
	
	/**
	 * Return a List of the institutional memberships of a given person
	 * 
	 * @param person the person
	 * @param pageSize The maximum number of institutional memberships returned (can be null for all memberships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of InstitutionalMembership instances
	 */
	public List<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a count of institutional memberships held by a person
	 *  
	 * @param person the person
	 * @return a count of InstitutionalMembership instances
	 */
	public int countInstitutionalMemberships(Person person);
	
	/**
	 * Return a List of members of a given team
	 * 
	 * @param team the team
	 * @param pageSize The maximum number of people returned (can be null for all members)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of Person instances
	 */
	public List<Person> getMembers(Team team, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a count of members of a given team
	 * 
	 * @param team the team
	 * @return a count of Person instances
	 */
	public int countMembers(Team team);

	/**
	 * Return a count of addresses of a given agent
	 * 
	 * @param agent the agent
	 * @return a count of Address instances
	 */
	public Integer countAddresses(AgentBase agent);

	/**
	 * Return a List of addresses of a given agent
	 * 
	 * @param agent the agent
	 * @param pageSize The maximum number of addresses returned (can be null for all addresses)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of Address instances
	 */
	public List<Address> getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber);
}
