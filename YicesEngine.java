package aprove.GraphUserInterface.Factories.Solvers.Engines;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.stream.*;

import com.sri.yices.*;

import aprove.*;
import aprove.Framework.Logic.*;
import aprove.Framework.PropositionalLogic.*;
import aprove.Framework.PropositionalLogic.Formulae.*;
import aprove.Framework.PropositionalLogic.SMTLIB.*;
import aprove.Framework.PropositionalLogic.SMTLIB.SMTLIBFunctions.*;
import aprove.Framework.Utility.GenericStructures.*;
import aprove.Framework.Utility.SMTUtility.*;
import aprove.GraphUserInterface.Factories.Solvers.Engines.Variable;
import aprove.Strategies.Abortions.*;
import aprove.Strategies.Annotations.*;

/**
 * Calls yices and tries to get a model for a given SMT formula.
 *
 * @author Andreas Kelle-Emden
 */
public class YicesEngine extends SMTEngine {
    private static final Logger LOG =
        Logger.getLogger("aprove.GraphUserInterface.Factories.Solvers.Engines.YicesEngine");

    public static class Arguments extends SMTEngine.Arguments {
        /** Extra options that are passed to yices when it is called. */
        public String ARGUMENTS = "";
    }

    /** The arguments given to this processor. */
    private final Arguments args;

    @ParamsViaArgumentObject
    public YicesEngine(final Arguments arguments) {
        super(arguments);
        this.args = arguments;
    }

    public YicesEngine() {
        this(new Arguments());
    }

