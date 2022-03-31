package com.nuchange.nuacare.data.persister.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuchange.nuacare.data.persister.domain.*;
import com.nuchange.psiutil.AnalyticsUtil;
import com.nuchange.psiutil.model.FormConcept;
import com.nuchange.psiutil.model.FormLabel;
import com.nuchange.psiutil.model.FormTable;
import com.nuchange.psiutil.model.Forms;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    private JdbcTemplate openmrsJDBCTemplate;

    @Autowired
    @Qualifier("analyticsJdbcTemplate")
    private JdbcTemplate analyticsJdbcTemplate;

    //TODO:
    //1. display the tables for the forms which has conflicts : done
    //2. Upgrade form formName
    //3. display sql queries

    public void updateMetaDataTable( String formName, Integer formVersion) {
        String query = "UPDATE form_meta_data set version = ? where form_name = ?";
        analyticsJdbcTemplate.update(query, formVersion, formName);
    }

    public void executeQuery(String query) {
        analyticsJdbcTemplate.update(query);
    }

    public HashSet<FormDetails> getAllFormsWithHighestPublishedVersion(){
        final String query = "select form_id as formId, name as formName, version from form where published = 1 order by version desc";
        List<FormDetails> formDetails = openmrsJDBCTemplate.query(query, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(FormDetails.class));
        HashSet<FormDetails> formDetailsHashSet= new HashSet<>();
        HashSet<String> name = new HashSet<>();
        for(FormDetails formDetails1 : formDetails){
            if(name.add(formDetails1.getFormName())){
                formDetailsHashSet.add(formDetails1);
            }
        }
        return formDetailsHashSet;
    }

    public FormLabel getFormResourceDetailsForId(Integer formID){
        String sql = "select distinct name as type, value_reference as value from form_resource where form_id = ?";
        List<FormLabel> formDetails = openmrsJDBCTemplate.query(sql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(FormLabel.class), formID);
        if(!CollectionUtils.isEmpty(formDetails)){
            return formDetails.get(0);
        }
        return null;
    }

    public Integer getFormIdForNameAndVersion(String formName, Integer version){
        String query = "select form_id from form where name = ? and version = ?";
        List<Integer> formId = openmrsJDBCTemplate.query(query, JdbcTemplateMapperFactory.newInstance().
                newRowMapper(Integer.class), formName, version);
        if(!CollectionUtils.isEmpty(formId)){
            return formId.get(0);
        }
        return null;
    }

    public FormDetails getFormIdForNameAndHighestPublishedVersion(String formName){
        String query = "select form_id as formId , name as formName, version from form where published = 1 and name = ? order by version desc";
        List<FormDetails> formDetails = openmrsJDBCTemplate.query(query, JdbcTemplateMapperFactory.newInstance().
                newRowMapper(FormDetails.class), formName);
        if(!CollectionUtils.isEmpty(formDetails)){
            return formDetails.get(0);
        }
        return null;
    }

    public FormDetails findFormMetaDataDetailsForName(String formName){
        //TODO: need to check form details format from form table (because form_meta_data currently reads information from form table)
        String sql = "select form_name as formName, version from form_meta_data where form_name  = ?";
        List<FormDetails> formDetails = analyticsJdbcTemplate
                .query(sql, JdbcTemplateMapperFactory.newInstance().newRowMapper(FormDetails.class), formName);
        if(!CollectionUtils.isEmpty(formDetails)) {
            return formDetails.get(0);
        }
        return null;
    }

