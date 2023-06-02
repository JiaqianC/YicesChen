package aprove.GraphUserInterface.Factories.Solvers.Engines;

import com.sri.yices.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.stream.*;

import aprove.*;
import aprove.Framework.Logic.*;
import aprove.Framework.PropositionalLogic.*;
import aprove.Framework.PropositionalLogic.Formulae.*;
import aprove.Framework.PropositionalLogic.SMTLIB.*;
import aprove.Framework.PropositionalLogic.SMTLIB.SMTLIBFunctions.*;
import aprove.Framework.Utility.GenericStructures.*;
import aprove.Framework.Utility.SMTUtility.*;
import aprove.Strategies.Abortions.*;
import aprove.Strategies.Annotations.*;

public class TestForAPI {

    public static void main(String[] args){
    try {
        int scalarType = Yices.newScalarType(1);
        int funcsyms = Terms.newUninterpretedTerm("funcsyms",scalarType);
        int intType = Yices.intType();
        int boolType = Yices.boolType();
        int realType = Yices.realType();
        
        
        //int funType = Yices.functionType(realType, realType);
        //int x = Terms.newUninterpretedTerm("x", realType);
        //int i = Terms.newUninterpretedTerm("i", realType);
        //int j = Terms.newUninterpretedTerm("j", realType);
        //int intType = Yices.intType();
        //int f = Terms.newUninterpretedFunction("f",realType,realType);
        //int p = Terms.parse("(= (* x x) 2)");
        //int p1 = Terms.parse("(= (- i 1) (+ j 2)))");
        
        
        String problem = 
         //define [name]::[type]
        "(define w0::int)\n"+
        "(assert (> w0 0))\n"+
        
        "(define a::int)\n"+
        "(assert (>= a w0))\n"+
        
        //define-type define the funcsyms to scalar type
        // funcsyms is a term, and ？ what i need is funcsyms scalar?!!!!!!!!
        "(define-type funcsyms (scalar _0_MY_ISA_IN_A))\n"+
        // relf is the function with 2
        "(define relf::(-> funcsyms int))\n"+
        "(define stat_0_MY_ISA_IN_A::(-> int int))\n"+
        "(define ge__::(-> int int bool))\n"+
        "(define gr__::(-> int int bool))\n"+
        "(define gr_1___1::(-> int int bool))\n"+
        "(define ge_1___1::(-> int int bool))\n"+
        "(assert (= (gr__ 1 1) (or (>  wf_0_MY_ISA_IN_A  wf_0_MY_ISA_IN_A) (and (=  wf_0_MY_ISA_IN_A  wf_0_MY_ISA_IN_A) (or (< (relf _0_MY_ISA_IN_A) (relf _0_MY_ISA_IN_A)) false )))))\n"+
        "(assert (= (ge__ 1 1) true ))\n"+
        "(assert (ge__ 1 1))\n"+
        "(assert (or (gr__ 1 1)))\n"+
        
        "(check)\n";
       /*
        List<String> a = justFun(problem);
        System.out.println(a);
        List<Integer> b = toInterpretFun(a);
        System.out.println(b);
        List<String> c = justTerms(problem);
        System.out.println(c);
        List<Integer> d = toInterpretTerm(c);
        System.out.println(d);
       */
  
        String pattern = "\\(define\\s+(\\w+)::\\(->\\s+(\\w+)\\s+(\\w+)\\)\\)";
        Pattern variablePattern = Pattern.compile(pattern);
        Matcher matcher = variablePattern.matcher(problem);
        List<Integer> uninterpretedTerms1 = new ArrayList<>();
        List<String> testq1 = new ArrayList<>();
        List<String> testq2 = new ArrayList<>();
        List<String> testq3 = new ArrayList<>();
        while (matcher.find()) {
            String term = matcher.group(1);   
            String returntype = matcher.group(2); 
            String functionname = matcher.group(3);
            Variable variable = new Variable(term, returntype, functionname);
            //System.out.println(variable.getTerm());
            //System.out.println(variable.getReturntype());
            //System.out.println(variable.getFunctionname());
            testq1.add(variable.getTerm());
            testq2.add(variable.getReturntype());
            testq3.add(variable.getFunctionname());
        }

    
        for(int i = 0; i <testq1.size(); i++) {     
            
            if (testq2.get(i).equals("bool")) { 
                int getType1 = boolType;
                int term = Terms.newUninterpretedFunction(testq1.get(i),getType1,intType);            
                System.out.println("func with 2:"+testq1.get(i));
       
                uninterpretedTerms1.add(term);
                }
            else if (testq2.get(i).equals("real")) { 
                int getType1 = realType;
                int term = Terms.newUninterpretedFunction(testq1.get(i),getType1,intType);
                System.out.println("func with 2:"+testq1.get(i));
            
                uninterpretedTerms1.add(term);
                }
            else { 
                int getType1 = intType;
                int term = Terms.newUninterpretedFunction(testq1.get(i),getType1,intType);
                System.out.println("func with 2:"+testq1.get(i));
        
                uninterpretedTerms1.add(term);
                }
        }

        String pattern1 = "\\(define\\s+(\\w+)::(\\w+)\\)";
        Pattern variablePattern1 = Pattern.compile(pattern1);
        Matcher matcher1 = variablePattern1.matcher(problem);
        List<Integer> uninterpretedTerms2 = new ArrayList<>();
        List<String> testq11 = new ArrayList<>();
        List<String> testq21 = new ArrayList<>();
        while (matcher1.find()) {
            String term = matcher1.group(1);   
            String returntype = matcher1.group(2); 
            VariabTerm variable1 = new VariabTerm(term, returntype);
            testq11.add(variable1.getTerm());
            testq21.add(variable1.getReturntype());   
        }
        for(int i = 0; i <testq11.size(); i++) {     
            System.out.println("term:"+testq11.get(i));
            int term = Terms.newUninterpretedTerm(testq11.get(i),intType);
            uninterpretedTerms2.add(term);
        } 
        
        String pattern2 = "\\(define\\s+(\\w+)::\\(->\\s+(\\w+)\\s+(\\w+)\\s+(\\w+)\\)\\)";
        Pattern variablePattern2 = Pattern.compile(pattern2);
        Matcher matcher2 = variablePattern2.matcher(problem);
        List<Integer> uninterpretedTerms3 = new ArrayList<>();
        List<String> testq41 = new ArrayList<>();
        List<String> testq42 = new ArrayList<>();
        List<String> testq43 = new ArrayList<>();
        List<String> testq44 = new ArrayList<>();       
        while (matcher2.find()) {
            String term2 = matcher2.group(1);   
            String g2 = matcher2.group(2); 
            String g3 = matcher2.group(3); 
            String g4 = matcher2.group(4); 
            VarFour variable3 = new VarFour(term2, g2, g3, g4);
            testq41.add(variable3.getTerm());
            testq42.add(variable3.getReturntype());   
            testq43.add(variable3.getFunctionname1());
            testq44.add(variable3.getFunctionname2());          
        }
    
        for(int j = 0; j <testq41.size(); j++) {     
            System.out.println("func with 3:"+ testq41.get(j));
            int term3 = Terms.newUninterpretedFunction(testq41.get(j),intType,intType, boolType);
            uninterpretedTerms3.add(term3);
        }  
        
        String pattern3 = "\\(define-type\\s+(\\w+)\\s+\\(scalar\\s+(\\w+)\\s+\\)\\)";
        Pattern variablePattern3 = Pattern.compile(pattern3);
        Matcher matcher3 = variablePattern3.matcher(problem);
  
        
        String withoutcheck = removeLastLine(problem);
        //System.out.print(withoutcheck);
        System.out.print("without check done\n");
       
        String pp = withoutAssert(withoutcheck);
        System.out.print(pp);
        System.out.print("without assert done\n");
        
        String withoutDineType = withoutDefinetype(pp);
   //     System.out.print(withoutDineType);
        System.out.print("without define type done\n");
        
        String filteredProblem = withoutDefine(pp);
        System.out.print(filteredProblem);
        System.out.print("without define done\n");
        
        String result = toGetResult(filteredProblem);
        System.out.print("got the result\n");
        
        System.out.print(filteredProblem);
        System.out.println(result);
        
        
        
        //int p2 = Terms.parse(problem1);
        //ctx.assertFormula(p2);
        //Status stat1 = ctx.check();
        //System.out.println(stat1);
        
        
        
        //Model m = c.getModel();
        //System.out.format("Model for %s\n", Terms.toString(p));
        //System.out.println(m);
        //}
        
        
    } catch(Exception e){
        System.err.println(e);
    }  
    }
    public static String removeLastLine(String text) {
        int startIndex = text.lastIndexOf("("); // find the last (
        int endIndex = text.lastIndexOf(")"); // find the last )

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return text.substring(0, startIndex) + text.substring(endIndex + 1);
        }

