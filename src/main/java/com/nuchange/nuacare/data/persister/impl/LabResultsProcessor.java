package com.nuchange.nuacare.data.persister.impl;

import com.nuchange.nuacare.data.persister.LineProcessor;
import com.nuchange.nuacare.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@Component
public class LabResultsProcessor extends LineProcessor {

    private final String[] header = {"patient identifier","ordered test name","ordered date","ordered by","result value","results available on"};

    /*     get order date
     find analysis with date, patientId, conceptId
     if analysis found :
                 update analysis status = 6 (finalized)
                 create test_result for analysis
    */
    @Override
    public String processLine(String[] line) {
        RowMapper<List<Object>> rowMapper = Utils.getRowMapperByColumnCount(1);

        // PATIENT ID
        String patientIdentifier = line[0];
        if (StringUtils.isEmpty(patientIdentifier)) {
            return " Patient Identifier can not be empty";
        }
        String patientSql = "select patient_id from patient_identifier where identifier = ?";
        int patientId = 0;
//        Integer patientId = jdbcTemplate.queryForObject(patientSql, Integer.class, patientIdentifier);
        List<List<Object>> patientIdResults = jdbcTemplate.query(patientSql, rowMapper, patientIdentifier);

        if (patientIdResults.isEmpty()) {
            return "Patient not found for identifier : " + patientIdentifier;
        } else if (patientIdResults.size() > 1) {
            return "More than one patients found for identifier : " + patientIdentifier;
        } else {
            patientId = (int) patientIdResults.get(0).get(0);
        }

        // TEST CONCEPT ID
        String conceptName = line[1];
        if (StringUtils.isEmpty(conceptName)) {
            return "Test name can not be empty";
        }
        String conceptSql = "select concept_id from concept_name cn inner join concept c on c.concept_id = cn.concept_id " +
                "where name = ? and cn.locale = 'en' and cn.concept_name_type = 'FULLY_SPECIFIED' and c.class_id = 31";
//        Integer conceptId = jdbcTemplate.queryForObject(conceptSql, Integer.class, conceptName);
        Integer conceptId = 0;
        List<List<Object>> conceptIdResults = jdbcTemplate.query(conceptSql, rowMapper, conceptName);

        if (conceptIdResults.isEmpty()) {
            return "Test Concept not found for name : " + conceptName;
        } else if (conceptIdResults.size() > 1) {
            return "More than one test concepts found for name : " + conceptName;
        } else {
            conceptId = (int) conceptIdResults.get(0).get(0);
        }

        // CONCEPT DATATYPE
        String conceptDataTypeSql = "select datatype_id from concept where concept_id = ?";
        Integer conceptDatatypeId = jdbcTemplate.queryForObject(conceptDataTypeSql, Integer.class, conceptId);

        // ORDERED DATE
        String orderedDateString = line[2];
        if (StringUtils.isEmpty(orderedDateString)) {
            return " Ordered Date can not be empty";
        }
        Timestamp orderedDate = null;
        try {
            orderedDate = getSqlDateValue(orderedDateString, "yyyy-MM-dd");
        } catch (ParseException e) {
            return e.getMessage();
        }

        String value = line[4];
        if (StringUtils.isEmpty(value)) {
            return " Result value can not be empty";
        }

        String flag = "NORMAL";
        if (conceptDatatypeId == 2) {
            String conceptAnswerSql = "select c.uuid from concept c inner join concept_name cn on cn.concept_id = c.concept_id where name = ? ";
//            String answerString = jdbcTemplate.queryForObject(conceptAnswerSql, String.class, value);
            List<List<Object>> conceptAnswerIdResults = jdbcTemplate.query(conceptAnswerSql, rowMapper, value);
            if (conceptAnswerIdResults.isEmpty()) {
                return "Result Concept not found for name : " + value;
            } else if (conceptAnswerIdResults.size() > 1) {
                return "More than one result concepts found for name : " + value;
            } else {
                value = (String) conceptAnswerIdResults.get(0).get(0);
            }
        } else if (conceptDatatypeId == 1) {
            String conceptAnswerSql = "select low_normal, high_normal from numeric_test_limits where test_id = ? ";
//            String answerString = jdbcTemplate.queryForObject(conceptAnswerSql, String.class, value);
            List<List<Object>> conceptAnswerIdResults = jdbcTemplate.query(conceptAnswerSql, Utils.getRowMapperByColumnCount(2), value);
            if (conceptAnswerIdResults.size() == 1) {
                Integer lowNormal = (Integer) conceptAnswerIdResults.get(0).get(0);
                Integer highNormal = (Integer) conceptAnswerIdResults.get(0).get(1);

                flag = getFlagForResult(value, lowNormal, highNormal);
            }
        }

        String analysisSql = "select max(analysis_id) from analysis a " +
                "inner join sample s on s.sample_id = a.sample_id inner join sample_acquisition sa on sa.sample_acquisition_id = s.sample_acquisition_id " +
                "where sa.patient_id = ? and a.test_id = ? and date(sa.date_created) = ? ";
//        Integer analysisId = jdbcTemplate.queryForObject(analysisSql, Integer.class, patientId, conceptId, orderedDate);
        Integer analysisId = 0;
        List<List<Object>> analysisIdResults = jdbcTemplate.query(analysisSql, rowMapper, patientId, conceptId, orderedDate);
        if (analysisIdResults.isEmpty()) {
            return "Analysis not found for patient : " + patientIdentifier + ", testConcept : " + conceptName + ", date : " + orderedDateString;
        } else if (analysisIdResults.size() > 1) {
            return "More than one Analysis found for patient : " + patientIdentifier + ", testConcept : " + conceptName + ", date : " + orderedDateString;
        } else {
            analysisId = (int) analysisIdResults.get(0).get(0);
            String updateAnalysisSql = "update analysis set status_id = 6 where analysis_id = ?";
            jdbcTemplate.update(updateAnalysisSql, analysisId);

            String testResultSql = "INSERT INTO test_result (test_result_id, value, flag, date_created, creator) " +
                    "VALUES (?, ?, ? , now(), 1)";
            jdbcTemplate.update(testResultSql, analysisId, value, flag);
        }

        System.out.println(Arrays.toString(line));
        return null;
    }

    private String getFlagForResult(String value, Integer lowNormal, Integer highNormal) {
        Integer result = Integer.parseInt(value);
        if (result >= lowNormal && result <= highNormal) {
            return "NORMAL";
        } else {
            return "ABNORMAL";
        }
    }

    @Override
    public String initValidation(String[] line) {
        return validateHeader(line, header);
    }
}