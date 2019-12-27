//Praneeth Sangani (PRS79)
import java.io.*;
import java.util.*;

public class ac_test
{
    private static DLB dictionary = new DLB();                                          //DLB containing all the words from "dictionary.txt"
    private static DLB userHistory = new DLB();                                         //DLB containing words that the user has previously entered, also contains words from "user_history.txt" which allows the suggestions to per
    private static ArrayList<String> predictions = new ArrayList<>();                   //ArrayList which contains the at most 5 predictions that will be displayed to the user
    private static ArrayList<Double> predictionGenerationTimes = new ArrayList<>();     //ArrayList that contains all the times it took to generate predictions
    private static Map<String, Integer> wordFrequency = new HashMap<>();                //HashMap used to contain the frequency of each word from the user's history

    public static void main(String[] args) throws IOException
    {
        ArrayList<String> dictionaryPredictions = new ArrayList<>();    //Contains the words that are in the dictionary which match a certain prefix
        ArrayList<String> historyPredictions = new ArrayList<>();       //Contains the words that the user has already entered which match a certain prefix

        createDictionaryDLB();                                          //Creates a DLB with the words from "dictionary.txt"
        createUserHistoryDLB();                                         //Creates a DLB with the words from "user_history.txt" if it exists

        run(dictionaryPredictions, historyPredictions);                 //Runs the main loop of the program
    }//End Main

    //Creates a DLB based on the words in the file "dictionary.txt" if it exists
    //If the file doesn't exist, error will be thrown when program is run
    private static void createDictionaryDLB() throws FileNotFoundException {
        Scanner file = new Scanner(new File("dictionary.txt"));
        while(file.hasNext()) {
            String word = file.next();  //Get the next word in the file
            dictionary.insert(word);    //Insert the word into the dictionary DLB from the file
        }
    }

    //Creates a DLB based on the words in the file "user_history.txt" if it exists
    //If the file doesn't exist, it will be made after the run completes
    private static void createUserHistoryDLB()
    {
        try{
            Scanner file = new Scanner(new File("user_history.txt"));
            while (file.hasNext()) {
                String word = file.next();                  //Get the next word in the file
                userHistory.insert(word);                   //Insert the word into the userHistory DLB from the file
                Integer freq = wordFrequency.get(word);     //Get the frequency of the word we are adding
                if(freq != null)                            //If the word has been added before
                    wordFrequency.put(word, ++freq);        //Then increment
                else                                        //Else
                    wordFrequency.put(word, 1);             //Add the word
            }
        } catch (FileNotFoundException e)
        {

        }
    }

    //Main loop of the program
    private static void run(ArrayList<String> dictionaryPredictions, ArrayList<String> historyPredictions) throws IOException
    {
        boolean firstRun = true;        //Different prompt on the first run
        boolean nextWord = false;       //Allows the user to be prompted when he begins a new word
        String userInput = "";          //Holds the characters that the user has entered so far
        //Used to keep track of words the user picked over the course of multiple runs
        PrintWriter writer = new PrintWriter(new FileWriter("user_history.txt", true ));

        while(!userInput.contains("!"))
        {
            nextWord = promptUser(firstRun, nextWord);        //Prompt the user to enter some input, sets firstRun to false after the first loop
            firstRun = promptUserFirstRun(firstRun);          //Prompts the user for the first letter
            char input = getInput();                          //Get the user's input

            //Special characters that the user can enter
            //! == exit the program
            //$ == add word to the user history
            if(input == '!') {
                exitProgram();
            }

            if(input == '$') {
                addWordToUserHistory(userInput, writer);    //Adds the word the user entered to the history so it can be suggested
                nextWord = true;                            //Word has been completed so user can enter a new word
                userInput = "";                             //Reset the user input so user can enter a new word
            }

            //Checks if the user entered a letter - adds it to the userInput
            //If they entered a number (chose a prediction) it adds it to the user history
            if(userEnteredCharacter(input)){
                userInput  += input;
            }
            else if(userChosePrediction(input, writer)) {
                nextWord = true;
                userInput = "";
            }

            //Generates the predictions and calculates the time it took to generate the predictions
            if(shouldGeneratePredictions(input)) {
                generatePredictions(dictionaryPredictions, historyPredictions, userInput);
            }

            //Check to see if predictions were found, else prompt the user to allow him to add the word to user history
            if(input != 's' && input != '$')
                checkIfPredictionsWereFound();

            //If there are predictions to print, and it is the proper situation(view method for specifications), then print them
            if(shouldPrintPredictions(input)) {
                printPredictions();
            }

            dictionaryPredictions.clear(); //Allows the predictions to update based on the user's input
            historyPredictions.clear();    //Allows the predictions to update based on previously entered words
        }
        writer.close();
    }//End run()

