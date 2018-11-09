/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.TermDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;
import eu.etaxonomy.cdm.persistence.dao.common.IRepresentationDao;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl extends IdentifiableServiceBase<DefinedTermBase,IDefinedTermDao> implements ITermService{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermServiceImpl.class);

	private ILanguageStringDao languageStringDao;

	@Autowired
	private IVocabularyService vocabularyService;

	@Autowired
	@Qualifier("langStrBaseDao")
	private ILanguageStringBaseDao languageStringBaseDao;
	private IRepresentationDao representationDao;

	@Autowired
	public void setLanguageStringDao(ILanguageStringDao languageStringDao) {
		this.languageStringDao = languageStringDao;
	}

	@Autowired
	public void setRepresentationDao(IRepresentationDao representationDao) {
		this.representationDao = representationDao;
	}

	@Override
    @Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}

	@Override
	public <T extends DefinedTermBase> List<T> listByTermType(TermType termType, Integer limit, Integer start,
	        List<OrderHint> orderHints, List<String> propertyPaths) {
	    return dao.listByTermType(termType, limit, start, orderHints, propertyPaths);
	}

	@Override
	public DefinedTermBase getByUri(URI uri) {
		return dao.findByUri(uri);
	}

	@Override
	public Language getLanguageByIso(String iso639) {
	    return dao.getLanguageByIso(iso639);
	}

	@Override
	public Language getLanguageByLabel(String label) {
	    return Language.getLanguageByLabel(label);
	}

	@Override
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales){
		return dao.getLanguagesByLocale(locales);
	}

	@Override
    public <TERM extends DefinedTermBase> TERM findByIdInVocabulary(String id, UUID vocabularyUuid, Class<TERM> clazz) throws IllegalArgumentException {
        List<TERM> list = dao.getDefinedTermByIdInVocabulary(id, vocabularyUuid, clazz, null, null);
		if (list.isEmpty()){
			return null;
		}else if (list.size() == 1){
			return list.get(0);
		}else{
			String message = "There is more then 1 (%d) term with the same id in vocabulary. This is forbidden. Check the state of your database.";
			throw new IllegalStateException(String.format(message, list.size()));
		}
	}


	@Override
	public NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation) {
		if (StringUtils.isBlank(tdwgAbbreviation)){ //TDWG areas should always have a label
			return null;
		}
		List<NamedArea> list = dao.getDefinedTermByIdInVocabulary(tdwgAbbreviation, NamedArea.uuidTdwgAreaVocabulary, NamedArea.class, null, null);
		if (list.isEmpty()){
			return null;
		}else if (list.size() == 1){
			return list.get(0);
		}else{
			String message = "There is more then 1 (%d) TDWG area with the same abbreviated label. This is forbidden. Check the state of your database.";
			throw new IllegalStateException(String.format(message, list.size()));
		}
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countGeneralizationOf(definedTerm);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getGeneralizationOf(definedTerm, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> getIncludes(Collection<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countIncludes(definedTerms);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getIncludes(definedTerms, pageSize, pageNumber,propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
        long numberOfResults = dao.countMedia(definedTerm);

		List<Media> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMedia(definedTerm, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countPartOf(definedTerms);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getPartOf(definedTerms, pageSize, pageNumber, propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {
		long numberOfResults = dao.count(level, type);

		List<NamedArea> results = new ArrayList<>();
		if (numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(level, type, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> findByRepresentationText(String label, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countDefinedTermByRepresentationText(label,clazz);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDefinedTermByRepresentationText(label, clazz, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> findByRepresentationAbbreviation(String abbrev, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countDefinedTermByRepresentationAbbrev(abbrev,clazz);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDefinedTermByRepresentationAbbrev(abbrev, clazz, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public List<LanguageString> getAllLanguageStrings(int limit, int start) {
		return languageStringDao.list(limit, start);
	}

	@Override
	public List<Representation> getAllRepresentations(int limit, int start) {
		return representationDao.list(limit,start);
	}

	@Override
	public UUID saveLanguageData(LanguageStringBase languageData) {
		return languageStringBaseDao.save(languageData).getUuid();
	}


	/** @deprecated use {@link #delete(DefinedTermBase, TermDeletionConfigurator)} instead
	 * to allow DeleteResult return type*/
	@Override
	@Deprecated
	public DeleteResult delete(DefinedTermBase term){
		DeleteResult result = new DeleteResult();

		TermDeletionConfigurator defaultConfig = new TermDeletionConfigurator();
		result = delete(term, defaultConfig);
		return result;
	}

	@Override
	@Deprecated
	@Transactional(readOnly = false)
	public DeleteResult delete(UUID termUuid){
	    DeleteResult result = new DeleteResult();

	    TermDeletionConfigurator defaultConfig = new TermDeletionConfigurator();
	    result = delete(dao.load(termUuid), defaultConfig);
	    return result;
	}

	@Override
	public DeleteResult delete(DefinedTermBase term, TermDeletionConfigurator config){
		if (config == null){
			config = new TermDeletionConfigurator();
		}
//		boolean isInternal = config.isInternal();

		Set<DefinedTermBase> termsToSave = new HashSet<DefinedTermBase>();

		DeleteResult result = isDeletable(term.getUuid(), config);
		//CdmBase.deproxy(dao.merge(term), DefinedTermBase.class);
		try {
			//generalization of
			Set<DefinedTermBase> specificTerms = term.getGeneralizationOf();
			if (specificTerms.size()>0){
				if (config.isDeleteGeneralizationOfRelations()){
					DefinedTermBase generalTerm = term.getKindOf();
					for (DefinedTermBase specificTerm: specificTerms){
						term.removeGeneralization(specificTerm);
						if (generalTerm != null){
							generalTerm.addGeneralizationOf(specificTerm);
							termsToSave.add(generalTerm);
						}
					}
				}else{
					//TODO Exception type
					String message = "This term has specifing terms. Move or delete specifiing terms prior to delete or change delete configuration.";
					result.addRelatedObjects(specificTerms);
					result.setAbort();
					Exception ex = new DataChangeNoRollbackException(message);
					result.addException(ex);
				}
			}

			//kind of
			DefinedTermBase generalTerm = term.getKindOf();
			if (generalTerm != null){
				if (config.isDeleteKindOfRelations()){
					generalTerm.removeGeneralization(term);
				}else{
					//TODO Exception type
					String message = "This term is kind of another term. Move or delete kind of relationship prior to delete or change delete configuration.";
					result.addRelatedObject(generalTerm);
					result.setAbort();
					DataChangeNoRollbackException ex = new DataChangeNoRollbackException(message);
					result.addException(ex);
					throw ex;
				}
			}

			//part of
			DefinedTermBase parentTerm = term.getPartOf();
			if (parentTerm != null){
				if (! config.isDeletePartOfRelations()){
					//TODO Exception type
					String message = "This term is included in another term. Remove from parent term prior to delete or change delete configuration.";
					result.addRelatedObject(parentTerm);
					result.setAbort();
					DataChangeNoRollbackException ex = new DataChangeNoRollbackException(message);
					result.addException(ex);
				}
			}


			//included in
			Set<DefinedTermBase> includedTerms = term.getIncludes();
			if (includedTerms.size()> 0){
//				if (config.isDeleteIncludedTerms()){
//					for (DefinedTermBase includedTerm: includedTerms){
//						config.setCheck(true);
//						DeleteResult includedResult = this.delete(includedTerm, config);
////						config.setCheck(isCheck);
//						result.includeResult(includedResult);
//					}
//				}else
					if (config.isDeleteIncludedRelations()){
					DefinedTermBase parent = term.getPartOf();
					for (DefinedTermBase includedTerm: includedTerms){
						term.removeIncludes(includedTerm);
						if (parent != null){
							parent.addIncludes(includedTerm);
							termsToSave.add(parent);
						}
					}
				}else{
					//TODO Exception type
					String message = "This term includes other terms. Move or delete included terms prior to delete or change delete configuration.";
					result.addRelatedObjects(includedTerms);
					result.setAbort();
					Exception ex = new DataChangeNoRollbackException(message);
					result.addException(ex);
				}
			}

			//part of
			if (parentTerm != null){
				if (config.isDeletePartOfRelations()){
					parentTerm.removeIncludes(term);
					termsToSave.add(parentTerm);
				}else{
					//handelede before "included in"
				}
			}

//			relatedObjects;


			if (result.isOk()){
				TermVocabulary voc = term.getVocabulary();
				if (voc!= null){
					voc.removeTerm(term);
				}
				//TODO save voc
				if (true /*!config.isInternal()*/){
					dao.delete(term);
					result.addDeletedObject(term);
					dao.saveOrUpdateAll(termsToSave);
//					for (DeleteResult.PersistPair persistPair : result.getObjectsToDelete()){
//						persistPair.dao.delete(persistPair.objectToPersist);
//					}
//					for (DeleteResult.PersistPair persistPair : result.getObjectsToSave()){
//						persistPair.dao.saveOrUpdate(persistPair.objectToPersist);
//					}

				}
			}
		} catch (DataChangeNoRollbackException e) {
			result.setStatus(Status.ERROR);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public DeleteResult delete(UUID termUuid, TermDeletionConfigurator config){
	    return delete(dao.load(termUuid), config);
	}

	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends DefinedTermBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<DefinedTermBase> cacheStrategy, IProgressMonitor monitor) {
		//TODO shouldnt this be TermBase instead of DefinedTermBase
		if (clazz == null){
			clazz = DefinedTermBase.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}

	@Override
    public DeleteResult isDeletable(UUID termUuid, DeleteConfiguratorBase config){
        DeleteResult result = new DeleteResult();
        TermBase term = load(termUuid);
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(term);
        if (references != null){
            result.addRelatedObjects(references);
            Iterator<CdmBase> iterator = references.iterator();
            CdmBase ref;
            while (iterator.hasNext()){
                ref = iterator.next();
                if (ref instanceof TermVocabulary){
                    result.getRelatedObjects().remove(ref);
                }else{

                    String message = "An object of " + ref.getClass().getName() + " with ID " + ref.getId() + " is referencing the object" ;
                    result.addException(new ReferencedObjectUndeletableException(message));
                    result.setAbort();
                }

            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public Map<UUID, Representation> saveOrUpdateRepresentations(Collection<Representation> representations){
        return representationDao.saveOrUpdateAll(representations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UuidAndTitleCache<NamedArea>> getUuidAndTitleCache(List<TermVocabulary> vocs, Integer limit, String pattern, Language lang) {
        List<NamedArea> areas = dao.getUuidAndTitleCache(vocs, limit, pattern);

        List<UuidAndTitleCache<NamedArea>> result = new ArrayList();
        UuidAndTitleCache<NamedArea> uuidAndTitleCache;
        for (NamedArea area: areas){
            uuidAndTitleCache = new UuidAndTitleCache<>(area.getUuid(), area.getId(), area.labelWithLevel(area, lang));
            result.add(uuidAndTitleCache);
        }

        return result;
    }

    @Override
    public TermDto getParentAsDto(TermDto childTerm) {
        return dao.getParentAsDto(childTerm);
    }

    @Override
    public Collection<TermDto> getIncludesAsDto(
            TermDto parentTerm) {
        return dao.getIncludesAsDto(parentTerm);
    }

    @Override
    public Collection<TermDto> getKindOfsAsDto(
            TermDto parentTerm) {
        return dao.getKindOfsAsDto(parentTerm);
    }

    @Transactional(readOnly = false)
    @Override
    public void moveTerm(TermDto termDto, UUID parentUUID) {
        moveTerm(termDto, parentUUID, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly = false)
    @Override
    public void moveTerm(TermDto termDto, UUID parentUuid, TermMovePosition termMovePosition) {
        boolean isKindOf = termDto.getKindOfUuid()!=null && termDto.getKindOfUuid().equals(parentUuid);
        TermVocabulary vocabulary = HibernateProxyHelper.deproxy(vocabularyService.load(termDto.getVocabularyUuid()));
        DefinedTermBase parent = HibernateProxyHelper.deproxy(dao.load(parentUuid));
        if(parent==null){
            //new parent is a vocabulary
            TermVocabulary parentVocabulary = HibernateProxyHelper.deproxy(vocabularyService.load(parentUuid));
            DefinedTermBase term = HibernateProxyHelper.deproxy(dao.load(termDto.getUuid()));
            if(parentVocabulary!=null){
                term.setKindOf(null);
                term.setPartOf(null);

                vocabulary.removeTerm(term);
                parentVocabulary.addTerm(term);
            }
            vocabularyService.saveOrUpdate(parentVocabulary);
        }
        else {
            DefinedTermBase term = HibernateProxyHelper.deproxy(dao.load(termDto.getUuid()));
            //new parent is a term
            if(parent.isInstanceOf(OrderedTermBase.class)
                    && term.isInstanceOf(OrderedTermBase.class)
                    && termMovePosition!=null
                    && HibernateProxyHelper.deproxy(parent, OrderedTermBase.class).getVocabulary().isInstanceOf(OrderedTermVocabulary.class)) {
                //new parent is an ordered term
                OrderedTermBase orderedTerm = HibernateProxyHelper.deproxy(term, OrderedTermBase.class);
                OrderedTermBase targetOrderedDefinedTerm = HibernateProxyHelper.deproxy(parent, OrderedTermBase.class);
                OrderedTermVocabulary otVoc = HibernateProxyHelper.deproxy(targetOrderedDefinedTerm.getVocabulary(), OrderedTermVocabulary.class);
                if(termMovePosition.equals(TermMovePosition.BEFORE)) {
                    orderedTerm.getVocabulary().removeTerm(orderedTerm);
                    otVoc.addTermAbove(orderedTerm, targetOrderedDefinedTerm);
                    if (targetOrderedDefinedTerm.getPartOf() != null){
                        targetOrderedDefinedTerm.getPartOf().addIncludes(orderedTerm);
                    }
                }
                else if(termMovePosition.equals(TermMovePosition.AFTER)) {
                    orderedTerm.getVocabulary().removeTerm(orderedTerm);
                    otVoc.addTermBelow(orderedTerm, targetOrderedDefinedTerm);
                    if (targetOrderedDefinedTerm.getPartOf() != null){
                        targetOrderedDefinedTerm.getPartOf().addIncludes(orderedTerm);
                    }
                }
                else if(termMovePosition.equals(TermMovePosition.ON)) {
                    orderedTerm.getVocabulary().removeTerm(orderedTerm);
                    targetOrderedDefinedTerm.addIncludes(orderedTerm);
                    targetOrderedDefinedTerm.getVocabulary().addTerm(orderedTerm);
                }
            }
            else{
                vocabulary.removeTerm(term);
                if(isKindOf){
                    parent.addGeneralizationOf(term);
                }
                else{
                    parent.addIncludes(term);
                }
                parent.getVocabulary().addTerm(term);
            }
            vocabularyService.saveOrUpdate(parent.getVocabulary());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly = false)
    @Override
    public void addNewTerm(TermType termType, UUID parentUUID, boolean isKindOf) {
        DefinedTermBase term = termType.getEmptyDefinedTermBase();
        dao.save(term);
        DefinedTermBase parent = dao.load(parentUUID);
        if(isKindOf){
            parent.addGeneralizationOf(term);
        }
        else{
            parent.addIncludes(term);
        }
        parent.getVocabulary().addTerm(term);
        dao.saveOrUpdate(parent);
    }

    public enum TermMovePosition{
        BEFORE,
        AFTER,
        ON
    }

}
