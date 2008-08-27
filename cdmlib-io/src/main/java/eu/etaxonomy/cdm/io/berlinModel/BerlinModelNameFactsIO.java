/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_ALSO_PUBLISHED_IN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_PROTOLOGUE;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;



/**
 * @author a.mueller
 *
 */
public class BerlinModelNameFactsIO  extends BerlinModelIOBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelNameFactsIO.class);

	private int modCount = 50;
	private int maxCount = 20;
	
	public BerlinModelNameFactsIO(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for NameFacts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, 
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		INameService nameService = cdmApp.getNameService();
		
		logger.info("start makeNameFacts ...");

		try {
			//get data from database
			String strQuery = 
					" SELECT NameFact.*, Name.NameID as nameId, NameFactCategory.NameFactCategory " + 
					" FROM NameFact INNER JOIN " +
                      	" Name ON NameFact.PTNameFk = Name.NameId  INNER JOIN "+
                      	" NameFactCategory ON NameFactCategory.NameFactCategoryID = NameFact.NameFactCategoryFK " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next() && i < maxCount){
				
				if ((i++ % modCount) == 0  && i!= 1 ){ logger.info("NameFacts handled: " + (i-1));}
				
				int nameFactId = rs.getInt("nameFactId");
				int nameId = rs.getInt("nameId");
				int nameFactRefFk = rs.getInt("nameFactRefFk");
				int categoryFk = rs.getInt("nameFactCategoryFk");
				String category = CdmUtils.Nz(rs.getString("NameFactCategory"));
				String nameFact = CdmUtils.Nz(rs.getString("nameFact"));
				
				TaxonNameBase taxonNameBase = taxonNameMap.get(nameId);
				//taxonNameBase = BotanicalName.NewInstance(null);
				
				if (taxonNameBase != null){
					//PROTOLOGUE
					if (category.equalsIgnoreCase(NAME_FACT_PROTOLOGUE)){
						//ReferenceBase ref = (ReferenceBase)taxonNameBase.getNomenclaturalReference();
						//ref = Book.NewInstance();
						try{
							Media media = getMedia(nameFact, bmiConfig.getMediaUrl(), bmiConfig.getMediaPath());
							if (media.getRepresentations().size() > 0){
								TaxonNameDescription description = TaxonNameDescription.NewInstance();
								TextData protolog = TextData.NewInstance(Feature.PROTOLOG());
								protolog.addMedia(media);
								description.addElement(protolog);
								taxonNameBase.addDescription(description);
							}//end NAME_FACT_PROTOLOGUE
						}catch(NullPointerException e){
							logger.warn("MediaUrl and/or MediaPath not set. Could not get protologue.");
						}						
					}else if (category.equalsIgnoreCase(NAME_FACT_ALSO_PUBLISHED_IN)){
						if (! nameFact.equals("")){
							
							
							TaxonNameDescription description = TaxonNameDescription.NewInstance();
							TextData additionalPublication = TextData.NewInstance(Feature.ADDITIONAL_PUBLICATION());
							//TODO language
							Language language = Language.DEFAULT();
							additionalPublication.putText(nameFact, language);
							description.addElement(additionalPublication);
							taxonNameBase.addDescription(description);
						}
					}else {
						//TODO
						logger.warn("NameFactCategory '" + category + "' not yet implemented");
					}
					
					//TODO
//					NameFactRefFk            int        Checked
//					DoubtfulFlag    bit        Checked
//					PublishFlag      bit        Checked
//					Created_When  datetime           Checked
//					Updated_When datetime           Checked
//					Created_Who    nvarchar(255)    Checked
//					Updated_Who  nvarchar(255)    Checked
//					Notes      nvarchar(1000)           Checked
//					NameFactRefDetail       nvarchar(80)      Checked
					
					taxonNameStore.add(taxonNameBase);
				}else{
					//TODO
					logger.warn("TaxonName for NameFact " + nameFactId + " does not exist in store");
				}
				//put
			}
			if (i >= maxCount - 1){ logger.warn("ONLY " + maxCount + " NAMEFACTS imported !!!" );};
			logger.info("Names to save: " + taxonNameStore.size());
			nameService.saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeNameFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoNameFacts();
	}
	
	
	public Media getMedia(String nameFact, URL mediaUrl, File mediaPath){
		if (mediaUrl == null){
			logger.warn("Media Url should not be null");
			return null;
		}
		String mimeTypeTif = "image/tiff";
		String mimeTypeJpg = "image/jpeg";
		String mimeTypePng = "image/png";
		String suffixTif = "tif";
		String suffixJpg = "jpg";
		String suffixPng = "png";
		String sep = File.separator;
		Integer size = null;
		
		logger.warn("Getting media for NameFact: " + nameFact);
		
		Media media = Media.NewInstance();
		
		String mediaUrlString = mediaUrl.toString();

		//tiff
		String urlStringTif = mediaUrlString + "tif/" + nameFact + "." + suffixTif;
		File file = new File(mediaPath, "tif" + sep + nameFact + "." + suffixTif);
		MediaRepresentation representationTif = MediaRepresentation.NewInstance(mimeTypeTif, suffixTif);
		if (file.exists()){
			representationTif.addRepresentationPart(makeImage(urlStringTif, size, file));
		}
		media.addRepresentation(representationTif);
		// end tif
		// jpg
		boolean fileExists = true;
		int jpgCount = 0;
		MediaRepresentation representationJpg = MediaRepresentation.NewInstance(mimeTypeJpg, suffixJpg);
		while(fileExists){
			jpgCount++;
			String urlStringJpeg = mediaUrlString + "jpeg/" + nameFact + "_p" + jpgCount + "." + suffixJpg;
			file = new File(mediaPath, "jpeg" + sep + nameFact + "_p" + jpgCount + "." + suffixJpg);
			if (file.exists()){ 
				representationJpg.addRepresentationPart(makeImage(urlStringJpeg, size, file));
			}else{
				fileExists = false;
			}
		}
		media.addRepresentation(representationJpg);
		// end jpg
		//png
		String urlStringPng = mediaUrlString + "png/" + nameFact + "." + suffixPng;
		file = new File(mediaPath, "png" + sep + nameFact + "." + suffixPng);
		MediaRepresentation representationPng = MediaRepresentation.NewInstance(mimeTypePng, suffixPng);
		if (file.exists()){ 
			representationPng.addRepresentationPart(makeImage(urlStringPng, size, file));
		}else{
			fileExists = true;
			int pngCount = 0;
			while (fileExists){
				pngCount++;
				urlStringPng = mediaUrlString + "png/" + nameFact + "00" + pngCount + "." + suffixPng;
				file = new File(mediaPath, "png" + sep + nameFact + "00" + pngCount + "." + suffixPng);
				
				if (file.exists()){ 
					representationPng.addRepresentationPart(makeImage(urlStringPng, size, file));
				}else{
					fileExists = false;
				}
			}
		} 
		media.addRepresentation(representationPng);
		//end png
		
		if(logger.isDebugEnabled()){
			for (MediaRepresentation rep : media.getRepresentations()){
				for (MediaRepresentationPart part : rep.getParts()){
					logger.debug("in representation: " + part.getUri());
				}
			}
		}
		
		return media;
	}
	
	
	private ImageFile makeImage(String imageUri, Integer size, File file){
		ImageMetaData imageMetaData = new ImageMetaData();
		imageMetaData.readFrom(file);
		ImageFile image = ImageFile.NewInstance(imageUri, size, imageMetaData);
		return image;
	}
	
	//for testing only
	public static void main(String[] args) {
		
		BerlinModelNameFactsIO nf = new BerlinModelNameFactsIO();
		
		URL url;
		try {
			url = new URL("http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/protolog/");
			File path = new File("/Volumes/protolog/protolog/");
			if(path.exists()){
				String fact = "Crepis_meletonis";
				// gotta make getMedia public for this to work
				Media media = nf.getMedia(fact, url, path);
				System.out.println(media);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
	}

}
