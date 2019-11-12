package com.nuchange.nuacare.data.persister;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Date;

/**
 * 
 * Created by sandeepe on 28/09/16.
 * 
 */
public class CSVLoader {

	private final String filePath;
	private final LineProcessor processor;


	private static final char seprator = ',';

	public CSVLoader(String filePath, LineProcessor processor) {
		this.filePath = filePath;
		this.processor = processor;
	}

	public void validateCSV() throws Exception {
		System.out.println("Begining validation of the file");
		CSVReader csvReader = null;
		int errorCount = 0;
		Date now = new Date();
		String fileName = processor.getClass().getName()+"_Validation_outpu_"+now.toString() + ".txt";
		BufferedWriter writer = null;
		try {
			csvReader = new CSVReader(new FileReader(filePath), seprator);
			File failedItems = new File(fileName);
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
						" Please refer to "+fileName+" for further details");
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
			writer.close();
			csvReader.close();
		}
		System.out.println("Validation of the file complete ( at:" +new Date()+ ")");
		if (errorCount >0){
			throw new Exception("Error in header line, could not proceed with the validation of file," +
					" Please refer to "+fileName+" for further details");
		}

	}

	private int updateErrorDetails(int errorCount, BufferedWriter writer, String[] firstLine, String status) throws IOException {
		for (String s : firstLine) {
            writer.write(s + ", ");
        }
		writer.write(status+"\n");
		errorCount ++;
		return errorCount;
	}

	public void loadCSV() throws Exception {

		CSVReader csvReader = null;
		BufferedWriter writer = null;
		try {
			csvReader = new CSVReader(new FileReader(filePath), seprator);
			Date now = new Date();
			String fileName = now.toString() + ".txt";
			File failedItems = new File(fileName);
			writer = new BufferedWriter(new FileWriter(failedItems, true));
			String[] nextLine;
			//reading header row
			String[] firstLine = csvReader.readNext();
			if(firstLine!=null){
				this.processor.init(firstLine);
			}
			while ((nextLine = csvReader.readNext()) != null) {
				this.processor.preProcess();
				String lineProcessStatus = this.processor.processLine(nextLine);
				if(StringUtils.isNotBlank(lineProcessStatus)) {
					System.out.println("ERROR Could not insert data, processing line");
					for (String s : nextLine) {
						writer.write(s + ", ");
					}
					writer.write(lineProcessStatus+"\n");
					//break;
				}
				this.processor.postProcess();

			}
			this.processor.finish();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error occured while executing file. "
					+ e.getMessage());
		}
		finally {
			writer.close();
			csvReader.close();
		}
	}

	public boolean needsValidation() {
		return processor.needsValidation();
	}
}
