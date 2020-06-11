import java.util.Stack;
import java.util.stream.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

//program class for an SRPN calculator. 
public class SRPN {
    // stack for numbers
    Stack<Integer> nums = new Stack<Integer>();
    // stack for signs
    Stack<Character> signs = new Stack<Character>();
    // stack for temp numbers
    Stack<Integer> tempNumsStack = new Stack<Integer>();
    // boolean flags
    boolean calcPower, reset, doInfix, singleInfix, firstInfix;
    // Integers
    Integer tempAnswer, answer, currRCount, sLength, signCount, signIndex, minInt, maxInt, currNumCount;
    // Strings
    String currS, tempNum, regexAll, regexNum, regexSign;
    // array of fixed "random" numbers
    Integer[] rNums = new Integer[] { 1804289383, 846930886, 1681692777, 1714636915, 1957747793, 424238335, 719885386,
            1649760492, 596516649, 1189641421, 1025202362, 1350490027, 783368690, 1102520059, 2044897763, 1967513926,
            1365180540, 1540383426, 304089172, 1303455736, 35005211 };
    // array to keep signs in order of input
    char[] indexOfSign;

    // enum class for all arithmetic
    enum Signs {
        ADD('+', (a, b) -> a + b), SUB('-', (a, b) -> a - b), MULTI('*', (a, b) -> a * b), DIV('/', (a, b) -> a / b),
        DIVMOD('%', (a, b) -> a % b), POWER('^', (a, b) -> (int) Math.pow(a, b));

        // sign variable
        char sign;
        // BiFunction instance to preform the arithmetic
        BiFunction<Integer, Integer, Integer> operation;

        // enum Signs constructor
        Signs(final char sign, final BiFunction<Integer, Integer, Integer> operation) {
            this.sign = sign;
            this.operation = operation;
        }

        // get the appropriate sign and BiFunction from the enum and return it
        public static Signs signArithmetic(final char sign) {
            return Stream.of(Signs.values()).filter(operator -> operator.sign == sign).findFirst().orElse(null);
        }

        // preform the arithmetic
        public Integer apply(final int a, final int b) {
            return operation.apply(a, b);
        }
    }

    // SRPN constructor
    SRPN() {
        this.minInt = Integer.MIN_VALUE;
        this.maxInt = Integer.MAX_VALUE;
        this.calcPower = true;
        this.currS = "";
        this.sLength = currS.length();
        this.tempNum = new String();
        this.tempAnswer = null;
        this.signCount = 0;
        this.signIndex = 1;
        this.doInfix = false;
        this.singleInfix = true;
        this.firstInfix = true;
        this.currRCount = 0;
        this.currNumCount = 0;
        this.regexAll = "(.*)([0-9 dr#+%^*/=-])(.*)";
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
        // initialize the array for the operators
        indexOfSign = new char[sLength];
        // loop thru the input string
        for (int i = 0; i < sLength; i++) {
            // declare and initialize variable for current char
            String currInput = new String();
            currInput = String.valueOf(s.charAt(i));
            // if nothing is inputted, exit
            if (sLength == 0) {
                return;
            } // if the input is not a valid sign, char or number tell user and move to the
              // next char in the loop
            else if (!Pattern.matches(regexAll, currInput)) {
                System.out.println("Unrecognized operator or operand " + "\"" + currInput + "\"" + ".");
                if (tempNum.length() > 0) {
                    pushNum();
                }
                continue;
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
                // if the sign is the first in the input > 1 it is treated as a arithmetic sign
                String prev = "";
                String next = "";
                if (i > 0) {
                    next = String.valueOf(currS.charAt(i + 1));
                    prev = String.valueOf(currS.charAt(i - 1));
                }
                if (Pattern.matches(regexSign, prev) && Pattern.matches(regexNum,next)) {
                    storeNum(currInput, i);
                    continue;
                } else {
                    storeSignOrSolve(currInput, i);
                    continue;
                }
            } // if it is a number store it
            else if (Pattern.matches(regexNum, currInput)) {
                storeNum(currInput, i);
            } // if it is a sign, store it if infix, but solve if single char
            else if (Pattern.matches(regexSign, currInput)) {
                storeSignOrSolve(currInput, i);
                continue;
            } // if it is the end of the loop and the doInfix flag true, preform the
              // arithmetic in correct order
            if (i == sLength - 1 && doInfix) {
                infix();
            }
        }
    }

