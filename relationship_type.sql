INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (1, 'Doctor', 'Patient', 0, 0, 'Relationship from a primary care provider to the patient', 1, '2015-08-27 10:41:04', 1, 1, '2016-01-15 22:59:25', 'Not needed', '03ecf59a-4c7a-11e5-9192-080027b662ec');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (2, 'Sibling', 'Sibling', 0, 0, 'Relationship between brother/sister, brother/brother, and sister/sister', 1, '2015-08-27 10:41:04', 0, null, null, null, '03ed3084-4c7a-11e5-9192-080027b662ec');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (3, 'Parent', 'Child', 0, 0, 'Relationship from a mother/father to the child', 1, '2015-08-27 10:41:04', 1, 1, '2016-01-15 22:59:33', 'n', '03ed79c5-4c7a-11e5-9192-080027b662ec');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (4, 'Father', 'Child', 0, 0, 'Relationship from a father to the child', 1, '2016-01-15 22:56:10', 0, null, null, null, '0a9d76f5-c5a8-4539-b744-f0a6a98ed7bb');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (5, 'Mother', 'Child', 0, 0, 'Relationship from a mother to the child', 1, '2016-01-15 22:56:28', 0, null, null, null, 'd8d0a4b9-e01b-4959-9bd6-d2770005778e');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (6, 'Son', 'Parent', 0, 0, 'Relationship from a son to mother/father', 1, '2016-01-15 23:01:00', 0, null, null, null, '6dd444f4-563c-47fb-803d-bbf9d134cd07');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (7, 'Daughter', 'Parent', 0, 0, 'Relationship from a Daughter to mother/father', 1, '2016-01-15 23:01:27', 0, null, null, null, 'f71576ae-5307-4454-ba16-6996e37b54c8');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (8, 'Care Taker', 'Patient', 0, 0, 'Care Taker', 4, '2016-02-19 12:27:31', 0, null, null, null, 'cd2c38fd-a265-4963-b890-a7f32d4ee5a7');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (9, 'Brother', 'Brother', 0, 0, 'Brother-Brother', 4, '2016-03-30 17:46:15', 0, null, null, null, '41b67da6-f70f-4f96-b44b-d86f94e95e81');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (10, 'Brother in-law', 'Brother in-law', 0, 0, 'Brother in law', 4, '2016-03-30 17:46:43', 0, null, null, null, '53853154-259c-4827-882b-42ec266a95dd');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (11, 'Daughter in-law', 'Parent', 0, 0, 'Daughter in-law', 4, '2016-03-30 17:47:10', 0, null, null, null, '110dafb6-a763-4652-980a-622c6fcff570');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (12, 'Father in-law', 'Child', 0, 0, 'Father in-law', 4, '2016-03-30 17:47:45', 0, null, null, null, 'cd50bcc7-9e21-4aef-946a-69aef3c5a1f8');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (13, 'Grand Daughter', 'Grand Parent', 0, 0, 'Grand Daughter', 4, '2016-03-30 17:49:04', 0, null, null, null, '12484415-02e2-4014-a9d9-b8e9ccc84743');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (14, 'Grand Father', 'Grand Child', 0, 0, 'Grand Father', 4, '2016-03-30 17:49:44', 0, null, null, null, '458917bf-2757-472e-b1e0-c343474061e6');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (15, 'Grand Mother', 'Grand Child', 0, 0, 'Grand Mother', 4, '2016-03-30 17:50:02', 0, null, null, null, 'ca20784c-a26c-40f1-84be-1a0c4aa31787');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (16, 'Grand Son', 'Grand Father', 0, 0, 'Grand Son', 4, '2016-03-30 17:50:19', 0, null, null, null, 'd17023c8-b879-4535-baf2-1abecd5f69cc');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (17, 'Husband', 'Wife', 0, 0, 'Husband', 4, '2016-03-30 17:50:57', 0, null, null, null, '1ab7737d-c4d3-42ed-90b0-26e677e5d6f1');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (18, 'Mother in-law', 'Parent', 0, 0, 'Mother in-law', 4, '2016-03-30 17:51:24', 0, null, null, null, '20e3a4a1-c62a-4b1b-a8d4-2cb748258dce');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (19, 'Sister', 'Sibling', 0, 0, 'Sister', 4, '2016-03-30 17:51:44', 0, null, null, null, '5caec481-a34a-4569-bb76-d24e23ac1bd5');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (20, 'Son in-law', 'Parent', 0, 0, 'Son in-law', 4, '2016-03-30 17:52:02', 0, null, null, null, '181401c8-6aba-4f9d-8e54-dfdfad8eee7f');
INSERT INTO openmrs.relationship_type (relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid) VALUES (21, 'Wife', 'Husband', 0, 0, 'Wife', 4, '2016-03-30 17:52:26', 0, null, null, null, '7af9067d-6921-4b24-a898-eedd70e73a4a');
