
//$Id$
	/**
	* Copyright (C) 2009 EDIT
	* European Distributed Institute of Taxonomy
	* http://www.e-taxonomy.eu
	*
	* The contents of this file are subject to the Mozilla Public License Version 1.1
	* See LICENSE.TXT at the top of this package for the full license terms.
	*/
package eu.etaxonomy.cdm.api.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;

	/**
	 * This class represents the result of an update action.
	 *
	 * @author k.luther
	 * @date 11.02.2015
	 *
	 */
public class UpdateResult implements Serializable{
	
	

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(UpdateResult.class);

		private Status status = Status.OK;

		private final List<Exception> exceptions = new ArrayList<Exception>();

		private final Set<CdmBase> upatedObjects = new HashSet<CdmBase>();
		
		private CdmBase cdmEntity;

//		private Set<PersistPair> objectsToDelete = new HashSet<PersistPair>();
	//
//		private Set<PersistPair> objectsToSave = new HashSet<DeleteResult.PersistPair>();

//		protected class PersistPair{
//			protected CdmBase objectToPersist;
//			protected ICdmEntityDao<CdmBase> dao;
//		}

		public enum Status {
			OK(0),
			ABORT(1),
			ERROR(3),
			;

			protected Integer severity;
			private Status(int severity){
				this.severity = severity;
			}

			public int compareSeverity(Status other){
				return this.severity.compareTo(other.severity);
			}
		}

	//***************************** GETTER /SETTER /ADDER *************************/
		/**
		 * The resulting status of an update action.
		 *
		 * @see UpdateStatus
		 * @return
		 */
		public Status getStatus() {
			return status;
		}
		public void setStatus(Status status) {
			this.status = status;
		}

		/**
		 * The highest exception that occurred during delete (if any).
		 * @return
		 */
		public List<Exception> getExceptions() {
			return exceptions;
		}
		public void addException(Exception exception) {
			this.exceptions.add(exception);
		}
		public void addExceptions(List<Exception> exceptions) {
			this.exceptions.addAll(exceptions);
		}

		/**
		 * Related objects that prevent the delete action to take place.
		 * @return
		 */
		public Set<CdmBase> getUpdatedObjects() {
			return upatedObjects;
		}
		public void addUpdatedObject(CdmBase relatedObject) {
			this.upatedObjects.add(relatedObject);
		}
		public void addUpdatedObjects(Set<? extends CdmBase> updatedObjects) {
			this.upatedObjects.addAll(updatedObjects);
		}


//		/**
//		 * @return
//		 */
//		public Set<PersistPair> getObjectsToDelete() {
//			return objectsToDelete;
//		}
//		public void setObjectsToDelete(Set<PersistPair> objectsToDelete) {
//			this.objectsToDelete = objectsToDelete;
//		}
	//
//		/**
//		 * @return
//		 */
//		public Set<PersistPair> getObjectsToSave() {
//			return objectsToSave;
//		}
//		public void setObjectsToSave(Set<PersistPair> objectsToSave) {
//			this.objectsToSave = objectsToSave;
//		}


	//****************** CONVENIENCE *********************************************/

		/**
		 * Sets the status to {@link DeleteStatus#ERROR} if not yet set to a more serious
		 * status.
		 */
		public void setError(){
			setMaxStatus(Status.ERROR);
		}

		/**
		 * Sets the status to {@link DeleteStatus#ABORT} if not yet set to a more serious
		 * status.
		 */
		public void setAbort(){
			setMaxStatus(Status.ABORT);
		}

		/**
		 * Sets status to most severe status. If maxStatus is more severe then existing status
		 * existing status is set to maxStatus. Otherwise nothing changes.
		 * If minStatus is more severe then given status minStatus will be the new status.
		 * @param maxStatus
		 */
		public void setMaxStatus(Status maxStatus) {
			if (this.status.compareSeverity(maxStatus) < 0){
				this.status = maxStatus;
			}
		}

		public void includeResult(UpdateResult includedResult){
			this.setMaxStatus(includedResult.getStatus());
			this.addExceptions(includedResult.getExceptions());
			this.addUpdatedObjects(includedResult.getUpdatedObjects());
		}

		public boolean isOk(){
			return this.status == Status.OK;
		}

		public boolean isAbort(){
			return this.status == Status.ABORT;
		}

		public boolean isError(){
			return this.status == Status.ERROR;
		}



		@Override
		public String toString(){
		    String separator = ", ";
		    String exceptionString = "";
		    for (Exception exception : exceptions) {
	            exceptionString += exception.getLocalizedMessage()+separator;
	        }
		    if(exceptionString.endsWith(separator)){
		        exceptionString = exceptionString.substring(0, exceptionString.length()-separator.length());
		    }
		    String relatedObjectString = "";
		    for (CdmBase upatedObject: upatedObjects) {
		        if(upatedObject instanceof IIdentifiableEntity){
		            relatedObjectString += ((IIdentifiableEntity) upatedObject).getTitleCache()+separator;
		        }
		        else{
		            relatedObjectString += upatedObject.toString()+separator;
		        }
		    }
		    if(relatedObjectString.endsWith(separator)){
		        relatedObjectString = relatedObjectString.substring(0, relatedObjectString.length()-separator.length());
		    }
			return "[DeleteResult]\n" +
					"Status: " + status.toString()+"\n" +
							"Exceptions: " + exceptionString+"\n" +
									"Related Objects: "+relatedObjectString;
		}
		public void setCdmEntity(CdmBase cdmBase) {
			this.cdmEntity = cdmBase;
			
		}


	

}
