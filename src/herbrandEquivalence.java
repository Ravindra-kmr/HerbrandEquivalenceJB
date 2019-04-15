

import java.util.*;
// 
// about too much complication by read statements and their effect on increasing valueNum.
//Question: should i start with project report or gvn implementation or should i do this completely(cleaning and add more input files).
//NPTEL Exam. Got a ticket on 26th but do i need to come back after giving the presentation on 25th.
//I might not be able to implement the changes suggested by panel at that moment because of preparation for exam. Can I ask panel to give their suggestion before the presenation or can I can work on suggestion after 28th from home.
//TODO create a nested loop program.
//TODO create a program which start with a loop.
//import java.util.regex.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//This class compute Herbrand equivalence and remove the redundant expression from the program.
public class herbrandEquivalence extends dataFlowFramework
{
	int termCounter;  //A counter to assign Value Number to a class. Terms within same equivalence class have same Value number.
	int counter;	
	int numClass;	//A Constant which indicates number of maximum equivalence classes possible 
	int numProgramPoints;	//A constant indicates number of program points in the given program.
	ArrayList<String> listofVariableUConstant;		//List of XUC where X = variables and C = constants in the given program.
	ArrayList<String> operators;	//List of operators used in the given program.
	ArrayList<ArrayList<expression>> availableExpression;	//List(List of available expressions at a program point)
	ArrayList<ArrayList<termID>> partitions;	//List(List of equivalence class at a program points.)
	
	//class constructor.
	herbrandEquivalence()	
	{
		super();
	}

