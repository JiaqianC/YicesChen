package aprove.GraphUserInterface.Factories.Solvers.Engines;


public class VariabTerm{
    private String term;
    private String returnType;
    
    public VariabTerm(String term, String returnType) {
        this.term = term;
        this.returnType = returnType;

    }
    
    public String getTerm() {
        return term;
    }
    
    public String getReturntype() {
        return returnType;
    }
    

}