    // print the current answer stack
    private void printAnswerStack() {
        while (!nums.isEmpty()) {
            int currPlate = nums.pop();
            tempNumsStack.push(currPlate);
        }
        while (!tempNumsStack.isEmpty()) {
            int oldPlate = tempNumsStack.pop();
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
        nums.push(answer);
        // increase the loop counter
        currRCount++;
    }

    // set class temp variable to whole single number
    private void storeNum(String currInput, int i) {
        tempNum += currInput;
        // if its on the last char in the input push the number to the stack
        if (i == (sLength - 1)) {
            pushNum();
        }
    }

    // store operator sign in the index for infix or do BODMAS
    private void storeSignOrSolve(String currInput, int i) {
        char sign = currInput.charAt(0);
        // if infix input
        if (sLength > 1) {
            signs.push(sign);
            doInfix = true;
            // if the input is not a single sign and there is still a number to be pushed to
            // the stack, push num, push sign, and set infix flag
            if (tempNum.length() > 0) {
                pushNum();
            }
        } // else must be a normal input style so do the arithmetic
        else {
            BODMAS(sign, 1);
            return;
        }
    }

    // push the full number to the number stack from the tempNum string
    private void pushNum() {
        // if default min int value still on stack, remove
        if (nums.peek() == minInt) {
            nums.pop();
        }
        // parse string to int
        int n = Integer.parseInt(tempNum);
        // push int to stack
        nums.push(n);
        // clear the temp number string
        tempNum = "";
        // add one to the counter for current arithmetic numbers
        currNumCount++;
    }

    // push answer
    private void pushAnswer() {
        nums.push(answer);
        answer = null;
    }

    // check the order of operations. If the current sign is a higher order sign
    // then return true
    private boolean orderOfOps(char sign1, char sign2) {
        if ((sign1 == '*' || sign1 == '/') && (sign2 == '+' || sign2 == '-')) {
            return true;
        } else {
            return false;
        }
    }

    // preform the infix calculations
    private void infix() {
        // while there are signs left, preform arithmetic
        while (!signs.isEmpty()) {
            // check and set single sign infix
            boolean singleOpp = signs.size() > 1 ? false : true;
            singleInfix = singleOpp;
            // get the index of the sign
            int index = signs.size();
            // get the sign itself
            char sign = signs.pop();

            // if only one sign in signs stack do arithmetic and exit
            if (singleInfix) {
                BODMAS(sign, index);
                return;
            } // if there is more than one sign left, compare the sings and preform the higher
              // sign arithmetic
            else if (orderOfOps(sign, signs.peek())) {
                BODMAS(sign, index);
            } // else the signs are in the wrong order so switch them while preserving their
              // original index
            else {
                index = signs.size();
                char higherSign = signs.pop();
                signs.push(sign);
                BODMAS(higherSign, index);
            }
        }
    }

    // flow control for arithmetic
    private void BODMAS(char sign, int index) {
        // ints for nums to be calculated
        int a, b, c;
        // switch for "=" and default for all the other signs
        switch (sign) {
            case '=':
                finalAnswer();
                break;
            default:
                // set the rest flag to false
                reset = false;
                // if it is a infix problem with more than 1 sign
                if (doInfix && !singleInfix) {
                    // if it is the first calculation in the infix and the sign was not the last
                    // sign entered
                    if (firstInfix && index != nums.size() - 1) {
                        // loop to the correct index in the numbers stack while popping the numbers into
                        // a temporary stack
                        for (int i = currNumCount - 1; i > index; i--) {
                            int plate = nums.pop();
                            tempNumsStack.push(plate);
                        }
                        // pop off the correct ints need for the arithmetic
                        b = nums.pop();
                        a = nums.pop();
                        // return the numbers that were removed from the stack back to the stack in the
                        // correct order
                        while (!tempNumsStack.isEmpty()) {
                            int plate = tempNumsStack.pop();
                            nums.push(plate);
                        }
                        // set firstInfix flag to false
                        firstInfix = false;
                    } // if it is the first calculation in the infix and the sign was entered last
                    else if (firstInfix && index == nums.size() - 1) {
                        // pop off the two numbers for the arithmetic
                        b = nums.pop();
                        a = nums.pop();
                    } else {
                        System.out.println("Something went very wrong.");
                        return;
                    }
                } // else continue the infix operations using the answer from the previous
                  // calculation and the next number in the stack
                else if (doInfix && !firstInfix) {
                    b = nums.pop();
                    a = tempAnswer;
                }
                // else it is a non-infix problem so pop off the most current two numbers from
                // the stack
                else {
                    b = nums.pop();
                    a = nums.pop();
                } // if divide by 0
                if (b == 0 && sign == '/') {
                    System.out.println("Divide by 0.");
                    answer = 0;
                    pushAnswer();
                    break;
                } // if negative power, set answer to negative power and break out of loop
                if (b < 0 && sign == '^') {
                    System.out.println("Negative power.");
                    answer = b;
                    pushAnswer();
                    break;
                } // if max int entered and added to set answer as max int
                if ((a == maxInt && sign == '+') || (b == maxInt && sign == '+')) {
                    answer = maxInt;
                    pushAnswer();
                    break;
                } // if a input number is min int and sub from set answer as min int
                if ((a == minInt && sign == '-') || (b == minInt && sign == '-')) {
                    answer = minInt;
                    pushAnswer();
                    break;
                } // send sign to enum for validation and function reference
                final Signs validSign = Signs.signArithmetic(sign);
                // if the sign is a valid operator
                if (validSign != null) {
                    // if it is a normal input style or single sign infix preform arithmetic
                    if (!doInfix || singleInfix) {
                        answer = validSign.apply(a, b);
                        pushAnswer();
                        break;
                    } // else do math for current set of numbers and save in tempAnswer variable to be
                      // use in the next arithmetic
                    else {
                        tempAnswer = validSign.apply(a, b);
                    }
                } // once all the arithmetic has been preformed for an infix, set the tempAnswer
                  // as the final answer
                if (signs.isEmpty()) {
                    answer = tempAnswer;
                    pushAnswer();
                    return;
                }
        }
    }

    // send answer to user, reset class and exit
    private void finalAnswer() {
        System.out.println(nums.peek());
        reset();
        return;
    }

    // reset class variables
    private void reset() {
        reset = true;
        doInfix = false;
        singleInfix = true;
        signCount = 0;
        currNumCount = 0;
        signIndex = 1;
        firstInfix = true;
    }
}