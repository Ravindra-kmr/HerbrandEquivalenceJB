
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
public class gvn extends dataFlowFramework {
	int counter;
	int numProgramPoints;
	ArrayList<ArrayList<equivalenceClass>> expressionPool;
	public gvn() {
	}
	
	public void startCounter()
	{
		this.counter = 0;
	}
	public int increaseCounter()
	{
		return(++this.counter);
	}

	public class equivalenceClass
	{
		int valueNum;
		char operator;
		equivalenceClass left;
		equivalenceClass right;
		ArrayList<String> variableList;
		equivalenceClass()
		{
			this.valueNum = increaseCounter();
			this.operator = 'x';
			this.left = null;
			this.right = null;
			this.variableList = new ArrayList<String>();
		}
		equivalenceClass(equivalenceClass object)
		{
			this.valueNum = object.valueNum;
			this.operator = object.operator;
			this.left = object.left;
			this.right = object.right;
			this.variableList = object.variableList;
		}
		equivalenceClass(int valueNum,ArrayList<String> variables)
		{
			this.valueNum = valueNum;
			this.operator = 'x';
			this.left = null;
			this.right = null;
			this.variableList = variables;
		}
		equivalenceClass(int valueNum,char operator,equivalenceClass left, equivalenceClass right)
		{
			this.valueNum = valueNum;
			this.operator = operator;
			this.left = left;
			this.right = right;
			this.variableList = new ArrayList<String>();
		}
		equivalenceClass(int valueNum,char operator,equivalenceClass left, equivalenceClass right, ArrayList<String> variables)
		{
			this.valueNum = valueNum;
			this.operator = operator;
			this.left = left;
			this.right = right;
			this.variableList = variables;
		}
		public String toCompare()
		{
			if(Objects.equals(this.left, null) && Objects.equals(this.right, null))
				return("("+"{"+"null"+"}"+this.operator+"{"+"null"+"},{" + Arrays.toString(this.variableList.toArray())+"}) ");
			else if(Objects.equals(this.left, null))
				return("("+"{"+"null"+"}"+this.operator+"{"+this.right+"},{" + Arrays.toString(this.variableList.toArray())+"}) ");
			else if(Objects.equals(this.right, null))
				return("("+"{"+this.left+"}"+this.operator+"{"+"null"+"},{" + Arrays.toString(this.variableList.toArray())+"}) ");
			else
				return("("+"{"+this.left+"}"+this.operator+"{"+this.right+"},{" + Arrays.toString(this.variableList.toArray())+"}) ");
		}
		@Override
		public String toString()
		{
			return("("+this.valueNum+"{"+this.left+"}"+this.operator+"{"+this.right+"},{" + Arrays.toString(this.variableList.toArray())+"}) ");
		}
	}
	
