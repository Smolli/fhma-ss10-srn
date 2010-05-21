package de.fhma.ss10.srn
.tischbein.gui.frames;

import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.SearchAction;
import de.fhma.ss10.srn.tischbein.gui.forms.UploadForm;

public class UploadFrame extends UploadForm {

	private User user;
	
	public UploadFrame()
	{
		this.searchButton.setAction(new SearchAction());
		this.abbortButton.setAction(null);
		this.saveButton.setAction(null);
		this.secretField.setAction(null);
		
		
		
	}
	
	
	
	
}
