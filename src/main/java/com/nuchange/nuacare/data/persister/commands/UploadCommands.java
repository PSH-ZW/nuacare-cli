package com.nuchange.nuacare.data.persister.commands;

import com.nuchange.nuacare.data.persister.CSVDataPersister;
import com.nuchange.nuacare.data.persister.LineProcessor;
import com.nuchange.nuacare.data.persister.impl.EditPersonAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadCommands implements CommandMarker {

	@Autowired
	private JLineShellComponent shell;

	@Autowired
	private EditPersonAttribute editPersonAttribute;

	@Autowired
	private CSVDataPersister csvDataPersister;

	@CliAvailabilityIndicator({"upload csv"})
	public boolean isSimpleAvailable() {
		//always available
		return true;
	}

	@CliCommand(value = "upload csv", help = "Upload CSV to Nuacare")
	public String upload(
		@CliOption(key = { "type" }, mandatory = true, help = "Type of data which need to be uploaded") final UploadType type,
		@CliOption(key = { "file"}, mandatory = true, help = "Full path of the csv file") final String filePath,
		@CliOption(key = { "validate"}, mandatory = true, help = "Pass false to real update, otherwise file will be just validated") final String validate
		) throws Exception {
		File fileToProcess = new File(filePath);
		boolean exists = fileToProcess.exists();
		if(!exists){
			return "File not found on the path provided";
		}else if(!fileToProcess.isFile()){
            return "Give a file as input, not directory";
        }
		else{
			//shell.flash(java.util.logging.Level.ALL, "Processing file "+filePath, "id");//();
            LineProcessor processorForType = getProcessorForType(type);
            if (processorForType==null){
                return "No Processor registered for type, " + type;
            }
            csvDataPersister.setProcessor(processorForType);
            csvDataPersister.updateCSV(filePath, validate);
            return "Processed file.." + filePath;
		}
	}

    private LineProcessor getProcessorForType(UploadType type) {
        switch (type){
            case EditPersonAttributes:
                return editPersonAttribute;
        }
        return null;
    }

    enum UploadType {
		EditPersonAttributes("EditPersonAttributes");

		private String type;
		
		private UploadType(String type){
			this.type = type;
		}
		
		public String getType(){
			return type;
		}
	}
}