    //Main prompting method. Asks the user for input based on the situation
    private static boolean promptUser(boolean firstRun, boolean nextWord)
    {
        if(nextWord) {
            System.out.print("Enter the first character of the next word: ");
            return false;
        } else if(!firstRun)
            System.out.print("Enter the next character: ");
        return false;
    }

    //This method only runs during the first loop of run(), it asks the for the first input
    private static boolean promptUserFirstRun(boolean firstRun)
    {
        if(firstRun) {
            System.out.print("Enter the first character: ");    //Prompts the user for the first letter
            return false;                                       //Sets first to false, so that this will never run again
        }
        return false;                                           //Sets first to false, so that this will never run again
    }

    //Gets the users input
    private static char getInput()
    {
        Scanner reader = new Scanner(System.in);
        return reader.next().charAt(0);                        //Limits to reading only the first character of what the user entered
    }

    //When the user enters '!' it runs this method which will exit the program before
    //it exits, it calculates and prints the average time it took to generate the predictions
    private static void exitProgram()
    {
        double averageGenerationTime = 0.0;
        //Loop which adds up the times it took to generate the predictions
        for (Double predictionGenerationTime : predictionGenerationTimes)
            averageGenerationTime += predictionGenerationTime;
        averageGenerationTime = averageGenerationTime / predictionGenerationTimes.size();   //Divide by the size of the ArrayList to get the average time
        System.out.println("\n\nAverage Time: " + averageGenerationTime +"s");              //Print out average time
        System.out.println("Bye!");
        System.exit(0);                                                              //End the program
    }

    //When the user enters '$'. This method will run from the loop. This writes the word
    //That the user was typing into the userHistory DLB and "user_history.txt"
    private  static void addWordToUserHistory(String userInput, PrintWriter writer)
    {
        userHistory.insert(userInput);                                              //Insert the word into the user history DLB so that we can predict it later
        Integer freq = wordFrequency.get(userInput);                                //Get the frequency of the word we are adding
        if(freq != null)                                                            //If the word has been added before
            wordFrequency.put(userInput, ++freq);                                   //Then increment
        else                                                                        //Else
            wordFrequency.put(userInput, 1);                                        //Add the word
        writeToUserHistory(userInput, writer);                                      //Calls the method to write the word to "user_history.txt"
        System.out.println("\nThe word \"" + userInput + "\" has been added\n");    //Prints the word the user added
    }

    //Writes str to "user_history.txt"
    private static void writeToUserHistory(String str, PrintWriter writer){
        writer.println(str);    //Writes the word
        writer.flush();         //Flushes the word into "user_history.txt"
    }

    //Checks if the user entered a character or  a ' and not a number,
    //Which would be making a selection from the predictions
    private static boolean userEnteredCharacter(char input)
    {
        return Character.isAlphabetic(input) || input == '\'';
    }

