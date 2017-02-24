package pcl.lc.irc.hooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaState.Library;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

public class JNLuaSandbox extends AbstractListener {
	private static LuaState luaState;

	public String chan;
	public String target = null;
	private StringBuilder output;

	private String luasb;
	private String selene;

	public JNLuaSandbox() throws IOException {
		super();
		InputStream luain = getClass().getResourceAsStream("/jnlua/luasb.lua");
		luasb = CharStreams.toString(new InputStreamReader(luain, Charsets.UTF_8));

		InputStream selenein = getClass().getResourceAsStream("/jnlua/selene/init.lua");
		selene = CharStreams.toString(new InputStreamReader(selenein, Charsets.UTF_8));

		initLua();
		IRCBot.registerCommand("lua", "Lua sandbox");
		IRCBot.registerCommand("resetlua", "Resets the lua sandbox");
	}

	private static String stackToString(LuaState luaState) {
		int top = luaState.getTop();
		if (top > 0) {
			StringBuilder results = new StringBuilder();
			for (int i = 1; i <= top; i++) {
				String result;
				if (luaState.isString(i) || luaState.isNumber(i))
					result = luaState.toString(i);
				else if (luaState.isBoolean(i))
					result = luaState.toBoolean(i) ? "true" : "false";
				else if (luaState.isNil(i))
					result = luaState.typeName(i);
				else
					result = String.format("%s: 0x%x", luaState.typeName(i), luaState.toPointer(i));
				results.append(result);
				if (i < top)
					results.append(", ");
			}
			return results.toString();
		} else {
			return "";
		}
	}

	protected void initLua() {
		if (luaState != null) {
			luaState.close();
		}
		luaState = new LuaState(1024*1024);
		luaState.openLib(Library.BASE);
		luaState.openLib(Library.COROUTINE);
		luaState.openLib(Library.TABLE);
		luaState.openLib(Library.STRING);
		luaState.openLib(Library.BIT32);
		luaState.openLib(Library.MATH);
		luaState.openLib(Library.OS);
		luaState.openLib(Library.PACKAGE);
		luaState.openLib(Library.DEBUG);
		luaState.setTop(0); // Remove tables from loaded libraries
		luaState.pushJavaFunction(new JavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				String results = stackToString(luaState);
				output.append(results + "\n");
				return 0;
			}
		});
		luaState.setGlobal("print");
		
		//THIS LINE DIES
		luaState.load(selene, "=selene");
		luaState.load(luasb, "=luasb");
		luaState.call(0, 0);
	}

	static String runScriptInSandbox(String script) {
		luaState.setTop(0); // Ensure stack is clean
		luaState.getGlobal("lua");
		luaState.pushString(script);
		try {
			luaState.call(1, LuaState.MULTRET);
		} catch (LuaException error) {
			luaState.setTop(0);
			return error.getMessage();
		}
		String results = stackToString(luaState);
		luaState.setTop(0); // Remove results from stack
		return results.toString();
	}

	@Override
	protected void initHook() {
		// Commands registered in JNLuaSandbox() constructor
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "lua") || command.equals(Config.commandprefix + "resetlua")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "lua")) {
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
			String message = "";
			for (String aCopyOfRange : copyOfRange)
			{
				message = message + " " + aCopyOfRange;
			}

			output = new StringBuilder();
			output.append(runScriptInSandbox(message));
			// Trim last newline
			if (output.length() > 0 && output.charAt(output.length()-1) == '\n')
				output.setLength(output.length()-1);
			String luaOut = Helper.ellipsize(output.toString().replace("\n", " | ").replace("\r", ""), 400);

			if (luaOut.length() > 0)
				IRCBot.getInstance().sendMessage(target , luaOut);

		} else if (command.equals(Config.commandprefix + "resetlua")) {
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
			String message = "";
			for (String aCopyOfRange : copyOfRange)
			{
				message = message + " " + aCopyOfRange;
			}

			initLua();
			IRCBot.getInstance().sendMessage(target, "Sandbox reset");
		}
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub

	}
}
