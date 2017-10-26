package java.util.practice.chapter10;
/**
 * ���׷�������
 *
 * @author yangqc
 * 2016��8��14��
 */
public class LeftRightDeadLock {
	private final Object left = new Object();
	private final Object right = new Object();

	public void leftRight() {
		synchronized (left) {
			synchronized (right) {
				System.out.println("leftRight!");
			}
		}
	}

	public void rightLeft() {
		synchronized (right) {
			synchronized (left) {
				System.out.println("rightLeft");
			}
		}
	}
}