    //If the user entered a number(choosing a prediction), this prints out the word
    //That the user chose and then inserts it into the user history DLB and file
    private static boolean userChosePrediction(char input, PrintWriter writer)
    {
        //Check if the user entered a number
        if(Character.isDigit(input)) {
            System.out.println("\nWORD COMPLETED: " + predictions.get(Character.getNumericValue(input) - 1) + "\n");    //Print out the word the user chose
            userHistory.insert(predictions.get(Character.getNumericValue(input) - 1));                                  //Insert into the DLB
            Integer freq = wordFrequency.get(predictions.get(Character.getNumericValue(input) - 1));                    //Get the frequency of the word we are adding
            if(freq != null)                                                                                            //If the word has been added before
                wordFrequency.put(predictions.get(Character.getNumericValue(input) - 1), ++freq);                       //Then increment
            else                                                                                                        //Else
                wordFrequency.put(predictions.get(Character.getNumericValue(input) - 1), 1);                            //Add the word
            writeToUserHistory(predictions.get(Character.getNumericValue(input) - 1), writer);                          //Write the word to "user_history.txt"
            return true;
        }
        return false;
    }

    //If the following conditions are valid then we should generate the predictions
    private static boolean shouldGeneratePredictions(char input)
    {
        return !Character.isDigit(input) && input != '$' && input != '!';
    }

    //Generates the predictions based on what the user has entered so far
    private static void generatePredictions(ArrayList<String> dictionaryPredictions, ArrayList<String> historyPredictions, String userInput)
    {
        long start = System.nanoTime();                                         //The start time
        getPredictions(dictionaryPredictions, historyPredictions, userInput);   //Generate the Predictions
        long end = System.nanoTime();                                           //The end time
        double seconds = (end - start) / 1000000000.0;                          //Calculate the elapsed time
        predictionGenerationTimes.add(seconds);                                 //Add the elapsed time to the ArrayList which stores all the times
        System.out.println("(" + seconds + "s)");                               //Print out the time
    }

    //Gets the predictions to display from both the user history and dictionary and then limits it to 5
    private static void getPredictions(ArrayList<String> dictionaryPredictions, ArrayList<String> historyPredictions, String userInput)
    {
        Node currNode = findNodeWithChar(userInput, dictionary);                    //Finds the node with the specified prefix (userInput) in dictionary DLB
        dictionary.findPredictions(currNode, userInput, dictionaryPredictions);     //Gets the predictions from the dictionary DLB
        currNode = findNodeWithChar(userInput, userHistory);                        //Finds the node with the specified prefix (userInput) in userHistory DLB
        userHistory.findPredictions(currNode, userInput, historyPredictions);       //Gets the predictions from the userHistory DLB

        predictions.clear();                                                        //Clears the predictions from the last loop

        //If there are words from the userHistory, they take priority and we add them to the dictionary predictions at the beginning
        if(historyPredictions.size() > 0) {
            historyPredictions = orderHistoryPredictions(historyPredictions);       //Puts the words in the predictions in order based on how many times the user used them
            int i = 0;
            while(i < 5 && i < historyPredictions.size()) {                         //Add up to the first 5 most frequently used words from the user's history
                dictionaryPredictions.add(i, historyPredictions.get(i));            //Add the user's history predictions to the front of our predictions
                i++;
            }
        }

        //Adds in words to predictions ArrayList, and make sure there are no duplicates
        int i = 0;
        while(i < 5 && i < dictionaryPredictions.size()) {
            if(!predictions.contains(dictionaryPredictions.get(i)))                 //When we add into the dictionary ArrayList, there can be more than 5,
                predictions.add(dictionaryPredictions.get(i));                      //So we add them to the predictions ArrayList, and only add 5
            else
                addDifferentWord(dictionaryPredictions);                            //If there would be a duplicate, adds a different word instead
            i++;
        }
    }

    //Find the node in the specified dlb with the specified prefix
    private static Node findNodeWithChar(String prefix, DLB dlb)
    {
        Node currNode = dlb.getRoot();                                              //Gets the root node of the specified DLB
        //Goes through the DLB to see if the prefix exits
        for(int i = 0; i < prefix.length(); i++) {
            while(currNode != null && (currNode.getValue() != prefix.charAt(i)))    //While the node's value doesn't match, we go to its right sibling
                currNode = currNode.getRightSib();

            if(currNode == null)                                                    //If the node was null then it doesn't exist in our DLB
                return null;

            currNode = currNode.getChild();                                         //Moves down to the child and then will loop again
        }
        return currNode;
    }

