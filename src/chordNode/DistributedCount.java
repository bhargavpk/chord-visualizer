package chordNode;

import java.util.concurrent.Semaphore;

public class DistributedCount {
	public int count;
	Semaphore countLock;
	Semaphore mutex;
	public DistributedCount(Semaphore sem, Semaphore mutex)
	{
		this.count = 0;
		this.countLock = sem;
		this.mutex = mutex;
	}
	public DistributedCount(int value, Semaphore sem, Semaphore mutex)
	{
		this.count = value;
		this.countLock = sem;
		this.mutex = mutex;
	}
	
	class IncrementCountThread extends Thread{
		DistributedCount distObj;
		IncrementCountThread(DistributedCount obj)
		{
			super();
			this.distObj = obj;
		}
		public void run()
		{
			try
			{
				this.distObj.mutex.acquire();
					this.distObj.count = this.distObj.count + 1;
					if(this.distObj.count == 1)
						this.distObj.countLock.acquire();
				this.distObj.mutex.release();
				
			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void incrementCount()
	{
		IncrementCountThread thread = new IncrementCountThread(this);
		thread.start();
	}
	
	class DecrementCountThread extends Thread
	{
		DistributedCount distObj;
		DecrementCountThread(DistributedCount obj)
		{
			super();
			this.distObj = obj;
		}
		public void run()
		{
			try
			{
				this.distObj.mutex.acquire();
					this.distObj.count = this.distObj.count - 1;
					if(this.distObj.count == 0)
						this.distObj.countLock.release();
				this.distObj.mutex.release();
				
			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void decrementCount()
	{
		DecrementCountThread thread = new DecrementCountThread(this);
		thread.start();
	}
}
