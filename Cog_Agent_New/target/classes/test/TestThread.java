package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class TestThread implements Runnable {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		ThreadPoolExecutor e = new ScheduledThreadPoolExecutor(10);
		List<TestThread> ts = new ArrayList<TestThread>();
		synchronized (e) {
			for (int i = 0; i < 20; i++) {
				TestThread t = new TestThread((i + 1) * 2,e);
				System.out.println("x pre exec: " + t.getX());
				e.execute(t);
				ts.add(t);

			}
			while (e.getActiveCount() != 0) {
				e.wait();
			}
			for (TestThread tt : ts) {
				System.out.println("x now finally: " + tt.getX());
			}
			System.out.println("reached");
			e.shutdown();
			while (e.getActiveCount() > 0) {
				if (System.nanoTime() % 1000000 == 0) {
					System.out.println("current count: " + e.getActiveCount() + " at: " + System.currentTimeMillis());
				}
			}
		}

	}

	private int x;
	private ThreadPoolExecutor e;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// System.out.println("reached run for : " + x);
		// x = generateX();
		// System.out.println("new x: " + x);
		synchronized (e) {

			System.out.println("start of run for: " + Thread.currentThread().getName());
			for (int i = 0; i < 10; i++) {
				System.out.println(x);
			}

			for (int j = 0; j < 10; j++) {

				for (int a = 0; a < 100000; a++) {

					while (System.currentTimeMillis() % 10000 != 0) {
						int k = 1 + 1;
						k= k+1;
						break;
					}

				}
			}
			
			x = x * 100;
			System.out.println("end of run for: " + Thread.currentThread().getName());
			e.notify();
			

		}
	}

	public TestThread(int i, ThreadPoolExecutor e) {
		x = i;
		this.e = e;
	}

	public int getX() {
		return x;
	}


}