    //Puts the words in the predictions in order based on how many times the user used them
    private static ArrayList<String> orderHistoryPredictions(ArrayList<String> historyPredictions)
    {
        ArrayList<Integer> freqOfWords = new ArrayList<>();                 //Used to extract data from the HashMap

        for(int i = 0; i < historyPredictions.size(); i++) {
            Integer freq = wordFrequency.get(historyPredictions.get(i));    //Get the frequency of the word we are adding
            freqOfWords.add(freq);                                          //Add the number of times the word was used to the ArrayList
        }

        Collections.sort(freqOfWords);                                      //Sort the ArrayList so that we can order the words properly
        Collections.reverse(freqOfWords);                                   //Reverse the order so that we order it from [most used words --> least used words]

        return putWordsInOrder(historyPredictions, freqOfWords);          //Now that we have the order it should be in, this method puts them in order
    }

    //Puts the words from historyPredictions in the order of frequency based of freqOfWords
    private static ArrayList<String> putWordsInOrder(ArrayList<String> historyPredictions, ArrayList<Integer> freqOfWords)
    {
        ArrayList<String> orderedHistoryPredictions = new ArrayList<>();            //New ArrayList to store the ordered predictions
        int i = 0;
        while(historyPredictions.size() > 0) {                                      //Continues until all the elements are added to our ordered ArrayList
            Integer freq = freqOfWords.get(i);                                      //Get the frequency of the word that we are looking for
            for(int j =0; j < historyPredictions.size(); j++) {
                if (freq.equals(wordFrequency.get(historyPredictions.get(j)))) {    //Condition is true if it found the right word from the HashMap
                    orderedHistoryPredictions.add(historyPredictions.get(j));       //Add word to the ordered ArrayList
                    historyPredictions.remove(j);                                   //Remove words as we go so that we don't keep adding the same word over and over if it shares a frequency with another word
                }
            }
            i++;
        }
        return orderedHistoryPredictions;
    }

    //Adds a different word into the predictions ArrayList to avoid duplicates
    private static void addDifferentWord(ArrayList<String> dictionaryPredictions)
    {
        //Make sure that the predictions ArrayList doesn't go over 5, and checks
        //If the dictionary has more than 5 words which we would then use to replace
        if(predictions.size() < 5 && dictionaryPredictions.size() > 5) {
            int i = 1;
            while (dictionaryPredictions.size() - i >= 5 && !predictions.contains(dictionaryPredictions.get(dictionaryPredictions.size() - i)) && predictions.size() < 5) {
                predictions.add(dictionaryPredictions.get(dictionaryPredictions.size() - i)); //Adds words from the dictionary predictions past the first 5, inorder to avoid duplicates
                i++;
            }
        }
    }

    //If no predictions were found then the user entered a new word thats not in the dictionary
    //or in the userHistory. This will let the user know that they can use '$' to add the word
    private static void checkIfPredictionsWereFound()
    {
        //If the size == 0 then the list is empty, thus no predictions
        if(predictions.size() == 0)
            System.out.println("No Predictions Found. Continue typing the word and end it with a '$' and it wil be added");
    }

    //If all the conditions are met then the predictions will be printed
    private static boolean shouldPrintPredictions(char input)
    {
        return predictions.size() > 0 && input != '$' && input != '!' && !Character.isDigit(input);
    }

    //Prints out up to 5 predictions which the user will then be able to choose from
    private static void printPredictions()
    {
        if(predictions.size() < 5) {                                                 //If there are fewer than 5 predictions, then print out how many ever there are
            for (int i = 0; i < predictions.size(); i++)
                System.out.print("(" + (i + 1) + ") " + predictions.get(i) + "  ");
        }
        else {                                                                      //Else we only print out the top 5 predictions
            for (int i = 0; i < 5; i++)
                System.out.print("(" + (i + 1) + ") " + predictions.get(i) + "  ");
        }
        System.out.println("\n");
    }
}//End Class