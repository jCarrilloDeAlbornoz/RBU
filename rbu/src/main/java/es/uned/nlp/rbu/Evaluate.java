package es.uned.nlp.rbu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
 * <p>This source is available to evaluate a pair of goldstandard/output, and generates as output an EvALL tsv report.</p>
 * 
 * <p>The input format for both files is described in the EvALL website (http://www.evall.uned.es/#formats), and that 
 * it is also described in the DiversificationFormat class.</p>
 * 
 * <p>The package must be invoked with 4 parameter: <i>ValueParameterP</i> <i>ValueParameterE</i> <i>pathGoldStandard</i> <i>pathSystemOutput</i><br>
 * 
 * 			&nbsp;&nbsp;&nbsp;&nbsp; Example: java -jar rbu-0.1.jar 0.8 0.03 test/gold.tsv test/output.tsv </p>
 *
 * @author Jorge Carrillo-de-Albornoz 
 * 
 *  Copyright (c) 20018 - Permission is granted for use and modification of this file for research, non-commercial purposes. 
 */

public class Evaluate 
{
    /**
     * <p>The package must be invoked with 4 parameter: <i>ValueParameterP</i> <i>ValueParameterE</i> <i>pathGoldStandard</i> <i>pathSystemOutput</i><br>
     * 
     * 			&nbsp;&nbsp;&nbsp;&nbsp; Example: java -jar rbu-0.1.jar 0.8 0.03 test/gold.tsv test/output.tsv </p>
	 * 
     * @param args Parameters
     */
    public static void main( String[] args )
    {
    	if(args.length!=4)
    	{
    		System.out.println("The number of parameters must be 4: Java rbu ValueParameterP ValueParameterE pathGoldStandard pathSystemOutput \n"
    				+ "Example: java -jar rbu-0.1.jar 0.8 0.03 test/gold.tsv test/output.tsv");
    		System.exit(0);
    	}
    	
    	Double paramP = Double.parseDouble(args[0]);
    	Double paramE = Double.parseDouble(args[1]);
    	String goldStandardFile = args[2];
    	String outputFile = args[3];
    	
    	if(goldStandardFile==null || goldStandardFile.equalsIgnoreCase(""))
		{
    		System.out.println("The name of the gold standard file cannot be empty");
    		System.exit(0);
		}
	
		if(outputFile==null || outputFile.equalsIgnoreCase(""))
		{
			System.out.println("The name of the system output file cannot be empty");
    		System.exit(0);
		}
    	
    	
		/**
		 * Check the gold standard for errors. Errors stop the analysis.
		 * */
    	DiversificationFormat gold = new DiversificationFormat();
		gold.parseFile(true, goldStandardFile);

		
		/**
		 * Check the system output for errors/warnings.
		 * */
    	DiversificationFormat output = new DiversificationFormat();
    	output.parseFile(false, outputFile);
		
		RBU rbu = new RBU(gold, output);
		rbu.setParameterPValue(paramP);
		rbu.setParameterEValue(paramE);
		rbu.evaluate();
		generateSingleTSVFileForOneOutput(output, gold, rbu);
		
    }
    
    
	/**
	 * Method that writes the EvALL tsv report
	 * 
	 * @param output	System output DiversificationFormat object
	 * @param gold		Gold Standard DiversificationFormat object
	 * @param rbu		Measure RBU object
	 */
	public static void generateSingleTSVFileForOneOutput(DiversificationFormat output, DiversificationFormat gold, RBU rbu)
	{
		
		File outputFile = new File("Results.tsv");			
		try
		{

			CsvWriter csvOutput = new CsvWriter(new FileOutputStream(outputFile, false), '\t', Charset.forName(StandardCharsets.UTF_8.displayName()));
			csvOutput.setTextQualifier('\"');
			csvOutput.setUseTextQualifier(true);
			csvOutput.setForceQualifier(true);
			
			/**
			 * Headers for EvALL tsv report
			 * */
				
				csvOutput.writeComment("############################################################################");
				csvOutput.writeComment("\t\t\t\tAUTOMATIC EvALL TSV REPORT\n#\n#\tWe kindly ask you to cite the following work when using EvALL:\n#\t\t\tAn Axiomatic Analysis of Diversity Evaluation Metrics: Introducing the Rank-Biased Utility Metric\n#\t\t\tEnrique Amigó, Damiano Spina, Jorge Carrillo-de-Albornoz\n#\t\t\tIn proceedings of SIGIR'18\n#");

				csvOutput.writeComment("\tThis file contains the results for the output: ");		

				String originalName = output.getPathFile();
				String mix =  MessageFormat.format("\t\t\t\u2022 {0}", originalName);
				csvOutput.writeComment(mix);			
				csvOutput.writeComment("");
				csvOutput.writeComment("\tThe next table contains the results for each test case in this output. \n#\tNotice that first are shown the test cases present in the gold, and after that those not present. \n#\tThose measures that do not satisfy the preconditions are marked with -.");
				csvOutput.writeComment("############################################################################");
				csvOutput.writeComment("The measures included in the table are:");
				csvOutput.writeComment("\t\t- " + rbu.getName());

				csvOutput.writeComment("############################################################################");
				
				
				String title[] = new String[2];
				title[0] = "Test Case";
				title[1] = rbu.getName();
				csvOutput.writeRecord(title);


				/**
				 * First we check the test case of the gold standard.
				 * */
				for (Map.Entry<String, HashMap<String, HashMap<String,Double>>> entry : gold.getTableOfTopics().entrySet()) 
				{ 
					String topic = entry.getKey();
					String record[] = new String[2];
					record[0] = topic;
					if(rbu.getResult().getResults().get(topic)!=null)
					{
						record[1] = String.format("%.4f",rbu.getResult().getResults().get(topic));
					}
					else
					{
						record[1] = "-";
					}
					csvOutput.writeRecord(record);
				}		
				
				/**
				 * Then we check the test case of the output and write those not present in the gold standard.
				 * */
				for (Map.Entry<String, HashMap<String, HashMap<String,Double>>> entry : output.getTableOfTopics().entrySet()) 
				{ 
					String topic = entry.getKey();
					if(gold.getTableOfTopics().get(topic)!=null)
					{
						continue;
					}
					String record[] = new String[2];
					record[0] = topic;
					if(rbu.getResult().getResults().get(topic)!=null)
					{
						record[1] = String.format("%.4f",rbu.getResult().getResults().get(topic));
					}
					else
					{
						record[1] = "-";
					}
					csvOutput.writeRecord(record);
				}			
			csvOutput.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
}
