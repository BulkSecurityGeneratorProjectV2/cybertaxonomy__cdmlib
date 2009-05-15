package eu.etaxonomy.cdm.hibernate;

import org.hibernate.search.bridge.StringBridge;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

public class PartialBridge implements StringBridge {

	public String objectToString(Object object) {
		if(object != null) {
			Partial partial = ((Partial)object);
			StringBuilder stringBuilder = new StringBuilder();
			if(partial.isSupported(DateTimeFieldType.year())) {
			    stringBuilder.append(partial.get(DateTimeFieldType.year()));
			
			    if(partial.isSupported(DateTimeFieldType.monthOfYear())) {
				    stringBuilder.append(partial.get(DateTimeFieldType.monthOfYear()));
				
				    if(partial.isSupported(DateTimeFieldType.dayOfYear())) {
					    stringBuilder.append(partial.get(DateTimeFieldType.dayOfYear()));
 				    }
			    }
		        return stringBuilder.toString();
			}
		}
		return null;
	}

}
