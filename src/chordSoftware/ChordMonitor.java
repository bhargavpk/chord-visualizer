package chordSoftware;

import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Semaphore;

import chordNode.*;

public class ChordMonitor {
	
	NodeList nodeList;
	DistributedCount distributedCount;
	Semaphore mutex;					//initialized to 1
	Semaphore stabilizationLock;		//initialized to 0
	Semaphore dataLock;
	Semaphore readWriteMutex;
	
	ChordMonitor()
	{
		this.nodeList = new NodeList();
		this.stabilizationLock = new Semaphore(1);
		this.mutex = new Semaphore(1);
		this.distributedCount = new DistributedCount(this.stabilizationLock, this.mutex);
		this.dataLock = new Semaphore(1);
		this.readWriteMutex = new Semaphore(1);
	}
	
	BigInteger encryptString (String str) throws NoSuchAlgorithmException 
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] messageDigest = md.digest(str.getBytes());
		BigInteger ans = new BigInteger(1,messageDigest);
		return ans;
	}
	
	void createNode(int nodeId) throws Exception
	{
		//Create node and stabilize it
		//Create a thread later to create a node
		
		NodeImpl obj = new NodeImpl();
		
		//bind node stub to registry
		NodeAbstract stub = (NodeAbstract)UnicastRemoteObject.exportObject(obj, 0);
		Registry register = LocateRegistry.getRegistry();
		register.bind(String.valueOf(nodeId), stub);
		
		obj.create(nodeId, this.distributedCount, this.stabilizationLock, this.dataLock, this.mutex);
		this.distributedCount.incrementCount();
		this.nodeList.addNode(nodeId);
		
		//join node with the chord
		int randRemoteNodeId = this.nodeList.getRandomNode(nodeId);
		System.out.println("Rand node index: " + randRemoteNodeId);
		if(randRemoteNodeId != -1)
			obj.join(randRemoteNodeId);
		obj.stabilize();
		
	}
	
	class AddDataThread extends Thread
	{
		ChordMonitor parentObj;
		int key, value;
		AddDataThread(ChordMonitor obj, int key, int value)
		{
			super();
			this.parentObj = obj;
			this.key = key;
			this.value = value;
		}
		void makeAddRequest()
		{
			try {
					if(this.parentObj.distributedCount.count != 0)
						this.parentObj.stabilizationLock.acquire();
					Registry register = LocateRegistry.getRegistry();
					int randomNodeId = this.parentObj.nodeList.getRandomNode();
					NodeAbstract remoteNode = (NodeAbstract)register.lookup(String.valueOf(randomNodeId));
					int successorKey = remoteNode.findSuccessor(key);
					NodeAbstract successorNode = (NodeAbstract)register.lookup(String.valueOf(successorKey));
					successorNode.storeData(key, value);
						
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		public void run()
		{
			try
			{
					//Chord not yet stabilized. Nodes must ping to only one pending request
					this.parentObj.readWriteMutex.acquire();
						makeAddRequest();
					this.parentObj.readWriteMutex.release();
					
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	void addData(int key, int value)
	{
		//Use semaphore to confirm stability
		//and then add the {key, value} pair to the chord
		AddDataThread thread = new AddDataThread(this, key, value);
		thread.start();
		
	}
	
	class GetDataThread extends Thread
	{
		ChordMonitor parentObj;
		int key;
		GetDataThread(ChordMonitor obj, int key)
		{
			super();
			this.parentObj = obj;
			this.key = key;
		}
		void makeGetRequest()
		{
			try {
					System.out.println("Count: " + this.parentObj.distributedCount.count);
					if(this.parentObj.distributedCount.count != 0)
						this.parentObj.stabilizationLock.acquire();
					this.parentObj.dataLock.acquire();
						Registry register = LocateRegistry.getRegistry();
						int randomNodeId = this.parentObj.nodeList.getRandomNode();
						System.out.println("Remote node for finding: " + randomNodeId);
						NodeAbstract remoteNode = (NodeAbstract)register.lookup(String.valueOf(randomNodeId));
						int successorKey = remoteNode.findSuccessor(key);
						System.out.println("Node for key " + key + " is " + successorKey);
						NodeAbstract successorNode = (NodeAbstract)register.lookup(String.valueOf(successorKey));
						int value = successorNode.getData(key);
						System.out.println("Value for key " + key + " is " + value);
					this.parentObj.dataLock.release();
				
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		public void run()
		{
			try
			{
				this.parentObj.readWriteMutex.acquire();
					makeGetRequest();
				this.parentObj.readWriteMutex.release();
				
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	void getData(int key)
	{
		//Use semaphore to confirm stability
		//and then add the {key, value} pair to the chord
		GetDataThread thread = new GetDataThread(this, key);
		thread.start();
		
	}
	
	public static void main(String[] args)
	{
		//Run tests
		ChordMonitor chord = new ChordMonitor();
		int[] nodeIdArr = new int[] {5,1,3,4,2,9};
		try {
			for(int i = 0;i < nodeIdArr.length; i++)
				chord.createNode(nodeIdArr[i]);
			chord.addData(43, 3);
			
//			Thread.sleep(1000);
			
			chord.createNode(45);
			
			chord.getData(43);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
