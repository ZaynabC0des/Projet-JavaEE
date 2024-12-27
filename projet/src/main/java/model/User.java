package model;

public class User {
	private String login;
	private String password;
	
	//getter et setter
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	//constructeur
	public User(String login, String password) {
		super();
		this.login=login;
		this.password=password;		
	}
}
