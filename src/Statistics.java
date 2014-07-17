public class Statistics {
	private double frontEnd;
	private double backEnd;
	private double thinkTime;
	private double throughput;

	public Statistics(double frontEnd, double backEnd, double thinkTime) {
		super();
		this.frontEnd = frontEnd;
		this.backEnd = backEnd;
		this.thinkTime = thinkTime;
	}

	public double getFrontEnd() {
		return frontEnd;
	}

	public void setFrontEnd(double frontEnd) {
		this.frontEnd = frontEnd;
	}

	public double getBackEnd() {
		return backEnd;
	}

	public void setBackEnd(double backEnd) {
		this.backEnd = backEnd;
	}

	public double getThinkTime() {
		return thinkTime;
	}

	public void setThinkTime(double thinkTime) {
		this.thinkTime = thinkTime;
	}

	public double getThroughput() {
		return throughput;
	}

	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}

}