	//Function to create a list(variables & constants) and list(operators).
	public void allVariableUConstant()
	{
		logger.debug("Inside allVariableUConstant function.");
		this.listofVariableUConstant = new ArrayList<String>();
		this.operators = new ArrayList<String>();
		for(int i = 0; i < super.statements.size(); i++)
		{
			String statement = super.statements.get(i); 
			if(statement.contains("=") && !(statement.contains("==")))
			{
				if(!this.operators.contains("+") && statement.contains("+"))
				{
					this.operators.add("+");
				}
				else if(!this.operators.contains("*") && statement.contains("*"))
				{
					this.operators.add("*");
				}
				StringTokenizer varOrConst = new StringTokenizer(statement,"=+*");
				while(varOrConst.hasMoreTokens())
				{
					String temp = varOrConst.nextToken().trim();
					if(!this.listofVariableUConstant.contains(temp))
					{
						this.listofVariableUConstant.add(temp);
					}
				}
			}
			else if(statement.contains("read"))
			{
				String temp = statement;
				temp = temp.substring(temp.indexOf("(")+1,temp.indexOf(")")).trim();
				if(!this.listofVariableUConstant.contains(temp))
				{
					this.listofVariableUConstant.add(temp);
				}
			}
		}
		this.listofVariableUConstant.trimToSize();
		for(int i = 0, j = this.listofVariableUConstant.size()-1; i < j;)
		{
			if(this.listofVariableUConstant.get(j).matches("-?\\d+(\\.\\d+)?"))
			{
				String temp = this.listofVariableUConstant.get(i);
				this.listofVariableUConstant.set(i,this.listofVariableUConstant.get(j));
				this.listofVariableUConstant.set(j,temp);
				i++;
			}
			else
			{
				j--;
			}
		}
		String outputString = "The variable and constants are: \n";
//		System.out.println("The variable and constants are:");
		for(int i = 0; i < this.listofVariableUConstant.size(); i++)
		{
			outputString = outputString + this.listofVariableUConstant.get(i) + "\n";
//			System.out.println(this.listofVariableUConstant.get(i));
		}
		outputString= outputString + "List of Operators:\n";
//		System.out.println("List of Operators:");
		for(int i = 0; i < this.operators.size(); i++)
		{
			outputString = outputString + this.operators.get(i) + "\n";
//			System.out.println(operators.get(i));
		}
		logger.info(outputString);
		logger.debug("Exiting allVariableUConstant function.");
	}
	public class termID
	{
		int termValueNum;
		int fieldType;
		int valueNum;
		termID leftOperand;
		termID rightOperand;
		public void cloneID(termID id)
		{
			this.termValueNum = id.termValueNum;
			this.fieldType = id.fieldType;
			this.valueNum = id.valueNum;
			this.leftOperand = id.leftOperand;
			this.rightOperand = id.rightOperand;
		}
		public termID()
		{
			// super();
		}
		public termID(termID id)
		{
			this.termValueNum = id.termValueNum;
			this.fieldType = id.fieldType;
			this.valueNum = id.valueNum;
			this.leftOperand = id.leftOperand;
			this.rightOperand = id.rightOperand;
		}
		@Override
		public String toString()
		{
			return(this.fieldType+" "+this.valueNum+" "+this.termValueNum+" ("+this.leftOperand+") ("+this.rightOperand+")");
		}
	}
	public termID atomicID()
	{
		termID newID = new termID();
		newID.termValueNum = increaseTermCounter();
		newID.fieldType = 0;
		newID.valueNum = increaseCounter();
		newID.leftOperand = null;
		newID.rightOperand = null;
		return newID;
	}
	public termID assignCompoundID(int termNum,termID left, termID right, int opType)
	{
		termID newID = new termID();
		newID.termValueNum = termNum;
		newID.fieldType = opType;
		newID.valueNum = -1;
		newID.leftOperand = left;
		newID.rightOperand = right;
		return newID;
	}
	public void startValueNum()
	{
		this.termCounter = 0;
	}
	public int increaseTermCounter()
	{
		return(++this.termCounter);
	}
	public void startCounter()
	{
		this.counter = 0;
	}
	public int increaseCounter()
	{
		return(++this.counter);
	}
	public void initialPartition(int partitionNum)
	{
		for(int i = 0; i < this.listofVariableUConstant.size(); i++)
		{
			termID newID = new termID();
			newID = atomicID();
			this.partitions.get(partitionNum).add(newID);
			// System.out.print(this.partitions.get(partitionNum).get(i));
		}
		for(int i = 0; i < this.operators.size();i++)
		{
			int opType = i+1;
			for(int j = 0; j < this.listofVariableUConstant.size();j++)
			{
				termID left = this.partitions.get(partitionNum).get(j);
				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
				{
					termID right = this.partitions.get(partitionNum).get(k);
					this.partitions.get(partitionNum).add(assignCompoundID(increaseTermCounter(),left,right,opType));
				}
			}
		}
	}
	void printPartition(int partitionNum)
	{
		// if(this.partitions.get(partitionNum).isEmpty())
		// {	partitionNum = 0;
		// }
		String outputString = "Inside the printpartion function, Partition no:"+partitionNum+"\n";
		System.out.println("Inside the printpartion function, Partition no:"+partitionNum);
//		for(int i = 0; i < this.listofVariableUConstant.size(); i++)
//		{
//			System.out.print(this.listofVariableUConstant.get(i)+" ");
//		}
//		System.out.println();
		int count=0;
		for(int i = 0; i < this.listofVariableUConstant.size(); i++,count++)
		{
			outputString = outputString + this.listofVariableUConstant.get(i)+" -> "+this.partitions.get(partitionNum).get(count) + "\n";
//			System.out.println(this.listofVariableUConstant.get(i)+" -> "+this.partitions.get(partitionNum).get(count));
		}
		for(int i = 0; i < this.operators.size();i++)
		{
//			int opType = i+1;
			for(int j = 0; j < this.listofVariableUConstant.size();j++)
			{
//				termID left = this.partitions.get(partitionNum).get(j);
				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
				{
//					termID right = this.partitions.get(partitionNum).get(k);
					outputString = outputString + "("+this.listofVariableUConstant.get(j)+this.operators.get(i)+this.listofVariableUConstant.get(k)+") -> "+this.partitions.get(partitionNum).get(count)+"\n";
//					System.out.println("("+this.listofVariableUConstant.get(j)+this.operators.get(i)+this.listofVariableUConstant.get(k)+") -> "+this.partitions.get(partitionNum).get(count));
//					this.partitions.get(partitionNum).add(assignCompoundID(left,right,opType));
					count++;
				}
			}
		}
		logger.debug(outputString);
//		for( i = 0; i < this.partitions.get(partitionNum).size(); i++)
//		{
//			System.out.println(this.partitions.get(partitionNum).get(i));
//		}
	}
	void printEquivalenceClass(int partitionNum)
	{
		ArrayList<Boolean> termFlag = new ArrayList<Boolean>(this.numClass);
		String outputString = "EquivalenceClasses for partition no."+partitionNum+"\n";
		for(int i = 0; i < this.numClass; i++)
		{
			termFlag.add(false);
		}
		for(int i = 0; i < this.numClass; i++)
		{
			if(termFlag.get(i).equals(false))
			{
				if(i<this.listofVariableUConstant.size())
				{
					outputString = outputString + "{"+this.listofVariableUConstant.get(i)+",";
					System.out.print("{"+this.listofVariableUConstant.get(i)+",");
				}
				else
				{
					int temp=i-this.listofVariableUConstant.size();
					int operatorIndex = temp/(int)Math.pow(this.listofVariableUConstant.size(), 2);
					temp = temp % (int)Math.pow(this.listofVariableUConstant.size(), 2);
					int leftOperandIndex = temp/this.listofVariableUConstant.size();
					temp = temp % this.listofVariableUConstant.size();
					int rightOperandIndex = temp;
					outputString = outputString + "{"+this.listofVariableUConstant.get(leftOperandIndex)+this.operators.get(operatorIndex)+this.listofVariableUConstant.get(rightOperandIndex)+",";
					System.out.print("{"+this.listofVariableUConstant.get(leftOperandIndex)+this.operators.get(operatorIndex)+this.listofVariableUConstant.get(rightOperandIndex)+",");
				}
				for(int j = i+1; j < this.numClass ; j++)
				{
					if(this.partitions.get(partitionNum).get(i).toString().equals(this.partitions.get(partitionNum).get(j).toString()))
					{
						if(j<this.listofVariableUConstant.size())
						{
							outputString = outputString + " "+this.listofVariableUConstant.get(j)+",";
							System.out.print(" "+this.listofVariableUConstant.get(j)+",");
						}
						else
						{
							int temp=j-this.listofVariableUConstant.size();
							int operatorIndex = temp/(int)Math.pow(this.listofVariableUConstant.size(), 2);
							temp = temp % (int)Math.pow(this.listofVariableUConstant.size(), 2);
							int leftOperandIndex = temp/this.listofVariableUConstant.size();
							temp = temp % this.listofVariableUConstant.size();
							int rightOperandIndex = temp;
							outputString = outputString + " "+this.listofVariableUConstant.get(leftOperandIndex)+this.operators.get(operatorIndex)+this.listofVariableUConstant.get(rightOperandIndex)+",";
							System.out.print(" "+this.listofVariableUConstant.get(leftOperandIndex)+this.operators.get(operatorIndex)+this.listofVariableUConstant.get(rightOperandIndex)+",");
						}
						termFlag.set(j, true);
					}
				}
				outputString = outputString + "},";
				System.out.print("},");
				termFlag.set(i, true);
			}
		}
		logger.info(outputString);
		System.out.println();
//		
//		
//		
//		// if(this.partitions.get(partitionNum).isEmpty())
//		// {	partitionNum = 0;
//		// }
//		System.out.println("Inside the printpartion function, Partition no:"+partitionNum);
//		for(int i = 0; i < this.listofVariableUConstant.size(); i++)
//		{
//			System.out.print(this.listofVariableUConstant.get(i)+" ");
//		}
//		System.out.println();
//		int count=0;
//		for(int i = 0; i < this.listofVariableUConstant.size(); i++,count++)
//		{
//			System.out.println(this.listofVariableUConstant.get(i)+" -> "+this.partitions.get(partitionNum).get(count));
//		}
//		for(int i = 0; i < this.operators.size();i++)
//		{
////			int opType = i+1;
//			for(int j = 0; j < this.listofVariableUConstant.size();j++)
//			{
////				termID left = this.partitions.get(partitionNum).get(j);
//				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
//				{
////					termID right = this.partitions.get(partitionNum).get(k);
//					System.out.println("("+this.listofVariableUConstant.get(j)+this.operators.get(i)+this.listofVariableUConstant.get(k)+") -> "+this.partitions.get(partitionNum).get(count));
////					this.partitions.get(partitionNum).add(assignCompoundID(left,right,opType));
//					count++;
//				}
//			}
//		}
////		for( i = 0; i < this.partitions.get(partitionNum).size(); i++)
////		{
////			System.out.println(this.partitions.get(partitionNum).get(i));
////		}
	}

