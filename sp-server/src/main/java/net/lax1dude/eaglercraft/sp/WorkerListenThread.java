package net.lax1dude.eaglercraft.sp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.NetHandler;

public class WorkerListenThread {
	/** Reference to the MinecraftServer object. */
	private final MinecraftServer mcServer;
	private final HashSet connections = new HashSet();
	private final HashMap<String, WorkerNetworkManager> channels = new HashMap();

	/** Whether the network listener object is listening. */
	public volatile boolean isListening = false;

	public WorkerListenThread(MinecraftServer par1MinecraftServer) {
		this.mcServer = par1MinecraftServer;
		this.isListening = true;
	}

	/**
	 * adds this connection to the list of currently connected players
	 */
	public void addPlayer(NetHandler par1NetServerHandler) {
		System.out.println("[Server][ADDPLAYER][" + par1NetServerHandler.getClass().getSimpleName() + "]");
		this.connections.add(par1NetServerHandler);
	}

	public void stopListening() {
		this.isListening = false;
	}
	
	public boolean openChannel(String player) {
		System.out.println("[Server][OPENCHANNEL][" + player + "]");
		return channels.put(player, new WorkerNetworkManager(player, mcServer, this)) == null;
	}
	
	public void recievePacket(String player, byte[] data) {
		WorkerNetworkManager channel = channels.get(player);
		if(channel == null) {
			return;
		}
		channel.addToRecieveQueue(data);
	}
	
	public boolean closeChannel(String player) {
		System.out.println("[Server][CLOSECHANNEL][" + player + "]");
		WorkerNetworkManager channel = channels.get(player);
		if(channel == null) {
			return false;
		}
		channels.remove(player);
		channel.networkShutdown(null, null, null);
		return true;
	}
	
	private void deleteDeadConnections() {
		Iterator<NetHandler> itr = this.connections.iterator();
		while(itr.hasNext()) {
			if(((NetHandler)itr.next()).shouldBeRemoved()) {
				itr.remove();
				//System.out.println("[Client][REMOVEDEAD]");
			}
		}
	}

	/**
	 * Handles all incoming connections and packets
	 */
	public void handleNetworkListenThread() {
		
		deleteDeadConnections();
		
		for (NetHandler var2 : (HashSet<NetHandler>)this.connections) {
			var2.handlePackets();
		}
		
		deleteDeadConnections();
		
	}

	public MinecraftServer getServer() {
		return this.mcServer;
	}
}
