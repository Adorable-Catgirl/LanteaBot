/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class l33t extends AbstractListener {

	public static String toLeet(String str){
		boolean ck = false;
		boolean s = false;
		if(str.endsWith("ck")){
			ck = true;
			str = str.substring(0, str.length() - 2);
		} else if(str.endsWith("s")){
			s = true;
			str = str.substring(0, str.length() - 1);
		}
		char[] arr = str.toCharArray();

		for(int i=0; i < str.length(); ++i){
			switch(arr[i]){
			case 'a':	arr[i]='@'; break;
			case 'e':	arr[i]='3'; break;
			case 'i':	arr[i]='1'; break;
			case 'o':	arr[i]='0'; break;
			case 'u':	arr[i]='v'; break;
			case 'f':	arr[i]='p'; break;
			case 's':	arr[i]='$'; break;
			case 'g':	arr[i]='9'; break;
			case 'y':	arr[i]='j'; break;
			case 't':	arr[i]='+'; break;
			case '!':	arr[i]='1'; break;
			}
			++i;
			if(Character.isLowerCase(arr[i-1])){
				arr[i-1] = Character.toUpperCase(arr[i-1]);
			} else /*if(Character.isUpperCase(arr[i]))*/ {
				arr[i-1] = Character.toLowerCase(arr[i-1]);
			}
		}

		String result = new String(arr);
		if(ck){
			result = result.concat("x");
		} else if(s) {
			result = result.concat("z");
		}

		return result;
	}

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("1337", "Returns leetspeak of inputted text");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "1337")) {
			if (!IRCBot.isIgnored(nick)) {
				String message = "";
				for( int i = 0; i < copyOfRange.length; i++)
				{
					message = message + " " + copyOfRange[i];
				}
				String s = message.trim();
				event.respond(toLeet(s));
			}
		}	
	}
}
