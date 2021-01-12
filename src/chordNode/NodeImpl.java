package chordNode;

import java.util.concurrent.Semaphore;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class NodeImpl implements NodeAbstract{
	
	public int nodeId = -1; 	//To be modified when joined
								//Models hashed IP Address of server
	int successor = -1;	//To be modified to successor list
	int predecessor = -1;
	int fingerTableSize = 7;
	FingerTable fingerTable = new FingerTable(7);
	int expectedSuccessor = -1;
	HashMap<Integer, Integer> dataStore = new HashMap<Integer, Integer>();	//Make a new class to split k-v pairs
	Semaphore mutex, countLock, dataLock;	//dataLock to be acquired while splitting and merging(in case of failure) data
	DistributedCount distributedCount;
	
	Boolean notifiedStatus = false;
	
	public int getSuccessor() throws RemoteException
	{
		return this.successor;
	}
	
	public int getPredecessor() throws RemoteException
	{
		return this.predecessor;
	}
	
	public int getExpectedSuccessor() throws RemoteException
	{
		return this.fingerTable.get(0);
	}
	
	public void simulate(int virtSuccessor, int virtPredecessor)
	{
		this.successor = virtSuccessor;
		this.predecessor = virtPredecessor;
	}
	
	public void create(int nodeIp, DistributedCount disCount, Semaphore countLock, Semaphore dataLock, Semaphore mutex) throws Exception
	{
		//Later chord software to determine nodeId and bind
		this.nodeId = nodeIp;
		this.successor = this.predecessor = this.nodeId;
		this.distributedCount = disCount;
		this.countLock = countLock;
		this.dataLock = dataLock;
		this.mutex = mutex;
	}
	
	public void join(int remoteNode) throws RemoteException	//Invoked by chord software
	{
		try {
			
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
				if(this.fingerTable.get(i) == -1)
					continue;
				if((this.fingerTable.get(i) > this.nodeId)&&(this.fingerTable.get(i) < target))
				{
					try {
						
						Registry register = LocateRegistry.getRegistry();
						NodeAbstract skeleton = (NodeAbstract)register.lookup(String.valueOf(this.fingerTable.get(i)));
						return skeleton.findSuccessor(target);
						
					}catch(Exception e) {
						//Error possibly due to failure of node
						System.out.println("Registry Exception:");
						e.printStackTrace();
					}
				}
			}
			//FingerTable not updated yet
			return this.successor;
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
				if(this.fingerTable.get(i) != -1)
				{
					try {
						
						Registry register = LocateRegistry.getRegistry(null);
						NodeAbstract skeleton = (NodeAbstract)register.lookup(String.valueOf(this.fingerTable.get(i)));
						return skeleton.findSuccessor(target);
						
					}catch(Exception e) {
						//Error possibly due to failure of node
						System.out.println("Registry Exception:");
						e.printStackTrace();
					}
				}
			}
			return this.successor;
		}
	}
	
	public void notify(int predNode) throws RemoteException
	{
		Boolean shouldUpdateFingers = false;
		if((this.predecessor == this.nodeId)&&(this.successor != this.nodeId))
			shouldUpdateFingers = true;
		if((this.predecessor == this.nodeId)&&(this.successor == this.nodeId))	//If it is the first node
			this.notifiedStatus = true;
		this.predecessor = predNode;
		if(shouldUpdateFingers)
			this.updateFingers();
		if((this.notifiedStatus == false)&&(this.successor != this.nodeId))
		{
			this.notifiedStatus = true;
			try {
				Registry register = LocateRegistry.getRegistry();
				NodeAbstract successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.successor));
				this.dataLock.acquire();
					this.dataStore = successorSkeleton.getHashMapWithKey(this.nodeId);
				this.dataLock.release();
				this.distributedCount.decrementCount();
				
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
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
					{
						this.nodeObj.notifiedStatus = true;	//If it was the first node to join
						this.nodeObj.successor = this.nodeObj.predecessor;
						this.nodeObj.updateFingers();
					}
					
					Registry register = LocateRegistry.getRegistry();
					NodeAbstract successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.nodeObj.successor));
					
					int predSuccNode = successorSkeleton.getPredecessor();
					
					if((predSuccNode > this.nodeObj.nodeId)&&(predSuccNode < this.nodeObj.successor))
					{
						this.nodeObj.successor = predSuccNode;
						successorSkeleton = (NodeAbstract)register.lookup(String.valueOf(this.nodeObj.successor));
					}
					if((this.nodeObj.successor < this.nodeObj.nodeId)&&((predSuccNode > this.nodeObj.nodeId)||(predSuccNode < this.nodeObj.successor)))
					{
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
					this.nodeObj.fingerTable.set(next, this.nodeObj.findSuccessor((this.nodeObj.nodeId + (1<<next))%128));
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
	
	public void storeData(int key, int value) throws RemoteException
	{
		//Use SHA to avoid collisions
		this.dataStore.put(key,value);
	}
	
	public int getData(int key) throws RemoteException
	{
		return this.dataStore.get(key);
	}
	
	public HashMap<Integer, Integer> getHashMapWithKey(int key) throws RemoteException
	{
		HashMap<Integer, Integer> dataStoreNew = new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> mapEle:this.dataStore.entrySet())
		{
			if(key < this.nodeId)
			{
				if(!((mapEle.getKey() > key)&&(mapEle.getKey() <= this.nodeId)))
					dataStoreNew.put(mapEle.getKey(), mapEle.getValue());
			}
			else
			{
				if(!((mapEle.getKey() > key)||(mapEle.getKey() <= this.nodeId)))
					dataStoreNew.put(mapEle.getKey(), mapEle.getValue());
			}
		}
		for(Map.Entry<Integer, Integer> mapEle:dataStoreNew.entrySet())
		{
			this.dataStore.remove(mapEle.getKey());
		}
		return dataStoreNew;
	}
}