	public void printExpressionPool(int partitionNum)
	{
		String outputString = "Partition " + partitionNum + "\n";
		for(int i=0 ; i < this.expressionPool.get(partitionNum).size();i++)
		{
			outputString = outputString + this.expressionPool.get(partitionNum).get(i) + "\n";
			System.out.println(this.expressionPool.get(partitionNum).get(i));
		}
		logger.info(outputString);
	}
	public boolean isSame(ArrayList<equivalenceClass> partition1, ArrayList<equivalenceClass> partition2)
	{
		if(partition1.size() != partition2.size())
			return false;
		for (int i = 0; i < partition1.size(); i++) 
		{
			int j;
			for (j = 0; j < partition2.size(); j++) 
			{
//				if(partition1.get(i).operator == partition2.get(j).operator && Objects.equals(partition1.get(i).left, partition2.get(j).left) && Objects.equals(partition1.get(i).right, partition2.get(j).right) && Objects.equals(partition1.get(i).variableList, partition2.get(j).variableList)) //partition1.get(i).left.equals(partition2.get(j).left) && partition1.get(i).right.equals(partition2.get(j).right))// && partition1.get(i).variableList.equals(partition2.get(j).variableList))
//				{
//					break;
//				}
//				System.out.println("Inside isSame");
				if(partition1.get(i).toCompare().equals(partition2.get(j).toCompare()))
				{
					break;
				}
			}
			if(j == partition2.size())
				return false;
		}
		return true;
	}
	public void compute_gvn()
	{
		startCounter();
		this.numProgramPoints = super.statements.size()+1;
		expressionPool = new ArrayList<ArrayList<equivalenceClass>>();
		for(int i = 0; i < this.numProgramPoints; i++)
		{
			this.expressionPool.add(new ArrayList<equivalenceClass>());
		}
		boolean convergenceFlag=false;
		while(convergenceFlag == false)
		{
			convergenceFlag = true;
			for(int i = 1; i < this.numProgramPoints; i++)
			{
				ArrayList<equivalenceClass> oldPartition;// = new ArrayList<termID>();
				if(this.expressionPool.get(i).isEmpty())
					oldPartition = new ArrayList<equivalenceClass>();
				else
					oldPartition = this.expressionPool.get(i);
				if(super.predecessorGraph.get(i-1).size()>1)
				{
					ArrayList<equivalenceClass> temp = new ArrayList<equivalenceClass>();
					temp = this.expressionPool.get(super.predecessorGraph.get(i-1).get(0)+1);
					int countPaths = 0;
					while(countPaths<super.predecessorGraph.get(i-1).size()-1)
					{
						temp=confluence(temp,this.expressionPool.get(super.predecessorGraph.get(i-1).get(++ countPaths)+1));
					}
					this.expressionPool.set(i,temp);
				}
				else if(super.statements.get(i-1).contains("=") && !super.statements.get(i-1).contains("=="))
				{
					String lhs = super.statements.get(i-1).substring(0,super.statements.get(i-1).indexOf("=")).trim();
					String rhs = super.statements.get(i-1).substring(super.statements.get(i-1).indexOf("=")+1,super.statements.get(i-1).length()).trim();
					ArrayList<equivalenceClass> temp = new ArrayList<equivalenceClass>();
					// System.out.println("dsfaa"+super.predecessorGraph.get(i-1));
					if(super.predecessorGraph.get(i-1).isEmpty())
					{
						temp = assignmentStatement(-1,lhs,rhs);
					}
					else
					{
						temp = assignmentStatement(super.predecessorGraph.get(i-1).get(0),lhs,rhs);
					}
					this.expressionPool.set(i,temp);
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
					ArrayList<equivalenceClass> temp = new ArrayList<equivalenceClass>();
					if(super.predecessorGraph.get(i-1).isEmpty())
					{
						temp = nonDetAssignment(-1,variable);
					}
					else
					{
						temp = nonDetAssignment(super.predecessorGraph.get(i-1).get(0),variable);
					}
					this.expressionPool.set(i,temp);
				}
				else if(!super.predecessorGraph.get(i-1).isEmpty())
					this.expressionPool.set(i, this.expressionPool.get(super.predecessorGraph.get(i-1).get(0)+1));
//				else if(this.expressionPool.get(i).isEmpty())		//Check in herbrandequivalence for this case use.
//					this.expressionPool.set(i, this.expressionPool.get(0));
				if(!(isSame(oldPartition,this.expressionPool.get(i))))
				{
					convergenceFlag = false;
				}
			}
			for(int i = 0; i < this.numProgramPoints;i++)
			{
				if(i > 0 && i <= super.statements.size())
				{
					logger.info(super.statements.get(i-1));
					System.out.println(super.statements.get(i-1));
					printExpressionPool(i);
				}
			}
		}
	}
	public ArrayList<equivalenceClass> confluence(ArrayList<equivalenceClass> partition1, ArrayList<equivalenceClass> partition2)
	{
		if(partition1.isEmpty())
			return partition2;
		if(partition2.isEmpty())
			return partition1;
		ArrayList<equivalenceClass> result = new ArrayList<equivalenceClass>();
		for (equivalenceClass class1 : partition1) 
		{
			for (equivalenceClass class2 : partition2) 
			{
				equivalenceClass tempClass = intersection(result,class1,class2);
				if(result.indexOf(tempClass) == -1 && !(tempClass.operator == 'x' && tempClass.variableList.isEmpty()))
				{
					result.add(tempClass);
				}
			}
		}
		return result;
	}
	public equivalenceClass intersection(ArrayList<equivalenceClass> partition, equivalenceClass class1, equivalenceClass class2)
	{
//		equivalenceClass result = new equivalenceClass();
		int newValueNum = -1;
		char newOperator = 'x';
		equivalenceClass newLeftClass = null;
		equivalenceClass newRightClass = null;
		ArrayList<String> newStringList = new ArrayList<String>();
		if(class1.equals(class2))
		{
			if(partition.indexOf(class1) != -1)
			{
				return(partition.get(partition.indexOf(class1)));
			}
//			System.out.println("class1 equals class2 "+ partition);
//			System.out.println("class1 "+class1);
//			System.out.println(partition.indexOf(class1));
			return class1;
		}
		for (String string1 : class1.variableList) 
		{
			for (String string2 : class2.variableList) 
			{
				if(string1.equals(string2))
				{
					newStringList.add(string1);
				}
			}
		}
		if(class1.operator != 'x' && class1.operator == class2.operator)		
		{
			if(class1.left.valueNum == class2.left.valueNum && class1.right.valueNum == class2.right.valueNum)
			{
				newOperator = class1.operator;
				newLeftClass = class1.left;
				newRightClass = class1.right;
			}
			else 
			{
				equivalenceClass temp1 = intersection(partition,class1.left, class2.left);
				equivalenceClass temp2 = intersection(partition,class1.right, class2.right);
				if(partition.indexOf(temp1) == -1 && !(temp1.operator == 'x' && temp1.variableList.isEmpty()))
				{
					partition.add(temp1);
				}
				if(partition.indexOf(temp2) == -1 && !(temp2.operator == 'x' && temp2.variableList.isEmpty()))
				{
					partition.add(temp2);
				}
				if(!(temp1.operator == 'x' && temp1.variableList.isEmpty()) && !(temp2.operator == 'x' && temp2.variableList.isEmpty()))
				{
					newOperator = class1.operator;
					newLeftClass = temp1;
					newRightClass = temp2;
				}
			}
//			if(class1.valueNum == class2.valueNum)
//			{
//				return (new equivalenceClass(class1.valueNum,newOperator,newLeftClass,newRightClass,newStringList);
//			}
//			else 
//			{
//				return(new equivalenceClass(increaseCounter(),newOperator,newLeftClass,newRightClass,newStringList);
//			}
		}
		if(class1.valueNum == class2.valueNum)// && class1.left.valueNum == class2.left.valueNum && !class1.right.valueNum == class2.right.valueNum)
		{
			newValueNum = class1.valueNum;
		}
		else if(!(newStringList.isEmpty() && newOperator == 'x')) //don't put this operator.
		{
			newValueNum = increaseCounter();
		}
		else {
			System.out.println("inside confluence. Assigning the new valueNum.");
		}
		return (new equivalenceClass(newValueNum,newOperator,newLeftClass,newRightClass,newStringList));
	}
	public ArrayList<equivalenceClass> deleteTerm(ArrayList<equivalenceClass> input, String term)
	{
//		ArrayList<equivalenceClass> result = new ArrayList<equivalenceClass>();
//		result.addAll(input); 
		int i;
		for(i = 0; i < input.size(); i++)
		{
			int j;
			for(j = 0; j < input.get(i).variableList.size();j++)
			{
				if(input.get(i).variableList.get(j).equals(term))
				{
					break;
				}
			}
			if(j < input.get(i).variableList.size())
			{
				if(input.get(i).operator == 'x' && input.get(i).variableList.size() == 1)
				{
					input = deleteSingletonClass(input,input.get(i));
				}
				else
				{
					ArrayList<String> tempVariableList = new ArrayList<String>();
					tempVariableList.addAll(input.get(i).variableList);
					tempVariableList.remove(term);
					equivalenceClass temp = new equivalenceClass(input.get(i).valueNum,input.get(i).operator,input.get(i).left,input.get(i).right,tempVariableList);
					for (int k = 0; k < input.size(); k++) {
						if(!(input.get(k).operator == 'x'))
						{
							if(input.get(k).left.equals(input.get(i)))
							{
								input.get(k).left = temp;
							}
							if(input.get(k).right.equals(input.get(i)))
							{
								input.get(k).right = temp;
							}
						}
					}
					input.set(i,temp);
				}
				break;
			}
		}
		return(input);
	}
	public ArrayList<equivalenceClass> deleteSingletonClass(ArrayList<equivalenceClass> input, equivalenceClass deletingClass)
	{
//		ArrayList<equivalenceClass> result = new ArrayList<equivalenceClass>();
//		result.addAll(input);
		for(int i=0;i<input.size();i++)
		{
//			boolean deleteFlag = false;				
			if(!(input.get(i).operator == 'x') && (input.get(i).left.equals(deletingClass) || input.get(i).right.equals(deletingClass)) )
			{
				if(input.get(i).variableList.isEmpty())
				{
					input = deleteSingletonClass(input,input.get(i));
					continue;
				}
				equivalenceClass temp = new equivalenceClass(input.get(i).valueNum,'x',null,null,input.get(i).variableList);
				// if(input.get(i).left.equals(deletingTerm))
				// {
				// 	input.
				// }
				input.set(i,temp);
			}
		}
		input.remove(deletingClass);
		return(input);
	}
	public ArrayList<equivalenceClass> assignmentStatement(int predLineIndexNo, String lhs, String rhs)
	{
		System.out.println("inside assign method "+ lhs);
		// ArrayList<equivalenceClass> input;// = new ArrayList<termID>(numClass);
		 ArrayList<equivalenceClass> result = new ArrayList<equivalenceClass>();
		// if(this.expressionPool.get(predLineIndexNo+1).isEmpty())
		// 	input = this.expressionPool.get(0);			//no use of if and else just need else statement. change in herbrand equivalence too.
		// else
		// 	input = this.expressionPool.get(predLineIndexNo+1);
		// for(int i = 0; i < input.size(); i++)
		// {
		// 	termID newID = new termID();
		// 	newID.cloneID(input.get(i));
		// 	result.add(newID);
		// }
		for(int i = 0; i < this.expressionPool.get(predLineIndexNo+1).size(); i++)
		{
			result.add(new equivalenceClass(this.expressionPool.get(predLineIndexNo+1).get(i)));
		}
//		result.addAll(this.expressionPool.get(predLineIndexNo+1));
		// int rhsIndexPosition;
		// for(int i = 0; i < result.size(); i++)
		// {
		// 	int j;
		// 	for(j = 0; j < result.get(i).variableList.size();j++)
		// 	{
		// 		if(result.get(i).get(j).equals(lhs))
		// 		{
		// 			break;
		// 		}
		// 	}
		// 	if(j < result.get(i).variableList.size())
		// 	{
		// 		equivalenceClass temp = new equivalenceClass();
		// 	}
		// }
		for(int i = 0; i < result.size(); i++)
		{
			if(result.get(i).operator != 'x')
			{
//				int index = input.indexOf(input.get(i).left)
//				System.out.println(this.expressionPool.get(predLineIndexNo+1).indexOf(this.expressionPool.get(predLineIndexNo+1).get(i).left));
				result.get(i).left = result.get(this.expressionPool.get(predLineIndexNo+1).indexOf(this.expressionPool.get(predLineIndexNo+1).get(i).left));
				result.get(i).right = result.get(this.expressionPool.get(predLineIndexNo+1).indexOf(this.expressionPool.get(predLineIndexNo+1).get(i).right));
			}
		}
		result = deleteTerm(result,lhs);
		if(rhs.contains("+") || rhs.contains("*"))
		{
			char operator;
			if(rhs.contains("+"))
			{
				operator = '+';
				//operatorIndex = this.operators.indexOf("+");
			}
			else
			{
				operator = '*';
				// operatorIndex = this.operators.indexOf("*");
			}
			String operand1 = rhs.substring(0,rhs.indexOf(operator)).trim();
			String operand2 = rhs.substring(rhs.indexOf(operator)+1,rhs.length()).trim();
			int operand1IndexPosition = -1;
			int operand2IndexPosition = -1;
			for(int i = 0; i < result.size(); i++)
			{
				for(int j = 0; j < result.get(i).variableList.size();j++)
				{
					if(result.get(i).variableList.get(j).equals(operand1))
					{
						operand1IndexPosition = i;
					}
					if(result.get(i).variableList.get(j).equals(operand2))
					{
						operand2IndexPosition = i;
					}
				}
			}
			if(operand1IndexPosition >=0 && operand2IndexPosition >= 0)
			{
				for(int i = 0; i < result.size(); i++)
				{
					if(result.get(i).operator == operator && Objects.equals(result.get(i).left, result.get(operand1IndexPosition)) && Objects.equals(result.get(i).right, result.get(operand2IndexPosition))) //result.get(i).left.equals(result.get(operand1IndexPosition)) && result.get(i).right.equals(result.get(operand2IndexPosition)))
					{
						ArrayList<String> temp = new ArrayList<String>();
						temp.addAll(result.get(i).variableList);
						temp.add(lhs);
						equivalenceClass newClass = new equivalenceClass(result.get(i).valueNum,operator,result.get(operand1IndexPosition),result.get(operand2IndexPosition) ,temp);
						result.set(i,newClass);
						for (equivalenceClass ss : result) {
							System.out.println(ss);
						}
						return result;
					}
				}
			}
			if(operand1IndexPosition == -1)
			{
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(operand1);
				equivalenceClass newClass = new equivalenceClass(increaseCounter() ,temp);
				operand1IndexPosition = result.size();
				result.add(newClass);
			}
			if(operand2IndexPosition == -1)
			{
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(operand2);
				equivalenceClass newClass = new equivalenceClass(increaseCounter(),temp);
				operand2IndexPosition = result.size();
				result.add(newClass);
			}
			ArrayList<String> newVariableList = new ArrayList<String>();
			newVariableList.add(lhs);
			equivalenceClass newClass = new equivalenceClass(increaseCounter(),operator,result.get(operand1IndexPosition),result.get(operand2IndexPosition),newVariableList);
			result.add(newClass);
//			return result;
			// rhsIndexPosition = (operand1IndexPosition+1)*this.listofVariableUConstant.size() + operand2IndexPosition;
		}
		else
		{
			int rhsIndexPosition = -1;
			for(int i = 0; i < result.size(); i++)
			{
				for(int j = 0; j < result.get(i).variableList.size();j++)
				{
					if(result.get(i).variableList.get(j).equals(rhs))
					{
						rhsIndexPosition = i;

					}
				}
			}
			if(rhsIndexPosition == -1)
			{
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(rhs);
				equivalenceClass newClass = new equivalenceClass(increaseCounter() ,temp);
				rhsIndexPosition = result.size();
				result.add(newClass);
			}
			ArrayList<String> temp = new ArrayList<String>();
			temp.addAll(result.get(rhsIndexPosition).variableList);
			temp.add(lhs);
			equivalenceClass newClass = new equivalenceClass(result.get(rhsIndexPosition).valueNum,result.get(rhsIndexPosition).operator,result.get(rhsIndexPosition).left,result.get(rhsIndexPosition).right ,temp);
			result.set(rhsIndexPosition,newClass);
		}
		for (equivalenceClass temp : result) {
			System.out.println(temp);
		}
		return result;
	}
	
