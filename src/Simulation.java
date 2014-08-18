import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Simulation {

	public static Rngs generator;
	public static double start = 0;
	public static double stop = 30000;
	public static double arrival;
	public static ArrayList<Session> sessionList;
	public static Clock systemClock;
	public static double throughput;
	public static int arrivedSessions;
	public static int frontEndRequestsNumber;
	public static int backEndRequestsNumber;
	public static int completedSessions;
	public static int currentSession;
	public static double sessionResidenceTime;
	// bound
	// public static int systemBound = 250;
	// public static int droppedSessions;
	// public static int abortedSessions;
	// public static double dropRatio;
	// public static double abortedRatio;
	// bound fine
	public static Statistics frontEnd;
	public static Statistics backEnd;
	public static Statistics infiniteServer;
	public static PrintWriter sessionAverageResidenceTimeWriter;
	public static PrintWriter sessionAverageInTheSystemWriter;
	public static PrintWriter throughputWriter;
	public static PrintWriter feUtilizationWriter;
	public static PrintWriter beUtilizationWriter;
	public static PrintWriter dropRatioWriter;
	public static PrintWriter abortedRatioWriter;

	public static void main(String[] args) throws IOException {

		int runNumber = 1;
		for (int i = 1; i <= runNumber; i++) {
			sessionList = new ArrayList<Session>();
			systemClock = new Clock();
			arrivedSessions = 0;
			frontEndRequestsNumber = 0;
			backEndRequestsNumber = 0;
			completedSessions = 0;
			sessionResidenceTime = 0;
			generator = new Rngs();
			throughput = 0.0;
			arrival = 0;
			// bound
			// abortedRatio=0;
			// abortedSessions=0;
			// droppedSessions=0;
			// dropRatio=0;
			// bound fine
			frontEnd = new Statistics(0.0, 0.0, 0.0);
			backEnd = new Statistics(0.0, 0.0, 0.0);
			infiniteServer = new Statistics(0.0, 0.0, 0.0);

			sessionAverageResidenceTimeWriter = new PrintWriter(
					new BufferedWriter(new FileWriter(
							"sessionAverageResidenceTime.txt", true)));

			sessionAverageInTheSystemWriter = new PrintWriter(
					new BufferedWriter(new FileWriter(
							"sessionAverageInTheSystem.txt", true)));

			throughputWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("throughput.txt", true)));

			feUtilizationWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("feUtilization.txt", true)));

			beUtilizationWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("beUtilization.txt", true)));

			dropRatioWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("dropRatio.txt", true)));

			abortedRatioWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("abortedRatio.txt", true)));

			boundSimulation();
			System.out.println("Round" + i + "completato");
			sessionAverageResidenceTimeWriter.close();
			sessionAverageInTheSystemWriter.close();
			throughputWriter.close();
			feUtilizationWriter.close();
			beUtilizationWriter.close();
			dropRatioWriter.close();
			abortedRatioWriter.close();

		}

	}

	public static void boundSimulation() throws FileNotFoundException,
			UnsupportedEncodingException {

		generator.plantSeeds(-1);
		double nextCompletionTime = Double.MAX_VALUE;
		double nextArrivalTime = Double.MAX_VALUE;

		nextArrivalTime = GetArrival();

		while ((systemClock.getCurrent() <= stop) || (sessionList.size() > 0)) {

			nextCompletionTime = GetNextCompletionTime();

			if (nextCompletionTime <= nextArrivalTime) {
				systemClock.setNext(nextCompletionTime);
			} else {
				systemClock.setNext(nextArrivalTime);
			}

			// -----------------------------------------CLOCK
			// PRINT------------------------------------------------------------
			System.out.println("Clock corrente =" + systemClock.getCurrent());
			// -----------------------------------------------------------------------------------------------------

			// ----------------------------TIME AVERAGE
			// STATISTICS-----------------------------------------------------------
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
									.getCurrent()) * (sessionList.size()
									- frontEndRequestsNumber - backEndRequestsNumber)));
			// ---------------------------------------------------------------------------------------

			systemClock.setCurrent(systemClock.getNext());

			if (systemClock.getCurrent() == nextArrivalTime) {
				// bound
				// if ((frontEndRequestsNumber + backEndRequestsNumber) <
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
				// droppedSessions++;
				// if (systemClock.getCurrent() < stop) {
				//
				// nextArrivalTime = GetArrival();
				// } else {
				// nextArrivalTime = Double.MAX_VALUE;
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
				// if ((frontEndRequestsNumber + backEndRequestsNumber) <
				// systemBound) {
				// // bound fine
				sessionList.get(currentSession).setThinkTimeCompletionTime(
						Double.MAX_VALUE);
				sessionList.get(currentSession).setFrontEndCompletionTime(
						GetMaxFrontEndCompletionTime() + GetFrontEndService());
				frontEndRequestsNumber++;
				// bound
				// } else {
				// abortedSessions++;
				// sessionList.remove(currentSession);
				// }
				// bound fine

			}

			// System.out
			// .println("-------------------------------------------------------------------------");
			System.out.println("sessioni nel sistema =" + sessionList.size());
			// System.out.println("sessioni nei server = " +
			// (frontEndRequestsNumber+backEndRequestsNumber));
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
		// bound