    /** {@inheritDoc} */
    @Override
    public YNM satisfiable(final List<Formula<SMTLIBTheoryAtom>> formulas, final SMTLogic logic, final Abortion aborter)
            throws AbortionException, WrongLogicException {
        return this.solveAndPutIntoFormula(formulas, logic, aborter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<YNM, Map<String, String>> solve(final List<Formula<SMTLIBTheoryAtom>> formulas,
        final SMTLogic logic,
        final Abortion aborter) throws AbortionException, WrongLogicException {
        final SMTFormulaToYICESVisitor vis = SMTFormulaToYICESVisitor.create();
        for (final Formula<SMTLIBTheoryAtom> f : formulas) {
            aborter.checkAbortion();
            vis.handleConstraint(f);
        }
        final Pair<YNM, Map<String, String>> resultPair = this.solve(vis.getResult(), logic, aborter);

        resultPair.y = SMTEngine.translateResultMapToOldNames(resultPair.y, vis.getVarNameMap());

        return resultPair;
    }

    /**
     * {@inheritDoc}
     */
    public YNM solveAndPutIntoFormula(final List<Formula<SMTLIBTheoryAtom>> formulas,
        final SMTLogic logic,
        final Abortion aborter) throws AbortionException, WrongLogicException {
        // Call the normal solve routine:
        final SMTFormulaToYICESVisitor vis = SMTFormulaToYICESVisitor.create();
        for (final Formula<SMTLIBTheoryAtom> f : formulas) {
            vis.handleConstraint(f);
        }
        final Pair<YNM, Map<String, String>> resultPair = this.solve(vis.getResult(), logic, aborter);

        //Be defensive:
        if (resultPair == null) {
            return YNM.MAYBE;
        }

        final YNM resType = resultPair.x;
        final Map<String, String> result = resultPair.y;
        if (result == null) {
            assert (resType != YNM.YES) : "SMT returned SAT, but we have no model!";
            return resType;
        }

        final SMTLIBVarNameMap varNameMap = vis.getVarNameMap();
        final Map<String, SMTLIBAssignableSemantics> nameToVarMap = varNameMap.getNameToVarMap();
        for (final Map.Entry<String, String> e : result.entrySet()) {
            final String key = e.getKey();
            final String val = e.getValue();

            if (key.startsWith("(")) {
                // Function value
                final SMTLIBFunction<?> v = (SMTLIBFunction<?>) nameToVarMap.get(key);
                if (v != null) {
                    final String[] sArr = key.split(" ");
                    final List<String> params = new ArrayList<String>(sArr.length);
                    for (final String element : sArr) {
                        params.add(element);
                    }
                    v.setResult(params, val);
                }
            } else {
                // Variable value
                final SMTLIBVariable<?> v = (SMTLIBVariable<?>) nameToVarMap.get(key);
                if (v != null) {
                    v.setResult(val);
                }
            }
        }
        return resType;
    }

    @Override
    public Pair<YNM, Map<String, String>> solve(final String smtString, final SMTLogic logic, final Abortion aborter)
            throws AbortionException, WrongLogicException {
        //System.err.println("Yices called."); // ... it wants its model back!
        if (logic == SMTLogic.QF_NIA) {
            throw new WrongLogicException("yices does not support QF_NIA");
        }
        final Process process;
        File input = null;
        try {
            aborter.checkAbortion();
            final long nanos1 = System.nanoTime();
            //use it for file input
            input = File.createTempFile("aproveSMT", ".ys");
            input.deleteOnExit();
            //System.out.print(input);
            
            final Writer inputWriter = new OutputStreamWriter(new FileOutputStream(input));
            inputWriter.write(smtString);
            inputWriter.close();
            aborter.checkAbortion();

            YicesEngine.LOG.log(Level.FINER, "SMT    to {0}\n", input.getCanonicalPath());

            YicesEngine.LOG.log(Level.FINER, "Invoking {0}\nyices -e");

            final Map<String, String> resMap = new LinkedHashMap<String, String>();

            aborter.checkAbortion();
            Pair<List<String>, List<String>> lines = new Pair<>(new ArrayList<>(),new ArrayList<>());
            final List<String> cmds = new ArrayList<>();
            
                    
            if (this.args.ARGUMENTS != "") {
                cmds.add(this.args.ARGUMENTS);
            }
            
            try {
                BufferedReader reader = new BufferedReader(new FileReader(input));
                String getcommand = reader.readLine();
                while (getcommand != null) {     
                    cmds.add(getcommand);
                    //System.out.println(getcommand);
                    getcommand = reader.readLine();
                } 
                reader.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            String joined = String.join("\n", cmds);
            //System.out.println(joined);
       
            try {
                
                int scalarType = Yices.newScalarType(1);
                int funcsyms = Terms.newUninterpretedTerm("funcsyms",scalarType);
                int intType = Yices.intType();
                int boolType = Yices.boolType();
                int realType = Yices.realType();
                
                String pattern = "\\(define\\s+(\\w+)::\\(->\\s+(\\w+)\\s+(\\w+)\\)\\)";
                Pattern variablePattern = Pattern.compile(pattern);
                Matcher matcher = variablePattern.matcher(joined);
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
                Matcher matcher1 = variablePattern1.matcher(joined);
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
                Matcher matcher2 = variablePattern2.matcher(joined);
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
                Matcher matcher3 = variablePattern3.matcher(joined);
          
                
                String withoutcheck = TestForAPI.removeLastLine(joined);
                //System.out.print(withoutcheck);
                System.out.print("without check done\n");
               
                String pp = TestForAPI.withoutAssert(withoutcheck);
                System.out.print(pp);
                System.out.print("without assert done\n");
                
                String withoutDineType = TestForAPI.withoutDefinetype(pp);
           //     System.out.print(withoutDineType);
            //    System.out.print("without define type done\n");
                
                String filteredProblem = TestForAPI.withoutDefine(pp);
                //System.out.print(filteredProblem);
                //System.out.print("without define done\n");
                
                String result = TestForAPI.toGetResult(filteredProblem);
                System.out.print("got the result\n");
                
                System.out.print(filteredProblem);
                System.out.println(result);
                //String withoutcheck = TestForAPI.removeLastLine(joined);
                //String pp = TestForAPI.withoutAssert(withoutcheck);   
                //String filteredProblem = TestForAPI.withoutDefine(pp);
                //String result = TestForAPI.toGetResult(filteredProblem);   
                List<String> stringList = new ArrayList<>();
                
                stringList.add(result);
                lines = new Pair<>(stringList,new ArrayList<>());
                
            } catch(Exception e){
                System.err.println(e);
            }


            for (final String line : lines.y) {
                if ("Error: feature not supported: non linear problem.".equals(line)) {
                    throw new WrongLogicException(line);
                } else {
                    System.err.println("YICES stderr: " + line);
                }
            }
            YNM resType = YNM.MAYBE;
            // aborter.checkAbortion();
            final Iterator<String> it = lines.x.iterator();
            while (it.hasNext()) {
                final String line = it.next();
                YicesEngine.LOG.log(Level.FINEST, "{0}\n", line);
                //System.err.println("yices-out: " + line);
                if (line.startsWith("unsat")) {
                    YicesEngine.LOG.log(Level.FINE, "YICES says: UNSAT\n");
                    resType = YNM.NO;
                }
                if (line.startsWith("sat")) {
                    YicesEngine.LOG.log(Level.FINE, "YICES says: SAT\n");
                    resType = YNM.YES;
                }
                if (line.startsWith("unknown")) {
                    YicesEngine.LOG.log(Level.FINE, "YICES says: UNKNOWN\n");
                    resType = YNM.MAYBE;
                }
                if (line.startsWith("(")) {
                    if (line.length() < 4) {
                        // A line with no information - very strange!
                        continue;
                    }
                    if (line.charAt(3) == '(') {
                        // Function result
                        String[] sArr = line.split(" ");
                        if (sArr.length < 4) {
                            // maybe the line is too long and the result is continued in the next line?
                            if (it.hasNext()) {
                                String nextLine = it.next();
                                nextLine = nextLine.trim();
                                sArr = (line + " " + nextLine).split(" ");
                            }
                        }

                        assert (sArr.length >= 4) : line + " " + input.getCanonicalPath();
                        final StringBuilder resx = new StringBuilder();
                        resx.append(sArr[1]);
                        for (int i = 2; i < sArr.length - 1; i++) {
                            resx.append(" ");
                            resx.append(sArr[i]);
                        }
                        final String resy = sArr[sArr.length - 1].substring(0, sArr[sArr.length - 1].length() - 1);
                        resMap.put(resx.toString(), resy);
                    } else {
                        // Variable result
                        final String[] sArr = line.split(" ");
                        if (sArr.length != 3) {
                            if (!Globals.DEBUG_NONE) {
                                System.err.println("line: ");
                                System.err.println(line);
                                System.err.println("following three lines: ");
                                int count = 0;
                                while (it.hasNext() && count < 3) {
                                    count++;
                                    final String nextLine = it.next();
                                    System.err.println(nextLine);
                                }
                            }
                            assert (false);
                        }
                        final String res = sArr[2].substring(0, sArr[2].length() - 1);
                        resMap.put(sArr[1], res);
                    }
                    //                } else {
                    //                    log.log(Level.WARNING, "Yices returns an unknown line: "+line);
                }
            }
            final long nanos2 = System.nanoTime();
            if (YicesEngine.LOG.isLoggable(Level.FINE)) {
                YicesEngine.LOG.fine("SMT solving with Yices took " + (nanos2 - nanos1) / 1000000L + " ms.");
            }
            input.delete();
            aborter.checkAbortion();
            return new Pair<YNM, Map<String, String>>(resType, resMap);

        } catch (final NoSuchElementException e) {
            // just return null
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.delete();
            }
        }
        return null;
    }
}
