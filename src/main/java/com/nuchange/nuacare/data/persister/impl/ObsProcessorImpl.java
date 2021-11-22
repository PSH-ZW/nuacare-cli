package com.nuchange.nuacare.data.persister.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuchange.nuacare.data.persister.LineProcessor;
import com.nuchange.nuacare.data.persister.ObsProcessor;
import com.nuchange.nuacare.data.persister.domain.FormConcept;
import com.nuchange.nuacare.data.persister.domain.FormControl;
import com.nuchange.nuacare.data.persister.domain.Forms;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;


@Component
public class ObsProcessorImpl extends JdbcDaoSupport implements ObsProcessor {

    private Map<String, String> formIdMap = new HashMap<>();
    private Map<Integer, String> conceptUuidMap = new HashMap<>();
    private Map<Integer, List<Integer>> conceptObsMap = new HashMap<>();
    private String locationName = "Bahmni^";
    private String versionString = ".1/";
    List<String> batchSqls = new ArrayList<>();
    Map<Integer, String> obsFnspMap = new HashMap<>();


    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    private TransactionTemplate transactionTemplate;


    @Autowired
    ObsProcessorImpl(DriverManagerDataSource dataSource, TransactionTemplate transactionTemplate){
        super();
        this.setDataSource(dataSource);
        this.setTransactionTemplate(transactionTemplate);
    }

