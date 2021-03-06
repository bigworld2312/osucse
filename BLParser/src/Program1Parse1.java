import components.map.Map;
import components.program.Program;
import components.program.Program1;
import components.queue.Queue;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary method {@code parse} for {@code Program}.
 *
 * @author Jake Alvord and Shannon O'Toole
 *
 */
public final class Program1Parse1 extends Program1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Parses a single BL instruction from {@code tokens} returning the
     * instruction name as the value of the function and the body of the
     * instruction in {@code body}.
     *
     * @param tokens
     *            the input tokens
     * @param body
     *            the instruction body
     * @return the instruction name
     * @replaces body
     * @updates tokens
     * @requires [<"INSTRUCTION"> is a proper prefix of tokens]
     * @ensures <pre>
     * if [an instruction string is a proper prefix of #tokens] then
     *  parseInstruction = [name of instruction at start of #tokens]  and
     *  body = [Statement corresponding to statement string of body of
     *          instruction at start of #tokens]  and
     *  #tokens = [instruction string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static String parseInstruction(Queue<String> tokens, Statement body) {
        assert tokens != null : "Violation of: tokens is not null";
        assert body != null : "Violation of: body is not null";
        assert tokens.length() > 0 && tokens.front().equals("INSTRUCTION") : ""
        + "Violation of: <\"INSTRUCTION\"> is proper prefix of tokens";

        String instruction = tokens.dequeue();
        String identifier = tokens.dequeue();
        String is = tokens.dequeue();

        body.parseBlock(tokens);

        String end = tokens.dequeue();
        String endID = tokens.dequeue();

        Reporter.assertElseFatalError(identifier.equals(endID),
                "Beginning and ending indentifiers do not match.");

        return identifier;
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Program1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(SimpleReader in) {
        assert in != null : "Violation of: in is not null";
        assert in.isOpen() : "Violation of: in.is_open";
        Queue<String> tokens = Tokenizer.tokens(in);
        this.parse(tokens);
    }

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
        + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        String program = tokens.dequeue();
        Reporter.assertElseFatalError(program.equals("PROGRAM"),
                "Expected: PROGRAM , Was: " + program);
        String id = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(id),
                "Expected an identifier , Was: " + id);

        this.replaceName(id);
        String is = tokens.dequeue();

        Reporter.assertElseFatalError(is.equals("IS"), "Expected: IS , Was: "
                + is);

        Map<String, Statement> context = this.newContext();

        Set<String> insts = new Set1L<String>();

        while (!tokens.front().equals("BEGIN")) {

            Statement body = this.newBody();
            String checkInstruction = parseInstruction(tokens, body);

            context.add(checkInstruction, body);

            Reporter.assertElseFatalError(!insts.contains(checkInstruction),
                    "Duplication of instruction identifiers.");

            insts.add(checkInstruction);

            while (insts.size() > 0) {
                String check = insts.removeAny();

                boolean checkIfPrim = false;
                if (check.toLowerCase().equals("turnright")
                        || check.toLowerCase().equals("turnleft")
                        || check.toLowerCase().equals("move")
                        || check.toLowerCase().equals("infect")
                        || check.toLowerCase().equals("skip")) {

                    checkIfPrim = true;

                    Reporter.assertElseFatalError(checkIfPrim,
                            "Instruction is a primitive call.");
                }

                Reporter.assertElseFatalError(!Tokenizer.isCondition(check),
                        "Insruction is a condition.");

                Reporter.assertElseFatalError(!Tokenizer.isIdentifier(check),
                        "Instruction is an identifier.");

                Reporter.assertElseFatalError(!Tokenizer.isKeyword(check),
                        "Instruction is a keyword.");
            }

        }

        Statement blockBody = this.replaceBody(this.newBody());
        String begin = tokens.dequeue();
        blockBody.parseBlock(tokens);

        String end = tokens.dequeue();
        String endId = tokens.dequeue();

        Reporter.assertElseFatalError(id.equals(endId),
                "Beginning and ending PROGRAM identifiers do not match.");

        Reporter.assertElseFatalError(
                Tokenizer.END_OF_INPUT.equals(tokens.front()),
                "Extra tokens are not allowed after the end of program.");

        this.replaceName(id);
        this.replaceContext(context);
        this.replaceBody(blockBody);

    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL program file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Program p = new Program1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        p.parse(tokens);
        /*
         * Pretty print the program
         */
        out.println("*** Pretty print of parsed program ***");
        p.prettyPrint(out);

        in.close();
        out.close();
    }

}
