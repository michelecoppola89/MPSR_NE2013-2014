public class Clock {

	private double current;
	private double next;

	public Clock() {

		current = 0;
		next = 0;
	}

	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
		this.current = current;
	}

	public double getNext() {
		return next;
	}

	public void setNext(double next) {
		this.next = next;
	}

}
