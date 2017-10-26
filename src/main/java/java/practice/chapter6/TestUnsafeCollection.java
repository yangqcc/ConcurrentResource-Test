package java.practice.chapter6;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * �̰߳�ȫ����������и��ϲ�����Ҳ�ᷢ���̲߳���ȫ����
 *
 * @author yangqc 
 * 2016��8��30��
 */
public class TestUnsafeCollection {
	private static Map<Integer, String> map = new ConcurrentHashMap<>();

	static {
		map.put(12, "123");
	}

	public void remove() {
		map.remove(12);
		System.out.println("remove!");
	}

	public void existAndGet() {
		if (map.containsKey(12)) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("in");
			if (map.get(12) == null) {
				throw new RuntimeException("�����жϲ�Ϊ�յ�!");
			}
		}
	}

	public static void main(String[] args) {
		final TestUnsafeCollection collection = new TestUnsafeCollection();
		Thread thread1 = new Thread() {
			public void run() {
				collection.remove();
			}
		};
		Thread thread2 = new Thread() {
			public void run() {
				collection.existAndGet();
			}
		};
		thread2.start();
		thread1.start();
	}
}
