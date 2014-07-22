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
	public static double stop = 100;
	public static double arrival;
	public static ArrayList<Session> sessionList;
	public static Clock systemClock;
	public static double throughput;
	public static int arrivedSessions = 0;
	public static int frontEndRequestsNumber = 0;
	public static int backEndRequestsNumber = 0;
	public static int completedSessions = 0;
	public static int completedRequests = 0;
	public static double lastArrivalRequestTime = 0;
	public static int currentSession;
	public static double sessionResidenceTime = 0;
	public static double requestResidenceTime = 0;
	// bound
	public static int systemBound = 250;
	public static int newSessionDropped;
	public static int runningSessionDropped;
	public static double dropRatio = 0;
	public static double abortedRatio = 0;
	// bound fine
	public static Statistics frontEnd;
	public static Statistics backEnd;
	public static Statistics infiniteServer;

	public static void main(String[] args) throws IOException {

		int runNumber =1;
		for (int i = 0; i <= runNumber; i++) {
			// bound
			newSessionDropped = 0;
			runningSessionDropped = 0;
			// bound fine
			sessionList = new ArrayList<Session>();
			systemClock = new Clock();
			arrivedSessions = 0;
			frontEndRequestsNumber = 0;
			backEndRequestsNumber = 0;
			completedSessions = 0;
			completedRequests = 0;
			lastArrivalRequestTime = 0;
			sessionResidenceTime = 0;
			requestResidenceTime = 0;
			generator = new Rngs();
			throughput = 0.0;
			arrival = 0;
			 frontEnd = new Statistics(0.0, 0.0, 0.0);
			 backEnd = new Statistics(0.0, 0.0, 0.0);
			 infiniteServer= new Statistics(0.0, 0.0, 0.0);
			PrintWriter averageResidenceTimeWriter = new PrintWriter(
					new BufferedWriter(new FileWriter(
							"sessionResidenceTime.txt", true)));
			PrintWriter averageSessionInTheSystemWriter = new PrintWriter(
					new BufferedWriter(new FileWriter(
							"averageSessionInTheSystem.txt", true)));
			PrintWriter throughputWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("throughput.txt", true)));
			PrintWriter feUtilizationWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter("feUtilization.txt", true)));
			PrintWriter beUtilizationWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter("beUtilization.txt", true)));
			noBoundSimulation(averageResidenceTimeWriter,
					averageSessionInTheSystemWriter, throughputWriter,
					feUtilizationWriter, beUtilizationWriter);
			//System.out.println("Round" + i + "completato");
			averageResidenceTimeWriter.close();
			averageSessionInTheSystemWriter.close();
			throughputWriter.close();
			feUtilizationWriter.close();
			beUtilizationWriter.close();

		}

	}

	public static void noBoundSimulation(PrintWriter writer,
			PrintWriter writer2, PrintWriter writer3, PrintWriter writer4,
			PrintWriter writer5) throws FileNotFoundException,
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
			//System.out.println("Clock corrente =" + systemClock.getCurrent());
			 
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
			// bound ??????? CONTROLLARE
//			if (newSessionDropped > 0) {
//				dropRatio = dropRatio
//						+ ((systemClock.getNext() - systemClock.getCurrent()) * (newSessionDropped / arrivedSessions));
//			}
//			if (runningSessionDropped > 0) {
//				abortedRatio = abortedRatio
//						+ ((systemClock.getNext() - systemClock.getCurrent()) * (runningSessionDropped / (frontEndRequestsNumber + backEndRequestsNumber)));
//
//			}
			// bound fine
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
				lastArrivalRequestTime = systemClock.getCurrent();
				// bound
				if ((frontEndRequestsNumber + backEndRequestsNumber) < systemBound) {
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
				} else {
					newSessionDropped++;
					if (systemClock.getCurrent() < stop) {

						nextArrivalTime = GetArrival();
					} else {
						nextArrivalTime = Double.MAX_VALUE;
					}
				}
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
				completedRequests++;
				// job average statistics: WELLFORD
				requestResidenceTime = requestResidenceTime
						+ ((systemClock.getCurrent() - lastArrivalRequestTime - requestResidenceTime) / completedRequests);
				// job average statistics: WELLFORD
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
					// il tempo di risposta mi interessa riferito alla richiesta
					// singola
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
				lastArrivalRequestTime = systemClock.getCurrent();
				// bound
				if ((frontEndRequestsNumber + backEndRequestsNumber) < systemBound) {
					// // bound fine
					sessionList.get(currentSession).setThinkTimeCompletionTime(
							Double.MAX_VALUE);
					sessionList.get(currentSession).setFrontEndCompletionTime(
							GetMaxFrontEndCompletionTime()
									+ GetFrontEndService());
					frontEndRequestsNumber++;
					// bound
				} else {
					runningSessionDropped++;
					sessionList.remove(currentSession);
				}
				// bound fine

			}

			// System.out
			// .println("-------------------------------------------------------------------------");
			// System.out.println("sessioni nel sistema =" +
			// sessionList.size());
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
		// + (backEnd.getAveragedPopulation()
		// + frontEnd.getAveragedPopulation() + infiniteServer
		// .getUtilization()));
		// System.out
		// .println("Tempo di residenza medio nel sistema per sessione = "
		// + sessionResidenceTime);
		// System.out.println("Troughput = " + throughput);
		// System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		// -----------------write on file
		writer.println(sessionResidenceTime + "     " + requestResidenceTime);
		writer2.println((backEnd.getAveragedPopulation() + frontEnd
				.getAveragedPopulation()));
		writer3.println(throughput);
		writer4.println(frontEnd.getUtilization());
		writer5.println(backEnd.getUtilization());

		// -----------------write on file
		// bound
//		System.out.println("Sessioni nuove rifiutate " + newSessionDropped);
//		System.out.println("Sessioni stoppate " + runningSessionDropped);
//		System.out.println("dropRatio = " + dropRatio);
//		System.out.println("aborted ratio = " + abortedRatio);
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
		arrival += Exponential(0.028571429);
		return arrival;
	}

	public static double GetFrontEndService() {
		generator.selectStream(1);
		return Exponential(0.00456);

	}

	public static double GetBackEndService() {
		generator.selectStream(2);
		double temp=Exponential(0.00117);
		return temp;

	}

	public static double GetThinkTime() {

		// + arrivedSessions);
		generator.selectStream(3);
		return Exponential(0.01);

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
