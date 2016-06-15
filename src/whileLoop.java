import java.util.ArrayList;

public class whileLoop {
    private ArrayList<Token> conditionTokens;
    //    private ArrayList<Token> instructionTokens;
    private int myStartingPoint;

    public whileLoop(ArrayList<Token> tokens) {
        conditionTokens = new ArrayList<Token>();
//        instructionTokens = new ArrayList<Token>();
        setConditionTokens(tokens.get(tokens.size() - 3), tokens.get(tokens.size() - 2), tokens.get(tokens.size() - 1));
        /*for(int i = 3; i < tokens.size(); i++){
            instructionTokens.add(tokens.get(i));
        }*/
    }

    public void setMyStartingPoint(int sP) {
        this.myStartingPoint = sP;
    }

    public int getMyStartingPoint() {
        return myStartingPoint;
    }

    private void setConditionTokens(Token fOperand, Token sOperand, Token operation) {
        conditionTokens.add(fOperand);
        conditionTokens.add(sOperand);
        conditionTokens.add(operation);
    }

    /*    public void setInstructionTokens(ArrayList<Token> tokens){
            for(int i = 0; i < tokens.size(); i++){
                instructionTokens.add(tokens.get(i));
            }
        }*/
    public ArrayList<Token> getConditionTokens() {
        return conditionTokens;
    }
/*    public ArrayList<Token> getInstructionTokens(){
        return instructionTokens;
    }*/
}
