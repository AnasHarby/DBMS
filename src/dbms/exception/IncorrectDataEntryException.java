package dbms.exception;

public class IncorrectDataEntryException extends Exception {
	private String message;

	public IncorrectDataEntryException() {
		super();
	}

	public IncorrectDataEntryException(String message) {
		super(message);
		this.message = message;
	}

	@Override
	public String toString() {
		return "IncorrectDataEntryException{" +
				"message='" + message + '\'' +
				'}';
	}
}