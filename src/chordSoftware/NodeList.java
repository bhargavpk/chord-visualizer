package chordSoftware;

import java.util.LinkedList;
import java.util.Random;

class NodeList {
	
	LinkedList<Integer> nodeList;
	
	NodeList()
	{
		this.nodeList = new LinkedList<Integer>();
	}
	
	Boolean contains(int nodeId)
	{
		return this.nodeList.contains(nodeId);
	}
	Boolean addNode(int nodeId)
	{
		if(this.nodeList.contains(nodeId))
			return false;
		this.nodeList.addLast(nodeId);
		return true;
	}
	Boolean removeNode(int nodeId)
	{
		if(!this.nodeList.contains(nodeId))
			return false;
		this.nodeList.removeFirstOccurrence(nodeId);
		return true;
	}
	int getRandomNode()
	{
		int size = this.nodeList.size();
		if(size == 0)
			return -1;
		Random random = new Random();
		int randIndex = random.nextInt(size);
		return this.nodeList.get(randIndex);
	}

}