	public ArrayList<equivalenceClass> nonDetAssignment(int predLineIndexNo,String lhs)
	{
		System.out.println("inside nonDetAssignment method");
//		if(this.partitions.get(predLineIndexNo+1).isEmpty())
//			input = this.partitions.get(0);
//		else
//			input = this.partitions.get(predLineIndexNo+1);
		ArrayList<equivalenceClass> result = new ArrayList<equivalenceClass>();
		for(int i = 0; i < this.expressionPool.get(predLineIndexNo+1).size(); i++)
		{
			result.add(new equivalenceClass(this.expressionPool.get(predLineIndexNo+1).get(i)));
		}
//		result.addAll(this.expressionPool.get(predLineIndexNo+1));
		for(int i = 0; i < result.size(); i++)
		{
			if(result.get(i).operator != 'x')
			{
//				int index = input.indexOf(input.get(i).left)
				System.out.println(this.expressionPool.get(predLineIndexNo+1).indexOf(this.expressionPool.get(predLineIndexNo+1).get(i).left));
				result.get(i).left = result.get(this.expressionPool.get(predLineIndexNo+1).indexOf(this.expressionPool.get(predLineIndexNo+1).get(i).left));
				result.get(i).right = result.get(this.expressionPool.get(predLineIndexNo+1).indexOf(this.expressionPool.get(predLineIndexNo+1).get(i).right));
			}
		}
		result = deleteTerm(result,lhs);
		ArrayList<String> tempVariableList = new ArrayList<String>();
		tempVariableList.add(lhs);
		equivalenceClass newClass = new equivalenceClass(increaseCounter(),'x',null,null,tempVariableList);
		result.add(newClass);
		return result;
	}
	final static Logger logger = Logger.getLogger(herbrandEquivalence.class);
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		gvn gvnObj = new gvn();

        if (0 < args.length) 
        {
        	logger.debug("Starting to read the file:"+ args[0]);
            gvnObj.readFile(args[0]);
            logger.debug("Read the file successfully.");
            logger.debug("Starting to create dataFlow framework:");
            gvnObj.dataFlowGraph();
            logger.debug("Successfully created dataFlow framework.");
            logger.debug("Starting to compute gvn:");
            gvnObj.compute_gvn();
            logger.debug("Successfully computed gvn.");
        }
        else
        {
           System.err.println("Invalid arguments count:" + args.length);
           System.exit(0);
        }
        // herbrandEquivalenceObj.numProgramPoints=herbrandEquivalenceObj.statements.size();
	}

}
