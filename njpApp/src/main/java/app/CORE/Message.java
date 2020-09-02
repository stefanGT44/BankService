package app.CORE;

public class Message {

	private String code;
	private String message;
	private Severity severity;
	
	public Message(String code, String message, Severity severity) {
		this.code = code;
		this.message = message;
		this.severity = severity;
	}
	
	public Message(String code, Severity severity) {
		this.code = code;
		this.severity = severity;
	}
	
	public String getCode() {
		return code;
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public String getMessage() {
		return message;
	}
	
}
