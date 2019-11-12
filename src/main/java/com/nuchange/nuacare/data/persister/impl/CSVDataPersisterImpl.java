package com.nuchange.nuacare.data.persister.impl;

import com.nuchange.nuacare.data.persister.CSVLoader;
import com.nuchange.nuacare.data.persister.CSVDataPersister;
import com.nuchange.nuacare.data.persister.LineProcessor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by sandeepe on 26/02/16.
 */
@Component
public class CSVDataPersisterImpl extends JdbcDaoSupport implements CSVDataPersister {

    final static Logger logger = Logger.getLogger(CSVDataPersisterImpl.class);

    public LineProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(LineProcessor processor) {
        this.processor = processor;
    }

    private LineProcessor processor;

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    private TransactionTemplate transactionTemplate;

    @Override
    public void updateCSV(String filePath, String validate) throws Exception {
        processor.setJdbcTemplate(getJdbcTemplate());
        processor.setTransactionTemplate(transactionTemplate);
        CSVLoader loader = new CSVLoader(filePath,processor);
        if ("false".equalsIgnoreCase(validate)){
            if (loader.needsValidation()){
                loader.validateCSV();
            }
            loader.loadCSV();
        } else {
            loader.validateCSV();
        }
    }
    @Autowired
    CSVDataPersisterImpl(DriverManagerDataSource dataSource, TransactionTemplate transactionTemplate){
        super();
        this.setDataSource(dataSource);
        this.setTransactionTemplate(transactionTemplate);
    }
}
