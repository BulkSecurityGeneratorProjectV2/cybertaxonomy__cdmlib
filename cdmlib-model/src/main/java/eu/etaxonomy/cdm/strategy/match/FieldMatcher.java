/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 07.08.2009
 */
public class FieldMatcher extends FieldMatcherBase{
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(FieldMatcher.class);

	private IMatchStrategy matchStrategy;

	public static FieldMatcher NewInstance(Field field, MatchMode matchMode, IMatchStrategy matchStrategy){
		return new FieldMatcher(field.getName(), field, matchMode, matchStrategy);
	}

	public static FieldMatcher NewInstance(Field field, MatchMode matchMode){
		return new FieldMatcher(field.getName(), field, matchMode, null);
	}

	private FieldMatcher (String propertyname, Field field, MatchMode matchMode, IMatchStrategy matchStrategy){
		super(propertyname, field, matchMode);
		this.matchStrategy = matchStrategy;
	}

	/**
	 * @return the matchStrategy
	 */
	public IMatchStrategy getMatchStrategy() {
		return matchStrategy;
	}

	/**
	 * @param matchStrategy the matchStrategy to set
	 */
	public void setMatchStrategy(IMatchStrategyEqual matchStrategy) {
		this.matchStrategy = matchStrategy;
	}


}
