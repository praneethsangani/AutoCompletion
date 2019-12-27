//Praneeth Sangani (PRS79)
import java.util.ArrayList;

public class DLB
{
    private Node root;                      //Root of the DLB
    private final char END_OF_WORD = '^';   //Marks the end of a word
    private final int MAX_PREDICTIONS = 5;  //The max number of predictions we should output

    //Initializing a new DLB
    public DLB()
    {
        root = new Node();
    }

    //Returns the root
    public Node getRoot()
    {
        return root;
    }

    public boolean insert(String str)
    {
        //Add on the END_OF_WORD marker (^), so we can compare
        str += END_OF_WORD;

        //Ensure that the string is valid
        if(!stringIsValid(str)) {
            return false;
        }

        Node currNode = root;   //Begin from the root

        //Loop which inserts each character in to the DLB
        for(int i = 0; i < str.length(); i++) {
            currNode = addChar(currNode, str.charAt(i));
        }
        return true;
    }

    //Checks if the string that we are adding is valid
    private boolean stringIsValid(String str)
    {
        return str != null && !str.isEmpty();
    }

    //Adds a single character to the tree
    private Node addChar(Node currNode, char c)
    {
        if(!currNode.hasValue()) {              //If the current node has no value
            currNode.setValue(c);                   //Assign the character to the node
            currNode.setChild(new Node());          //Create an empty child node
            currNode = currNode.getChild();         //Move to the empty node
        }
        else if(c == currNode.getValue()) {     //Else if the current node has the same value
            currNode = currNode.getChild();         //we just move to the child
        }
        else if(currNode.hasRightSib()) {       //Else if the current node has right sibling
            currNode = currNode.getRightSib();      //we move to that right sib to see if that could be equal to the char
            currNode = addChar(currNode, c);        //Recursively call the method checking the right sibling
        }
        else {                                 //Else if none of those conditions were met, then it isn't in the DLB and we need to create it
            currNode.setRightSib(new Node());       //Create an empty right sibling
            currNode = currNode.getRightSib();      //Move to the empty right sibling
            currNode = addChar(currNode, c);        //Recursively call the method which will trigger the first condition as the node is empty and it will add in a value
        }
        return currNode;
    }

    //Finds predictions based on the prefix in the DLB (this method is called from ac_test)
    public void findPredictions(Node currNode, String prefix, ArrayList<String> predictions)
    {
        if(currNode == null)                                                            //No node with the prefix was found
            return;
        prefix += currNode.getValue();                                                  //Add on the current nodes value onto the prefix
        if(currNode.getValue() == END_OF_WORD)                                          //If the conditions was met, then a word was found with the specified prefix.
            predictions.add(prefix.substring(0, prefix.length()-1));                    //Add the prediction to the ArrayList. Use substring to get rid of the END_OF_WORD marker.
        findPredictions(currNode.getChild(), prefix, predictions);                      //Recursively call itself but this time we go to the child(this lets us reach the END_OF_WORD marker so we can add a word)
        prefix = prefix.substring(0, prefix.length()-1);                                //Remove the last character we just added to the prefix inorder to prepare to go to the right child
        findPredictions(currNode.getRightSib(), prefix, predictions);                   //Recursively call itself but we go to the right sibling(this lets us find multiple words with the same prefix)

    }

    //Checks if a word (str) exits in the dlb
    public boolean contains(String str)
    {
        Node currNode = root;                                                           //Begin at the root
        str += END_OF_WORD;                                                             //Add on the END_OF_WORD marker

        for (int i = 0; i < str.length(); i++)                                          //Loop through every character of the string
        {
            while(currNode != null && (currNode.getValue() != str.charAt(i)))           //Checks if the current nodes value equals the character in the string
                currNode = currNode.getRightSib();                                      //If it doesn't, we go onto the right sibling

            if(currNode == null) {                                                      //If the current node is null then the string is not in the DLB
                return false;
            } else {
                if(str.charAt(i) == END_OF_WORD)                                        //Checks if we reached the end of the word. If so then it is in the DLB
                    return true;
                currNode = currNode.getChild();                                         //Move onto the child node since we found a right sibling which has the same value as the character in the string
            }
        }
        return false;
    }

}
