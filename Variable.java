package aprove.GraphUserInterface.Factories.Solvers.Engines;


public class Variable {
    private String term;
    private String returnType;
    private String functionName;
    
    public Variable(String term, String returnType, String functionName) {
        this.term = term;
        this.returnType = returnType;
        this.functionName = functionName;
    }
    
    public String getTerm() {
        return term;
    }
    
    public String getReturntype() {
        return returnType;
    }
    
    public String getFunctionname() {
        return functionName;
    }
    

}
