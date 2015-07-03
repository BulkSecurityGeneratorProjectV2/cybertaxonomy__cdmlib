package eu.etaxonomy.cdm.io.common;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
@Component
public class DeleteNonReferencedreferencesUpdater extends CdmIoBase<DefaultImportState<DeleteNonReferencedReferencesConfigurator>> {

	@Override
	protected void doInvoke(
			DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		List<Reference> references =getReferenceService().list(Reference.class, null, null, null, null);
		DeleteResult result;
		int deleted = 0;
		System.out.println("There are " + references.size() + " references");
		for (Reference ref: references){
			Set<CdmBase> refObjects = getCommonService().getReferencingObjects(ref);
			if (refObjects.isEmpty()) {
				result = getReferenceService().delete(ref);
				deleted++;
				if (!result.isOk()){
					System.out.println("Reference " + ref.getTitle() + " with id " + ref.getId() + " could not be deleted.");
					result = null;
				}
			}
		}
		System.out.println(deleted + " references are deleted.");
		
	}

	@Override
	protected boolean doCheck(
			DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isIgnore(
			DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		// TODO Auto-generated method stub
		return false;
	}

}
