package eu.etaxonomy.cdm.io.operation;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.operation.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseShortSecCacheStrategy;

@Component
public class CacheUpdaterWithNewCacheStrategy extends CacheUpdater {

    private static final long serialVersionUID = 8720272266844232502L;
    private static final Logger logger = Logger.getLogger(CacheUpdaterWithNewCacheStrategy.class);

	@Override
	protected void doInvoke(DefaultImportState<CacheUpdaterConfigurator> state) {
		CacheUpdaterConfigurator config = state.getConfig();

		//handle class list
		handleClassList(config.getClassList());

		return;
	}

	private boolean handleClassList(List<Class<? extends IdentifiableEntity>> classList) {
		boolean result = true;
		for (Class<? extends IdentifiableEntity> clazz : classList){
			//WE need to separate classes , because hibernate
			//returns multiple values for service.count() for e.g. IdentifableEntity.class
			//which leads to an exception
			result &= this.handleSingleTableClass(clazz);
		}
		return result;
	}


	private boolean handleSingleTableClass(Class<? extends IdentifiableEntity> clazz) {
		logger.warn("Updating class " + clazz.getSimpleName() + " ...");
		try {
			//TermBase
			if (DefinedTermBase.class.isAssignableFrom(clazz)){
				getTermService().updateCaches((Class) clazz, null, null, null);
			}else if (FeatureTree.class.isAssignableFrom(clazz)){
				getFeatureTreeService().updateCaches((Class) clazz, null, null, null);
			}else if (TermVocabulary.class.isAssignableFrom(clazz)){
				getVocabularyService().updateCaches((Class) clazz, null, null, null);
			}
			//DescriptionBase
			else if (DescriptionBase.class.isAssignableFrom(clazz)){
				getDescriptionService().updateCaches((Class) clazz, null, null, null);
			}
			//Media
			else if (Media.class.isAssignableFrom(clazz)){
				getMediaService().updateCaches((Class) clazz, null, null, null);
			}//TaxonBase
			else if (TaxonBase.class.isAssignableFrom(clazz)){
				TaxonBaseShortSecCacheStrategy<TaxonBase> cacheStrategy = new TaxonBaseShortSecCacheStrategy<TaxonBase>();
				getTaxonService().updateCaches((Class) clazz, null,cacheStrategy , null);
			}
			//IdentifiableMediaEntity
			else if (AgentBase.class.isAssignableFrom(clazz)){
				getAgentService().updateCaches((Class) clazz, null, null, null);
			}else if (Collection.class.isAssignableFrom(clazz)){
				getCollectionService().updateCaches((Class) clazz, null, null, null);
			}else if (Reference.class.isAssignableFrom(clazz)){
				getReferenceService().updateCaches((Class) clazz, null, null, null);
			}else if (SpecimenOrObservationBase.class.isAssignableFrom(clazz)){
				getOccurrenceService().updateCaches((Class) clazz, null, null, null);
			}
			//Sequence
			else if (Sequence.class.isAssignableFrom(clazz)){
				//TODO misuse TaxonServic for sequence update, use sequence service when it exists
				getTaxonService().updateCaches((Class) clazz, null, null, null);
			}
			//TaxonName
			else if (TaxonName.class.isAssignableFrom(clazz)){
				getNameService().updateCaches((Class) clazz, null, null, null);
			}
			//Classification
			else if (Classification.class.isAssignableFrom(clazz)){
				getClassificationService().updateCaches((Class) clazz, null, null, null);
			}
			//unknown class
			else {
				String warning = "Unknown identifable entity subclass + " + clazz.getName();
				logger.error(warning);
				return false;
				//getTaxonService().updateTitleCache((Class) clazz);
			}
			return true;
		} catch (Exception e) {
			String warning = "Exception occurred when trying to update class + " + clazz.getName();
			warning += " Exception was: " + e.getMessage();
			logger.error(warning);
			e.printStackTrace();
			return false;
		}
	}

}