//    public void initializeFormMetaDataTable() throws Exception {
//        //TODO: unique constraint on form name
//        //before adding check whether
//        String deleteQuery = "delete from form_meta_data";
//        analyticsJdbcTemplate.update(deleteQuery);
//        String sql = "select distinct name as formName, version from form where published = 1 and version = 1 group by formName";
//        //TODO: should we select only forms with version 1 or highest as for the first time all form versions would be 1
//        List<FormDetails> formDetails = openmrsJDBCTemplate.query(sql, JdbcTemplateMapperFactory.newInstance().newRowMapper(FormDetails.class));
//        if(!CollectionUtils.isEmpty(formDetails)){
//            String insertSql = "insert into form_meta_data(form_name, version) values(?, ?)";
//            for(FormDetails formDetail: formDetails){
//                analyticsJdbcTemplate.update(insertSql, formDetail.getFormName(), formDetail.getVersion());
//            }
//        }else{
//            throw new Exception("Empty data");
//        }
//    }

    private void insertIntoMetaDataTable(String formName, Integer version) {
        String insertSql = "insert into form_meta_data(form_name, version) values(?, ?)";
        analyticsJdbcTemplate.update(insertSql, formName, version);
    }

    public String displayAllConflicts() throws Exception {
        List<OldAndNewFormDetails> allTableConflicts = new ArrayList<>();
        HashSet<FormDetails> formDetailsHashSet = getAllFormsWithHighestPublishedVersion();
        HashSet<FormDetails> createTableForForms = new HashSet<>();
        for(FormDetails formDetailsLine : formDetailsHashSet){
            FormDetails currentMetaDataDetail = findFormMetaDataDetailsForName(formDetailsLine.getFormName());
            if(currentMetaDataDetail == null) {
                createTableForForms.add(formDetailsLine);
            }
            //if form version in form table is greater than form version in form_meta_data table get form_resource details to check for column conflicts
            else{
                Integer oldFormId = getFormIdForNameAndVersion(currentMetaDataDetail.getFormName(), currentMetaDataDetail.getVersion());
                if(formDetailsLine.getVersion() > currentMetaDataDetail.getVersion()) {
                    OldAndNewFormDetails oldAndNewFormDetails = new OldAndNewFormDetails();
                    if(oldFormId == null) throw new Exception("could not find form details for name/version" +
                            currentMetaDataDetail.getFormName() + "/" + currentMetaDataDetail.getVersion());
                    oldAndNewFormDetails.setOldFormLabel(getFormResourceDetailsForId(oldFormId));
                    oldAndNewFormDetails.setNewFormLabel(getFormResourceDetailsForId(formDetailsLine.getFormId()));
                    oldAndNewFormDetails.setHighestVersion(formDetailsLine.getVersion());
                    allTableConflicts.add(oldAndNewFormDetails);
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        String row = "";
        if(CollectionUtils.isEmpty(allTableConflicts) && CollectionUtils.isEmpty(createTableForForms)) {
            return "No Conflicts found";
        }
        if(!CollectionUtils.isEmpty(createTableForForms)){
            stringBuilder.append("\n");
            stringBuilder.append("##########Create Table##########");
            stringBuilder.append("\n");
            for(FormDetails line: createTableForForms) {
                stringBuilder.append(row);
                row = "\n";
                stringBuilder.append("|form name:").append(line.getFormName()).append("|Table does not exist")
                        .append(commandString(line.getFormName(), line.getVersion()));
            }
        }
        if(!CollectionUtils.isEmpty(allTableConflicts)) {
            stringBuilder.append("\n");
            stringBuilder.append("##########Update Table##########");
            stringBuilder.append("\n");
            for (OldAndNewFormDetails line : allTableConflicts) {
                stringBuilder.append(row);
                row = "\n";
                stringBuilder.append(displayColumnConflicts(line.getOldFormLabel().getValue(), line.getNewFormLabel()
                        .getValue(), line.getHighestVersion(), line.getNewFormLabel().getType()));
            }
        }
        return stringBuilder.toString();
    }

    public String displayColumnConflicts(String oldFormPath, String newFormPath, Integer highestVersion,
                                         String nameOfTheForm) throws IOException {
        //TODO: need to check correct format before string manipulation and currently both form paths are hardcoded
        TableInformation oldTableInformation = getFormColumns(oldFormPath);
        TableInformation newTableInformation = getFormColumns(newFormPath);
        HashSet<String> oldFormColumns = oldTableInformation.getColumns();
        HashSet<String> newFormColumns = newTableInformation.getColumns();
        HashSet<String> missingColumns = new HashSet<>();
        for(String newFormColumn : newFormColumns){
            if(!oldFormColumns.contains(newFormColumn)) missingColumns.add(newFormColumn);
        }
//        if(missingColumns.isEmpty()) return "";
        return "|form name:" + nameOfTheForm +
                "| missing columns:" + missingColumns + "| Highest Version available:" + highestVersion +
                commandString(nameOfTheForm, highestVersion);
    }

    private String commandString(String nameOfTheForm, Integer highestVersion) {
        return "|command :fix_form --form_name \""+nameOfTheForm+"\" --dry_run false";
    }

    public String fixForm(String formName, Boolean showQueryOnly) throws Exception {
        FormDetails currentMetaDataDetail = findFormMetaDataDetailsForName(formName);
        if(currentMetaDataDetail != null) {
            Integer oldFormId = getFormIdForNameAndVersion(currentMetaDataDetail.getFormName(), currentMetaDataDetail.getVersion());
//            Integer newFormId = getFormIdForNameAndVersion(formName, newVersion);
            FormDetails newFormDetail = getFormIdForNameAndHighestPublishedVersion(formName);
            Integer newFormId = newFormDetail.getFormId();
            if (oldFormId == null || newFormId == null) throw new Exception("could not upgrade form");
            TableInformation oldTableInformation = getFormColumns(getFormResourceDetailsForId(oldFormId).getValue());
            TableInformation newTableInformation = getFormColumns(getFormResourceDetailsForId(newFormId).getValue());
            HashSet<String> oldFormColumns = oldTableInformation.getColumns();
            HashSet<String> newFormColumns = newTableInformation.getColumns();
            HashSet<String> missingColumns = new HashSet<>();
            for (String newFormColumn : newFormColumns) {
                if (!oldFormColumns.contains(newFormColumn)) missingColumns.add(newFormColumn);
            }
            String generateAlterQuery = null;
            if (!CollectionUtils.isEmpty(missingColumns)) {
                generateAlterQuery = generateAlterQuery(missingColumns, oldTableInformation.getTableName());
            }
            if(!showQueryOnly) {
                if (generateAlterQuery != null) {
                    executeQuery(generateAlterQuery);
                    updateMetaDataTable(formName, newFormDetail.getVersion());
                } else {
                    updateMetaDataTable(formName, newFormDetail.getVersion());
                    return "form name : "+ formName +"| No additional columns were needed and therefore only the version is updated";
                }
            }
            return generateAlterQuery != null ? "form name: " + " query: "+ generateAlterQuery : "No additional columns" +
                    " needed, " +
                    "Rerun without show_query_only flag for changes to be applied";
        }else{
            return createTableForForm(formName, showQueryOnly);
        }
    }

    private String generateAlterQuery(HashSet<String> missingColumns, String tableName) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("ALTER TABLE ").append(tableName);
        String prefix = "";
        for(String column : missingColumns){
            queryString.append(prefix);
            prefix = ",";
            queryString.append(" ADD ").append(column).append(" varchar");
        }
        return queryString.toString();
    }

    public TableInformation getFormColumns(String formName) throws IOException {
        TableInformation tableInformation = new TableInformation();
        HashSet<String> columns = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();
//        local only
//        Forms forms = AnalyticsUtil.parseForm(mapper.readTree(AnalyticsService.class.getClassLoader().getResource(formName)));
        Forms forms = AnalyticsUtil.parseForm(formName);
        Map<String, FormTable> obsWithConcepts = new HashMap<>();
        AnalyticsUtil.handleObsControls(forms.getControls(), obsWithConcepts, forms.getName(), null);
        if (obsWithConcepts.containsKey(forms.getName())) {
            for (FormConcept concept : obsWithConcepts.get(forms.getName()).getConcepts()) {
                String name = AnalyticsUtil.generateColumnName(concept.getName());
                columns.add(name);
            }
        }
        tableInformation.setColumns(columns);
        tableInformation.setTableName(AnalyticsUtil.generateColumnName(forms.getName()));
        return tableInformation;
    }

    private String createTableForForm(String formName, boolean showQueryOnly) throws IOException {
//        Integer formId = getFormIdForNameAndVersion(formName, highestVersion);
        FormDetails newFormDetail = getFormIdForNameAndHighestPublishedVersion(formName);
        Integer formId = newFormDetail.getFormId();
        String formPath = getFormResourceDetailsForId(formId).getValue();
        String createQuery = AnalyticsUtil.generateCreateTableForForm(formPath);
        if(!showQueryOnly){
            executeQuery(createQuery);
            insertIntoMetaDataTable(formName, newFormDetail.getVersion());
            return "|table created for form : " + formName + "|query :"+ createQuery;
        }
        return "form name :" + formName + " query: " + createQuery +
                "Rerun without show_query_only flag for changes to be applied";
    }

}
