package aprove.GraphUserInterface.Factories.Solvers.Engines;


public class VarFour {
    private String term;
    private String returnType;
    private String functionName1;
    private String functionName2;
    
    public VarFour(String term, String returnType, String functionName1, String functionName2) {
        this.term = term;
        this.returnType = returnType;
        this.functionName1 = functionName1;
        this.functionName2 = functionName2;
    }
    
    public String getTerm() {
        return term;
    }
    
    public String getReturntype() {
        return returnType;
    }
    
    public String getFunctionname1() {
        return functionName1;
    }
    
    public String getFunctionname2() {
        return functionName2;
    }
    

}
