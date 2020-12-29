package chordNode;

import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class NodeImpl implements NodeAbstract{
	
	public int nodeId = -1; 	//To be modified when joined
								//Models hashed IP Address of server
	int successor = -1;	//To be modified to successor list
	int predecessor = -1;
	int fingerTableSize = 7;
	int[] fingerTable = new int[fingerTableSize];
	
	public int getSuccessor() throws RemoteException
	{
		return this.successor;
	}
	
	public int getPredecessor() throws RemoteException
	{
		return this.predecessor;
	}
	
	BigInteger encryptString (String str) throws NoSuchAlgorithmException 
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] messageDigest = md.digest(str.getBytes());
		BigInteger ans = new BigInteger(1,messageDigest);
		return ans;
	}
	
	public int create() throws Exception
	{
		//Later chord software to determine nodeId and bind
		Random random = new Random();
		int randNum = random.nextInt();
		if(randNum < 0)
			randNum *= -1;			
		BigInteger bigNodeId = encryptString(String.valueOf(randNum)).mod(new BigInteger("128"));
		
		NodeImpl obj = new NodeImpl();
		obj.nodeId = bigNodeId.intValue();
		System.out.println("Creating with nodeId " + obj.nodeId);
		obj.successor = obj.predecessor = obj.nodeId;
		//bind node stub to registry
		NodeAbstract stub = (NodeAbstract)UnicastRemoteObject.exportObject(obj, 0);
		Registry register = LocateRegistry.getRegistry();
		register.bind(String.valueOf(obj.nodeId), stub);
			
		System.out.println("Create done!");
		
		return bigNodeId.intValue();
	}
	
	public void join(int remoteNode) throws RemoteException	//Invoked by chord software
	{
		try {
			
			System.out.println("join, Node " + this.nodeId + " Remote: " + remoteNode);
			Registry register = LocateRegistry.getRegistry();
			NodeAbstract skeleton = (NodeAbstract)register.lookup(String.valueOf(remoteNode));
			this.successor = skeleton.findSuccessor(this.nodeId);
			
		}catch(Exception e) {
			
			//Handle error
			//Error possibly due to failure of a node
			System.out.println("Registry error:");
			e.printStackTrace();
		}
	}
	
	public int findSuccessor(int target) throws RemoteException
	{
		
		//Assuming chord is stabilized
		if(target > this.nodeId)
		{
			if(this.successor == this.nodeId)	//If it is the only node
			{
				this.successor = target;		//If it is the first node to join
				return this.nodeId;
			}
			else if(this.successor > this.nodeId)
			{
				if((target > this.nodeId)&&(target <= this.successor))
					return this.successor;
			}
			else
			{
				return this.successor;
			}
			for(int i = this.fingerTableSize-1;i >= 0;i--)
			{
				if(this.fingerTable[i] == -1)
					continue;
				if((this.fingerTable[i] > this.nodeId)&&(this.fingerTable[i] < target))
				{
					try {
						
						Registry register = LocateRegistry.getRegistry();
						NodeAbstract skeleton = (NodeAbstract)register.lookup(String.valueOf(this.fingerTable[i]));
						return skeleton.findSuccessor(target);
						
					}catch(Exception e) {
						//Error possibly due to failure of node
						System.out.println("Registry Exception:");
						e.printStackTrace();
					}
				}
			}
			System.out.println("Value not found!");
			//Value not found
			return -1;
		}
		else
		{
			if(this.predecessor == this.nodeId)		//If it is the only node
			{
				this.successor = target;			//If it is the first node to join
				return this.nodeId;
			}
			if(this.predecessor > this.nodeId)
				return this.nodeId;
			for(int i = 0;i < this.fingerTableSize;i++)
			{
				if(this.fingerTable[i] == -1)
					continue;
				if(this.fingerTable[i] < this.nodeId)
				{
					try {
						
						Registry register = LocateRegistry.getRegistry(null);
						NodeAbstract skeleton = (NodeAbstract)register.lookup(String.valueOf(this.fingerTable[i]));
						return skeleton.findSuccessor(target);
						
					}catch(Exception e) {
						//Error possibly due to failure of node
						System.out.println("Registry Exception:");
						e.printStackTrace();
					}
				}
			}
			System.out.println("Value not found!");
			return -1;
		}
	}
	
	public void notify(int predNode) throws RemoteException
	{
		this.predecessor = predNode;
	}
	
	public void stabilize() throws RemoteException
	{
		//Should run periodically
		//Use thread to run indefinitely
		//Lookup in table to see if node hasnt failed
		try {
			
			Registry register = LocateRegistry.getRegistry();
			NodeAbstract successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.successor));
			int predSuccNode = successorSkeleton.getPredecessor();
			if((predSuccNode > this.nodeId)&&(predSuccNode < this.successor))
				this.successor = predSuccNode;
			successorSkeleton.notify(this.nodeId);
			
		}catch(Exception e) {
			//Errors due to failure of nodes
			e.printStackTrace();
		}
	}
	
	public void updateFingers() throws RemoteException
	{
		//Should run periodically
		//Use thread to run indefinitely
		//Lookup in table to see if node hasnt failed
		int next = 0;
		while(true)
		{
			next = next%7;
			this.fingerTable[next] = this.findSuccessor((this.nodeId + (1<<next))%128);
			next++;
		}
	}
}
