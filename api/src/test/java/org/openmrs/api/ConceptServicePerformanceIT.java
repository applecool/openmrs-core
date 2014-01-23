/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.openmrs.test.OpenmrsMatchers.hasId;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.openmrs.Concept;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;

/**
 * Performance tests for {@link ConceptService}.
 */
@SkipBaseSetup
@BenchmarkHistoryChart
public class ConceptServicePerformanceIT extends BaseContextSensitiveTest {
	
	@Rule
	public TestRule benchmarkRule = new BenchmarkRule();
	
	@Resource(name = "conceptService")
	ConceptService conceptService;
	
	static boolean dictionaryLoaded = false;
	
	@Before
	public void loadDictionary() throws Exception {
		if (!dictionaryLoaded) {
			executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
			executeLargeDataSet("org/openmrs/contrib/mvpconceptdictionary/dbunit.xml");
			
			authenticate();
			
			getConnection().commit();
			
			dictionaryLoaded = true;
		}
	}
	
	@Test
	public void shouldTestGetConceptsByName() {
		List<Concept> concepts = conceptService.getConceptsByName("hiv positive");
		
		assertThat(concepts, containsInAnyOrder(hasId(138571), hasId(159804)));
	}
	
	@Test
	public void shouldTestGetConceptByName() {
		Concept concept = conceptService.getConceptByName("hiv positive");
		
		assertThat(concept, hasId(138571));
	}
}
