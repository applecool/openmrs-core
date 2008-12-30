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
package org.openmrs.logic.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * A result from the logic service. A result can be 0-to-n date-values pairs. You can treat the
 * result as a list or easily coerce it into a simple value as needed. When possible, results carry
 * references to more complex objects so that code that deals with results and has some prior
 * knowledge of the objects returned by a particular rule can more easily get to the full-featured
 * objects instead of the simplified values in the date-value pairs. TODO: eliminate unnecessary
 * methods (toDatetime(), getDatetime(), and getDate() should all do the same thing) TODO: better
 * support/handling of NULL_RESULT
 */
public class Result extends ArrayList<Result> {
	
	private static final long serialVersionUID = -5587574403423820797L;
	
	/**
	 * Core datatypes for a result. Each result is one of these datatypes, but can be easily coerced
	 * into the other datatypes. To promote flexibility and maximize re-usability of logic rules,
	 * the value of a result can be controlled individually for each datatype &mdash; i.e., specfic
	 * datatype representations of a single result can be overridden. For example, a result could
	 * have a <em>numeric</em> value of 0.15 and its text value could be overridden to be
	 * "15 percent" or "Fifteen percent."
	 */
	public enum Datatype {
		BOOLEAN, CODED, DATETIME, NUMERIC, TEXT
	}
	
	private Datatype datatype;
	
	private Date resultDatetime;
	
	private Boolean valueBoolean;
	
	private Concept valueCoded;
	
	private Date valueDatetime;
	
	private Double valueNumeric;
	
	private String valueText;
	
	private Object resultObject;
	
	private static final Result emptyResult = new EmptyResult();
	
	public Result() {
	}
	
	/**
	 * Builds result upon another result &mdash; the first step in create a result that contains a
	 * list of other results.
	 * 
	 * @param result the result that will be the sole member of the new result
	 */
	public Result(Result result) {
		if (result != null) {
			this.add(result);
		}
	}
	
	/**
	 * Builds a result from a list of results
	 * 
	 * @param list a list of results
	 */
	public Result(List<Result> list) {
		if (!(list == null || list.size() < 1))
			this.addAll(list);
	}
	
	/**
	 * Builds a boolean result with a result date of today
	 * 
	 * @param valueBoolean
	 */
	public Result(Boolean valueBoolean) {
		this(new Date(), valueBoolean, null);
	}
	
	/**
	 * Builds a boolean result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueBoolean
	 */
	public Result(Date resultDate, Boolean valueBoolean, Object obj) {
		this(resultDate, Datatype.BOOLEAN, valueBoolean, null, null, null, null, obj);
	}
	
	/**
	 * Builds a coded result with a result date of today
	 * 
	 * @param valueCoded
	 */
	public Result(Concept valueCoded) {
		this(new Date(), valueCoded, null);
	}
	
	/**
	 * Builds a coded result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueCoded
	 */
	public Result(Date resultDate, Concept valueCoded, Object obj) {
		this(resultDate, Datatype.CODED, null, valueCoded, null, null, null, obj);
	}
	
	/**
	 * Builds a coded result from an observation
	 * 
	 * @param obs
	 */
	public Result(Obs obs) {
		this(obs.getObsDatetime(), null, obs.getValueAsBoolean(), obs.getValueCoded(), obs.getValueDatetime(), obs
		        .getValueNumeric(), obs.getValueText(), obs);
		
		Concept concept = obs.getConcept();
		ConceptDatatype conceptDatatype = null;
		
		if (concept != null) {
			conceptDatatype = concept.getDatatype();
			
			if (conceptDatatype == null) {
				return;
			}
			if (conceptDatatype.isCoded())
				this.datatype = Datatype.CODED;
			else if (conceptDatatype.isNumeric())
				this.datatype = Datatype.NUMERIC;
			else if (conceptDatatype.isDate())
				this.datatype = Datatype.DATETIME;
			else if (conceptDatatype.isText())
				this.datatype = Datatype.TEXT;
			else if (conceptDatatype.isBoolean())
				this.datatype = Datatype.BOOLEAN;
		}
	}
	
	/**
	 * Builds a datetime result with a result date of today
	 * 
	 * @param valueDatetime
	 */
	public Result(Date valueDatetime) {
		this(new Date(), valueDatetime, null);
	}
	
