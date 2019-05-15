public class ParseError extends Exception {

	public ParseError(String errorMessage) {
		super("parse error: "+errorMessage);
	}
}
