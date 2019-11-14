package com.nuchange.nuacare.data.persister.impl;

import com.nuchange.nuacare.data.persister.LineProcessor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;

@Component
public class ConditionsProcessor extends LineProcessor {

    private final String[] header = {"patient identifier","diagnosis concept id","date active since"};

    @Override
    public String processLine(String[] line) {

        // patient Id
        String patientIdentifier = line[0];
        String sql = "select patient_id from patient_identifier where identifier = ?)";
        Integer patientId = jdbcTemplate.queryForObject(sql, Integer.class, patientIdentifier);

        // concept Id
        int conceptId = Integer.parseInt(line[1]);
        if (conceptId <= 0) {
            return "Invalid Concept Id" + conceptId;
        }

        // onset date
        String onsetDateString = line[2];
        Timestamp onsetDate = null;
        try {
            onsetDate = getSqlDateValue(onsetDateString, "yyyy-MM-dd hh:mm:ss");
        } catch (ParseException e) {
            return e.getMessage();
        }

        if (patientId == null || patientId <= 0) {
            return "Patient not found with identifier : " + patientIdentifier;
        }

        String query = "INSERT INTO conditions (patient_id, status, concept_id, onset_date, creator, date_created, voided, uuid) VALUES (?, 'ACTIVE', ?, ?, 2, now(), 0, UUID())";
        jdbcTemplate.update(query, patientId, conceptId, onsetDate);

        return null;
    }

    @Override
    public String initValidation(String[] line) {
        return validateHeader(line, header);
    }
}
