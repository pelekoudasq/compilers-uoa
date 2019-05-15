import java.io.InputStream;
import java.io.IOException;

class Parser {

	private int lookaheadToken;

	private InputStream in;

	public Parser(InputStream in) throws IOException {
		this.in = in;
		lookaheadToken = in.read();
	}

	private void consume(int symbol) throws IOException, ParseError {
		if (lookaheadToken != symbol)
			throw new ParseError("expected "+symbol+", got "+lookaheadToken);
		lookaheadToken = in.read();
	}

	private int evalDigit(int digit){
		return digit - '0';
	}

	private int expr() throws IOException, ParseError {
		int sub = sub();
		int retVal = expr2();
		if (retVal != -1)
			return (sub ^ retVal);
		return sub;
	}

	private int expr2() throws IOException, ParseError {
		if (lookaheadToken == '\n' || lookaheadToken == -1 || lookaheadToken == ')')
			return -1;
		if (lookaheadToken != '^')
			throw new ParseError("expected ^, got "+lookaheadToken);
		consume('^');
		int sub = sub();
		int retVal = expr2();
		if (retVal != -1)
			return (sub ^ retVal);
		return sub;
	}

	private int sub() throws IOException, ParseError {
		int num = num();
		int retVal = sub2();
		if (retVal != -1)
			return (num & retVal);
		return num;
	}

	private int sub2() throws IOException, ParseError {
		if (lookaheadToken == '^' || lookaheadToken == '\n' || lookaheadToken == -1 || lookaheadToken == ')')
			return -1;
		if (lookaheadToken != '&')
			throw new ParseError("expected &, got "+lookaheadToken);
		consume('&');
		int num = num();
		int retVal = sub2();
		if (retVal != -1)
			return (num & retVal);
		return num;
	}

	private int num() throws IOException, ParseError {
		if (lookaheadToken >= '0' && lookaheadToken <= '9'){
			int digit = evalDigit(lookaheadToken);
			consume(lookaheadToken);
			return digit;
		}
		if(lookaheadToken == '('){
			consume('(');
			int parenthesisExp = expr();
			consume(')');
			return parenthesisExp;
		}
		else
			throw new ParseError("expected number or (, got "+lookaheadToken);
	}

	public int eval() throws IOException, ParseError {
		int rv = expr();
		if (lookaheadToken != '\n' && lookaheadToken != -1)
			throw new ParseError("eval");
		return rv;
	}

	public static void main(String[] args) {
		try {
			Parser evaluate = new Parser(System.in);
			System.out.println(evaluate.eval());
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
		}
		catch(ParseError err){
			System.err.println(err.getMessage());
		}
	}
}
