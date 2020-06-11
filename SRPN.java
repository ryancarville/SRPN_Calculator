import java.util.Stack;
import java.util.Queue;

import java.util.stream.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

//program class for an SRPN calculator. 
public class SRPN {
    // stack for numbers
    Stack<String> nums = new Stack<String>();
    // temp stack for numbers
    Stack<String> tempNumsStack = new Stack<String>();
    // stack for signs
    Stack<String> signs = new Stack<String>();
    // boolean flags
    boolean calcPower, reset;
    // Integers
    Integer tempAnswer, answer, currRCount, sLength, currNumCount;
    // Strings
    String currS, tempNum, regexAll, regexNum, regexSign, minInt, maxInt;
    // array of fixed "random" numbers
    Integer[] rNums = new Integer[] { 1804289383, 846930886, 1681692777, 1714636915, 1957747793, 424238335, 719885386,
            1649760492, 596516649, 1189641421, 1025202362, 1350490027, 783368690, 1102520059, 2044897763, 1967513926,
            1365180540, 1540383426, 304089172, 1303455736, 35005211 };

    // SRPN constructor
    SRPN() {
        this.minInt = String.valueOf(Integer.MIN_VALUE);
        this.maxInt = String.valueOf(Integer.MAX_VALUE);
        this.calcPower = true;
        this.currS = "";
        this.sLength = currS.length();
        this.tempNum = new String();
        this.tempAnswer = null;
        this.currRCount = 0;
        this.currNumCount = 0;
        this.regexAll = "(.*)([0-9 dr+%^*/=-])(.*)";
        this.regexSign = "(.*)([+%^*/=-])(.*)";
        this.regexNum = "(.*)([0-9])(.*)";
    }

    // main program the input calls
    public void processCommand(String s) {
        // add minimum int number to stack at start
        if (nums.isEmpty()) {
            nums.push(minInt);
        }
        // remove any white spaces and set to class variable
        currS = s.replaceAll(" ", "");
        // get the length of the current string and update class variable
        sLength = currS.length();
        // loop thru the input string
        for (int i = 0; i < sLength; i++) {
            // declare and initialize variable for current char
            String currInput = new String();
            currInput = String.valueOf(s.charAt(i));
            // if nothing is inputted, exit
            if (sLength == 0) {
                return;
            } // if input is a single "#" set calculator on/off flag and exit
            else if (sLength == 1 && currInput.matches("#")) {
                calcPower = calcPower ? false : true;
                return;
            } // if calculator flag false, exit function
            else if (!calcPower) {
                return;
            } // if input has 2 or more "#" it is a comment so clear nums any nums add from
              // the input and exit
            else if (sLength > 1 && currInput.matches("#")) {
                String sub = currS.substring(i, sLength);
                if (sub.contains("#")) {
                    while (currNumCount > 0) {
                        nums.pop();
                        currNumCount--;
                    }
                    return;
                } else if (tempNum.length() > 0) {
                    pushNum();
                    continue;
                }
                continue;

            } // if the input is not a valid sign, char or number tell user and move to the
              // next char in the loop
            else if (!Pattern.matches(regexAll, currInput)) {
                System.out.println("Unrecognized operator or operand " + "\"" + currInput + "\"" + ".");
                if (tempNum.length() > 0) {
                    pushNum();
                }
                continue;
            } // when input is "d" print out the number stack and continue thru the loop
            else if (currInput.matches("d")) {
                printAnswerStack();
                continue;
            } // if the input is "r" add current position of rNums array to the numbers stack
              // then continue thru the loop
            else if (currInput.matches("r")) {
                rNumsLoop();
                continue;
            } // if there is a negative sign
            else if (currInput.matches("-")) {
                handleMinus(currInput, i);
                continue;
            } // if it is a number store it
            else if (Pattern.matches(regexNum, currInput)) {
                storeNum(currInput, i);
            } // if it is a sign, store it if infix, but solve if single char
            else if (Pattern.matches(regexSign, currInput)) {
                storeSignOrSolve(currInput);
                continue;
            }
            if ((currS.endsWith("=") && i == (sLength - 2)) || i == sLength - 1) {
                postfix();
            }
        }
    }

    private void handleMinus(String currInput, int i) {
        // if the sign is the first in the input > 1 it is treated as a arithmetic sign
        String prev = "+";
        String next = String.valueOf(currS.charAt(i));
        String sub = currS.substring(1, sLength);
        if (i > 0) {
            next = String.valueOf(currS.charAt(i + 1));
            prev = String.valueOf(currS.charAt(i - 1));
        } // if the minus sign is between a operator and number it is a negative so store
          // it with number
        if (Pattern.matches(regexSign, prev) && Pattern.matches(regexNum, next)) {
            storeNum(currInput, i);
            return;
        } else if (sLength > 1 && currS.startsWith("-") && !sub.contains("-")) {
            storeNum(currInput, i);
        } // else it is a subtraction sign store it
        else {
            storeSignOrSolve(currInput);
            return;
        }
    }