	public void computeHerbrandEquivalence()
	{
		logger.debug("Inside computeHerbrandEquivalence() function.");
		startCounter();
		this.numClass = this.operators.size()*((int)Math.pow(this.listofVariableUConstant.size(),2))+this.listofVariableUConstant.size();
		this.numProgramPoints = super.statements.size()+1;
		logger.info("Num of ProgramPoints: "+this.numProgramPoints+" and Num of classes: "+this.numClass);
		System.out.println("Num of ProgramPoints: "+this.numProgramPoints+" and Num of classes: "+this.numClass);
		this.partitions = new ArrayList<ArrayList<termID>>(this.numProgramPoints);
		for(int i = 0; i < this.numProgramPoints; i++)
		{
			this.partitions.add(new ArrayList<termID>(this.numClass));
		}
		// for(int i = 0; i < this.partitions.size(); i++)
		// {
		// 	System.out.println(this.partitions.get(i));
		// }
		initialPartition(0);
//		printPartition(0);
//		printPartition(1);
		boolean convergenceFlag=false;
		while(convergenceFlag == false)
		{
//			logger.info("Starting of iteration. ConvergenceFlag:"+convergenceFlag);
			convergenceFlag = true;
			for(int i = 1; i < this.numProgramPoints; i++)
			{
				ArrayList<termID> oldPartition;// = new ArrayList<termID>();
				if(this.partitions.get(i).isEmpty())
					oldPartition = this.partitions.get(0);
				else
					oldPartition = this.partitions.get(i);
				if(super.predecessorGraph.get(i-1).size()>1)
				{
					ArrayList<termID> temp = new ArrayList<termID>();
					temp = this.partitions.get(super.predecessorGraph.get(i-1).get(0)+1);
					int countPaths = 0;
					while(countPaths<super.predecessorGraph.get(i-1).size()-1)
					{
						logger.debug("Calling confluence function.");
						temp=confluence(temp,this.partitions.get(super.predecessorGraph.get(i-1).get(++ countPaths)+1));
					}
					this.partitions.set(i,temp);
				}
				else if(super.statements.get(i-1).contains("=") && !super.statements.get(i-1).contains("=="))
				{
					String lhs = super.statements.get(i-1).substring(0,super.statements.get(i-1).indexOf("=")).trim();
					String rhs = super.statements.get(i-1).substring(super.statements.get(i-1).indexOf("=")+1,super.statements.get(i-1).length()).trim();
					ArrayList<termID> temp = new ArrayList<termID>();
					// System.out.println("dsfaa"+super.predecessorGraph.get(i-1));
					logger.debug("Calling assignmentStatement() function.");
					if(super.predecessorGraph.get(i-1).isEmpty())
					{
						temp = assignmentStatement(-1,lhs,rhs);
					}
					else
					{
						temp = assignmentStatement(super.predecessorGraph.get(i-1).get(0),lhs,rhs);
					}
					this.partitions.set(i,temp);
//					int t=0;
//					while(t<i)
//					{
//							// printPartition(0);
//							// this.partitions.get(0).get(1).valueNum = 5;
//						printPartition(t++);
//
//					}
//					printPartition(i);
				}
				else if(super.statements.get(i-1).contains("read"))
				{
					String variable = super.statements.get(i-1).substring(super.statements.get(i-1).indexOf("(")+1,super.statements.get(i-1).indexOf(")")).trim();
					ArrayList<termID> temp = new ArrayList<termID>();
					logger.debug("Calling nonDetAssignment() function.");
					if(super.predecessorGraph.get(i-1).isEmpty())
					{
						temp = nonDetAssignment(-1,variable);
					}
					else
					{
						temp = nonDetAssignment(super.predecessorGraph.get(i-1).get(0),variable);
					}
					this.partitions.set(i,temp);
				}
				else if(!super.predecessorGraph.get(i-1).isEmpty())
					this.partitions.set(i, this.partitions.get(super.predecessorGraph.get(i-1).get(0)+1));
				else if(this.partitions.get(i).isEmpty())
					this.partitions.set(i, this.partitions.get(0));
				else 
				{
					System.out.println("Unknown type statement. "+ super.statements.get(i-1));
					logger.error("Unknown type statement. "+ super.statements.get(i-1));
				}
				if(!(isSame(oldPartition,this.partitions.get(i))))
				{
					convergenceFlag = false;
				}
			}
			if(convergenceFlag == true)
				logger.info("\t\t\t\t*********************************************************************************\t\t\t\t");
			for(int i = 0; i < this.numProgramPoints;i++)
			{
				if(i>0 && i <= super.statements.size())
				{
					logger.info("Next Statement: "+super.statements.get(i-1));
					System.out.println(super.statements.get(i-1));
				}
				printPartition(i);
				printEquivalenceClass(i);
			}
			logger.info("End of iteration. ConvergenceFlag: "+convergenceFlag);
		}
		logger.debug("Exiting ComputeherbrandEquivalence() function.");
	}
	
	public ArrayList<termID> nonDetAssignment(int predLineIndexNo,String lhs)
	{
		logger.debug("Inside nonDetAssignment method");
		ArrayList<termID> input;
		if(this.partitions.get(predLineIndexNo+1).isEmpty())
			input = this.partitions.get(0);
		else
			input = this.partitions.get(predLineIndexNo+1);
		ArrayList<termID> result = new ArrayList<termID>();
		result.addAll(input);
		termID newID = atomicID();
		result.set(this.listofVariableUConstant.indexOf(lhs),newID);//.cloneID(newID);
		// result.remove(this.listofVariableUConstant.indexOf(lhs));
		// result.get(0).valueNum = result.get(0).valueNum+5;
		for(int i = 0; i < this.operators.size();i++)
		{
			int opType = i+1;
			for(int j = 0; j < this.listofVariableUConstant.size();j++)
			{
				termID left = result.get(j);
				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
				{
					termID right = result.get(k);
					if(this.listofVariableUConstant.get(j).equals(lhs) || this.listofVariableUConstant.get(k).equals(lhs))
					{
						int termNum = -1;
						for(int l = 0; l < result.size(); l++)
						{
							if(result.get(l).fieldType == opType && Objects.equals(result.get(l).leftOperand.toString(), result.get(j).toString()) && Objects.equals(result.get(l).rightOperand.toString(), result.get(k).toString()))
							{
								termNum = result.get(l).termValueNum;
								break;
							}
						}
						if(termNum == -1)
							termNum = increaseTermCounter();
						// System.out.println("dfs "+i+" "+j+" "+k);
						// result.get((j+1)*this.listofVariableUConstant.size() + k).cloneID(assignCompoundID(left,right,opType));
//						termID temp = new termID();
//						temp.cloneID(assignCompoundID(left,right,opType));
						result.set(i*(int)Math.pow(this.listofVariableUConstant.size(), 2)+(j+1)*this.listofVariableUConstant.size() + k,assignCompoundID(termNum,left,right,opType));
						// for(int t =0; t< result.size();t++)
						// 	System.out.println(result.get(t));
						
					}
				}
			}
		}
		logger.debug("Exiting the nonDetAssignment() function.");
		return result;
	}
	
