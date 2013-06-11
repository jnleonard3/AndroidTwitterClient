package eva.twitter.api;

public class Tweet {
	
	private String screenName;
	
	private String text;
	
	public Tweet(String screenName, String text) {
		
		this.screenName = screenName;
		this.text = text;		
	}
	
	public String getScreenName() {
		
		return screenName;
	}
	
	public String getText() {
		
		return text;
	}

}
