package com.nuchange.nuacare.data.persister;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sandeepe on 28/09/16.
 */
public abstract class LineProcessor {


    protected JdbcTemplate jdbcTemplate;
    protected TransactionTemplate transactionTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public abstract String processLine(String[] line);

    public String validateLine(String[] line){
        return null;
    }

    public void postProcess(){

    }
    public void preProcess(){

    }
    public void init(String[] Line){

    }
    public String initValidation(String[] Line){
        return null;
    }
    public String finishValidation(){
        return null;
    }
    public void finish(){

    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }
    public static final String letters = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String ALLOWED_CHARS = "0123456789" + letters;

    public static final int NUMBER_OF_CODEPOINTS = ALLOWED_CHARS.length();
    public static final int CODESIZE = 11;

    public static String generateCode()
    {
        // Using the system default algorithm and seed

        SecureRandom sr = new SecureRandom();
        char[] randomChars = new char[CODESIZE];
        // first char should be a letter
        randomChars[0] = letters.charAt( sr.nextInt( letters.length() ) );
        for ( int i = 1; i < CODESIZE; ++i )
        {
            randomChars[i] = ALLOWED_CHARS.charAt( sr.nextInt( NUMBER_OF_CODEPOINTS ) );
        }
        return new String( randomChars );
    }

    public boolean needsValidation(){
        return true;
    }

    protected String validateHeader(String[] csvLine, String[] header) {
        if (csvLine.length != header.length) {
            return "CSV Header validation failed : column count does not match required format!";
        }
        for (int i = 0; i < csvLine.length; i++) {
            if (!csvLine[i].equals(header[i])) {
                return "CSV Header validation failed : name of column does not match! Required : " + header[i] + ", Found : " + csvLine[i];
            }
        }
        return null;
    }

    public java.sql.Timestamp getSqlDateValue(String input, String pattern) throws ParseException {
        if (StringUtils.isEmpty(input)) {
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(input);
            return new java.sql.Timestamp(date.getTime());
        }
    }

}
