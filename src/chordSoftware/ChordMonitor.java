package chordSoftware;

import chordNode.NodeImpl;

public class ChordMonitor {

	public static void main(String[] args) {
		
		NodeImpl node1 = new NodeImpl();
//		NodeImpl node2 = new NodeImpl();
		//Remote node to be picked from NodeList
		
		try {
			node1.create();
//			node2.create();
//			node2.join(node1.nodeId);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Sleep exception:");
			e.printStackTrace();
		}
		
		System.out.println("ID1 " + node1.nodeId);
//		System.out.println("ID2 " + node2.nodeId);
	}

}
