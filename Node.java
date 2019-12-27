//Praneeth Sangani (PRS79)
public class Node
{
    private char value;     // value in the node
    private Node rightSib;  // The right sibling of the node
    private Node child;     // Child of the node

    //Node initialized without any parameters - all set to null
    public Node()
    {
        value = '\u0000'; //'\u0000' == "null" char
        rightSib = null;
        child = null;
    }

    //Node initialized with the value - value is set, rest null
    public Node(char val)
    {
        value = val;
        rightSib = null;
        child = null;
    }

    //Node initialized with all information, all values set as specified
    public Node(char val, Node rightSib, Node child)
    {
        value = val;
        this.rightSib = rightSib;
        this.child = child;
    }

    //Returns the value of the node
    public char getValue() {
        return value;
    }

    //Sets the value of the node
    public void setValue(char value) {
        this.value = value;
    }

    //Checks if Node has a value. True = has value, false = no value
    public boolean hasValue()
    {
        return value != '\u0000';
    }

    //Returns the right sibling of the node
    public Node getRightSib()
    {
        return rightSib;
    }

    //Sets the right sibling of the node
    public void setRightSib(Node rightSib) {
        this.rightSib = rightSib;
    }

    //Checks for the right sibling of the node; True = has RS, False = no RS
    public boolean hasRightSib()
    {
        return rightSib != null;
    }

    //Returns the child of the node
    public Node getChild() {
        return child;
    }

    //Sets the child of the node
    public void setChild(Node child) {
        this.child = child;
    }

    //Checks for child; True = has a child, False = no child
    public boolean hasChild()
    {
        return child != null;
    }

}