    private String getFormNameSpaceAndPath(Integer conceptId){
        if (conceptId != null) {
            String conceptUuid = conceptUuidMap.get(conceptId);
            if(conceptUuid!=null) {
                return formIdMap.get(conceptUuid);
            }
            String sql = "select uuid from concept where concept_id = ?";
            String uuid = getJdbcTemplate().queryForObject(
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
        String version = array.get("formJson").get("version").asText();
        if(!StringUtils.isEmpty(version)){
            versionString = "." + version + "/";
        }
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
        String formNameSpaceAndPath = locationName + formName + versionString +control.getId()+"-0";
        if(control.getConcept()!=null) {
            formIdMap.put(control.getConcept().getUuid(), formNameSpaceAndPath);
        }
        if(!CollectionUtils.isEmpty(control.getControls())){
            for(FormControl innerControl : control.getControls()){
                if(innerControl.getControls()!=null){
                    parseObj(innerControl, formName);
                }
                else{
                    FormConcept concept = innerControl.getConcept();
                    if(concept!=null){
                        //Bahmni^testform3.2/2-0
                        formNameSpaceAndPath = locationName + formName + versionString +innerControl.getId()+"-0";
                        formIdMap.put(concept.getUuid(), formNameSpaceAndPath);
                        if(concept.getUuid().equals("9d472fc0-ef3b-45b3-a44b-b1d16d60d44f")){
                            System.out.println("next");
                        }
                    }
                }
            }
        }
        else{
            FormConcept concept = control.getConcept();
            if(concept!=null){
                //Bahmni^testform3.2/2-0
                formNameSpaceAndPath = locationName + formName + versionString +control.getId()+"-0";
                formIdMap.put(concept.getUuid(), formNameSpaceAndPath);
            }
        }
    }

    @Override
    public void migrateForm(Integer conceptId, String path){
//        convert form --conceptId 4498 --json /home/nuchanger/Documents/PSI/form_json/HIVSTF.json
//        convert form --conceptId 6685 --json /home/nuchanger/Downloads/AddMoreForm_3.json
//        convert form --conceptId 3326 --json "/home/nuchanger/Documents/PSI/form_json_new/Provider HIV test counselling 1_1.json"

        generateMap(path);
        String sql = "select obs_id from obs where concept_id = ? and voided = false ;";
        final List<Map<String, Object>> observations = getJdbcTemplate().queryForList(
                sql, new Object[]{conceptId});
        List<String> obsId = new ArrayList<>();
        for(Map<String, Object> map : observations){
            obsId.add(map.get("obs_id").toString());
        }
        updateMembers(obsId);
        updateSql(conceptObsMap, batchSqls);
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsId.size(); i++) {

            sb.append(obsId.get(i));

            // if not the last item
            if (i != obsId.size() - 1) {
                sb.append(sep);
            }
        }
        String updateSql = "update obs set obs_group_id = null where obs_group_id in ( " + sb.toString() + " );";
        System.out.println(updateSql);
        String deleteSql = "delete from obs where concept_id = " + conceptId + " and voided = false ;";
        batchSqls.add(updateSql);
        updateAddMoreValues();
//        batchSqls.add(deleteSql);
        File file = new File("update.sql");
        try {
            FileUtils.writeLines(file, batchSqls, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getJdbcTemplate().batchUpdate(batchSqls.toArray(new String[batchSqls.size()]));
        batchSqls.clear();
        conceptUuidMap.clear();
        conceptObsMap.clear();
        formIdMap.clear();

    }

    private void updateMembers(List<String> obsId){
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsId.size(); i++) {

            sb.append(obsId.get(i));

            // if not the last item
            if (i != obsId.size() - 1) {
                sb.append(sep);
            }
        }
        String sql = "select obs_id, concept_id, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, " +
                "value_numeric, value_modifier, value_text, value_complex from obs where obs_group_id in (" + sb.toString() + ")";
        List<Map<String, Object>> members = new ArrayList<>();
        if(obsId.size()>0){
            members = getJdbcTemplate().queryForList(sql );
        }
        List<String> memberId  = new ArrayList<>();
        for(Map<String, Object> obs : members){
            if(obs.get("value_group_id") == null && obs.get("value_coded") == null && obs.get("value_coded_name_id") == null &&
                    obs.get("value_drug") == null && obs.get("value_datetime") == null && obs.get("value_modifier") ==null &&
                    obs.get("value_text") == null && obs.get("value_complex") == null  && obs.get("value_numeric") ==null){
                memberId.add((obs.get("obs_id").toString()));
            }
            Integer conceptId = (Integer) obs.get("concept_id");
            Integer obs_id = (Integer) obs.get("obs_id");

            if(!conceptObsMap.containsKey(conceptId)){
                conceptObsMap.put(conceptId, new ArrayList<Integer>());
            }
            List<Integer> obsList = conceptObsMap.get(conceptId);
            obsList.add(obs_id);
        }
        if(!CollectionUtils.isEmpty(memberId)){
            updateMembers(memberId);
        }
    }

    private void updateSql(Map<Integer, List<Integer>> obsId, List<String> batchSqls){

        for(Integer key : obsId.keySet()){
            List<Integer> obsGrp = obsId.get(key);
            String fnsp = getFormNameSpaceAndPath(key);

            String sep = ",";
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < obsGrp.size(); i++) {

                sb.append(obsGrp.get(i));

                // if not the last item
                if (i != obsGrp.size() - 1) {
                    sb.append(sep);
                }
            }

            if(!StringUtils.isEmpty(fnsp)){
                String sql = "update obs set form_namespace_and_path = \"" + fnsp + "\" where obs_id in ( " + sb.toString() + ");";
                System.out.println(sql + " " + batchSqls.size());
                batchSqls.add(sql);
            }
        }
    }

    private void updateAddMoreValues(){
        Integer conceptId = 7220;
        if(conceptUuidMap.containsKey(conceptId)) {
//            String fnsp = formIdMap.get(conceptUuidMap.get(conceptId)).replace("-0","-");
            String fnsp = formIdMap.get(conceptUuidMap.get(conceptId)).replace("-0","-");
            String sql = "select * from obs where concept_id = " + conceptId + " and voided = false ";
            List<Map<String, Object>> obs = getJdbcTemplate().queryForList(sql, new Object[]{});
            Map<Integer, List<Integer>> encounterObsMap = new HashMap<>();
            for (Map<String, Object> o : obs) {
                Integer obsId = Integer.parseInt(o.get("obs_id").toString());
                Integer encounterId = Integer.parseInt(o.get("encounter_id").toString());
                if (!encounterObsMap.containsKey(encounterId)) {
                    encounterObsMap.put(encounterId, new ArrayList<Integer>());
                    encounterObsMap.get(encounterId).add(obsId);
                } else {
                    Integer index = (encounterObsMap.get(encounterId).size());
                    sql = "update obs set form_namespace_and_path = \"" + fnsp + index + "\" where obs_id = " + obsId ;
                    batchSqls.add(sql);
                }
            }
        }
    }


    @Override
    public void migrateProgramForm(Integer conceptId, String path) {
        //        convert form --conceptId 4498 --json /home/nuchanger/Documents/PSI/form_json/HIVSTF.json
        //        convert form --conceptId 6353 --json /home/nuchanger/Documents/PSI/form_json/Art initial Visit compulsory Question 1 of 2 new_1.json
        //        convert form --conceptId 6353 --json /home/nuchanger/Downloads/AddMoreForm_3.json

        generateMap(path);
        Map<Integer, List<Integer>> encounterObsMap = new HashMap<>();
        String sql = "select * from obs where concept_id = ? and voided = false ;";
        final List<Map<String, Object>> observations = getJdbcTemplate().queryForList(
                sql, new Object[]{conceptId});
        List<String> obsId = new ArrayList<>();
        for(Map<String, Object> map : observations){
            Integer id = Integer.parseInt(map.get("obs_id").toString());
            Integer encounterId = Integer.parseInt(map.get("encounter_id").toString());
            String fnsp="";
            if (!encounterObsMap.containsKey(encounterId)) {
                encounterObsMap.put(encounterId, new ArrayList<Integer>());
                encounterObsMap.get(encounterId).add(id);
                Integer index = (encounterObsMap.get(encounterId).size());
                fnsp = getFormNameSpaceAndPath(conceptId).replace("-0","-");
                String val = fnsp + (index-1);
                obsFnspMap.put(id,val);
            } else {
                encounterObsMap.get(encounterId).add(id);
                Integer index = (encounterObsMap.get(encounterId).size());
                fnsp = formIdMap.get(conceptUuidMap.get(conceptId)).replace("-0","-");
                String val = fnsp + (index-1);
                obsFnspMap.put(id,val);
            }
            sql = "update obs set form_namespace_and_path = \"" + obsFnspMap.get(id) + "\" where obs_id = " + id + ";";
            batchSqls.add(sql);
            obsId.add(map.get("obs_id").toString());
        }
        updateProgramMembers(obsId);
        updateProgramsSql(conceptObsMap, batchSqls);
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsId.size(); i++) {

            sb.append(obsId.get(i));

            // if not the last item
            if (i != obsId.size() - 1) {
                sb.append(sep);
            }
        }
        String updateSql = "update obs set obs_group_id = null where obs_group_id in ( " + sb.toString() + " );";
        System.out.println(updateSql);
        String deleteSql = "delete from obs where concept_id = " + conceptId + " and voided = false ;";
        batchSqls.add(updateSql);
        updateAddMoreValues();
        batchSqls.add(deleteSql);
        File file = new File("update.sql");
        try {
            FileUtils.writeLines(file, batchSqls, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        getJdbcTemplate().batchUpdate(batchSqls.toArray(new String[batchSqls.size()]));
        batchSqls.clear();
        conceptUuidMap.clear();
        conceptObsMap.clear();
        formIdMap.clear();
    }

    private void updateProgramMembers(List<String> obsIdList){
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsIdList.size(); i++) {

            sb.append(obsIdList.get(i));

            // if not the last item
            if (i != obsIdList.size() - 1) {
                sb.append(sep);
            }
        }
        String sql = "select obs_id, concept_id, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, " +
                "value_numeric, value_modifier, value_text, value_complex, obs_group_id from obs where obs_group_id in (" + sb.toString() + ")";
        List<Map<String, Object>> members = new ArrayList<>();
        if(obsIdList.size()>0){
            members = getJdbcTemplate().queryForList(sql );
        }
        List<String> memberId  = new ArrayList<>();
        for(Map<String, Object> obs : members){
            if(obs.get("value_group_id") == null && obs.get("value_coded") == null && obs.get("value_coded_name_id") == null &&
                    obs.get("value_drug") == null && obs.get("value_datetime") == null && obs.get("value_modifier") ==null &&
                    obs.get("value_text") == null && obs.get("value_complex") == null  && obs.get("value_numeric") ==null){
                memberId.add((obs.get("obs_id").toString()));
            }
            Integer conceptId = (Integer) obs.get("concept_id");
            Integer obsId = (Integer) obs.get("obs_id");

            if(!conceptObsMap.containsKey(conceptId)){
                conceptObsMap.put(conceptId, new ArrayList<Integer>());
            }
            List<Integer> obsList = conceptObsMap.get(conceptId);
            obsList.add(obsId);

            Integer obsGroupId = Integer.parseInt(obs.get("obs_group_id").toString());
            String value = obsFnspMap.get(obsGroupId);
            obsFnspMap.put(obsId, value);
        }
        if(!CollectionUtils.isEmpty(memberId)){
            updateMembers(memberId);
        }
    }

    private void updateProgramsSql(Map<Integer, List<Integer>> obsId, List<String> batchSqls){

        for(Integer key : obsId.keySet()){
            List<Integer> obsGrp = obsId.get(key);
            String fnsp = getFormNameSpaceAndPath(key);
            for (int i = 0; i < obsGrp.size(); i++) {
                Integer id = obsGrp.get(i);
                String pre = obsFnspMap.get(id) + '/' + fnsp.substring(fnsp.lastIndexOf('/')+1);
                String sql = "update obs set form_namespace_and_path = \"" + pre + "\" where obs_id = " + id + ";";
                System.out.println(sql + " " + batchSqls.size());
                batchSqls.add(sql);
            }
        }
    }

}
