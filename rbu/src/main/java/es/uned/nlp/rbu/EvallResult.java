package es.uned.nlp.rbu;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * <p>This source implements the metric RBU presented in the paper:<br><br>
 * 			&nbsp;&nbsp;&nbsp;&nbsp; An Axiomatic Analysis of Diversity Evaluation Metrics: Introducing the Rank-Biased Utility Metric.<br> 	
 * 			&nbsp;&nbsp;&nbsp;&nbsp; Enrique Amigó, Damiano Spina, Jorge Carrillo-de-Albornoz. In proceedings of SIGIR'18.</p>
 * 
 * <p>Please, if you use this resource please cite it.</p>
 * 
 * <p>This package is also included in the Evaluation Platform EvALL (www.evall.uned.es), along with extended features: pdf and latex reports,
 * other diversity metrics, statistical significance test, etc.</p>
 *
 * @author Jorge Carrillo-de-Albornoz 
 * 
 *  Copyright (c) 20018 - Permission is granted for use and modification of this file for research, non-commercial purposes. 
 */

public class EvallResult 
{
	/**
	 * Contains a result for each test case.
	 */
	private HashMap<String,Double> results = new HashMap<String, Double>();
	
	private Double aggregatedResult = null;

	public HashMap<String, Double> getResults() 
	{
		return results;
	}

	public void setResults(HashMap<String, Double> results) 
	{
		this.results = results;
	}

	public Double getAggregatedResult()
	{
		return aggregatedResult;
	}

	public void setAggregatedResult(Double aggregatedResult)
	{
		this.aggregatedResult = aggregatedResult;
	}
	
	/**
	 * Normalize the results for all test case. Only test cases present in the gold standard are covered.
	 * The results is stored in a local variable.
	 * */
	public void normalizeResult()
	{
		if(results.size()==0)
		{
			this.aggregatedResult = null;
		}
		else
		{
			double total =0;
			double numElems = 0;
			for (Map.Entry<String, Double> entry : results.entrySet()) 
			{ 
				if(entry.getValue()!=null)
				{
					total = total + entry.getValue();
					numElems++;
				}
			}
			if(total == 0)
			{
				this.aggregatedResult = 0.0d;
			}
			else if(numElems!= 0.0d)
			{
				this.aggregatedResult = total/numElems;
			}
		}
	}	
}
