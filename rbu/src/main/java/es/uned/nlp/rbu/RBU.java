package es.uned.nlp.rbu;

import java.util.ArrayList;
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

public class RBU
{
	private DiversificationFormat goldStandard;
	private DiversificationFormat output;
	private String name = "RBU";
	private EvallResult result = new EvallResult();
	private Double parameterPValue = 0.8d;	
	private Double parameterEValue = 0.03d;	
	
	/**
	 * @param gold Gold Standard DiversificationFormat object
	 * @param out System output DiversificationFormat object
	 */
	public RBU(DiversificationFormat gold, DiversificationFormat out) 
	{
		this.goldStandard = gold;
		this.output = out;	
	}

	/**
	 * Method that evaluates a system output with a gold standard, both in a DiversificationFormat object, using the RBU metric
	 */
	public void evaluate() 
	{	
		
		/**
		 * Evaluate according to the test case present in the gold. Calculate each result, and average over them.
		 * */
		for (Map.Entry<String, HashMap<String, HashMap<String, Double>>> entry : (this.goldStandard).getTableOfTopics().entrySet()) 
		{ 
			String testCase = entry.getKey();
			HashMap<String, HashMap<String, Double>> valuesGold = entry.getValue();	
			HashMap<String,Double> lstAspect = (this.goldStandard).getTableOfAspects().get(testCase);
			ArrayList<String> rankingOutputTestCase = (this.output).getRankingOfTopics().get(testCase);			
			
			Double rbu = 0.0d;
			double rbuLetf = 0.0d;
			double rbuRight = 0.0d;
			/**
			 * For each aspect check the output and the perfect ranking and evaluate.
			 * */
			for (Map.Entry<String, Double> entry2 : lstAspect.entrySet()) 
			{ 
				String aspect = entry2.getKey();
				Double weight = entry2.getValue();	
				ArrayList<String> perfectRankingTestCase = (this.goldStandard).gerPerfectRanking(testCase, aspect);	

				double powMaxValueGoldPerAspect = 0.0d;				
				/**
				 * Get the max value of the perfect ranking in the gold. The max value will be in position 0.
				 * */
				if(perfectRankingTestCase!=null && perfectRankingTestCase.size()>0)
				{
					powMaxValueGoldPerAspect = Math.pow(2,valuesGold.get(perfectRankingTestCase.get(0)).get(aspect));
				}
				
				double rbpMULerr= 0.0d;
				if(rankingOutputTestCase!=null && rankingOutputTestCase.size()>0)
				{
					for(int i=0;i<rankingOutputTestCase.size();i++)
					{
						/**
						 * We added one to perfectly represent the formula indexes as in the EvALL report.
						 * */
						double posI= i+1;
						
						/**
						 * If the element k is not present in the gold is considered relevance 0 and is not added.
						 * */		
						double RELi = 0.0d;
						double errMulti = 1.0d;
						if((valuesGold.get(rankingOutputTestCase.get(i))!=null) && valuesGold.get(rankingOutputTestCase.get(i)).containsKey(aspect))
						{
							/**
							 * It is less or equal because the k must go from 1 according to the formula indexes in the EvALL report.
							 * */
							for(int j=0;j<=i-1;j++)
							{
								double RELj= 0.0d;
								/**
								 * If it is not in the gold it is understood relevance 0 and it is not evaluated.
								 * */
								if((valuesGold.get(rankingOutputTestCase.get(j))!=null) && valuesGold.get(rankingOutputTestCase.get(j)).containsKey(aspect))
								{
									if(powMaxValueGoldPerAspect!=0.0d)
									{
										RELj = (Math.pow(2, valuesGold.get(rankingOutputTestCase.get(j)).get(aspect))-1)/powMaxValueGoldPerAspect;
									}			
								}
								errMulti*=(1-RELj);
							}							
							RELi = (Math.pow(2, valuesGold.get(rankingOutputTestCase.get(i)).get(aspect))-1)/powMaxValueGoldPerAspect;
						}
						/**
						 * Multiply errMulti by Reli and by parameter p and we have rbp*ERR
						 * */
						rbpMULerr+=  Math.pow(parameterPValue, posI-1) * RELi* errMulti;
					}					
				}
				rbuLetf += weight*rbpMULerr* (1-parameterPValue);
			}
			
			/**
			 * We calculate the right side, basically add the part of rbp and multiply it by the parameter p. It is only calculated once, not for each aspect.
			 * */
			if(rankingOutputTestCase!=null && rankingOutputTestCase.size()>0)
			{
				for(int i=0;i<rankingOutputTestCase.size();i++)
				{
					/**
					 * We added one to perfectly represent the formula indexes as in the EvALL report.
					 * */
					double posI= i+1;
					rbuRight += Math.pow(parameterPValue, posI-1);
				}
				rbuRight = rbuRight*parameterEValue * (1-parameterPValue);		
			}
			
			rbu = rbuLetf - rbuRight;
			this.result.getResults().put(testCase, rbu);
		}
	}

	public EvallResult getResult() 
	{
		return result;
	}

	public String getName() 
	{
		return name;
	}

	public void setParameterPValue(Double parameterPValue)
	{
		this.parameterPValue = parameterPValue;
	}

	public void setParameterEValue(Double parameterEValue) 
	{
		this.parameterEValue = parameterEValue;
	}	
}
