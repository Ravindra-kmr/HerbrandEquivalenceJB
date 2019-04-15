
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.FileWriter;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.Logger;

public class dataFlowFramework
{
	protected Logger logger = Logger.getLogger(dataFlowFramework.class);
	public ArrayList<String> statements;
	public ArrayList<ArrayList<Integer>> successorGraph;	
	public ArrayList<ArrayList<Integer>> predecessorGraph;
	public ArrayList<labelDictionary> labelLineNumberDict;
	// public static void main(String[] args)
	// {
	// 	herbrand herbrandObj = new herbrand();
	//     try
	//     {
	//         statements=new ArrayList<String>();
	//         if (0 < args.length) 
	//         {
	//             statements = herbrandObj.readFile(args[0]);
 //            }

 //            else
 //            {
 //               System.err.println("Invalid arguments count:" + args.length);
 //               System.exit(0);
 //            }
 //            successorGraph = herbrandObj.dataFlowGraph(statements);
	//     }
	//     catch(Exception Ex)
	//     {
	//         System.out.println("Exception occured: " + Ex);
	//     }
	// }get linked list java
	dataFlowFramework(ArrayList<String> statements,ArrayList<ArrayList<Integer>> successorGraph,ArrayList<ArrayList<Integer>> predecessorGraph,ArrayList<labelDictionary> labelLineNumberDict)
	{
		this.statements = statements;
		this.successorGraph = successorGraph;		
		this.predecessorGraph = predecessorGraph;
		this.labelLineNumberDict = labelLineNumberDict;
	}

	dataFlowFramework()
	{
	}
	
	public void readFile(String fileName)
	{
        this.statements=new ArrayList<String>();
	    try
	    {
	        File inFile = new File(fileName);
			BufferedReader br = new BufferedReader( new FileReader(inFile));
			String strLine = "";
			StringTokenizer stringTokenizer = null;
			
			//read comma separated file line by line
			while( (strLine = br.readLine()) != null)
			{
				
				//break comma separated line using ";"
				stringTokenizer = new StringTokenizer(strLine, ";");
				
				while(stringTokenizer.hasMoreTokens())
				{
				    this.statements.add(stringTokenizer.nextToken().trim());
				}
			}
			br.close();
			String outputString = "Entered program:\n";
//			System.out.println("Entered program:");
            for(int i=0;i<this.statements.size();i++)
            {
            	outputString = outputString + (i)+". "+this.statements.get(i) + "\n";
//                System.out.println((i)+". "+this.statements.get(i));
            }
            logger.info(outputString);
	    }
	    catch(Exception Ex)
	    {
	    	logger.error("Exception occured while reading the file: " + Ex);
	        System.out.println("Exception occured while reading the file: " + Ex);
	    }
	}

	public class labelDictionary
	{
		String label;
		int lineNum;
		labelDictionary(String label,int lineNum)
		{
			this.label = label;
			this.lineNum = lineNum;
		}
	}

