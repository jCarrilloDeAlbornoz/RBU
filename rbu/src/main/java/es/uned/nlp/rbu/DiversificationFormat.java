package es.uned.nlp.rbu;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;


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
 *
 * <p>The diversification task uses a 2 column tsv format without headers, where the first column represents the TEST CASE and the second column represents the ID of the item. 
 * The order of the rows in the system output file will be interpreted as the ranking of the items. Your can find and example in EvALL website (http://www.evall.uned.es/#formats) and in the folder test.
 * Duplicate ids of items at test case level are not allowed. Besides, empty values or different number of columns are not permitted. These restrictions will produce 
 * warnings when parsing the output file (the evaluation can continue but might not be reliable).</p>
 * 
 * <p>
 * Unlike, the gold standard format for diversification must contain 5 columns in a tsv format, indicating the TEST CASE, the ID, the RELEVANCE, the ASPECT TAG and the ASPECT WEIGHT, 
 * respectively. Notice that the RELEVANCE must be represented as a positive numeric value. Higher values are ranked first. Similarly, ASPECT WEIGHT must be a numeric positive value.
 * Notice that, in the diversification input, duplicate ids of items with different ASPECT TAG at TEST CASE level are allowed. Finally, the ASPECT WEIGHT must be the same for all items
 * with the same ASPECT TAG at TEST CASE level. Errors in these restrictions when parsing the gold standard will stop the evaluation until they are solved. Your can find and example in EvALL 
 * website (http://www.evall.uned.es/#formats) and in the folder test
 * </p>
 *
 *
 * @author Jorge Carrillo-de-Albornoz 
 * 
 *  Copyright (c) 20018 - Permission is granted for use and modification of this file for research, non-commercial purposes. 
 */

public class DiversificationFormat
{
	private boolean isGold;
	private String pathFile;
	private boolean stop= false;
	
	/**
	 * Contains the list of data for different test cases. DiversificatioItem includes the relevance value and its aspect.  
	 * 				Topic			id				aspect,relevance
	 * */
	private HashMap<String, HashMap<String, HashMap<String, Double>>> tableOfTestCase = new HashMap<String, HashMap<String,HashMap<String, Double>>>();
	
	/**
	 * Contains the aspect per test case with its associated weight.
	 * 				topic			aspect, weight
	 * */
	private HashMap<String, HashMap<String,Double>> tableOfAspects = new HashMap<String, HashMap<String,Double>>();
	
	/**
	 * Ranking of the system output according to the rows. 
	 * */
	private HashMap<String, ArrayList<String>> rankingOfTestCases = new HashMap<String, ArrayList<String>>();	

	
	public boolean isGold() 
	{
		return isGold;
	}

	public String getPathFile()
	{
		return pathFile;
	}	

	public boolean isStop() 
	{
		return stop;
	}

	public void parseFile(boolean isGold, String pathFile)
	{
		this.isGold = isGold;
		this.pathFile = pathFile;

		try 
		{
			InputStream streamOutput = new FileInputStream(pathFile);
			if(streamOutput!=null)
			{		
				CsvReader reader = new CsvReader(new InputStreamReader(streamOutput, Charset.forName(StandardCharsets.UTF_8.displayName())), '\t');			
				parserInternal(reader); 
				reader.close();
			}			
		} 
		catch (FileNotFoundException e1)
		{
			System.out.println("File not found: " + pathFile);
		}			
	}

	
	private void parserInternal(CsvReader reader) 
	{
		System.out.println("Parsing file " + this.pathFile);
		reader.setUseTextQualifier(true);
        reader.setTextQualifier('\"');
        long inLine = 0;
        int rowWithNo2Columns = 0;     
        int rowWithNo5Columns = 0;
        try
        {
            while(reader.readRecord())
            {
            	inLine++;
            	String[] record = reader.getValues();
            	if(this.isGold())
            	{
	            	if(record.length!=5)
	            	{
	            		System.out.println("Format error: the number of columns must be 5. Line " + inLine);
    	            	rowWithNo5Columns++;
    	            	stop=true;
    	            	continue;
	            	}
            	}
            	else
            	{
	            	if(record.length!=2)
	            	{
	            		System.out.println("Format warning: the number of columns must be 2. Line " + inLine);
	            		rowWithNo2Columns++;
	            		continue;	
	            	}
            	}            	
            	
            	String testCase = record[0];
            	String id = record[1];
            	String value = null;
            	String aspect = null;
            	String weight = null;
            	if(this.isGold())
            	{
            		value = record[2];
            	}
            	else
            	{
            		value = String.valueOf(inLine);
            	}
            	
            	if(this.isGold())
            	{
            		aspect = record[3];
            		weight = record[4];
                	if(testCase.equalsIgnoreCase("") || id.equalsIgnoreCase("") || value.equalsIgnoreCase("") || aspect.equalsIgnoreCase("") ||weight.equalsIgnoreCase(""))
                	{
	            		System.out.println("Format error: the columns in the rows cannot be empty. Line " + inLine);
	            		stop=true;
    	            	continue;
                	}
            	}
            	else
            	{
                	if(testCase.equalsIgnoreCase("") || id.equalsIgnoreCase("") || value.equalsIgnoreCase(""))
                	{
                		System.out.println("Format warning: the columns in the rows cannot be empty. Line " + inLine);
	            		continue;
                	}
            	}            	

            	/**
            	 * Check if there are duplicated ids (not allowed in the output, permitted in the gold standard at test case level with different aspects).
            	 * */
            	if((tableOfTestCase.containsKey(testCase))&&(tableOfTestCase.get(testCase).containsKey(id)))
            	{
            		if(!this.isGold())
            		{
                		System.out.println("Format warning: this format does not allow duplicated ids at test case level, EvALL will only consider the first instance. Line " + inLine);
	            		continue;	
            		}
            		else if(tableOfTestCase.get(testCase).get(id).containsValue(aspect))
            		{
            			System.out.println("Format error: this format does not allow duplicated ids at test case level. Line " + inLine);
            			stop=true;
    	            	continue;
            		}
            	}
            	
            	/**
            	 * If gold standard, check if the values are numerical and if are higher than 0.
            	 * */
            	if(this.isGold())
            	{
            		if(!isNumeric(value) || !isNumeric(weight))
            		{
            			System.out.println("Format error: the value is not a valid number. Line " + inLine);
            			stop=true;
    	            	continue;
            		}
            		Double valueParsed = Double.parseDouble(value);
        			if(valueParsed<0.0d)
        			{
        				System.out.println("Format error: The values must be greater than 0. Line " + inLine);
        				stop=true;
    	            	continue;
        			}
        			
            		Double weightParsed = Double.parseDouble(weight);
        			if(weightParsed<0.0d)
        			{
        				System.out.println("Format error: The values must be greater than 0. Line " + inLine);
        				stop=true;
    	            	continue;
        			}
            	}
            	
            	/**
            	 * Check weight value
            	 * */
            	if(this.isGold())
            	{
            		Double weightParsed = Double.parseDouble(weight);
            		if(tableOfAspects.get(testCase)!=null && tableOfAspects.get(testCase).get(aspect)!=null &&
            				!(tableOfAspects.get(testCase).get(aspect).compareTo(weightParsed)==0))
            		{
            			System.out.println("Format error: There is a previous aspect with different weight. This weight is ignored. Line " + inLine);
    	            	continue;
            		}
            	}
            	
            	/**
            	 * Everything is correct and we update the tables.
            	 * */

            	if (tableOfTestCase.get(testCase)!=null)
            	{
            		/**
            		 * Update table of test case.
            		 * */
            		HashMap<String, HashMap<String, Double>> procesado = tableOfTestCase.get(testCase);
            		/**
            		 * Check if the id is present in the table, it must be with other aspect.  If it is the gold we put the aspect, 
            		 * if it is not put null and does not matter, it is not used with the system output.
            		 * */
            		if(procesado.get(id)!=null)
            		{
            			HashMap<String, Double> aspects = procesado.get(id);
            			aspects.put(aspect, Double.parseDouble(value));
            			procesado.put(id, aspects);
            			tableOfTestCase.put(testCase,procesado);
            		}
            		else
            		{
            			HashMap<String, Double> aspects = new HashMap<String, Double>();
            			aspects.put(aspect, Double.parseDouble(value));
            			procesado.put(id, aspects);
            			tableOfTestCase.put(testCase,procesado);
            		}
            		
            		/**
            		 * Update the ranking of test cases.
            		 * */
            		ArrayList<String> ranking = rankingOfTestCases.get(testCase);
            		ranking.add(id);
            		rankingOfTestCases.put(testCase, ranking);
            		
            		if(this.isGold())
            		{
	            		/**
	            		 * Update the aspect is not present.
	            		 * */
	            		if(tableOfAspects.get(testCase).get(aspect)==null)
	            		{
	            			tableOfAspects.get(testCase).put(aspect, Double.parseDouble(weight));
	            		}
            		}
            	}
            	else
            	{
            		/**
            		 * Update table of test case.
            		 * */
            		HashMap<String,HashMap<String, Double>> procesado = new HashMap<String, HashMap<String, Double>>();  		
        			HashMap<String, Double> aspects = new HashMap<String, Double>();
        			aspects.put(aspect, Double.parseDouble(value));
            		procesado.put(id, aspects);
            		tableOfTestCase.put(testCase,procesado);            		

            		/**
            		 * Update the ranking of test cases.
            		 * */
            		ArrayList<String> ranking = new ArrayList<String>();
            		ranking.add(id);
            		rankingOfTestCases.put(testCase, ranking);
            		
            		if(this.isGold())
            		{
	            		/**
	            		 * Update the aspect.
	            		 * */
	            		HashMap<String,Double> aspectsWeight = new HashMap<String,Double>();
	            		aspectsWeight.put(aspect, Double.parseDouble(weight));
	            		tableOfAspects.put(testCase, aspectsWeight);
            		}
            	}		     
            }	            
            if(inLine==0 && !reader.readRecord())
            {
            	System.out.println("Format error: The file is empty.");
            	stop=true;
            }
            else if(rowWithNo5Columns==inLine)
        	{
        		System.out.println("Format error: The number of columns must be 5 in all lines.");
        		stop=true;
        	}
            else if(rowWithNo2Columns==inLine)
            {
            	System.out.println("Format error: The number of columns must be 2 in all lines.");
            	stop=true;
            }
        }
        catch (IOException e)
        {	        
        	System.out.println("IO error: input file not well formed.");
        	stop=true;
        }
	}
	
	public boolean isNumeric(String str)
	{
		try 
		{
			Double.parseDouble(str);
		} 
		catch (NumberFormatException nfe) {return false;}
		return true;
	}
	
	
	public boolean isMoreThanOneTopic() 
	{
		return this.tableOfTestCase.size()!=1;
	}
	
	public HashMap<String, HashMap<String,HashMap<String, Double>>> getTableOfTopics() 
	{
		return tableOfTestCase;
	}
	
	public HashMap<String, ArrayList<String>> getRankingOfTopics() 
	{
		return rankingOfTestCases;
	}
	
	public HashMap<String, HashMap<String,Double>> getTableOfAspects() 
	{
		return tableOfAspects;
	}
	
	/**
	 * @param testCase Test case to analyze
	 * @param aspect Aspect to analyze
	 * @return A table with only relevant ids and its associated aspect weight
	 */
	public HashMap<String, Double> getRelevants(String testCase, String aspect)
	{
		HashMap<String, HashMap<String,Double>> topicsValues = this.tableOfTestCase.get(testCase);
		HashMap<String, Double> result = new HashMap<String, Double>();
		if(topicsValues!=null)
		{
			for (Map.Entry<String, HashMap<String,Double>> entry : topicsValues.entrySet()) 
			{
				String id = entry.getKey();
				HashMap<String,Double> aspects = entry.getValue();
				if(aspects.containsKey(aspect) && aspects.get(aspect)>0.0d)
				{
						result.put(id, aspects.get(aspect));						
				}
			}
		}		
		return result;
	}
	

	/**
	 * @param testCase Test case to analyze
	 * @param aspect Aspect to analyze
	 * @return A list of ids with the perfect ranking, sort from highest to lowest values
	 */
	public ArrayList<String> gerPerfectRanking(String testCase, String aspect)
	{
		HashMap<String, Double> topicsValues = this.getRelevants(testCase, aspect);
		ArrayList<String> perfectRanking = new ArrayList<String>();		
		List<Map.Entry<String, Double>> list = new LinkedList<>( topicsValues.entrySet());	
		
	    Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
	    {
	        @Override
	        public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
	        {
	    		if (o1.getValue() == null) 
	    		{
	    	        return o2.getValue() == null ? 0 : -1;
	    	    }
	    	    if (o2.getValue() == null)
	    	    {
	    	        return 1;
	    	    }
	    		return o1.getValue().compareTo(o2.getValue());
	        }
	    } );

	    for (Map.Entry<String, Double> entry : list)
	    {
	    	perfectRanking.add(entry.getKey());
	    }
	    Collections.reverse(perfectRanking);
		return perfectRanking;
	}

}
