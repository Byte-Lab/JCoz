package com.vernetperronllc.jcoz.service;

public class ProfileException extends JCozException {
	private static final long serialVersionUID = -1481974051271289571L;

	public ProfileException(){
		super();
	}

	public ProfileException(String message) {
		super(message);
	}
	
	public ProfileException(Throwable cause){
		super(cause);
	}
	
	public ProfileException(String message, Throwable cause){
		super(message, cause);
	}
}
