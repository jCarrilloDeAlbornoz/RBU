***************************************************************
************								*******************
***************************************************************
This source implements the metric RBU presented in the paper:

		An Axiomatic Analysis of Diversity Evaluation Metrics: Introducing the Rank-Biased Utility Metric.
		Enrique Amigó, Damiano Spina, Jorge Carrillo-de-Albornoz. In proceedings of SIGIR'18.
		
Please, if you use this resource please cite it.

This package is also included in the Evaluation Platform EvALL (www.evall.uned.es), along with extended features: pdf and latex reports, other diversity metrics, statistical significance test, etc.


This source is available to evaluate a pair of goldstandard/output, and generates as output an EvALL tsv report.

The input format for both files is described in the EvALL website (http://www.evall.uned.es/#formats), and that it is also described in the DiversificationFormat class.
 
The package must be invoked with four parameters: `ValueParameterP ValueParameterE pathGoldStandard pathSystemOutput`

 		Example: `java -jar target/rbu-0.1-jar-with-dependencies.jar 0.8 0.03 test/qrels.all_EvALL_FORMAT.txt test/input.uogTrDuax_EvALLi`

		
***************************************************************
************	DIVERSIFICATION FORMAT		*******************
***************************************************************

The diversification task uses a 2 column tsv format without headers, where the first column represents the TEST CASE and the second column represents the ID of the item. The order of the rows in the system output file will be interpreted as the ranking of the items. Your can find and example in EvALL website (http://www.evall.uned.es/#formats) and in the folder test. Duplicate ids of items at test case level are not allowed. Besides, empty values or different number of columns are not permitted. These restrictions will produce warnings when parsing the output file (the evaluation can continue but might not be reliable).</p>

Unlike, the gold standard format for diversification must contain 5 columns in a tsv format, indicating the TEST CASE, the ID, the RELEVANCE, the ASPECT TAG and the ASPECT WEIGHT, respectively. Notice that the RELEVANCE must be represented as a positive numeric value. Higher values are ranked first. Similarly, ASPECT WEIGHT must be a numeric positive value. Notice that, in the diversification input, duplicate ids of items with different ASPECT TAG at TEST CASE level are allowed. Finally, the ASPECT WEIGHT must be the same for all items with the same ASPECT TAG at TEST CASE level. Errors in these restrictions when parsing the gold standard will stop the evaluation until they are solved. Your can find and example in EvALL website (http://www.evall.uned.es/#formats) and in the folder test.