        return text;
    }
    public static String withoutAssert(String text) {
        String pp = text          
                .replaceAll("\\(assert\\s", "")               
                .replaceAll("\\)$", "")
                ;
        return pp;
    }
    public static String withoutDefinetype(String text) {
        String k = text
                .replaceAll("\\(define-type\\s", "")
                .replaceAll("\\)$", "");
        return k;
    }
    
    public static String withoutDefine(String text) {
        String[] lines = text.split("\\r?\\n");
        String filteredProblem = Arrays.stream(lines)
                .filter(line -> !line.contains("define"))
                .collect(Collectors.joining("\n"));
        return filteredProblem;
    }
    
    public static String toGetResult(String text) {
        Context ctx = YLib.makeContext();
        int p2 = Terms.parse(text);
        ctx.assertFormula(p2);
        Status stat1 = ctx.check();
        String stat = "";
        if(stat1 == Status.SAT){
            stat = "sat";
        }
        else if(stat1 == Status.UNSAT) {
            stat  = "unsat";
        }
        else {
            stat = "unknown";
        }
        //ctx.close();
        return stat; 
    }
    
    public static List<String> toGetTerms(String text) {
        String pattern = "\\(define\\s+([a-zA-Z_][a-zA-Z0-9_]*)::";
        Pattern variablePattern = Pattern.compile(pattern);
        Matcher matcher = variablePattern.matcher(text);
        List<String> variableList = new ArrayList<>();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            variableList.add(variableName);
        }
        return variableList;
        
    }
    
    
    public static List<String> justFun(String text){
        //"(define f::(-> int int))\n" like this 
        String pattern = "\\(define\\s+(\\w+)::\\(->\\s+(\\w+)\\s+(\\w+)\\)\\)";
        Pattern variablePattern = Pattern.compile(pattern);
        Matcher matcher = variablePattern.matcher(text);
        List<String> variableList = new ArrayList<>();
        List<String> variabletypeList = new ArrayList<>();
        while (matcher.find()) {
            String term = matcher.group(1);   
            String returntype = matcher.group(2); 
            String functionname = matcher.group(3);  
            variableList.add(term); 
        }
        return variableList;
    }
    
    public static List<String> justTerms(String text){
        //"(define i::int)\n" like this 
        String pattern = "\\(define\\s+(\\w+)::(\\w+)\\)";
        Pattern variablePattern = Pattern.compile(pattern);
        Matcher matcher = variablePattern.matcher(text);
        List<String> variableList = new ArrayList<>();
        while (matcher.find()) {
            String term = matcher.group(1);  
            String type = matcher.group(2);  
            variableList.add(term);
        }
        return variableList;
    }
    
    
    public static List<Integer> toInterpretTerm(List<String> a) {
        
        int realType = Yices.realType();
        List<Integer> uninterpretedTerms = new ArrayList<>();
        for(String ab:a) {
            int term = Terms.newUninterpretedTerm(ab, realType);
            uninterpretedTerms.add(term);
        }
        return uninterpretedTerms;        
    }
    
    public static List<Integer> toInterpretFun(List<String> a) {
        int realType = Yices.realType();
        List<Integer> uninterpretedTerms = new ArrayList<>();
        for(String ab:a) {
            int term = Terms.newUninterpretedFunction(ab,realType,realType);
            uninterpretedTerms.add(term);
        }
        return uninterpretedTerms;        
    }
    
    
    
}
