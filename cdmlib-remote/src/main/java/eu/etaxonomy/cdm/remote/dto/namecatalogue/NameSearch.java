/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.namecatalogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;

/**
 * The class representing the response from the CDM Remote Web Service API to a single name search query.
 * All information contained in this class originates from a call to {@link TaxonName taxon names}
 * <P>
 *
 * @author c.mathew
 * @since 17-Apr-2012 13:00:43
 */
public class NameSearch implements RemoteResponse {

    private NameSearch.NameSearchRequest request;
    private List<NameSearch.NameSearchResponse> response;

    public NameSearch() {
        this.response = new ArrayList<>();
    }

    public void setRequest(String q) {
        request = new NameSearchRequest();
        request.setQuery(q);
    }

    public NameSearchRequest getRequest() {
        return this.request;
    }

    public void addToResponseList(String title, String name, float score, String nameUuid, Set<TaxonBase> taxonBases,Set<TaxonBase> accTaxonBases) {

        NameSearch.NameSearchResponse res = responseWithtitle(title);
        if (res == null) {
            res = new NameSearch.NameSearchResponse();
            res.setTitle(title);
            res.setName(name);
            res.setScore(score);
            response.add(res);
        }
        res.addToNameUuids(nameUuid);
        for (TaxonBase tb : taxonBases) {
            res.addToTaxonConceptUuids(tb.getUuid().toString());
        }
        for (TaxonBase acctb : accTaxonBases) {
            res.addToAcceptedTaxontUuids(acctb.getUuid().toString());
        }
    }

    public void addToResponseList(String title,
    		String name,
    		float score,
    		String nameUuid,
    		String[] taxonBaseUuids,
    		String[] accTaxonUuids) {

        NameSearch.NameSearchResponse res = responseWithtitle(title);
        if (res == null) {
            res = new NameSearch.NameSearchResponse();
            res.setTitle(title);
            res.setName(name);
            res.setScore(score);
            response.add(res);
        }
        res.addToNameUuids(nameUuid);
        for (String tbuuid : taxonBaseUuids) {
            res.addToTaxonConceptUuids(tbuuid);
        }
        for (String acctbuuid : accTaxonUuids) {
            res.addToAcceptedTaxontUuids(acctbuuid);
        }
    }

    public List<NameSearch.NameSearchResponse> getResponse() {
        return this.response;
    }

    private NameSearch.NameSearchResponse responseWithtitle(String title) {
        for(NameSearch.NameSearchResponse nsres : response) {
            if(nsres.getTitle().trim().equals(title.trim())) {
                return nsres;
            }
        }
        return null;
    }

    public class NameSearchRequest {
        private String query;
        public NameSearchRequest() {
            this.query = "";
        }

        public void setQuery(String q) {
            this.query = q;
        }

        public String getQuery() {
            return this.query;
        }
    }

    public class NameSearchResponse {
        private String title;
        private String name;
        private float score;
		private Set<String> nameUuids;
        private Set<String> taxonConceptUuids;
        private Set<String> acceptedTaxontUuids;

        public NameSearchResponse() {
            title = "";
            name = "";
            score = 0;
            nameUuids = new HashSet<>();
            taxonConceptUuids = new HashSet<>();
            acceptedTaxontUuids = new HashSet<>();
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public float getScore() {
			return score;
		}

		public void setScore(float score) {
			this.score = score;
		}

        public void addToNameUuids(String nameUuid) {
            nameUuids.add(nameUuid);
        }

        public Set<String> getNameUuids() {
            return this.nameUuids;
        }

        public void addToTaxonConceptUuids(String taxonUuid) {
            taxonConceptUuids.add(taxonUuid);
        }

        public Set<String> getTaxonConceptUuids() {
            return this.taxonConceptUuids;
        }

        public void addToAcceptedTaxontUuids(String taxonUuid) {
            acceptedTaxontUuids.add(taxonUuid);
        }

        public Set<String> getAcceptedTaxonUuids() {
            return this.acceptedTaxontUuids;
        }
    }
}