	/*
	public ArrayList<termID> nonDetAssignment(int predLineIndexNo,String lhs)
	{
		logger.debug("Inside nonDetAssignment method");
		ArrayList<termID> input;
		if(this.partitions.get(predLineIndexNo+1).isEmpty())
			input = this.partitions.get(0);
		else
			input = this.partitions.get(predLineIndexNo+1);
		ArrayList<termID> result = new ArrayList<termID>();
		ArrayList<termID> q1 = new ArrayList<termID>();
		ArrayList<termID> q2 = new ArrayList<termID>();
		q1.addAll(input);
		q2.addAll(input);
		termID newID1 = new termID();
		termID newID2 = new termID();
		if(!(this.listofVariableUConstant.get(0).equals(lhs) || this.listofVariableUConstant.get(1).equals(lhs)))
		{
			newID1.cloneID(input.get(0));
			newID2.cloneID(input.get(1));
		}
		else if(!(this.listofVariableUConstant.get(0).equals(lhs) || this.listofVariableUConstant.get(2).equals(lhs)))
		{
			newID1.cloneID(input.get(0));
			newID2.cloneID(input.get(2));
		}
		else
		{
			newID1.cloneID(input.get(1));
			newID2.cloneID(input.get(2));
		}
		q1.set(this.listofVariableUConstant.indexOf(lhs),newID1);//.cloneID(newID);
		q2.set(this.listofVariableUConstant.indexOf(lhs),newID2);//.cloneID(newID);
		for(int i = 0; i < this.operators.size();i++)
		{
			int opType = i+1;
			for(int j = 0; j < this.listofVariableUConstant.size();j++)
			{
				termID left1 = q1.get(j);
				termID left2 = q2.get(j);
				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
				{
					termID right1 = q1.get(k);
					termID right2 = q2.get(k);
					if(this.listofVariableUConstant.get(j).equals(lhs) || this.listofVariableUConstant.get(k).equals(lhs))
					{
						int termNum1 = -1;
						for(int l = 0; l < q1.size(); l++)
						{
							if(q1.get(l).fieldType == opType && Objects.equals(q1.get(l).leftOperand.toString(), q1.get(j).toString()) && Objects.equals(q1.get(l).rightOperand.toString(), q1.get(k).toString()))
							{
								termNum1 = q1.get(l).termValueNum;
							}
						}
						if(termNum1 == -1)
							termNum1 = increaseTermCounter();
						
						int termNum2 = -1;
						for(int l = 0; l < q2.size(); l++)
						{
							if(q2.get(l).fieldType == opType && Objects.equals(q2.get(l).leftOperand.toString(), q2.get(j).toString()) && Objects.equals(q2.get(l).rightOperand.toString(), q2.get(k).toString()))
							{
								termNum2 = q2.get(l).termValueNum;
							}
						}
						if(termNum2 == -1)
							termNum2 = increaseTermCounter();
						
						
						// System.out.println("dfs "+i+" "+j+" "+k);
						// result.get((j+1)*this.listofVariableUConstant.size() + k).cloneID(assignCompoundID(left,right,opType));
//						termID temp1 = new termID();
//						temp1.cloneID(assignCompoundID( left1,right1,opType));
						q1.set(i*(int)Math.pow(this.listofVariableUConstant.size(), 2)+(j+1)*this.listofVariableUConstant.size() + k,assignCompoundID(termNum1,left1,right1,opType));
//						termID temp2 = new termID();
//						temp2.cloneID(assignCompoundID( left2,right2,opType));
						q2.set(i*(int)Math.pow(this.listofVariableUConstant.size(), 2)+(j+1)*this.listofVariableUConstant.size() + k,assignCompoundID(termNum2, left2,right2,opType));
						// for(int t =0; t< result.size();t++)
						// 	System.out.println(result.get(t));
					}
				}
			}
		}
		result = confluence(q1,q2);
		logger.debug("Exiting the nonDetAssignment() function.");
		return(confluence(result,input));
	}
	*/
	public ArrayList<termID> confluence(ArrayList<termID> partition1, ArrayList<termID> partition2)
	{	
		logger.debug("Inside confluence() function.");
		if(partition1.isEmpty())
		{
			return(partition2);
		}
		if(partition2.isEmpty())
		{
			return(partition1);
		}
		ArrayList<termID> result = new ArrayList<termID>(numClass);
		ArrayList<Boolean> accessFlag = new ArrayList<Boolean>();
		for(int i=0; i < this.listofVariableUConstant.size(); i++)
		{
			accessFlag.add(false);
		}
		for(int i = 0; i < this.listofVariableUConstant.size(); i++)
		{
			if(accessFlag.get(i) == false)
			{
				accessFlag.set(i,true);
				if(partition1.get(i).toString().equals(partition2.get(i).toString()))
				{
					result.add(partition1.get(i));
				}
				else
				{
					ArrayList<Integer> set1,set2 = new ArrayList<Integer>();
					set1 = getClass(partition1.get(i),partition1);
					set2 = getClass(partition2.get(i),partition2);
					termID newID = atomicID();
					for(int j=0;j < set1.size();j++)
					{
						for(int k = 0; k < set2.size(); k++)
						{
							if(set1.get(j) == set2.get(k))
							{
								result.add(set1.get(j),newID);
								accessFlag.set(set1.get(j),true);
							}
						}
					}
				}
			}
		}
		for(int i = 0; i < this.operators.size();i++)
		{
			int opType = i+1;
			for(int j = 0; j < this.listofVariableUConstant.size();j++)
			{
				termID left = result.get(j);
				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
				{
					int termNum = -1;
					for(int l = 0; l < result.size(); l++)
					{
						if(Objects.equals(result.get(l).fieldType, opType) && Objects.equals(result.get(l).leftOperand.toString(), result.get(j).toString()) && Objects.equals(result.get(l).rightOperand.toString(), result.get(k).toString()))
						{
							termNum = result.get(l).termValueNum;
						}
					}
					if(termNum == -1)
						termNum = increaseTermCounter();
					termID right = result.get(k);
					result.add(assignCompoundID(termNum,left,right,opType));
				}
			}
		}
		logger.debug("Exiting the confluence() function.");
		return result;
	}
	public ArrayList<Integer> getClass(termID t, ArrayList<termID> p)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i = 0; i < this.listofVariableUConstant.size(); i++)
		{
//			System.out.println(t);
//			System.out.println(p.get(i));
			if(t.toString().equals(p.get(i).toString()))
				result.add(i);
			// System.out.print(this.partitions.get(partitionNum).get(i));
		}
		// int count = this.listofVariableUConstant.size();
		// for(int i = 0; i < this.operators.size();i++)
		// {
		// 	// int opType = i+1;
		// 	for(int j = 0; j < this.listofVariableUConstant.size();j++)
		// 	{
		// 		// termID left = this.partitions.get(partitionNum).get(j);
		// 		for(int k = 0; k < this.listofVariableUConstant.size(); k++)
		// 		{
		// 			if(t.equals(p.get(count)))
		// 				result.add(count);
		// 			count++;
		// 			// termID right = this.partitions.get(partitionNum).get(k);
		// 			// this.partitions.get(partitionNum).add(assignCompoundID(left,right,opType));
		// 		}
		// 	}
		// }
		return result;
	}
	public ArrayList<termID> assignmentStatement(int predLineIndexNo, String lhs, String rhs)
	{
		logger.debug("inside assign method");
		ArrayList<termID> input;// = new ArrayList<termID>(numClass);
		ArrayList<termID> result = new ArrayList<termID>(numClass);
		if(this.partitions.get(predLineIndexNo+1).isEmpty())
			input = this.partitions.get(0);
		else
			input = this.partitions.get(predLineIndexNo+1);
		// for(int i = 0; i < input.size(); i++)
		// {
		// 	termID newID = new termID();
		// 	newID.cloneID(input.get(i));
		// 	result.add(newID);
		// }
		result.addAll(input);
		int rhsIndexPosition;
		if(rhs.contains("+") || rhs.contains("*"))
		{
			int operatorIndex;
			if(rhs.contains("+"))
			{
				operatorIndex = this.operators.indexOf("+");
			}
			else
			{
				operatorIndex = this.operators.indexOf("*");
			}
			String operand1 = rhs.substring(0,rhs.indexOf(this.operators.get(operatorIndex))).trim();
			String operand2 = rhs.substring(rhs.indexOf(this.operators.get(operatorIndex))+1,rhs.length()).trim();
			int operand1IndexPosition = 0;
			int operand2IndexPosition = 0;
			for(int i = 0; i < this.listofVariableUConstant.size(); i++)
			{
				if(this.listofVariableUConstant.get(i).equals(operand1))
				{
					operand1IndexPosition = i;
				}
				if(this.listofVariableUConstant.get(i).equals(operand2))
				{
					operand2IndexPosition = i;
				}
			}
			rhsIndexPosition = (operatorIndex*(int)Math.pow(this.listofVariableUConstant.size(), 2)) + (operand1IndexPosition+1)*this.listofVariableUConstant.size() + operand2IndexPosition;
		}
		else
		{
			rhsIndexPosition = this.listofVariableUConstant.indexOf(rhs);
		}
		// result.get(this.listofVariableUConstant.indexOf(lhs)).cloneID(input.get(rhsIndexPosition));
		termID newID = new termID();
		newID.cloneID(input.get(rhsIndexPosition));
		result.set(this.listofVariableUConstant.indexOf(lhs),newID);//.cloneID(newID);
		// result.remove(this.listofVariableUConstant.indexOf(lhs));
		// result.get(0).valueNum = result.get(0).valueNum+5;
		for(int i = 0; i < this.operators.size();i++)
		{
			int opType = i+1;
			for(int j = 0; j < this.listofVariableUConstant.size();j++)
			{
				termID left = result.get(j);
				for(int k = 0; k < this.listofVariableUConstant.size(); k++)
				{
					termID right = result.get(k);
					if(this.listofVariableUConstant.get(j).equals(lhs) || this.listofVariableUConstant.get(k).equals(lhs))
					{
						int termNum = -1;
						for(int l = 0; l < result.size(); l++)
						{
							if(result.get(l).fieldType == opType && Objects.equals(result.get(l).leftOperand.toString(), result.get(j).toString()) && Objects.equals(result.get(l).rightOperand.toString(), result.get(k).toString()))
							{
								termNum = result.get(l).termValueNum;
								break;
							}
						}
						if(termNum == -1)
							termNum = increaseTermCounter();
						// System.out.println("dfs "+i+" "+j+" "+k);
						// result.get((j+1)*this.listofVariableUConstant.size() + k).cloneID(assignCompoundID(left,right,opType));
//						termID temp = new termID();
//						temp.cloneID(assignCompoundID(left,right,opType));
						result.set(i*(int)Math.pow(this.listofVariableUConstant.size(), 2)+(j+1)*this.listofVariableUConstant.size() + k,assignCompoundID(termNum,left,right,opType));
						// for(int t =0; t< result.size();t++)
						// 	System.out.println(result.get(t));
						
					}
				}
			}
		}
		logger.debug("Exiting the assignmentStatement() function.");
		return result;
	}
	public boolean isSame(ArrayList<termID> p, ArrayList<termID> q)
	{
		if(p.isEmpty() || q.isEmpty())
			return false;
		for(int i = 0; i < this.numClass; i++)
		{
//			if(!p.get(i).toString().equals(q.get(i).toString()))
//			{
//				return false;
//			}
			ArrayList<Integer> equivalenceClass1 = new ArrayList<Integer>();
			ArrayList<Integer> equivalenceClass2 = new ArrayList<Integer>();
			for(int j = 0; j < this.numClass ; j++)
			{
				if(p.get(i).toString().equals(p.get(j).toString()))
				{
					equivalenceClass1.add(j);
				}
			}
			for(int j = 0; j < this.numClass ; j++)
			{
				if(q.get(i).toString().equals(q.get(j).toString()))
				{
					equivalenceClass2.add(j);
				}
			}
			if(!(equivalenceClass1.size() == equivalenceClass2.size()))
			{
				return false;
			}
			for(int j = 0; j < equivalenceClass1.size(); j++)
			{
				if(!(equivalenceClass1.get(j) == equivalenceClass2.get(j)))
				{
					return false;
				}
			}
		}
		return true;
	}
	class expression
	{
		int lineIndexNum;
		char operator;
		String left;
		String right;
		public expression(int lineNum, char operator, String left, String right) 
		{
			this.lineIndexNum = lineNum;
			this.operator = operator;
			this.left = left;
			this.right = right;
		}
		@Override
		public String toString()
		{
			return left+operator+right;
		}
	}
	public void computeAvailableExpressions()
	{
		logger.info("\n");
		logger.info("Available Expression at different program points:");
		this.availableExpression = new ArrayList<ArrayList<expression>>(numProgramPoints);
		for(int i=0; i<numProgramPoints; i++)
		{
			this.availableExpression.add(new ArrayList<expression>());
		}
		boolean convergenceFlag = false;
		while(convergenceFlag== false)
		{
			convergenceFlag = true;
			for(int i = 1; i < this.numProgramPoints; i++)
			{
				ArrayList<expression> oldExressionList;// = new ArrayList<termID>();
//				if(this.partitions.get(i).isEmpty())
//					oldPartition = this.partitions.get(0);
//				else
				oldExressionList = this.availableExpression.get(i);
				if(super.predecessorGraph.get(i-1).size()>1)
				{
					ArrayList<expression> temp = new ArrayList<expression>();
					temp = this.availableExpression.get(super.predecessorGraph.get(i-1).get(0)+1);
					int countPaths = 0;
					while(countPaths<super.predecessorGraph.get(i-1).size()-1)
					{
						logger.debug("Calling availableExpressionConfluence() function. ");
						temp=availableExpressionConfluence(temp,this.availableExpression.get(super.predecessorGraph.get(i-1).get(++ countPaths)+1));
					}
					this.availableExpression.set(i,temp);
				}
				else if(super.statements.get(i-1).contains("=") && !super.statements.get(i-1).contains("=="))
				{
					String lhs = super.statements.get(i-1).substring(0,super.statements.get(i-1).indexOf("=")).trim();
					String rhs = super.statements.get(i-1).substring(super.statements.get(i-1).indexOf("=")+1,super.statements.get(i-1).length()).trim();
					ArrayList<expression> temp = new ArrayList<expression>();
					// System.out.println("dsfaa"+super.predecessorGraph.get(i-1));
					logger.debug("Calling availableExpressionAssignmentStatement() function. ");
					if(super.predecessorGraph.get(i-1).isEmpty())
					{
						temp = availableExpressionAssignmentStatement(i-1,-1,lhs,rhs);
					}
					else
					{
						temp = availableExpressionAssignmentStatement(i-1,super.predecessorGraph.get(i-1).get(0),lhs,rhs);
					}
					this.availableExpression.set(i,temp);
//					int t=0;
//					while(t<i)
//					{
//							// printPartition(0);
//							// this.partitions.get(0).get(1).valueNum = 5;
//						printPartition(t++);
//
//					}
//					printPartition(i);
				}
				else if(super.statements.get(i-1).contains("read"))
				{
					String variable = super.statements.get(i-1).substring(super.statements.get(i-1).indexOf("(")+1,super.statements.get(i-1).indexOf(")")).trim();
					ArrayList<expression> temp = new ArrayList<expression>();
					logger.debug("Calling availableExpressionReadStatement() function.");
					if(super.predecessorGraph.get(i-1).isEmpty())
					{
						temp = availableExpressionReadStatement(-1,variable);
					}
					else
					{
						temp = availableExpressionReadStatement(super.predecessorGraph.get(i-1).get(0),variable);
					}
					this.availableExpression.set(i,temp);
				}
				else if(!super.predecessorGraph.get(i-1).isEmpty())
				{
					this.availableExpression.set(i, this.availableExpression.get(super.predecessorGraph.get(i-1).get(0)+1));
				}
				else if(super.predecessorGraph.get(i-1).isEmpty())
				{
					continue;
				}
				else 
				{
					System.out.println("Unknown type statement. "+ super.statements.get(i-1));
					logger.error("Unknown type statement. "+ super.statements.get(i-1));
				}
				if(!(isSameExpression(oldExressionList,this.availableExpression.get(i))))
				{
					convergenceFlag = false;
				}
			}
			if(convergenceFlag == true)
				logger.info("\t\t\t\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t\t\t\t");
			for(int i = 0; i < this.numProgramPoints;i++)
			{
				if(i>0 && i <= super.statements.size())
				{
					logger.info("Next Statement: "+super.statements.get(i-1));
					System.out.println(super.statements.get(i-1));
				}
				printAvailableExpressions(i);
			}
			logger.info("End of iteration. ConvergenceFlag: " + convergenceFlag);
		}
		logger.debug("Exiting the computeAvailableExpressions() function.");
	}
	public void printAvailableExpressions(int partitionNum)
	{
		String outputString="Available Expression after partition no. "+ partitionNum+"\n";
		for(int i =0; i<this.availableExpression.get(partitionNum).size();i++)
		{
			outputString = outputString + "{"+this.availableExpression.get(partitionNum).get(i)+"}, ";
			System.out.print("{"+this.availableExpression.get(partitionNum).get(i)+"}, ");
		}
		System.out.println();
		logger.info(outputString);
	}
	public ArrayList<expression> availableExpressionAssignmentStatement(int lineNum, int predLineNo,String lhs,String rhs)
	{
		logger.debug("Inside availableExpressionAssignmentStatement() function.");
		ArrayList<expression> input= new ArrayList<expression>();
		ArrayList<expression> result= new ArrayList<expression>();
		input = this.availableExpression.get(predLineNo+1);
		result.addAll(input);
		if(rhs.contains("+") || rhs.contains("*"))
		{
			int i;
			char operator;
			if(rhs.contains("+"))
			{
				operator = '+';
			}
			else
			{
				operator = '*';
			}
			String leftOperand = rhs.substring(0,rhs.indexOf(operator)).trim();
			String rightOperand = rhs.substring(rhs.indexOf(operator)+1,rhs.length()).trim();
			expression newExpression = new expression(lineNum, operator,leftOperand,rightOperand);
			for(i=0; i<result.size();i++)
			{
				if(result.get(i).toString().equals(newExpression.toString()))
					break;
			}
			if(i == result.size())
			{
				result.add(newExpression);
			}
			
		}
		result = killOperation(result,lhs);
		logger.debug("Exiting the availableExpressionAssignmentStatement() function.");
		return result;
	}
	public ArrayList<expression> availableExpressionReadStatement(int predLineNo,String lhs)
	{
		logger.debug("Entering availableExpressionReadStatement() function.");
		ArrayList<expression> input= new ArrayList<expression>();
		ArrayList<expression> result= new ArrayList<expression>();
		input = this.availableExpression.get(predLineNo+1);
		result.addAll(input);
		result = killOperation(result, lhs);
		logger.debug("Exiting availableExpressionReadStatement() function.");
		return result;
	}
	public ArrayList<expression> killOperation(ArrayList<expression> input,String lhs)
	{
		for(int i = 0; i<input.size();i++)
		{
			if(input.get(i).left.equals(lhs) || input.get(i).right.equals(lhs))
			{
				input.remove(i--);
			}
		}
		return input;
	}
	public ArrayList<expression> availableExpressionConfluence(ArrayList<expression> partition1, ArrayList<expression> partition2)
	{
		logger.debug("Inside availableExpressionConfluence() function.");
		if(partition1.isEmpty())
			return partition2;
		if(partition2.isEmpty())
			return partition1;
		ArrayList<expression> result = new ArrayList<expression>();
		for(int i = 0; i < partition1.size();i++)
		{
			for(int j = 0; j < partition2.size(); j++)
			{
				if(partition1.get(i).toString().equals(partition2.get(j).toString()))
				{
					result.add(partition1.get(i));
				}
			}
		}
		logger.debug("Exiting availableExpressionConfluence() function");
		return result;
	}
	public boolean isSameExpression(ArrayList<expression> partition1, ArrayList<expression > partition2)
	{
		if(partition1.size() != partition2.size())
			return false;
		for(int i = 0; i<partition1.size();i++)
		{
			int j;
			for(j = 0; j < partition2.size(); j++)
			{
				if(partition1.get(i).toString().equals(partition2.get(j).toString()))
					break;
			}
			if(j == partition2.size())
				return false;
		}
		return true;
	}
	public String findPrefixString() 
	{
		logger.debug("Inside the findPrefixString() function.");
		int maxLength=0;
		for(int i=0; i<this.listofVariableUConstant.size(); i++)
		{
			int counter = 0;
			String variable = this.listofVariableUConstant.get(i);
			for(int j=0; j<variable.length(); i++)
			{
				if(variable.charAt(j) == '_')
					counter++;
				else
					break;
			}
			if(counter > maxLength)
				maxLength = counter;
		}
		StringBuffer resultantString = new StringBuffer(maxLength+1);
		for (int i = 0; i < maxLength+1; i++){
		   resultantString.append("_");
		}
		resultantString.append("V");
		logger.debug("Exiting the findPrefixString() function.");
		return resultantString.toString();
	}
	public void printRedundantExpression(File file)
	{
		try {
			String filePathString = file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf('/')+1);
			String filenameExtension = file.getName().substring(file.getName().lastIndexOf('.'), file.getName().length());
			String filename = file.getName().substring(0,file.getName().lastIndexOf('.'));
			File transferedCodeFile = new File(filePathString+"../"+"transferedCode/"+filename+".tc"+filenameExtension);
			File redundantExpressionfile = new File(filePathString+"../"+"redundantExpression/"+filename+".rc"+filenameExtension);
			FileWriter transferedCodeFileWriter = new FileWriter(transferedCodeFile,false);
			FileWriter redundantExpressionfileWriter = new FileWriter(redundantExpressionfile,false);
			logger.debug("Inside the printRedundantExpression() function.");
			logger.debug("Starting the findPrefixString() function.");
			String prefixString = findPrefixString();
			logger.debug("Successfully executed findPrefixString() function.");
			StringBuffer outputString = new StringBuffer();
			logger.info("Modified resultant program is:");
			String tempString = "Entered Program after preprocessing:\n";
	        for(int i=0;i<this.statements.size();i++)
	        {
	        	tempString = tempString + (i+1)+". "+this.statements.get(i) +"\n";
	        }
	        redundantExpressionfileWriter.write(tempString);
			redundantExpressionfileWriter.write(String.format("\n\n%-20s %-30s %-30s %s%n", "Line Number", "Actual Expression", "Replaced With","Line Number"));
			redundantExpressionfileWriter.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			for(int i=0; i<super.statements.size();i++)
			{
				if(super.statements.get(i).contains("=") && !super.statements.get(i).contains("==") && (super.statements.get(i).contains("+") || super.statements.get(i).contains("*")))
				{
					String rhs = super.statements.get(i).substring(super.statements.get(i).indexOf("=")+1,super.statements.get(i).length()).trim();
					if(rhs.contains("+") || rhs.contains("*"))
					{
						int operatorIndex;
						int j;
						if(rhs.contains("+"))
						{
							operatorIndex = this.operators.indexOf("+");
						}
						else
						{
							operatorIndex = this.operators.indexOf("*");
						}
						String operand1 = rhs.substring(0,rhs.indexOf(this.operators.get(operatorIndex))).trim();
						String operand2 = rhs.substring(rhs.indexOf(this.operators.get(operatorIndex))+1,rhs.length()).trim();
						int operand1IndexPosition = 0;
						int operand2IndexPosition = 0;
						int rhsIndexPosition;
						for(j = 0; j < this.listofVariableUConstant.size(); j++)
						{
							if(this.listofVariableUConstant.get(j).equals(operand1))
							{
								operand1IndexPosition = j;
							}
							if(this.listofVariableUConstant.get(j).equals(operand2))
							{
								operand2IndexPosition = j;
							}
						}
						rhsIndexPosition = (operatorIndex*(int)Math.pow(this.listofVariableUConstant.size(), 2)) + (operand1IndexPosition+1)*this.listofVariableUConstant.size() + operand2IndexPosition;
						int predIndexNum = -1;
						if(super.predecessorGraph.get(i).size() > 1)
						{
							logger.error("Inside printRedundantExpression() function and more than 1 predecessor for Statement IndexNumber." + i);
							System.out.println("Error: Inside printRedundantExpression() function and more than 1 predecessor for Statement IndexNumber." + i);
						}
						else if(super.predecessorGraph.get(i).size() == 1)
						{
							predIndexNum = super.predecessorGraph.get(i).get(0);
						}
						else 
						{
							predIndexNum = -1;
						}
						ArrayList<termID> referredPartition = this.partitions.get(predIndexNum+1);
//					boolean foundFlag = false;
						for(j = 0; j < this.availableExpression.get(predIndexNum+1).size(); j++)
						{
							String availableExpressionLeftOperand =  this.availableExpression.get(predIndexNum+1).get(j).left;
							String availableExpressionRightOperand =  this.availableExpression.get(predIndexNum+1).get(j).right;
							char availableExpressionOperator = this.availableExpression.get(predIndexNum+1).get(j).operator;
							int availableExpressionOperatorIndex = this.operators.indexOf(Character.toString(availableExpressionOperator));
							int availableExpressionLeftOperandIndex = this.listofVariableUConstant.indexOf(availableExpressionLeftOperand);
							int availableExpressionRightOperandIndex = this.listofVariableUConstant.indexOf(availableExpressionRightOperand);
							int availableExpressionIndexPosition = (availableExpressionOperatorIndex*(int)Math.pow(this.listofVariableUConstant.size(), 2)) + (availableExpressionLeftOperandIndex+1)*this.listofVariableUConstant.size() + availableExpressionRightOperandIndex;
							ArrayList<Integer> equivalentClass = new ArrayList<Integer>(); 
							for(int k=0; k<this.numClass; k++)
							{
								if(referredPartition.get(availableExpressionIndexPosition).toString().equals(referredPartition.get(k).toString()))
									equivalentClass.add(k);
							}
							if(equivalentClass.contains(rhsIndexPosition))
							{
//							foundFlag = true;
								if(equivalentClass.get(0) < super.statements.size())
								{
									if(super.statements.get(i).substring(0,super.statements.get(i).indexOf("=")).trim().equals(this.listofVariableUConstant.get(equivalentClass.get(0))))
									{
										break;
									}
									outputString.append(super.statements.get(i).substring(0,super.statements.get(i).indexOf("=")+1));
									redundantExpressionfileWriter.write(String.format("%-20d %-30s %-30s %d\n", (i+1),operand1+this.operators.get(operatorIndex)+operand2,this.listofVariableUConstant.get(equivalentClass.get(0)),this.availableExpression.get(predIndexNum+1).get(j).lineIndexNum+1));
									outputString.append(this.listofVariableUConstant.get(equivalentClass.get(0)) + ";\n");
									System.out.println("{"+operand1+this.operators.get(operatorIndex)+operand2+"} is replaced by valueNum "+ this.listofVariableUConstant.get(equivalentClass.get(0)) +" "+(this.availableExpression.get(predIndexNum+1).get(j).lineIndexNum+1));
								}
								else 
								{
									outputString.append(super.statements.get(i).substring(0,super.statements.get(i).indexOf("=")+1));
									redundantExpressionfileWriter.write(String.format("%-20d %-30s %-30s %d\n", (i+1),operand1+this.operators.get(operatorIndex)+operand2,this.availableExpression.get(predIndexNum+1).get(j),this.availableExpression.get(predIndexNum+1).get(j).lineIndexNum+1));
									int temp = this.availableExpression.get(predIndexNum+1).get(j).lineIndexNum+1;
									int insertionIndexPosition = outputString.indexOf(";");
									while(--temp > 0 && insertionIndexPosition != -1)
									{
										insertionIndexPosition = outputString.indexOf(";",insertionIndexPosition+1);
									}
									String tempString1 = outputString.substring(0,insertionIndexPosition+1);
									String tempString2 = outputString.substring(insertionIndexPosition+1,outputString.length());
									String statementToInsert = "\n"+prefixString+ referredPartition.get(availableExpressionIndexPosition).termValueNum +"=" +super.statements.get(this.availableExpression.get(predIndexNum+1).get(j).lineIndexNum).substring(super.statements.get(i).indexOf("=")+1,super.statements.get(i).length()).trim()+";";
									outputString = new StringBuffer();
									outputString.append(tempString1+statementToInsert+tempString2);
									outputString.append(prefixString+ referredPartition.get(availableExpressionIndexPosition).termValueNum + ";\n");
									System.out.println("{"+operand1+this.operators.get(operatorIndex)+operand2+"} is replaced by valueNum "+ referredPartition.get(availableExpressionIndexPosition).termValueNum +" "+(this.availableExpression.get(predIndexNum+1).get(j).lineIndexNum+1));
									
								}
								logger.info("{"+operand1+this.operators.get(operatorIndex)+operand2+"} at line number "+ i+" is already computed as {"+this.availableExpression.get(predIndexNum+1).get(j)+"} = " + prefixString+ referredPartition.get(availableExpressionIndexPosition).termValueNum);
								System.out.println("{"+operand1+this.operators.get(operatorIndex)+operand2+"} is replaced by available class {"+this.availableExpression.get(predIndexNum+1).get(j)+"}");
								break;
							}
//						for(int k=0; k<equivalentClass.size(); k++)
//						{
//							int temp=equivalentClass.get(k)-this.listofVariableUConstant.size();
//							int equivalenceClassOperator = temp/(int)Math.pow(this.listofVariableUConstant.size(), 2);
//							temp = temp % (int)Math.pow(this.listofVariableUConstant.size(), 2);
//							int equivalenceClassleftOperandIndex = temp/this.listofVariableUConstant.size();
//							temp = temp % this.listofVariableUConstant.size();
//							int equivalenceClassrightOperandIndex = temp;
//							if(equivalenceClassOperator == operatorIndex && equivalenceClassleftOperandIndex == operand1IndexPosition && equivalenceClassrightOperandIndex == operand2IndexPosition)
//							{
//								foundFlag = true;
//								logger.info("{"+operand1+this.operators.get(operatorIndex)+operand2+"} at line number "+ i+" is already computed as {"+this.availableExpression.get(predIndexNum+1).get(j)+"} = "+ referredPartition.get(equivalentClass.get(k)).termValueNum);
//								System.out.println("{"+operand1+this.operators.get(operatorIndex)+operand2+"} is replaced by available class {"+this.availableExpression.get(predIndexNum+1).get(j)+"}");
//								System.out.println("{"+operand1+this.operators.get(operatorIndex)+operand2+"} is replaced by valueNum "+ referredPartition.get(equivalentClass.get(k)).termValueNum);
//								break;
//							}
//						}
//						if(foundFlag == true)
//							break;
						}
						if(j == this.availableExpression.get(predIndexNum+1).size())
						{
							outputString.append(rhs+";\n");
						}
					}
				}
				else 
				{
					int j;
					for(j=0; j<super.labelLineNumberDict.size(); j++)
					{
						if(i == super.labelLineNumberDict.get(j).lineNum)
						{
							outputString.append(super.statements.get(i)+": ");
							break;
						}
					}
					if(j == super.labelLineNumberDict.size())
					{
						outputString.append(super.statements.get(i));
						outputString.append(";\n");
						
					}
				}
			}
			logger.info(outputString);
			redundantExpressionfileWriter.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			transferedCodeFileWriter.write(outputString.toString());
			logger.debug("Exiting the printRedundantExpression() expression.");
			transferedCodeFileWriter.close();
			redundantExpressionfileWriter.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	public static void forEachFile(File inputFolder)
	{
		for (File file : inputFolder.listFiles()) 
		{
			if(file.isFile())
			{
//				System.out.println(file.getAbsolutePath());
				herbrandEquivalence herbrandEquivalenceObj = new herbrandEquivalence();
				PropertyConfigurator.configure("log4j.properties");
	        	logger.info("Starting to read the file:"+ file.getName());
	            herbrandEquivalenceObj.readFile(file.getAbsolutePath());
	            logger.debug("Read the file successfully.");
	            // herbrandEquivalenceObj.numProgramPoints=herbrandEquivalenceObj.statements.size();
	            logger.debug("Starting to create dataFlow framework:");
	            herbrandEquivalenceObj.dataFlowGraph();
	            logger.debug("Successfully created dataFlow framework.");
	            logger.debug("Creating (variable & constant) and operators list:");
	            herbrandEquivalenceObj.allVariableUConstant();
	            logger.debug("Successfully created (variable & constant) and operators list.");
	            logger.debug("Starting to compute herbrand Equivalence:");
	            herbrandEquivalenceObj.computeHerbrandEquivalence();
	            logger.debug("Successfully computed the herbrand Equivalence.");
	            logger.debug("Starting to compute the Available expressions:");
	            herbrandEquivalenceObj.computeAvailableExpressions();
	            logger.debug("Successfully computed the available expressions.");
	            logger.debug("Starting to execute printRedundantExpression() function.");
	            herbrandEquivalenceObj.printRedundantExpression(file);
	            logger.debug("Successfully executed printRedundantExpression() function.");
			}
			else if(file.isDirectory())
			{
				forEachFile(file);
			}
		}
	}
	static Logger logger = Logger.getLogger(herbrandEquivalence.class);
	public static void main(String[] args)  
	{
		final File inputFolder = new File("../inputFiles");
		final File redundantExpressionFolder = new File("../redundantExpression");
		final File transferedCodeFolder = new File("../transferedCode");
		if(!inputFolder.exists())
		{
			logger.fatal("Create inputFolder and insert some files.");
			System.out.println("Create inputFolder and insert some files.");
			System.exit(0);
		}
		if(!redundantExpressionFolder.exists())
		{
			boolean successfull = redundantExpressionFolder.mkdir();
			if(!successfull)
			{
				logger.fatal("Cannot create "+redundantExpressionFolder.getName()+" folder. Check write permission of current folder.");
			}
		}
		if(!transferedCodeFolder.exists())
		{
			boolean successfull = transferedCodeFolder.mkdir();
			if(!successfull)
			{
				logger.fatal("Cannot create "+transferedCodeFolder.getName()+" folder. Check write permission of current folder.");
			}
		}
		forEachFile(inputFolder);
	}
}


