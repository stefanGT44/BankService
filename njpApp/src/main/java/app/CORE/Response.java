package app.CORE;

import java.util.ArrayList;
import java.util.List;

public class Response {
	
	private String data;
	private List<Message> messages;
	
	public Response() {
		messages = new ArrayList<>();
	}
	
	public Response(String data) {
		this.data = data;
		messages = new ArrayList<>();
	}
	
	public Response(String data, Message message) {
		this.data = data;
		messages = new ArrayList<>();
		messages.add(message);
	}
	
	public void addMessage(Message message) {
		messages.add(message);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	
	public boolean containsError() {
		for (Message msg:messages)
			if (msg.getSeverity().equals(Severity.ERROR))
				return true;
		return false;
	}

}