	/**
	 * Builds a datetime result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueDatetime
	 */
	public Result(Date resultDate, Date valueDatetime, Object obj) {
		this(resultDate, Datatype.DATETIME, null, null, valueDatetime, null, null, obj);
	}
	
	/**
	 * Builds a numeric result with a result date of today
	 * 
	 * @param valueNumeric
	 */
	public Result(Double valueNumeric) {
		this(new Date(), valueNumeric, null);
	}
	
	/**
	 * Builds a numeric result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueNumeric
	 */
	public Result(Date resultDate, Double valueNumeric, Object obj) {
		this(resultDate, Datatype.NUMERIC, null, null, null, valueNumeric, null, obj);
	}
	
	/**
	 * Builds a numeric result with a result date of today
	 * 
	 * @param valueNumeric
	 */
	public Result(Integer valueNumeric) {
		this(new Date(), valueNumeric, null);
	}
	
	/**
	 * Builds a numeric result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueNumeric
	 */
	public Result(Date resultDate, Integer valueNumeric, Object obj) {
		this(resultDate, Datatype.NUMERIC, null, null, null, valueNumeric.doubleValue(), null, obj);
	}
	
	/**
	 * Builds a text result with a result date of today
	 * 
	 * @param valueText
	 */
	public Result(String valueText) {
		this(new Date(), valueText, null);
	}
	
	/**
	 * Builds a text result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueText
	 */
	public Result(Date resultDate, String valueText, Object obj) {
		this(resultDate, Datatype.TEXT, null, null, null, null, valueText, obj);
	}
	
	/**
	 * Builds a result date with specific (overloaded) values &mdash; i.e., instead of simply
	 * accepting the default translation of one datatype into another (e.g., a date translated
	 * automatically into string format), this contructor allows the various datatype
	 * representations of the result to be individually controlled. Any values set to <em>null</em>
	 * will yield the natural translation of the default datatype. For example,
	 * 
	 * <pre>
	 * Result result = new Result(new Date(), 2.5);
	 * assertEqualtes(&quot;2.5&quot;, result.toString());
	 * 
	 * Result result = new Result(new Date(),
	 *                            Result.Datatype.NUMERIC,
	 *                            2.5,
	 *                            null,
	 *                            null,
	 *                            &quot;Two and a half&quot;,
	 *                            null);
	 * assertEquals(&quot;Two and a half&quot;, result.toString());
	 * </pre>
	 * 
	 * @param resultDate
	 * @param datatype
	 * @param valueNumeric
	 * @param valueDatetime
	 * @param valueCoded
	 * @param valueText
	 * @param valueBoolean
	 */
	public Result(Date resultDate, Datatype datatype, Boolean valueBoolean, Concept valueCoded, Date valueDatetime,
	    Double valueNumeric, String valueText, Object object) {
		this.resultDatetime = resultDate;
		this.valueNumeric = valueNumeric;
		this.valueDatetime = valueDatetime;
		this.valueCoded = valueCoded;
		this.valueText = valueText;
		this.valueBoolean = valueBoolean;
		this.datatype = datatype;
		this.resultObject = object;
	}
	
	@Deprecated
	public static final Result nullResult() {
		return emptyResult;
	}
	
	/**
	 * @return null/empty result
	 */
	public static final Result emptyResult() {
		return emptyResult;
	}
	
	/**
	 * Returns the datatype of the result. If the result is a list of other results, then the
	 * datatype of the first element is returned
	 * 
	 * @return datatype of the result
	 */
	public Datatype getDatatype() {
		if (isSingleResult())
			return this.datatype;
		// TODO: better option than defaulting to first element's datatype?
		return this.get(0).getDatatype();
	}
	
	/**
	 * Changes the result date time &mdash; not to be confused with a value that is a date. The
	 * result date time is typically the datetime that the observation was recorded.
	 * 
	 * @param resultDatetime
	 */
	public void setResultDate(Date resultDatetime) {
		this.resultDatetime = resultDatetime;
	}
	
