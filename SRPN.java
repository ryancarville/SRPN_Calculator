import java.lang.Math;
import java.util.Stack;
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
    boolean calcPower, findCommentEnd, reset;
    // Integers
    Integer a, b, answer, currRCount, sLength, currNumCount;
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
        this.currRCount = 0;
        this.currNumCount = 0;
        this.regexAll = "(.*)([0-9 dr+%^*/=-])(.*)";
        this.regexSign = "(.*)([+%^*/=-])(.*)";
        this.regexNum = "(.*)([0-9])(.*)";
    }

    // main program the input calls
    public void processCommand(String s) {
        try {
            // set all stack minium capacity
            nums.ensureCapacity(100);
            tempNumsStack.ensureCapacity(100);
            signs.ensureCapacity(10);
            // add minimum int number to stack at start
            if (nums.isEmpty()) {
                nums.push(minInt);
            }
            // remove any white spaces and set to class variable
            currS = s;
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
                } else if (currInput.equals(" ")) {
                    pushNum();
                    continue;
                }
                // if input is a "#"
                else if (currInput.equals("#")) {
                    String prev = null;
                    String next = null;
                    // push number if there was one before
                    pushNum();
                    // if it is a single "#" input or it is at the end of a input preceded by a
                    // space
                    if (sLength > 1) {
                        next = currS.substring(i + 1, i + 2);
                        if (i > 0) {
                            prev = currS.substring(i - 1, i);
                        }
                    } else {
                        boolean soloHash = currS.replaceAll(" ", "").equals("#");
                        if (soloHash) {
                            calcPower = calcPower ? false : true;
                            return;
                        }
                    }
                    if (next.isBlank()) {
                        int nextHash = currS.indexOf("#", i + 1);
                        String nextHashPrev = currS.substring(nextHash - 1, nextHash);
                        if (nextHashPrev.isBlank() && i < sLength) {
                            i = nextHash;
                            continue;
                        } else {
                            if (sLength - 1 > i) {
                                next = currS.substring(nextHash + 1, nextHash + 2);
                            }

                        }

                    } else {
                        notValidInput(currInput);
                        continue;
                    }

                } // if calculator is turned off
                else if (!calcPower) {
                    return;
                } // when input is "d" print out the number stack and continue thru the loop
                else if (currInput.equals("d")) {
                    printNumsStack();
                } // if the input is "r" add current position of rNums array to the numbers stack
                  // then continue thru the loop
                else if (currInput.equals("r")) {
                    rNumsLoop();
                } // if there is a negative sign
                else if (currInput.equals("-")) {
                    handleMinus(currInput, i);
                } // if it is a number store it
                else if (Pattern.matches(regexNum, currInput)) {
                    storeNum(currInput, i);
                } // if it is a sign, store it if infix, but solve if single char
                else if (Pattern.matches(regexSign, currInput)) {
                    storeSignOrSolve(currInput);
                } // if the input is not a valid sign, char or number tell user and move to the
                  // next char in the loop
                else {
                    notValidInput(currInput);
                }
                if (currS.endsWith("=") && i == (sLength - 2)) {
                    pushNum();
                    postfix();
                    break;
                } else if (i == sLength - 1) {
                    postfix();
                    break;
                }
            }
        } catch (

        Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void notValidInput(String currInput) {
        System.out.println("Unrecognized operator or operand " + "\"" + currInput + "\"" + ".");
        if (tempNum.length() > 0) {
            pushNum();
        }
    }

    // print the current answer stack
    private void printNumsStack() {
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
        if (currRCount == MAX - 1) {
            currRCount = 0;
        }
        // set the rNum at current index as the answer
        answer = rNums[currRCount];
        // add the answer to the number stack
        nums.push(String.valueOf(answer));
        // increase the loop counter
        currRCount++;
    }

    // sort if a minus sign is a operator or a negative
    private void handleMinus(String currInput, int i) {
        // if the sign is the first in the input > 1 it is treated as a arithmetic sign
        String prev = " ";
        String next = String.valueOf(currS.charAt(i + 1));
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

    // set class temp variable to whole single number
    private void storeNum(String currInput, int i) {
        tempNum += currInput;
        // if its on the last char in the input push the number to the stack
        if (i == (sLength - 1)) {
            pushNum();
            if (signs.size() > 0) {
                postfix();
            }
        }
    }

    // recursive function for BODMAS sorting
    private void storeSignOrSolve(String sign) {
        // if a single input sign do the arithmetic
        if (sLength == 1) {
            BODMAS(sign);
            return;
        } // if there is still a number in the holding variable, push it to the stack
        if (tempNum.length() > 0) {
            pushNum();
        } // if a equals, BODMAS to print answer
        if (sign.equals("=")) {
            BODMAS(sign);
            return;
        } // if there are no signs in the stack, add the sign, exit
        if (signs.isEmpty()) {
            signs.push(sign);
            return;
        } // else check the signs for BODMAS and preform arithmetic if higher sign popped,
          // else add the sign to the stack
        else {
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

    // push the full number to the number stack from the tempNum string
    private void pushNum() {
        // if default min int value still on stack, remove
        if (nums.peek().equals(minInt)) {
            nums.pop();
        }
        if (tempNum.length() > 0) {
            // push number to stack
            nums.push(tempNum);
            // clear the temp number string
            tempNum = "";
            // add one to the counter for current set of numbers
            currNumCount++;
        }
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

    // if div by zero, set answer to 0, and exit
    private boolean divByZero(int b, String sign) {
        if (b == 0 && sign.equals("/")) {
            return true;
        }
        return false;
    }

    // if negative power, set answer to negative power, and exit
    private boolean negativePower(int b, String sign) {
        if (b < 0 && sign.equals("^")) {
            return true;
        }
        return false;
    }

    // flow control for arithmetic
    private void BODMAS(String sign) {
        // ints for nums to be calculated
        Integer max = Integer.parseInt(maxInt), min = Integer.parseInt(minInt);
        // if not equals, get numbers
        if (!sign.equals("=")) {
            if (nums.size() <= 1) {
                System.out.println("Stack underflow.");
                answer = Integer.parseInt(nums.peek());
                pushAnswer();
                return;
            }
            // pop numbers
            b = Integer.parseInt(nums.pop());
            a = Integer.parseInt(nums.pop());
            // check divide by 0
            if (divByZero(b, sign)) {
                System.out.println("Divide by 0.");
                answer = 0;
                pushAnswer();
                return;
            }
            // check negative power
            if (negativePower(b, sign)) {
                System.out.println("Negative power.");
                answer = b;
                pushAnswer();
                return;
            }
            // if int entered exceeds limits set answer to correct limit
            try {
                Integer intTest = Math.addExact(a, b);
            } catch (Exception e) {
                if (e.getMessage().equals("integer overflow")) {
                    if (sign.equals("+")) {
                        answer = max;
                        pushAnswer();
                        return;
                    } else if (sign.equals("-")) {
                        answer = min;
                        pushAnswer();
                        return;
                    }
                }
            }
        }
        // all arithmetic is done here
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

    // send answer to user, reset and exit
    private void finalAnswer() {
        System.out.println(nums.peek());
        reset = false;
        currNumCount = 0;
        return;
    }
}