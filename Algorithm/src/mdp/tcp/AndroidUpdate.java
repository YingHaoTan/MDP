package mdp.tcp;

public class AndroidUpdate extends StatusMessage {

	private String message;
	
	public AndroidUpdate(String msg) {
		super(StatusMessageType.ANDROID_UPDATE);
		this.message = msg;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public byte[] toBytes() {
		byte[] toSend = new byte[1 + message.length()];
		toSend[0] = StatusMessageType.ANDROID_UPDATE.getByte();
		
		int count = 0;
		for(char character: message.toCharArray())
			toSend[++count] = (byte) character;
		
		return toSend;
	}
	
	

}
