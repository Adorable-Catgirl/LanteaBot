/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.types.GenericCTCPEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Caitlyn
 *
 */
public class GenericEventListener extends AbstractListener{
	@Override
	protected void initHook() {
		System.out.println("onConnect listener loaded");
		System.out.println("onNickChange listener loaded");
		System.out.println("onInvite listener loaded");
		System.out.println("onGenericCTCP listener loaded");
		System.out.println("onPing listener loaded");
	}

	@Override
	public void onGenericMessage(final GenericMessageEvent event) {
		super.onGenericMessage(event);
		if (!(event instanceof MessageEvent))
			IRCBot.log.info("<-- Query: " + event.getUser().getNick() + ": " + event.getMessage());
	}
	
	@Override
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		IRCBot.log.info("<-- Msg: " + event.getChannel().getName().toString() + " " + event.getUser().getNick() + ": " + event.getMessage());
		if (!IRCBot.isIgnored(sender) && !args[0].startsWith(Config.commandprefix)) {
			if (event.getMessage().matches("s/(.+)/(.+)")) {
			} else {
				List<String> list = new ArrayList<String>();
				list.add(event.getChannel().getName().toString());
				list.add(sender);
				list.add(String.join(" ", args));
				IRCBot.messages.put(UUID.randomUUID(), list);
			}			
		}
		
/*		if (!event.getUser().getNick().equals(IRCBot.ournick)) {
			if (!IRCBot.authed.containsKey(event.getUser().getNick())) {
				IRCBot.bot.sendRaw().rawLineNow("who " + event.getUser().getNick() + " %an");
				if (!event.getUser().getNick().equals(IRCBot.ournick) && !event.getUser().getServer().isEmpty()) {
					IRCBot.users.put(event.getUser().getNick(), event.getUser().getServer());
				}
				if(IRCBot.authed.containsKey(event.getUser().getNick())) {
					IRCBot.authed.remove(event.getUser().getNick());
				}
			}
		}*/
	}

	@Override
	public void onAction(final ActionEvent event) {
		IRCBot.log.info("<-- Act:" + event.getChannel().getName().toString() + " " + event.getUser().getNick() + ": " + event.getAction());
	}
	

	@Override
	public void onConnect(final ConnectEvent event) {
		IRCBot.ournick = event.getBot().getNick();
	}

	@Override
	public void onNickChange(final NickChangeEvent event) {
		if (event.getOldNick().equals(IRCBot.ournick)) {
			IRCBot.ournick = event.getNewNick();
		} else {
			String server = IRCBot.users.get(event.getOldNick());
			IRCBot.users.remove(event.getOldNick());
			IRCBot.users.put(event.getNewNick(), server);
			IRCBot.authed.remove(event.getOldNick());
			IRCBot.bot.sendRaw().rawLineNow("who " + event.getNewNick() + " %an");
		}
	}

	@Override
	public void onJoin(final JoinEvent event) {
		IRCBot.log.info("<-- " + event.getChannel().getName().toString() + " Joined: " + event.getUser().getNick() + " " + event.getUser().getHostmask());

	}

	@Override
	public void onPart(final PartEvent event) {
		IRCBot.log.info("<-- " + event.getChannel().getName().toString() + " Parted: " + event.getUser().getNick() + " " + event.getUser().getHostmask() + " " + event.getReason());

	}

	@Override
	public void onQuit(final QuitEvent event) {
		IRCBot.log.info("<-- " + "Quit: " + event.getUser().getNick() + " " + event.getUser().getHostmask());
		if(event.getReason().equals("*.net *.split")) {
			IRCBot.authed.remove(event.getUser().getNick());
			IRCBot.users.remove(event.getUser().getNick());
		}
		if(IRCBot.authed.containsKey(event.getUser().getNick())) {
			IRCBot.authed.remove(event.getUser().getNick());
			IRCBot.users.remove(event.getUser().getNick());
		}
	}

	@Override
	public void onKick(final KickEvent event) {

	}

	@Override
	public void onInvite(InviteEvent event) {
		if (IRCBot.invites.containsKey(event.getChannel())) {
			event.getBot().sendIRC().joinChannel(event.getChannel());
			IRCBot.invites.remove(event.getChannel());
		}
	}

	@Override
	public void onGenericCTCP(final GenericCTCPEvent event) {

	}

	@Override
	public void onPing(final PingEvent event) {
		//event.respond(event.getPingValue());
	}

	@Override
	public void onServerResponse(final ServerResponseEvent event) {
		//if (IRCBot.getDebug())
			//System.out.println(event.getCode());
		if(event.getCode() == 352) {
			//System.out.println(event.getParsedResponse());
			Object nick = event.getParsedResponse().toArray()[5];
			Object server = event.getParsedResponse().toArray()[4];
			if (IRCBot.users.containsKey(nick)) {
				IRCBot.users.remove(nick);
			}
			IRCBot.users.put(nick.toString(), server.toString());
		}
		if(event.getCode() == 354) {
			Object nick = event.getParsedResponse().toArray()[1];
			Object nsaccount = event.getParsedResponse().toArray()[2];
			if (IRCBot.authed.containsKey(nick)) {
				IRCBot.authed.remove(nick);
			}
			if (!nsaccount.toString().equals("0")) {
				IRCBot.authed.put(nick.toString(),nsaccount.toString());
			}
		}
		if(event.getCode() == 330) {
			Object nick = event.getParsedResponse().toArray()[1];
			Object nsaccount = event.getParsedResponse().toArray()[2];
			if (IRCBot.authed.containsKey(nick)) {
				IRCBot.authed.remove(nick);
			}
			IRCBot.authed.put(nick.toString(),nsaccount.toString());
		}
		if(event.getCode() == 372) {

		}
	}

	@Override
	public void onUnknown(final UnknownEvent event) {
		if (IRCBot.getDebug())
			System.out.println("UnknownEvent: "+ event.getLine());
		if(event.getLine().contains("ACCOUNT")) {
			String nick = event.getLine().substring(event.getLine().indexOf(":") + 1, event.getLine().indexOf("!"));
			if(event.getLine().split("\\s")[2].equals("*")) {
				IRCBot.authed.remove(nick);
				if (IRCBot.getDebug())
					System.out.println(nick + " Logged out");
			} else {
				IRCBot.authed.put(nick, event.getLine().split("\\s")[2].toString());
				if (IRCBot.getDebug())
					System.out.println(nick + " Logged in");
			}
		}
	}
}
