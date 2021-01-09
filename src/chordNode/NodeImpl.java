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
	int expectedSuccessor = -1;
	
	public int getExpectedSuccessor() throws RemoteException
	{
		return this.expectedSuccessor;
	}
	
	public int getSuccessor() throws RemoteException
	{
		return this.successor;
	}
	
	public int getPredecessor() throws RemoteException
	{
		return this.predecessor;
	}
	
	public int[] getFingerTable() throws RemoteException
	{
		return this.fingerTable;
	}
	
	public void simulate(int virtSuccessor, int virtPredecessor)
	{
		this.successor = virtSuccessor;
		this.predecessor = virtPredecessor;
	}
	
	BigInteger encryptString (String str) throws NoSuchAlgorithmException 
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] messageDigest = md.digest(str.getBytes());
		BigInteger ans = new BigInteger(1,messageDigest);
		return ans;
	}
	
	public int create(int nodeIp) throws Exception
	{
		//Later chord software to determine nodeId and bind
		Random random = new Random();
		int randNum = random.nextInt();
		if(randNum < 0)
			randNum *= -1;			
		BigInteger bigNodeId = encryptString(String.valueOf(randNum)).mod(new BigInteger("128"));
		bigNodeId = new BigInteger(String.valueOf(nodeIp));
		
		NodeImpl obj = new NodeImpl();
		obj.nodeId = bigNodeId.intValue();
		System.out.println("Creating with nodeId " + obj.nodeId);
		obj.successor = obj.predecessor = obj.nodeId;
		for(int i = 0;i < this.fingerTableSize;i++)
			obj.fingerTable[i] = -1;
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
//		if((this.nodeId == 1)&&(this.nodeId+1 == target))
//			System.out.println("node: " + this.nodeId + " successor: " + this.successor + " target: " + target);
		if(target > this.nodeId)
		{
			if(this.successor == this.nodeId)	//If it is the only node
				return this.nodeId;
			else if(this.successor > this.nodeId)
			{
				if(target <= this.successor)
				{
					return this.successor;
				}
			}
			else
				return this.successor;
			
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
			//Value not found
			return -1;
		}
		else
		{
			if(this.predecessor == this.nodeId)		//If it is the only node
				return this.nodeId;
			if(this.predecessor > this.nodeId)
				return this.nodeId;
			if((this.successor < this.nodeId)&&(target <= this.successor))
				return this.successor;
			for(int i = this.fingerTableSize-1;i >= 0;i--)
			{
				if(this.fingerTable[i] != -1)
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
			return -1;
		}
	}
	
	public void notify(int predNode) throws RemoteException
	{
//		System.out.println(this.nodeId + " notified of " + predNode);
		this.predecessor = predNode;
	}
	
	class StabilizeThread extends Thread
	{
		NodeImpl nodeObj;
		StabilizeThread(NodeImpl obj)
		{
			super();
			this.nodeObj = obj;
		}
		public void run()
		{
			try
			{
				while(true) {
						
					if((this.nodeObj.successor == this.nodeObj.nodeId)&&(this.nodeObj.predecessor != this.nodeObj.nodeId))
						this.nodeObj.successor = this.nodeObj.predecessor;
//					if(this.nodeObj.successor != this.nodeObj.nodeId)
//						this.nodeObj.fingerTable[0] = this.nodeObj.successor;
					Registry register = LocateRegistry.getRegistry();
					NodeAbstract successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.nodeObj.successor));
					int predSuccNode = successorSkeleton.getPredecessor();
					
					if((predSuccNode > this.nodeObj.nodeId)&&(predSuccNode < this.nodeObj.successor))
					{
//						this.nodeObj.fingerTable[0] = predSuccNode;
						System.out.println("Pred succ!");
						this.nodeObj.successor = predSuccNode;
						successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.nodeObj.successor));
					}
					if((this.nodeObj.successor < this.nodeObj.nodeId)&&((predSuccNode > this.nodeObj.nodeId)||(predSuccNode < this.nodeObj.successor)))
					{
//						this.nodeObj.fingerTable[0] = predSuccNode;
						System.out.println("Pred succ!");
						this.nodeObj.successor = predSuccNode;
						successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.nodeObj.successor));
					}
					successorSkeleton.notify(this.nodeObj.nodeId);
				}
			}catch(Exception e) {
				//Errors due to failure of nodes
				e.printStackTrace();
			}
		}
	}
	
	public void stabilize() throws RemoteException
	{
		//Should run periodically
		//Use thread to run indefinitely
		//Lookup in table to see if node has'nt failed
		StabilizeThread thread = new StabilizeThread(this);
		thread.start();
	}

	class UpdateFingerThread extends Thread
	{
		NodeImpl nodeObj;
		UpdateFingerThread(NodeImpl nodeObj)
		{
			super();
			this.nodeObj = nodeObj;
		}
		public void run()
		{
			int next = 0;
			try
			{
				while(true)	
				{
					next = next%7;
//					if(next == 0)
//						this.nodeObj.expectedSuccessor = this.nodeObj.findSuccessor((this.nodeObj.nodeId + (1<<next))%128);
//					if((this.nodeObj.nodeId == 1)&&(next == 0))
//							System.out.println("successor: " + this.nodeObj.successor);
					this.nodeObj.fingerTable[next] = this.nodeObj.findSuccessor((this.nodeObj.nodeId + (1<<next))%128);
//					if((next == 0)&&(this.nodeObj.nodeId == 1))
//						System.out.println("E(succ) = " + this.nodeObj.fingerTable[next]);
					next++;
				}
			}catch(RemoteException e)
			{
				//Error due to failure of node
				e.printStackTrace();
			}
		}
	}
	
	public void updateFingers() throws RemoteException
	{
		//Should run periodically
		//Use thread to run indefinitely
		//Lookup in table to see if node has'nt failed
		UpdateFingerThread thread = new UpdateFingerThread(this);
		thread.start();
	}
}
