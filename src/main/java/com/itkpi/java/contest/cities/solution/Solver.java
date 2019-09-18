package com.itkpi.java.contest.cities.solution;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Solver {
    private List<String> input;
    private List<String> result;

    private boolean[] deadBegLetters;
    private boolean[] deadEndLetters;

    private int alphabetLength;

    /**
     * This method should order cities names (strings) from allCitiesList argument
     * into longest chain possible following the rules of the "Cities game".
     *
     * <br><br>
     *
     * <b> Cities game rules: </b>
     * <li> Cities names should be ordered to the chain in which every next city name starting with the last letter of the previous city name. </li>
     * <li> The first city name can be any. </li>
     * <li> Each city name should be real. </li>
     * <li> All city names in the chain should be unique. </li>
     *
     * <br>
     *
     * <b>Example:</b> London, Naga, Aurora, Aswan
     *
     * <br><br>
     *
     * <b>Additional requirements:</b>
     * Time limit is 2 minutes
     */
    public List<String> solveCitiesGame(List<String> allCitiesList) {
        init(allCitiesList);

        solve();

        outputHandler();
        return result;
    }


    private List<String> solve() {
        findFirstElement();
        int counter = 0;



        //  result = 1487
        int previousRoughResult = 0;
        int currentRoughResult = input.size() + 1;
        for (int i=0 ; i<input.size() ; ++i) {
            if(currentRoughResult > previousRoughResult) {
                previousRoughResult = result.size();
                roughSolver();
                currentRoughResult = result.size();
                //  System.out.format("Current: %d, previous: %d, diff: %d\n", currentRoughResult, previousRoughResult, currentRoughResult - previousRoughResult);

            } else if(counter < 50) {
                deadEndsTrimmer();
                counter++;
                currentRoughResult = previousRoughResult + 1;
            }
            else
                break;
        }

        return result;
    }


    /**
     * Tries to add each element from the input at the each position to the result, does so, if possible.
     * @ warning very time-consuming, call with caution
     */
    private void preciseSolver() {
        for(int i=0 ; i<input.size() ; ++i) {
            String string = input.get(i);
            for(int j=0 ; j<result.size()-1 ; ++j) {
                //  can insert ?
                if (string.charAt(0) == lastChar(result.get(j)) && lastChar(string) == result.get(j+1).charAt(0)){
                    result.add(j+1, string);
                    input.remove(i);
                    string = input.get(i);
                }
            }
        }
    }


    private void deadEndsTrimmer() {
        while (!hasFollowing() && !hasPrevious()) {
            result.remove(0);
            result.remove(result.size()-1);
            inputTrimmer();

            addGoodBeginning();
            getGoodEnding();
        }
    }


    private void addGoodBeginning() {
        int[][] presentMatr = new int[alphabetLength][alphabetLength];

        for (String string : input)
            ++presentMatr[(int)string.charAt(0) - 97][(int)lastChar(string) - 97];

        /*
            Now we have to find string, that is suitable to be the first
        */

        char preferableFirstChar = (char)0;
        char ch = result.get(0).charAt(0);
        end:
        for (String s : input) {
            if (lastChar(s) == ch) {
                for (int j = 0; j < alphabetLength; ++j)
                    if (presentMatr[j][(int) ch - 97] > 0) {
                        preferableFirstChar = (char) (j + 97);
                        break end;

                    }
            }
        }

        for(int i=0 ; i<input.size() ; ++i) {
            if(lastChar(input.get(i)) == ch && input.get(i).charAt(0) == preferableFirstChar) {
                result.add(0, input.get(i));
                input.remove(i);
                return;
            }
        }
    }


    private void getGoodEnding() {
        int[][] presentMatr = new int[alphabetLength][alphabetLength];

        for (String string : input)
            ++presentMatr[(int)string.charAt(0) - 97][(int)lastChar(string) - 97];

        /*
            Now we have to find string, that is suitable to be the first
        */

        char preferableLastChar = (char)0;
        char ch = lastChar(result.get(result.size() - 1));
        end:
        for (String s : input) {
            if (s.charAt(0) == ch) {
                for (int j = 0; j < alphabetLength; ++j)
                    if (presentMatr[(int) ch - 97][j] > 0) {
                        preferableLastChar = (char) (j + 97);
                        break end;

                    }
            }
        }

        for(int i=0 ; i<input.size() ; ++i) {
            if(input.get(i).charAt(0) == ch && lastChar(input.get(i)) == preferableLastChar) {
                result.add(input.get(i));
                input.remove(i);
                return;
            }
        }
    }


    /**
     *  Adds such element from the input to the result, that CAN be easily increased
     */
    private void findFirstElement() {
        int[][] entries = new int[alphabetLength][alphabetLength];

        for (String str : input)
            ++entries[str.charAt(0) - 97][str.charAt(str.length()-1) - 97];

        int richestEntry = 0;
        int finalFirstLetterIndex = 0;
        int finalLastLetterIndex = 0;

        for (int i=0 ; i<alphabetLength ; ++i)
            for (int j=0 ; j<alphabetLength ; ++j)
                if(entries[i][j] > richestEntry){
                    richestEntry = entries[i][j];
                    finalFirstLetterIndex = i;
                    finalLastLetterIndex = j;
                }

        for (String string :input)
            if((int)string.charAt(0) - 97 == finalFirstLetterIndex && (int)string.charAt(string.length()-1)-97 == finalLastLetterIndex) {
                result.add(string);
                input.remove(string);
                return;
            }

    }


    /**
     * Adds suitable elements at the ends of the result List
     */
    private void roughSolver() {
        for(int i=0 ; i<input.size() ; ++i) {
            if(input.get(i).charAt(0) == lastChar(result.get(result.size()-1))) {
                result.add(result.size(), input.get(i));
                input.remove(i);
            } else if (lastChar(input.get(i)) == result.get(0).charAt(0)) {
                result.add(0, input.get(i));
                input.remove(i);
            }
        }
    }


    /**
     * Removes dead words from the input
     */
    private void inputTrimmer() {
        updateDeadLetters();

        for(int i=0 ; i<input.size() ; ++i) {
            if(!deadBegLetters[(int) input.get(0).charAt(0) - 97]
                    && !deadEndLetters[(int) lastChar(input.get(i)) - 97]) {
                input.remove(i);
                --i;
            }
            else if (!deadBegLetters[(int) input.get(0).charAt(0) - 97]) {
                //  deadBegWords.add(input.get(i));
                input.remove(i);
                --i;
            }
            else if(!deadEndLetters[(int) lastChar(input.get(i)) - 97]) {
                //  deadEndWords.add(input.get(i));
                input.remove(i);
                --i;
            }
        }

    }


    private void init(List<String> input) {
        this.input = input;
        inputHandler();
        result = new LinkedList<>();

        alphabetLength = 27;
        deadEndLetters = new boolean[alphabetLength];
        deadBegLetters = new boolean[alphabetLength];
        updateDeadLetters();

    }


    private void updateDeadLetters() {
        for (int i=0 ; i<alphabetLength ; ++i) {
            deadBegLetters[i] = false;
            deadEndLetters[i] = false;
        }

        for(String string : input) {
            deadEndLetters[(int)string.charAt(0) - 97] = true;
            deadBegLetters[(int)lastChar(string) -97]  = true;
        }
    }


    /**
     * Recapitalizas the first input letter
     */
    private void inputHandler() {
        input = input
                .stream()
                .map(this::decapitalize)
                .collect(Collectors.toList());

    }


    /**
     * Places capital letters back
     */
    private void outputHandler() {
        result = result
                .stream()
                .map(this::inCapitalize)
                .collect(Collectors.toList());
    }


    private String decapitalize(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }


    private String inCapitalize(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }


    /**
     * Inserts "\n" between each element pair, so result will be written by lines
     * @return modified result
     * @ warning this class is unusable after this method is called!
     */


    /**
     * @return true, if it is possible to add another string to the end of the result,
     *         false otherwise
     */
    private boolean hasFollowing() {
        char lastResultLetter = lastChar(result.get(result.size()-1));
        for (String string : input) {
            if(lastResultLetter  == string.charAt(0))
                return true;
        }
        return false;
    }


    /**
     * @return true, if it is possible to add another string to the beginning of the result,
     *               false otherwise
     */
    private boolean hasPrevious() {
        char firstResultLetter = result.get(0).charAt(0);
        for (String string : input) {
            if(firstResultLetter == lastChar(string))
                return true;
        }
        return false;
    }


    /**
     * Simple method, that returns last character of the String.
     */
    private static char lastChar(String str) {
        return str.charAt(str.length()-1);
    }
}
