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
			@CliOption(key = {"conceptUuid"}, mandatory = false, help = "Concept id of the form") final String conceptUuid,
			@CliOption(key = {"json"}, mandatory = false, help = "Path of the json form") String path
	){
		obsProcessor.migrateForm(conceptUuid, path);
		return "Processed file.." + path;
	}

	@CliCommand(value = "convert programs form", help = "Convert programs form to 2.0")
	public String convertAddMoreForm(
			@CliOption(key = {"conceptUuid"}, mandatory = false, help = "Concept id of the form") final String conceptUuid,
			@CliOption(key = {"json"}, mandatory = false, help = "Path of the json form") String path
	){
		obsProcessor.migrateProgramForm(conceptUuid, path);
		return "Processed file.." + path;
	}

	@CliCommand(value = "show_conflicts", help = "shows tables which are inconsistent as they have been upgraded")
	public String showConflicts() throws Exception {
		String concatenatedConflicts = analyticsService.displayAllConflicts();
		return concatenatedConflicts;
	}

	@CliCommand(value = "fix_form", help = "upgrade old table which is inconsistent to new version")
	//example : upgrade_form_table --form_name "AViac Form Template 8681"
	public String upgradeFormTables(
			@CliOption(key = {"form_name"}, mandatory = true, help = "name of the json form") String oldTable,
//			@CliOption(key = {"new_version"}, mandatory = true, help = "path of the json form") Integer version,
//			@CliOption(key = {"show_query_only"}, mandatory = false, help = "path of the json form", systemProvided = false) Boolean show_query_only
			@CliOption(key = {"dry_run"}, mandatory = false, help = "dry_run if true indicates a trial and the query " +
					"shown will not be executed and if false the query would be executed", systemProvided = false) Boolean show_query_only
	) throws Exception {
		if(show_query_only == null) show_query_only = true;
		String result = analyticsService.fixForm(oldTable, show_query_only);
		return result;
	}

//	@CliCommand(value = "initialize_form_details", help = "initializes form_meta_data_table for the first time")
//	public String initializeMetaData() throws Exception {
//		analyticsService.initializeFormMetaDataTable();
//		return "initialization completed.";
//	}

	@CliCommand(value = "convert forms", help = "Convert obs of form to 2.0")
	public String convertForms(
			@CliOption(key = {"json"}, mandatory = true, help = "Path of the json file containing concept uuid and form name") String path,
			@CliOption(key = {"folder"}, mandatory = true, help = "Path of the folder containing forms") String folder
	){
		obsProcessor.migrateForms(path, folder);
		return "Processed forms from.." + path;
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
