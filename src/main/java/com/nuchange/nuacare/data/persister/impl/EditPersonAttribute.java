package com.nuchange.nuacare.data.persister.impl;

import com.nuchange.nuacare.data.persister.LineProcessor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by sandeepe on 28/09/16.
 */
@Component
public class EditPersonAttribute extends LineProcessor {
    private int noOfColumns = 0;
    private int lineNo = 1;
    private String[] headers = null;
    private static Set<String> supportedBands = new HashSet<String>(
            (Collection<? extends String>) Arrays.asList("Band 1","Band 2","Band 3","Band 4","Band 0"));
    private static Set<String> supportedPensionTypes = new HashSet<String>(
            (Collection<? extends String>) Arrays.asList("SP","WP","NP","OFP","RH","DSG","GCSPF","FP"));
    private static Set<String> supportedRelations = new HashSet<String>(
            (Collection<? extends String>) Arrays.asList("Self","Widow","Parent","Wife","Child","Son","Daughter","Father","Mother","Husband"));
    private Map<String, Integer> attributeTypeIdMap = new HashMap<>();

    @Override
    public String initValidation(String[] line) {
        if (line.length < 2) {
            return "You need atleast 2 columns in the csv file to process..";
        }
        if (!"gwsId".equals(line[0])){
            return "The first column should have the header gwsId, " +
                    "which is going to be the key against which other attributes will be updated";
        }
        for (String s : line) {
            String sql = "select count(person_attribute_type_id) from person_attribute_type where name = ?";
            int count = jdbcTemplate.queryForObject(
                    sql, new Object[] { s }, Integer.class);

            if (count < 1) {
                return "No attribute found in the system for, " + s;
            }
            if (count > 1) {
                return "More than one attribute found in the system for, " + s;
            }
        }
        headers = line;
        noOfColumns = line.length;
        return null;
    }

    private boolean isBandValid(String value){
        return supportedBands.contains(value);
    }

    private boolean isRelationValid(String value){
        return supportedRelations.contains(value);
    }

    private boolean isPensionTypeValid(String value){
        return supportedPensionTypes.contains(value);
    }

    @Override
    public String validateLine(String[] line) {
        StringBuffer error = new StringBuffer();
        int index = 0;
        for (String token : line) {
            token = token.trim();
            if (StringUtils.isBlank(token)) {
                error.append("Value empty for attribute "+headers[index]);
                continue;
            }
            if ("gwsId".equals(headers[index])) {
                String sql = "select count(person_id) from person_attribute where person_attribute_type_id in " +
                        "(select person_attribute_type_id from person_attribute_type where name = ?) and value=?";
                int count = jdbcTemplate.queryForObject(
                        sql, new Object[] { "gwsId", token }, Integer.class);
                if (count < 1) {
                    error.append("No patient found in the system for gwsId, " + token +"\n");
                }
                if (count > 1) {
                    error.append("More than one patient found in the system for gwsId, " + token +"\n");
                }
            }
            if ("pensionType".equals(headers[index])) {
                if (!isPensionTypeValid(token)) {
                    error.append("Data not valid for pensionType, " + token +"\n");
                }
            }
            if ("relation".equals(headers[index])) {
                if (!isRelationValid(token)) {
                    error.append("Data not valid for relation, " + token +"\n");
                }
            }
            if ("pensionBand".equals(headers[index])) {
                if (!isBandValid(token)) {
                    error.append("Data not valid for pensionBand, " + token +"\n");
                }
            }
            index++;
        }
        lineNo++;
        return error.toString();
    }

    @Override
    public String processLine(String[] line) {
        final String[] inputString = line;
        int index = 0;
        //Check for identifier uniqueness

        Integer gwsIdTypeId = attributeTypeIdMap.get("gwsId");
        String sql = "select person_id from person_attribute where person_attribute_type_id = ? " +
                " and value=?";
        int pId = jdbcTemplate.queryForObject(
                sql, new Object[] { gwsIdTypeId, line[index++] }, Integer.class);
        if (pId < 1){
            return "Person with gwsId "+line[0]+" does not found in the system";
        }
        String uuidsql = "select uuid from person where person_id = ? ";
        String uuid = jdbcTemplate.queryForObject(
                uuidsql, new Object[] { pId }, String.class);

        List<String> batchSqls = new ArrayList<>(noOfColumns - 1);
        while (index < noOfColumns) {
            String attribSql = "select count(person_id) from person_attribute where person_attribute_type_id = ? and person_id=? ";
            Integer attrTyId = attributeTypeIdMap.get(headers[index]);
            int count = jdbcTemplate.queryForObject(
                    attribSql, new Object[] { attrTyId, pId }, Integer.class);
            if (count > 1) {
                return "More than one value for the same attribute in the system for attribute, "
                        + headers[index] +" for person "+pId+" \n";
            }
            String value = line[index];
            if (count < 1) {
                // insert
                batchSqls.add("INSERT INTO person_attribute " +
                        "(person_id, value, person_attribute_type_id, creator, date_created, voided, uuid) values " +
                        "( " + pId + " , '" + value + "', " + attrTyId+
                        ", 4,now(),0,uuid());");

            } else {
                // update
                attribSql = "select person_attribute_id from person_attribute where person_attribute_type_id = ? and person_id=? ";
                int pKey = jdbcTemplate.queryForObject(
                        attribSql, new Object[] { attrTyId, pId }, Integer.class);
                batchSqls.add("update person_attribute set value = '" + value +
                        "' where person_attribute_id="+pKey);
            }
            index++;
        }
//Raise event for person..
        String patientFhirEvent = "insert into event_records_queue (uuid, category, object, tags, timestamp, title, uri) values (uuid(), 'FhirPatientExt', '" +
                uuid+"', null, now(), 'FhirPatientExt', null)";
        batchSqls.add(patientFhirEvent);
        String TEMPLATE = "/openmrs/ws/rest/v1/patient/%s?v=full";
        String patientEvent = "insert into event_records_queue (uuid, category, object, tags, timestamp, title, uri) values (uuid(), 'patient', '" +
                String.format(TEMPLATE, uuid)+"', null, now(), 'Patient', null)";
        batchSqls.add(patientEvent);
        jdbcTemplate.batchUpdate(batchSqls.toArray(new String[batchSqls.size()]));

        return null;


    }


    public void postProcess() {
        System.out.println("postProcess()");
    }

    public void preProcess() {
        System.out.println("processing line " + lineNo++);
        System.out.println("preProcess()");

    }

    public void init(String[] line) {
        System.out.println("init()");
        for (String s : line) {
            String sql = "select person_attribute_type_id from person_attribute_type where name = ?";
            int count = jdbcTemplate.queryForObject(
                    sql, new Object[] { s }, Integer.class);
            attributeTypeIdMap.put(s, count);
        }
        headers = line;
        noOfColumns = line.length;
    }

    public void finish() {
        System.out.println("finish()");
    }

}