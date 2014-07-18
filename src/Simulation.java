import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Simulation {

	public static Rngs generator = new Rngs();
	public static double start = 0;
	public static double stop = 1000;
	public static double arrival = 0;
	public static ArrayList<Session> sessionList = new ArrayList<Session>();
	public static Clock systemClock = new Clock();
	public static double throughput;
	public static int arrivedSessions = 0;
	public static int frontEndRequestsNumber = 0;
	public static int backEndRequestsNumber = 0;
	public static int completedSessions = 0;
	public static int currentSession;
	public static double sessionResidenceTime = 0;
	// bound
	// public static int systemBound = 250;
	// public static int newSessionDropped = 0;
	// public static int runningSessionDropped = 0;
	// bound fine
	public static Statistics frontEnd = new Statistics(0.0, 0.0, 0.0);
	public static Statistics backEnd = new Statistics(0.0, 0.0, 0.0);
	public static Statistics infiniteServer = new Statistics(0.0, 0.0, 0.0);

	public static void main(String[] args) throws FileNotFoundException,
			UnsupportedEncodingException {
		PrintWriter averageResidenceTime = new PrintWriter(
				"sessionResidenceTime.txt", "UTF-8");
		PrintWriter averageSessionInTheSystem = new PrintWriter(
				"averageSessionInTheSystem.txt", "UTF-8");
		PrintWriter throughput = new PrintWriter("throughput.txt",
				"UTF-8");
		PrintWriter feUtilization = new PrintWriter("feUtilization.txt",
				"UTF-8");
		PrintWriter beUtilization = new PrintWriter("beUtilization.txt",
				"UTF-8");
		int runNumber = 0;
		for (int i = 0; i < runNumber; i++)
			noBoundSimulation(averageResidenceTime, averageSessionInTheSystem,
					throughput,feUtilization,beUtilization);
	}

	public static void noBoundSimulation(PrintWriter writer,
			PrintWriter writer2, PrintWriter writer3, PrintWriter writer4,PrintWriter writer5 )
			throws FileNotFoundException, UnsupportedEncodingException {

		generator.plantSeeds(-1);
		double nextCompletionTime = Double.MAX_VALUE;
		double nextArrivalTime = Double.MAX_VALUE;
		GetArrival();
		nextArrivalTime = arrival;

		while ((systemClock.getCurrent() <= stop) || (sessionList.size() != 0)) {

			nextCompletionTime = GetNextCompletionTime();
			if (nextCompletionTime <= nextArrivalTime) {
				systemClock.setNext(nextCompletionTime);
			} else {
				systemClock.setNext(nextArrivalTime);
			}
			// System.out.println("Clock corrente =" + systemClock.getNext());
			// ----------------------------------------------------------
			if (frontEndRequestsNumber > 0) {
				frontEnd.setAveragedPopulation(frontEnd.getAveragedPopulation()
						+ ((systemClock.getNext() - systemClock.getCurrent()) * frontEndRequestsNumber));
				frontEnd.setAveragePopulationInQueue(frontEnd
						.getAveragePopulationInQueue()
						+ ((systemClock.getNext() - systemClock.getCurrent()) * (frontEndRequestsNumber - 1)));

				frontEnd.setUtilization(frontEnd.getUtilization()
						+ ((systemClock.getNext() - systemClock.getCurrent())));

			}
			if (backEndRequestsNumber > 0) {
				backEnd.setAveragedPopulation(backEnd.getAveragedPopulation()
						+ ((systemClock.getNext() - systemClock.getCurrent()) * backEndRequestsNumber));
				backEnd.setAveragePopulationInQueue(backEnd
						.getAveragePopulationInQueue()
						+ ((systemClock.getNext() - systemClock.getCurrent()) * (backEndRequestsNumber - 1)));
				backEnd.setUtilization(backEnd.getUtilization()
						+ ((systemClock.getNext() - systemClock.getCurrent())));
			}
			infiniteServer
					.setUtilization(infiniteServer.getUtilization()
							+ ((systemClock.getNext() - systemClock
									.getCurrent()) * (arrivedSessions
									- completedSessions
									- frontEndRequestsNumber - backEndRequestsNumber)));
			// ----------------------------------------------------------

			systemClock.setCurrent(systemClock.getNext());

			if (systemClock.getCurrent() == nextArrivalTime) {
				// bound
				// if (frontEndRequestsNumber + backEndRequestsNumber <
				// systemBound) {
				// bound fine
				arrivedSessions++;
				frontEndRequestsNumber++;
				Session newSession = new Session();
				newSession.setArrivalTime(systemClock.getCurrent());
				newSession.setRequestNumber(GetNewRequestsNumber());
				newSession
						.setFrontEndCompletionTime(GetMaxFrontEndCompletionTime()
								+ GetFrontEndService());

				sessionList.add(newSession);

				if (systemClock.getCurrent() < stop) {

					nextArrivalTime = GetArrival();
				} else {
					nextArrivalTime = Double.MAX_VALUE;

				}
				// bound
				// } else {
				// newSessionDropped++;
				// if (systemClock.getCurrent() < stop) {
				//
				// nextArrivalTime = GetArrival();
				// }
				// }
				// bound fine

			} else if (sessionList.get(currentSession) != null
					&& systemClock.getCurrent() == sessionList.get(
							currentSession).getFrontEndCompletionTime()) {
				sessionList.get(currentSession).setFrontEndCompletionTime(
						Double.MAX_VALUE);
				sessionList.get(currentSession).setBackEndCompletionTime(
						GetMaxBACKEndCompletionTime() + GetBackEndService());
				frontEndRequestsNumber--;
				backEndRequestsNumber++;

				// nextCompletionTime=Double.MAX_VALUE;
			} else if (sessionList.get(currentSession) != null
					&& systemClock.getCurrent() == sessionList.get(
							currentSession).getBackEndCompletionTime()) {
				sessionList.get(currentSession).setRequestNumber(
						sessionList.get(currentSession).getRequestNumber() - 1);

				backEndRequestsNumber--;
				if (sessionList.get(currentSession).getRequestNumber() == 0) {
					completedSessions++;
					// job average statistics: WELLFORD
					sessionResidenceTime = sessionResidenceTime
							+ ((systemClock.getCurrent()
									- sessionList.get(currentSession)
											.getArrivalTime() - sessionResidenceTime) / completedSessions);
					// job average statistics: WELLFORD
					sessionList.remove(currentSession);

				} else {
					sessionList.get(currentSession).setBackEndCompletionTime(
							Double.MAX_VALUE);
					sessionList.get(currentSession).setThinkTimeCompletionTime(
							systemClock.getCurrent() + GetThinkTime());

				}

			} else if (sessionList.get(currentSession) != null
					&& systemClock.getCurrent() == sessionList.get(
							currentSession).getThinkTimeCompletionTime()) {
				// bound
				// if (frontEndRequestsNumber + backEndRequestsNumber <
				// systemBound) {
				// // bound fine
				sessionList.get(currentSession).setThinkTimeCompletionTime(
						Double.MAX_VALUE);
				sessionList.get(currentSession).setFrontEndCompletionTime(
						GetMaxFrontEndCompletionTime() + GetFrontEndService());
				frontEndRequestsNumber++;
				// bound
				// } else {
				// runningSessionDropped++;
				// sessionList.remove(currentSession);
				// }
				// bound fine

			}

			// System.out
			// .println("-------------------------------------------------------------------------");
			// System.out.println("sessioni nel sistema =" +
			// sessionList.size());
			// System.out.println("Numero di sessioni arrivate nel sistema ="
			// + arrivedSessions);
			// System.out.println("Numero di sessioni partite dal sistema ="
			// + completedSessions);
			// System.out.println("Numero di sessioni nel front end ="
			// + frontEndRequestsNumber);
			// System.out.println("Numero di sessioni nel back end ="
			// + backEndRequestsNumber);
			// System.out.println("Numero di sessioni in think time ="
			// + (arrivedSessions - completedSessions
			// - frontEndRequestsNumber - backEndRequestsNumber));
			// System.out.println("Prossimo istante di completamento ="
			// + nextCompletionTime);
			// System.out
			// .println("Prossimo istante di arrivo =" + nextArrivalTime);
		}
		frontEnd.setAveragePopulationInQueue(frontEnd
				.getAveragePopulationInQueue() / systemClock.getCurrent());
		frontEnd.setUtilization(frontEnd.getUtilization()
				/ systemClock.getCurrent());
		frontEnd.setAveragedPopulation(frontEnd.getAveragedPopulation()
				/ systemClock.getCurrent());
		backEnd.setAveragePopulationInQueue(backEnd
				.getAveragePopulationInQueue() / systemClock.getCurrent());
		backEnd.setUtilization(backEnd.getUtilization()
				/ systemClock.getCurrent());
		backEnd.setAveragedPopulation(backEnd.getAveragedPopulation()
				/ systemClock.getCurrent());
		infiniteServer.setUtilization(infiniteServer.getUtilization()
				/ systemClock.getCurrent());
		throughput = completedSessions / systemClock.getCurrent();
		System.out
				.println("<<<<<<<<<<<<<<Statistiche time averaged<<<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println("Numero medio di sessioni in coda nel front end = "
				+ (frontEnd.getAveragePopulationInQueue()));
		System.out.println("Utilizzazione media nel front end = "
				+ (frontEnd.getUtilization()));
		System.out.println("Numero medio di sessioni nel front end = "
				+ (frontEnd.getAveragedPopulation()));
		System.out.println("Numero medio di sessioni in coda nel back end = "
				+ (backEnd.getAveragePopulationInQueue()));
		System.out.println("Utilizzazione media nel back end = "
				+ (backEnd.getUtilization()));
		System.out.println("Numero medio di sessioni nel back end = "
				+ (backEnd.getAveragedPopulation()));
		System.out.println("Numero medio di sessioni nel sistema = "
				+ (backEnd.getAveragedPopulation()
						+ frontEnd.getAveragedPopulation() + infiniteServer
							.getUtilization()));
		System.out
				.println("Tempo di residenza medio nel sistema per sessione = "
						+ sessionResidenceTime);
		System.out.println("Troughput = " + throughput);
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		// -----------------write on file
		writer.println(sessionResidenceTime);
		writer.close();

		writer2.println((backEnd.getAveragedPopulation() + frontEnd
				.getAveragedPopulation()));
		writer2.close();
		writer3.println(throughput);
		writer3.close();
		writer4.println(frontEnd.getUtilization());
		writer4.close();
		writer5.println(backEnd.getUtilization());
		writer5.close();
		
		// -----------------write on file
		// bound
		// System.out.println("Sessioni nuove rifiutate " + newSessionDropped);
		// System.out.println("Sessioni stoppate " + runningSessionDropped);
		// bound fine
	}

	public static double GetNextCompletionTime() {

		if (sessionList.size() > 0) {

			double ret = Double.MAX_VALUE;
			currentSession = 0;

			for (int i = 0; i < sessionList.size(); i++) {
				if (sessionList.get(i).minEventTime() <= ret) {
					ret = sessionList.get(i).minEventTime();
					currentSession = i;
				}
			}

			return ret;

		} else {
			currentSession = -1;
			return Double.MAX_VALUE;

		}

	}

	static double GetMaxFrontEndCompletionTime() {

		double ret = systemClock.getCurrent();

		if (sessionList.size() > 0) {

			for (int i = 0; i < sessionList.size(); i++) {
				if (sessionList.get(i).getFrontEndCompletionTime() != Double.MAX_VALUE) {
					if (sessionList.get(i).getFrontEndCompletionTime() >= ret) {
						ret = sessionList.get(i).getFrontEndCompletionTime();
					}

				}
			}
		}

		return ret;

	}

	static double GetMaxBACKEndCompletionTime() {
		double ret = systemClock.getCurrent();

		if (sessionList.size() > 0) {

			for (int i = 0; i < sessionList.size(); i++) {
				if (sessionList.get(i).getBackEndCompletionTime() != Double.MAX_VALUE) {
					if (sessionList.get(i).getBackEndCompletionTime() >= ret) {
						ret = sessionList.get(i).getBackEndCompletionTime();
					}

				}
			}
		}

		return ret;

	}

	public static double GetArrival() {
		generator.selectStream(0);
		// arrival += Exponential(0.028571429); giusto
		arrival += Exponential(0.00119);

		return arrival;
	}

	public static double GetFrontEndService() {
		generator.selectStream(1);
		// return Exponential(0.00456); giusto
		return Exponential(0.00117);

	}

	public static double GetBackEndService() {
		generator.selectStream(2);
		return Exponential(0.00117);

	}

	public static double GetThinkTime() {

		// + arrivedSessions);
		generator.selectStream(3);
		return Exponential(7);

	}

	public static int GetNewRequestsNumber() {
		generator.selectStream(4);
		return Equilikely(5, 35);
	}

	public static double Exponential(double m) {
		double temp = (-m * Math.log(1 - generator.random()));
		return temp;// (-m * Math.log(1 - generator.random()));
	}

	public static int Equilikely(int a, int b) {
		return (a + (int) (generator.random() * (b - a + 1)));
	}

}