	/**
	 * Changes the default datatype of the result
	 * 
	 * @param datatype
	 */
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}
	
	/**
	 * Overrides the boolean representation of ths result without changing the default datatype
	 * 
	 * @param valueBoolean
	 */
	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}
	
	/**
	 * Overrides the coded representation of ths result without changing the default datatype
	 * 
	 * @param valueCoded
	 */
	public void setValueCoded(Concept valueCoded) {
		this.valueCoded = valueCoded;
	}
	
	/**
	 * Overrides the datetime representation of ths result without changing the default datatype
	 * 
	 * @param valueDatetime
	 */
	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}
	
	/**
	 * Overrides the numeric representation of ths result without changing the default datatype
	 * 
	 * @param valueNumeric
	 */
	public void setValueNumeric(Integer valueNumeric) {
		this.valueNumeric = valueNumeric.doubleValue();
	}
	
	/**
	 * Overrides the numeric representation of ths result without changing the default datatype
	 * 
	 * @param valueNumeric
	 */
	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}
	
	/**
	 * Overrides the text representation of ths result without changing the default datatype
	 * 
	 * @param valueText
	 */
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}
	
	/**
	 * Returns the data of the result (not to be confused with a data value). For example, if a
	 * result represents an observation like DATE STARTED ON HIV TREATMENT, the <em>result date</em>
	 * (returned by this method) would be the date the observation was recorded while the
	 * <em>toDatetime()</em> method would be used to get the actual answer (when the patient started
	 * their treatment).
	 * 
	 * @return date of the result (usually the date the result was recorded or observed)
	 * @see #toDatetime()
	 */
	public Date getResultDate() {
		if (isSingleResult())
			return resultDatetime;
		return this.get(0).getResultDate();
	}
	
	public Object getResultObject() {
		return this.resultObject;
	}
	
	public void setResultObject(Object object) {
		this.resultObject = object;
	}
	
	/**
	 * @return boolean representation of the result. For non-boolean results, this will either be
	 *         the overridden boolean value (if specifically defined) or a boolean representation of
	 *         the default datatype. If the result is a list, then return false only if all members
	 *         are false
	 *         <table>
	 *         <th>
	 *         <td>Datatype</td>
	 *         <td>Returns</td></th>
	 *         <tr>
	 *         <td>CODED</td>
	 *         <td>false for concept FALSE<br>
	 *         true for all others</td>
	 *         </tr>
	 *         <tr>
	 *         <td>DATETIME</td>
	 *         <td>true for any date value<br>
	 *         false if the date is null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>NUMERIC</td>
	 *         <td>true for any non-zero number<br>
	 *         false for zero</td>
	 *         </tr>
	 *         <tr>
	 *         <td>TEXT</td>
	 *         <td>true for any non-blank value<br>
	 *         false if blank or null</td>
	 *         </tr>
	 *         </table>
	 */
	public Boolean toBoolean() {
		
		if (isSingleResult()) {
			
			if (datatype == null) {
				return valueBoolean;
			}
			
			switch (datatype) {
				case BOOLEAN:
					return (valueBoolean == null ? false : valueBoolean);
				case CODED:
					return (valueCoded == null ? false : true); // TODO: return
					// false for "FALSE"
					// concept
				case DATETIME:
					return (valueDatetime == null ? false : true);
				case NUMERIC:
					return (valueNumeric == null || valueNumeric == 0 ? false : true);
				case TEXT:
					return (valueText == null || valueText.length() < 1 ? false : true);
				default:
					return valueBoolean;
			}
		}
		for (Result r : this)
			if (!r.toBoolean())
				return false;
		return true;
	}
	
	/**
	 * @return concept for result. For non-concept results, returns the concept value if it was
	 *         overridden (specifically defined for the result), otherwise returns <em>null</em>. If
	 *         the result is a list, then the concept for the first member is returned.
	 */
	public Concept toConcept() {
		if (isSingleResult())
			return valueCoded;
		return this.get(0).toConcept();
	}
	
	/**
	 * @return the datetime representation of the result <em>value</em> (not to be confused with the
	 *         result's own datetime). For non-datetime results, this will return the overridden
	 *         datetime value (if specifically defined) or datetime representation of the default
	 *         datatype. If the result is a list, then the datetime representation of the first
	 *         member is returned.
	 *         <table>
	 *         <th>
	 *         <td>Datatype</td>
	 *         <td>Returns</td></th>
	 *         <tr>
	 *         <td>BOOLEAN</td>
	 *         <td>null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>CODED</td>
	 *         <td>null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>NUMERIC</td>
	 *         <td>null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>TEXT</td>
	 *         <td>If the text can be parsed into a date, then that value is returned;<br>
	 *         otherwise returns <em>null</em></td>
	 *         </tr>
	 *         </table>
	 */
	public Date toDatetime() {
		if (isSingleResult()) {
			if (valueDatetime != null)
				return valueDatetime;
			if (datatype == Datatype.TEXT && valueText != null) {
				try {
					return OpenmrsUtil.getDateFormat().parse(valueText);
				}
				catch (Exception e) {}
			}
			return valueDatetime;
		}
		return this.get(0).toDatetime();
	}
	
	/**
	 * @return numeric representation of the result. For non-numeric results, this will either be
	 *         the overridden numeric value (if specifically defined) or a numeric representation of
	 *         the default datatype. If the result is a list, then the value of the first element is
	 *         returned.
	 *         <table>
	 *         <th>
	 *         <td>Datatype</td>
	 *         <td>Returns</td></th>
	 *         <tr>
	 *         <td>BOOLEAN</td>
	 *         <td>1 for true<br>
	 *         0 for false</td>
	 *         </tr>
	 *         <tr>
	 *         <td>CODED</td>
	 *         <td>zero (0)</td>
	 *         </tr>
	 *         <tr>
	 *         <tr>
	 *         <td>DATETIME</td>
	 *         <td>Number of milliseconds since Java's epoch</td>
	 *         </tr>
	 *         <td>TEXT</td>
	 *         <td>numeric value of text if it can be parsed into a number<br>
	 *         otherwise zero (0)</td> </tr>
	 *         </table>
	 */
	public Double toNumber() {
		if (isSingleResult()) {
			if (datatype == null) {
				return valueNumeric;
			}
			switch (datatype) {
				
				case BOOLEAN:
					return (valueBoolean == null || !valueBoolean ? 0D : 1D);
				case CODED:
					return 0D;
				case DATETIME:
					return (valueDatetime == null ? 0 : new Long(valueDatetime.getTime()).doubleValue());
				case NUMERIC:
					return (valueNumeric == null ? 0D : valueNumeric);
				case TEXT:
					try {
						return Double.parseDouble(valueText);
					}
					catch (Exception e) {
						return 0D;
					}
				default:
					return valueNumeric;
			}
		}
		return this.get(0).toNumber();
	}
	
	/**
	 * @return string representation of the result. For non-text results, this will either be the
	 *         overridden text value (if specifically defined) or a string representation of the
	 *         default datatype value. If the result is a list, then the string representation of
	 *         all members a joined with commas.
	 */
	public String toString() {
		if (isSingleResult()) {
			if (datatype == null) {
				return valueText;
			}
			
			switch (datatype) {
				case BOOLEAN:
					return (valueBoolean ? "true" : "false");
				case CODED:
					return (valueCoded == null ? "" : valueCoded.getBestName(Context.getLocale()).getName());
				case DATETIME:
					return (valueDatetime == null ? "" : OpenmrsUtil.getDateFormat().format(valueDatetime));
				case NUMERIC:
					return (valueNumeric == null ? "" : String.valueOf(valueNumeric));
				case TEXT:
					return (valueText == null ? "" : valueText);
				default:
					return valueText;
			}
		}
		StringBuffer s = new StringBuffer();
		for (Result r : this) {
			if (s.length() > 0)
				s.append(",");
			s.append(r.toString());
		}
		return s.toString();
	}
	
	/**
	 * @return the object associated with the result (generally, this is used internally or for
	 *         advanced rule design)
	 */
	public Object toObject() {
		if (this.size() < 1)
			return null;
		if (this.size() == 1)
			return this.get(0).toObject();
		return this.toArray();
	}
	
	/**
	 * @return true if result is empty
	 */
	public boolean isNull() {
		return false; //EmptyResult has its own implementation
		//that should return true
	}
	
	/**
	 * @return true if the result has any non-zero, non-empty value
	 */
	public boolean exists() {
		if (isSingleResult()) {
			return ((valueBoolean != null && valueBoolean) || valueCoded != null || valueDatetime != null
			        || (valueNumeric != null && valueNumeric != 0) || (valueText != null && valueText.length() > 0));
		}
		for (Result r : this) {
			if (r.exists())
				return true;
		}
		return false;
	}
	
	public boolean contains(Concept concept) {
		return containsConcept(concept.getConceptId());
	}
	
	/**
	 * @returns all results greater than the given value
	 */
	public Result gt(Integer value) {
		if (isSingleResult()) {
			if (valueNumeric == null || valueNumeric <= value)
				return emptyResult;
			return this;
		}
		List<Result> matches = new ArrayList<Result>();
		for (Result r : this) {
			if (!r.gt(value).isEmpty())
				matches.add(r);
		}
		if (matches.size() < 1)
			return emptyResult;
		return new Result(matches);
	}
	
	/**
	 * @return true if result contains a coded value with the given concept id (if the result is a
	 *         list, then returns true if <em>any</em> member has a matching coded value)
	 */
	public boolean containsConcept(Integer conceptId) {
		if (isSingleResult())
			return (valueCoded != null && valueCoded.getConceptId().equals(conceptId));
		for (Result r : this) {
			if (r.containsConcept(conceptId))
				return true;
		}
		return false;
	}
	
	/**
	 * @return true if the result is equal to the given result or is a list containing a member
	 *         equal to the given result
	 */
	public boolean contains(Result result) {
		if (isSingleResult())
			return this.equals(result);
		for (Result r : this) {
			if (r.contains(result))
				return true;
		}
		return false;
	}
	
	/**
	 * @return a result with all duplicates removed
	 */
	public Result unique() {
		if (isSingleResult())
			return this;
		Integer something = new Integer(1);
		HashMap<Result, Integer> map = new HashMap<Result, Integer>();
		for (Result r : this)
			map.put(r, something);
		List<Result> uniqueList = new ArrayList<Result>(map.keySet());
		return new Result(uniqueList);
	}
	
	//TODO rewrite this method
	//
	//	/**
	//	 * @see java.lang.Object#hashCode()
	//	 */
	//	public int hashCode() {
	//		int hashCode = 49867; // some random number
	//		hashCode += this.hashCode();
	//	
	//		return hashCode;
	//	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Result))
			return false;
		Result r = (Result) obj;
		
		if (r instanceof EmptyResult && obj instanceof EmptyResult) {
			return true;
		}
		
		if (isSingleResult() && r.isSingleResult()) {
			
			if (datatype == null) {
				return false;
			}
			// both are single results
			switch (datatype) {
				case BOOLEAN:
					return (valueBoolean.equals(r.valueBoolean));
				case CODED:
					return (valueCoded.equals(r.valueCoded));
				case DATETIME:
					return (valueDatetime.equals(r.valueDatetime));
				case NUMERIC:
					return (valueNumeric.equals(r.valueNumeric));
				case TEXT:
					return (valueText.equals(r.valueText));
				default:
					return false;
			}
		}
		if (isSingleResult() || r.isSingleResult())
			// we already know they're not both single results, so if one is
			// single, it's not a match
			return false;
		if (this.size() != r.size())
			return false;
		// at this point, we have two results that are lists, so members must
		// match exactly
		for (int i = 0; i < this.size(); i++) {
			if (!this.get(i).equals(r.get(i)))
				return false;
		}
		return true;
	}
	
	/**
	 * @return the <em>index</em> element of a list. If the result is not a list, then this will
	 *         return the result only if <em>index</em> is equal to zero (0); otherwise, returns an
	 *         empty result
	 * @see java.util.List#get(int)
	 */
	@Override
	public Result get(int index) {
		if (isSingleResult())
			return (index == 0 ? this : emptyResult);
		
		if (index >= this.size()) {
			return emptyResult;
		}
		return super.get(index);
	}
	
	/**
	 * @return the chronologically (based on result date) first result
	 */
	public Result earliest() {
		if (isSingleResult())
			return this;
		Result first = emptyResult();
		for (Result r : this) {
			if (r != null && r.getResultDate() != null
			        && (first.getResultDate() == null || r.getResultDate().before(first.getResultDate()))) {
				first = r;
			}
		}
		return first;
	}
	
	/**
	 * @return the chronologically (based on result date) last result
	 */
	public Result latest() {
		if (isSingleResult())
			return this;
		Result last = emptyResult();
		for (Result r : this) {
			if ((last.getResultDate() == null || r.getResultDate().after(last.getResultDate()))) {
				last = r;
			}
		}
		return last;
	}
	
	private boolean isSingleResult() {
		return (this.size() < 1);
	}
	
}
