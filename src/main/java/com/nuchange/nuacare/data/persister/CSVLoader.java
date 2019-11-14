package com.nuchange.nuacare.data.persister;

import au.com.bytecode.opencsv.CSVReader;
import com.nuchange.nuacare.util.Utils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * Created by sandeepe on 28/09/16.
 * 
 */
public class CSVLoader {

	private final String filePath;
	private final LineProcessor processor;


	private static final char seprator = ',';
	private static final String ERROR_FILE_DIRECTORY = "/tmp/uploadErrors/";

	public CSVLoader(String filePath, LineProcessor processor) {
		this.filePath = filePath;
		this.processor = processor;
	}

	public void validateCSV() throws Exception {
		System.out.println("Begining validation of the file");
		CSVReader csvReader = null;
		int errorCount = 0;
		Utils.createDirectoryIfNotExist(ERROR_FILE_DIRECTORY);
		String errorFileName = getErrorFileName("_Validation_output_", ".txt");
		BufferedWriter writer = null;
		try {
			csvReader = new CSVReader(new FileReader(filePath), seprator);
			File failedItems = new File(errorFileName);
			writer = new BufferedWriter(new FileWriter(failedItems, true));
			String[] nextLine;
			//reading header row
			String[] firstLine = csvReader.readNext();
			if(firstLine!=null){
				String status = this.processor.initValidation(firstLine);
				if(StringUtils.isNotBlank(status)) {
					errorCount = updateErrorDetails(errorCount, writer, firstLine, status);
					//break;
				}
			}
			if (errorCount > 0){
				throw new Exception("Error in header line, could not proceed with the validation of file," +
						" Please refer to "+errorFileName+" for further details");
			}
			while ((nextLine = csvReader.readNext()) != null) {
				this.processor.preProcess();
				String success = this.processor.validateLine(nextLine);
				if(StringUtils.isNotBlank(success)) {
					errorCount = updateErrorDetails(errorCount, writer, nextLine, success);
					//break;
				}
				this.processor.postProcess();
			}
			this.processor.finishValidation();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error occured while executing file. "
					+ e.getMessage());
		}
		finally {
			if (writer != null) {
				writer.close();
			}
			if (csvReader != null) {
				csvReader.close();
			}
		}
		System.out.println("Validation of the file complete ( at:" +new Date()+ ")");
		if (errorCount >0){
			throw new Exception("Error in header line, could not proceed with the validation of file," +
					" Please refer to "+errorFileName+" for further details");
		}

	}

	private int updateErrorDetails(int errorCount, BufferedWriter writer, String[] firstLine, String status) throws IOException {
		writer.write(status+"\n" + "CSV line : ");
		for (String s : firstLine) {
			writer.write(s + ", ");
		}
		errorCount ++;
		return errorCount;
	}

	public void loadCSV() throws Exception {

		CSVReader csvReader = null;
		BufferedWriter writer = null;
		StringBuilder header = new StringBuilder();
		boolean errorsInCsv = false;

		try {
			csvReader = new CSVReader(new FileReader(filePath), seprator);

			Utils.createDirectoryIfNotExist(ERROR_FILE_DIRECTORY);
			String errorFileName = getErrorFileName( "_Errors_", ".csv");
			File failedItems = new File(errorFileName);
			writer = new BufferedWriter(new FileWriter(failedItems, true));

			String[] nextLine;
			//reading header row
			String[] firstLine = csvReader.readNext();
			if(firstLine!=null){
				this.processor.init(firstLine);
				for (String s : firstLine) {
					header.append(s).append(", ");
				}
				header.append("errors\n");
			}
			// reading data
			while ((nextLine = csvReader.readNext()) != null) {
				this.processor.preProcess();

				String lineProcessStatus = this.processor.processLine(nextLine);

				if(StringUtils.isNotBlank(lineProcessStatus)) {
					if (!errorsInCsv) {
						writer.write(header.toString());
						errorsInCsv = true;
					}
					for (String s : nextLine) {
						writer.write(s + ", ");
					}
					writer.write(lineProcessStatus+"\n");
				}
				this.processor.postProcess();
			}
			this.processor.finish();

			if (errorsInCsv) {
				System.out.println("Errors occurred while executing file! Please refer to " + errorFileName + " for further details");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error occurred while executing file. "
					+ e.getMessage());
		}
		finally {
			if (writer != null) {
				writer.close();
			}
			if (csvReader != null) {
				csvReader.close();
			}
		}
	}

	private String getErrorFileName(String nameType, String extention) {
		Date now = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return ERROR_FILE_DIRECTORY + processor.getClass().getSimpleName() + nameType + sf.format(now) + extention;
	}

	public boolean needsValidation() {
		return processor.needsValidation();
	}
}
