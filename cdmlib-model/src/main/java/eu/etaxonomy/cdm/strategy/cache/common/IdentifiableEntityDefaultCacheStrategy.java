package eu.etaxonomy.cdm.strategy.cache.common;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.StrategyBase;

public class IdentifiableEntityDefaultCacheStrategy<T extends IdentifiableEntity> extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<T> {

	final static UUID uuid = UUID.fromString("85cbecb0-2020-11de-8c30-0800200c9a66");
	
	public String getTitleCache(T object) {
		return "";
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

}
