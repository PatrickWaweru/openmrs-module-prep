/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.prep.reporting.data.converter.definition.evaluator.prep;

import org.openmrs.annotation.Handler;
import org.openmrs.module.prep.reporting.data.converter.definition.prep.PrEPVisitDateDataDefinition;
import org.openmrs.module.prep.reporting.data.converter.definition.prep.PrEPVisitMonthDataDefinition;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.evaluator.EncounterDataEvaluator;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Evaluates PrEPVisitMonth EncounterDataEvaluator
 */
@Handler(supports = PrEPVisitMonthDataDefinition.class, order = 50)
public class PrEPVisitMonthDataEvaluator implements EncounterDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	public EvaluatedEncounterData evaluate(EncounterDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedEncounterData c = new EvaluatedEncounterData(definition, context);
		
		String qry = "select  v.encounter_id as encounter_id,timestampdiff(MONTH,e.visit_date,v.visit_date) as visit_month  from kenyaemr_etl.etl_prep_followup v\n"
		        + "inner join kenyaemr_etl.etl_prep_enrolment e on e.patient_id=v.patient_id\n"
		        + "where v.form='prep-consultation' group by encounter_id\n"
		        + "UNION\n"
		        + "select  r.encounter_id as encounter_id,timestampdiff(MONTH,e.visit_date,r.visit_date) as visit_month from kenyaemr_etl.etl_prep_monthly_refill r\n"
		        + "inner join kenyaemr_etl.etl_prep_enrolment e on e.patient_id=r.patient_id\n" + "group by encounter_id;";
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(qry);
		Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