    // print the current answer stack
    private void printAnswerStack() {
        while (!nums.isEmpty()) {
            String currPlate = nums.pop();
            tempNumsStack.push(currPlate);
        }
        while (!tempNumsStack.isEmpty()) {
            String oldPlate = tempNumsStack.pop();
            System.out.println(oldPlate);
            nums.push(oldPlate);
        }
    }

    // loop the array of fixed "r" numbers and keep index in class variable
    private void rNumsLoop() {
        // maximum input length can be this
        int MAX = rNums.length;
        // if the default answer is the only answer in the stack pop it
        if (nums.size() == 1 && nums.peek().equals(minInt)) {
            nums.pop();
        }
        // if the loop counter is == to the MAX limit - 1 reset the loop counter
        if (currRCount == MAX) {
            currRCount = 0;
        }
        // set the rNum at current index as the answer
        answer = rNums[currRCount];
        // add the answer to the number stack
        nums.push(String.valueOf(answer));
        // increase the loop counter
        currRCount++;
    }

    // set class temp variable to whole single number
    private void storeNum(String currInput, int i) {
        tempNum += currInput;
        // if its on the last char in the input push the number to the stack
        if (i == (sLength - 1)) {
            pushNum();
            currNumCount++;

            if (signs.size() > 0) {
                postfix();
            }
        }
    }

    // check the order of operations. If the current sign is a higher order sign
    // then return true
    private boolean orderOfOps(String sign1, String sign2) {
        if ((sign1.equals("*") || sign1.equals("/")) && (sign2.equals("+") || sign2.equals("-"))) {
            return true;
        } else if ((sign1.equals("+") || sign1.equals("-")) && (sign2.equals("+") || sign2.equals("-"))) {
            return true;
        } else {
            return false;
        }
    }

    // store operator sign in the index for infix or do BODMAS
    private void storeSignOrSolve(String sign) {
        if (sLength == 1) {
            BODMAS(sign);
            return;
        }
        if (tempNum.length() > 0) {
            pushNum();
        }
        if (sign.equals("=")) {
            BODMAS(sign);
            return;
        }
        if (signs.isEmpty()) {
            signs.push(sign);
            return;
        } else {
            boolean bool = orderOfOps(sign, signs.peek());
            if (bool) {
                signs.push(sign);
                return;
            } else {
                String higherSign = signs.pop();
                BODMAS(higherSign);
                storeSignOrSolve(sign);
            }
        }

    }

    // push the full number to the number stack from the tempNum string
    private void pushNum() {
        // if default min int value still on stack, remove
        if (nums.peek().equals(minInt)) {
            nums.pop();
        }
        // push int to stack
        nums.push(tempNum);
        // clear the temp number string
        tempNum = "";
        // add one to the counter for current arithmetic numbers
        currNumCount++;
    }

    // push answer
    private void pushAnswer() {
        nums.push(String.valueOf(answer));
        answer = null;
    }

    // preform the infix calculations
    private void postfix() {
        // while there are signs left, preform arithmetic
        while (!signs.isEmpty()) {
            BODMAS(signs.pop());
        }
    }

    // flow control for arithmetic
    private void BODMAS(String sign) {
        // ints for nums to be calculated
        Integer a = null, b = null, max = Integer.parseInt(maxInt), min = Integer.parseInt(minInt);
        if (!sign.equals("=")) {
            b = Integer.parseInt(nums.pop());
            a = Integer.parseInt(nums.pop());
            // if divide by 0
            if (b == 0 && sign.equals("/")) {
                System.out.println("Divide by 0.");
                answer = 0;
                pushAnswer();
                return;
            } // if negative power, set answer to negative power and break out of loop
            if (b < 0 && sign.equals("^")) {
                System.out.println("Negative power.");
                answer = b;
                pushAnswer();
                return;
            } // if max int entered and added to set answer as max int
            if ((a == max || b == max) && sign.equals("+")) {
                answer = max;
                pushAnswer();
                return;
            } // if a input number is min int and sub from set answer as min int
            if ((a == min || b == min) && sign.equals("-")) {
                answer = min;
                pushAnswer();
                return;
            }
        }
        // switch for "=" and default for all the other signs
        switch (sign) {
            case "=":
                finalAnswer();
                break;
            case "^":
                answer = (int) (Math.pow(a, b));
                pushAnswer();
                break;
            case "*":
                answer = a * b;
                pushAnswer();
                break;
            case "/":
                answer = a / b;
                pushAnswer();
                break;
            case "%":
                answer = a % b;
                pushAnswer();
                break;
            case "+":
                answer = a + b;
                pushAnswer();
                break;
            case "-":
                answer = a - b;
                pushAnswer();
                break;
        }
    }

    // send answer to user, reset class and exit
    private void finalAnswer() {
        System.out.println(nums.peek());
        reset = false;
        currNumCount = 0;
        return;
    }
}