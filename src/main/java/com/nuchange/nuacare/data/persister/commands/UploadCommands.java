package com.nuchange.nuacare.data.persister.commands;

import com.nuchange.nuacare.data.persister.CSVDataPersister;
import com.nuchange.nuacare.data.persister.LineProcessor;
import com.nuchange.nuacare.data.persister.ObsProcessor;
import com.nuchange.nuacare.data.persister.impl.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class UploadCommands implements CommandMarker {

	@Autowired
	private JLineShellComponent shell;

	@Autowired
	private EditPersonAttribute editPersonAttribute;

	@Autowired
	private LabResultsProcessor labResultsProcessor;

	@Autowired
	private ConditionsProcessor conditionsProcessor;

	@Autowired
	private FormsProcessor formsProcessor;

	@Autowired
	private CSVDataPersister csvDataPersister;

	@Autowired
	private ObsProcessor obsProcessor;

	@Autowired
	AnalyticsService analyticsService;

	@CliAvailabilityIndicator({"upload csv"})
	public boolean isSimpleAvailable() {
		//always available
		return true;
	}

	@CliCommand(value = "upload csv", help = "Upload CSV to Nuacare")
	public String upload(
			@CliOption(key = {"type"}, mandatory = true, help = "Type of data which need to be uploaded") final UploadType type,
			@CliOption(key = {"file"}, mandatory = true, help = "Full path of the csv file") final String filePath,
			@CliOption(key = {"validate"}, mandatory = false, help = "Pass false to real update, otherwise file will be just validated") String validate,
			@CliOption(key = {"json"}, mandatory = false, help = "Path of the json form") String path
	) throws Exception {
		File fileToProcess = new File(filePath);
		boolean exists = fileToProcess.exists();
		if (StringUtils.isEmpty(validate)) {
			validate = "false";
		}
		if (!exists) {
			return "File not found on the path provided";
		} else if (!fileToProcess.isFile()) {
			return "Give a file as input, not directory";
		} else {
			//shell.flash(java.util.logging.Level.ALL, "Processing file "+filePath, "id");//();
			LineProcessor processorForType = getProcessorForType(type);
			if (processorForType == null) {
				return "No Processor registered for type, " + type;
			}
			csvDataPersister.setProcessor(processorForType);
			if(type==UploadType.Forms){
				formsProcessor.generateMap(path);
			}
			csvDataPersister.updateCSV(filePath, validate);
			return "Processed file.." + filePath;
		}
	}

	@CliCommand(value = "convert form", help = "Convert form to 2.0")
	public String convert(
			@CliOption(key = {"conceptId"}, mandatory = false, help = "Concept id of the form") final Integer conceptId,
			@CliOption(key = {"json"}, mandatory = false, help = "Path of the json form") String path
	){
		obsProcessor.migrateForm(conceptId, path);
		return "Processed file.." + path;
	}

	@CliCommand(value = "convert programs form", help = "Convert programs form to 2.0")
	public String convertAddMoreForm(
			@CliOption(key = {"conceptId"}, mandatory = false, help = "Concept id of the form") final Integer conceptId,
			@CliOption(key = {"json"}, mandatory = false, help = "Path of the json form") String path
	){
		obsProcessor.migrateProgramForm(conceptId, path);
		return "Processed file.." + path;
	}

	@CliCommand(value = "show_conflicts", help = "shows tables which are inconsistent as they have been upgraded")
	public String formProgramMetaDataHelper() throws Exception {
		String concatenatedConflicts = analyticsService.displayAllColumnConflicts();
		return concatenatedConflicts;
	}

	@CliCommand(value = "upgrade_form_table", help = "upgrade old table which is inconsistent to new version")
	//example : upgrade_form_table --form_name "AViac Form Template 8681" --new_version 6
	public String upgradeFormTables(
			@CliOption(key = {"form_name"}, mandatory = true, help = "name of the json form") String oldTable,
			@CliOption(key = {"new_version"}, mandatory = false, help = "path of the json form") Integer version
	) throws Exception {
		String result = analyticsService.upgradeForm(oldTable, version);
		return "updated query :" + result;
	}

	@CliCommand(value = "initialize_form_details", help = "initializes form_meta_data_table for the first time")
	public String initializeMetaData() throws Exception {
		analyticsService.initializeFormMetaDataTable();
		return "initialization completed.";
	}

	private LineProcessor getProcessorForType(UploadType type) {
		switch (type) {
			case EditPersonAttributes:
				return editPersonAttribute;
			case LabResults:
				return labResultsProcessor;
			case Conditions:
				return conditionsProcessor;
			case Forms: ;
				return formsProcessor;
		}
		return null;
	}

	enum UploadType {
		EditPersonAttributes("EditPersonAttributes"),
		LabResults("LabResults"),
		Conditions("Conditions"),
		Forms("Forms");

		private String type;

		UploadType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	@CliCommand(value = "test")
	public String test() {
		System.out.print("Success");
		return "";
	}
}