//		abortedRatio = ((double) abortedSessions / (double) arrivedSessions);
//		dropRatio = ((double) droppedSessions / (double) (arrivedSessions + droppedSessions));
		// bound fine
		
		
		// System.out
		// .println("<<<<<<<<<<<<<<Statistiche time averaged<<<<<<<<<<<<<<<<<<<<<<<<<<");
		// System.out.println("Numero medio di sessioni in coda nel front end = "
		// + (frontEnd.getAveragePopulationInQueue()));
		// System.out.println("Utilizzazione media nel front end = "
		// + (frontEnd.getUtilization()));
		// System.out.println("Numero medio di sessioni nel front end = "
		// + (frontEnd.getAveragedPopulation()));
		// System.out.println("Numero medio di sessioni in coda nel back end = "
		// + (backEnd.getAveragePopulationInQueue()));
		// System.out.println("Utilizzazione media nel back end = "
		// + (backEnd.getUtilization()));
		// System.out.println("Numero medio di sessioni nel back end = "
		// + (backEnd.getAveragedPopulation()));
		// System.out.println("Numero medio di sessioni nel sistema = "
		// +( (backEnd.getAveragedPopulation()
		// + frontEnd.getAveragedPopulation() + infiniteServer
		// .getUtilization())));
		// System.out.println("Numero medio di sessioni nel think time = "
		// + infiniteServer.getUtilization());
		// System.out
		// .println("Tempo di residenza medio nel sistema per sessione = "
		// + sessionResidenceTime);
		// System.out.println("Troughput = " + throughput);
		// System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		// -----------------write on file
		sessionAverageResidenceTimeWriter.println(sessionResidenceTime);

		double temp = (backEnd.getAveragedPopulation()
				+ frontEnd.getAveragedPopulation() + infiniteServer
				.getUtilization());
		sessionAverageInTheSystemWriter.println(temp);
		throughputWriter.println(throughput);
		feUtilizationWriter.println(frontEnd.getUtilization());
		beUtilizationWriter.println(backEnd.getUtilization());
		//bound
//		dropRatioWriter.println(dropRatio);
//		abortedRatioWriter.println(abortedRatio);
//		System.out.println("DROP=" + droppedSessions + "/"
//				+ (arrivedSessions + droppedSessions));
		//bound fine
		double temp2 = frontEnd.getAveragedPopulation()
				+ backEnd.getAveragedPopulation();
		System.out.println("uscite" + completedSessions);
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
		arrival += Exponential(0.028571429);
		return arrival;
	}

	public static double GetFrontEndService() {
		generator.selectStream(1);
		return Exponential(0.00456);

	}

	public static double GetBackEndService() {
		generator.selectStream(2);
		double temp = Exponential(0.00117);
		return temp;

	}

	public static double GetThinkTime() {

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
