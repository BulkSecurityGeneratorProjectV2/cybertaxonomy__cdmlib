/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represent page based subsets of the total set of results returned by a search query.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 15.02.2008 15:26:06
 *
 */
public class ResultSetPageSTO<T extends BaseSTO> {

	/**
	 * Total number of matching records. Maybe distributed across several pages
	 */
	private long totalResultsCount;
	/**
	 * Total number of pages
	 */
	private int totalPageCount;
	/**
	 * Then number of items per page. Defaults to 25
	 */
	private int pageSize = 25;
	/**
	 * The number of this page. First default page has index 1.
	 */
	private int pageNumber = 1;
	/**
	 * The number of records on this page
	 */
	private int resultsOnPage;
	/**
	 * A list containing the items for this result page.
	 * The number of items will not exceed the {@link #pageSize} value.
	 * The last page may contain less items. 
	 */
	private List<T> results = new ArrayList<T>();
	
	
	public long getTotalResultsCount() {
		return totalResultsCount;
	}
	public void setTotalResultsCount(long totalResultsCount) {
		this.totalResultsCount = totalResultsCount;
		this.totalPageCount = (int) Math.ceil(totalResultsCount / pageSize);
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
		this.resultsOnPage = (int) (totalResultsCount-(pageNumber-1)*pageSize);
	}
	public int getTotalPageCount() {
		return totalPageCount;
	}
	public List<T> getResults() {
		return results;
	}
	public int getResultsOnPage() {
		return resultsOnPage;
	}
}