	public void dataFlowGraph()
	{
		this.successorGraph = new ArrayList<ArrayList<Integer>>();
		this.predecessorGraph = new ArrayList<ArrayList<Integer>>();
		this.labelLineNumberDict = new ArrayList<labelDictionary>();
		for (int i = 0; i < this.statements.size(); i++ ) 
		{
			String regex = "\\S+\\s?:";
			String statement = this.statements.get(i);
			Pattern patternObj = Pattern.compile(regex);
			Matcher matcherObj = patternObj.matcher(statement);
			if(matcherObj.find())
			{
				this.labelLineNumberDict.add(new labelDictionary(statement.substring(matcherObj.start(),matcherObj.end()-1).trim(),i));
				if(statement.substring(statement.indexOf(":")+1,statement.length()).trim().length() != 0)
				{
					this.statements.add(i,statement.substring(0,statement.indexOf(":")).trim());
					// i=i+1;
					this.statements.set(i+1,statement.substring(statement.indexOf(":")+1,statement.length()).trim());
					// statement = this.statements.get(i);
				}
				else {
					this.statements.set(i, statement.substring(0,statement.indexOf(":")).trim());
				}
			}
		}
		String outputString = "Entered Program after preprocessing:\n";
//		logger.info("Entered Program after preprocessing:");
		System.out.println("Entered program after preprocessing:");
        for(int i=0;i<this.statements.size();i++)
        {
        	outputString = outputString + (i)+". "+this.statements.get(i) +"\n";
//        	logger.info((i)+". "+this.statements.get(i));
            System.out.println((i)+". "+this.statements.get(i));
        }
        logger.info(outputString);
        outputString = "Label Dictionary with line no.:\n";
//		System.out.println("Label Dictionary with line no.:");
		for(int i=0;i<this.labelLineNumberDict.size();i++)
        {
			outputString = outputString + this.labelLineNumberDict.get(i).label + " at " + this.labelLineNumberDict.get(i).lineNum+"\n";
//			logger.info(this.labelLineNumberDict.get(i).label + " at " + this.labelLineNumberDict.get(i).lineNum);
//            System.out.println(this.labelLineNumberDict.get(i).label + " at " + this.labelLineNumberDict.get(i).lineNum);
        }
        logger.info(outputString);

		for(int i = 0; i < this.statements.size();i++)
		{
			String regex = "jump\\s+\\S+";
			String statement = this.statements.get(i);
			Pattern patternObj = Pattern.compile(regex); //,Pattern.CASE_INSENSITIVE
			Matcher matcherObj = patternObj.matcher(statement);
			this.successorGraph.add(new ArrayList<Integer>());
			if(!(statement.contains("jump") && !(statement.contains("if"))))
			{
				this.successorGraph.get(i).add(i+1);
			}
			if(matcherObj.find())
			{
				String toLabel = statement.substring(matcherObj.start()+4,matcherObj.end()).trim();
				for(int j = 0; j < this.labelLineNumberDict.size();j++)
				{
					if(toLabel.equals(this.labelLineNumberDict.get(j).label))
					{
						this.successorGraph.get(i).add(this.labelLineNumberDict.get(j).lineNum);
						break;
					}
				}
			}
		}

		for(int i = 0; i <= this.successorGraph.size(); i++ )
		{
			this.predecessorGraph.add(new ArrayList<Integer>());
		}
		for(int i = 0; i < this.successorGraph.size(); i++ )
		{
			for(int j = 0; j < this.successorGraph.get(i).size(); j++)
			{
//				System.out.println(this.successorGraph.get(i).get(j));
				this.predecessorGraph.get(this.successorGraph.get(i).get(j)).add(i);
			}
		}
//		logger.info("successorGraph:");
//		System.out.println("successorGraph:");
		outputString="successorGraph:\n";
		for(int i = 0; i < this.successorGraph.size(); i++)
		{
			outputString=outputString + i +". ";
//			System.out.print(i+". ");
			for(int j = 0; j < this.successorGraph.get(i).size(); j++)
			{
				outputString = outputString + this.successorGraph.get(i).get(j)+" ";
//				System.out.print(this.successorGraph.get(i).get(j)+" ");
			}
			outputString=outputString+"\n";
//			System.out.println();
		}
		logger.info(outputString);
//		logger.info("PredecessorGraph:");
//		System.out.println("PredecessorGraph:");
		outputString="PredecessorGraph:\n";
		for(int i = 0; i < this.predecessorGraph.size(); i++)
		{
			outputString = outputString + i +". ";
//			System.out.print(i+". ");
			for(int j = 0; j < this.predecessorGraph.get(i).size(); j++)
			{
				outputString=outputString + this.predecessorGraph.get(i).get(j)+" ";
//				System.out.print(this.predecessorGraph.get(i).get(j)+" ");
			}
			outputString=outputString+"\n";
//			System.out.println();
		}
		logger.info(outputString);
	}
}
