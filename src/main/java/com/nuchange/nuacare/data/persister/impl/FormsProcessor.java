package com.nuchange.nuacare.data.persister.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuchange.nuacare.data.persister.LineProcessor;
import com.nuchange.psiutil.model.FormConcept;
import com.nuchange.psiutil.model.FormControl;
import com.nuchange.psiutil.model.Forms;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class FormsProcessor extends LineProcessor {

    private final String[] header = {"obs_id", "person_id", "concept_id", "encounter_id", "order_id", "obs_datetime", "location_id", "obs_group_id", "accession_number", "value_group_id", "value_coded", "value_coded_name_id", "value_drug", "value_datetime", "value_numeric", "value_modifier", "value_text", "value_complex", "comments", "creator", "date_created", "voided", "voided_by", "date_voided", "void_reason", "uuid", "previous_version", "form_namespace_and_path", "status", "interpretation"};

    private Map<String, String> formIdMap = new HashMap<>();
    private Map<Integer, String> conceptUuidMap = new HashMap<>();
    private String locationName = "Bahmni^";
    private String versionString = ".1/";
    List<String> batchSqls = new ArrayList<>();

    @Override
    public String processLine(String[] line) {
        String id = line[0]; //obs_id
        String conceptId = line[2]; //concept_id
        String coded = line[10];//"value_coded"
        String drug = line[12];//"value_drug"
        String datetime = line[13];//"value_datetime"
        String numeric = line[14];//"value_numeric"
        String text = line[16];//"value_text"
        String complex = line[17];//"value_complex"
        String formNameSpaceAndPath = getFormNameSpaceAndPath(Integer.parseInt(conceptId));
        if (coded != null || drug != null || datetime != null || numeric != null || text != null || complex != null) {
//            batchSqls.add("update obs set form_namespace_and_path = \"" + formNameSpaceAndPath + "\" where obs_id = " + id);
            jdbcTemplate.update("update obs set form_namespace_and_path = \"" + formNameSpaceAndPath + "\" where obs_id = " + id);
        }
        return null;
    }

    private String getFormNameSpaceAndPath(Integer conceptId){
        if (conceptId != null) {
            String conceptUuid = conceptUuidMap.get(conceptId);
            if(conceptUuid!=null) {
                return formIdMap.get(conceptUuid);
            }
            String sql = "select uuid from concept where concept_id = ?";
            String uuid = jdbcTemplate.queryForObject(
                    sql, new Object[] { conceptId }, String.class);
            conceptUuidMap.put(conceptId, uuid);
            return formIdMap.get(uuid);
        }
        return null;
    }
    public void generateMap(String path){
        JsonNode array = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            array = objectMapper.readValue(new File(path), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parseForm(array);
    }


     private void parseForm(JsonNode array){
        JsonNode resources = array.get("formJson").get("resources");
        String value = resources.get(0).get("value").toString();
        ObjectMapper mapper = new ObjectMapper();
        Forms c = null;
        try {
            c = mapper.readValue(value.replace("\\", "").replaceAll("^\"|\"$", ""), Forms.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(FormControl control : c.getControls()){
            parseObj(control, c.getName());
        }
    }

     private void parseObj(FormControl control, String formName) {

            if(!CollectionUtils.isEmpty(control.getControls())){
                for(FormControl innerControl : control.getControls()){
                    if(innerControl.getControls()!=null){
                        parseObj(innerControl, formName);
                    }
                    else{
                        FormConcept concept = innerControl.getConcept();
                        if(concept!=null){
                            //Bahmni^testform3.2/2-0
                            String formNameSpaceAndPath = locationName + formName + versionString +innerControl.getId()+"-0";
                            formIdMap.put(concept.getUuid(), formNameSpaceAndPath);
                        }
                    }
                }
            }
    }
}
