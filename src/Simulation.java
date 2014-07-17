import java.util.ArrayList;

public class Simulation {

	public static Rngs generator = new Rngs();
	public static double start = 0;
	public static double stop = 50;
	public static double arrival = 0;
	public static ArrayList<Session> sessionList = new ArrayList<Session>();
	public static Clock systemClock = new Clock();

	public static int arrivedSessions = 0;
	public static int frontEndRequestsNumber = 0;
	public static int backEndRequestsNumber = 0;
	public static int completedSessions = 0;
	public static int currentSession;
	// bound
	// public static int systemBound=250;
	// public static int newSessionDropped=0;
	// public static int runninSessionDropped=0;
	// bound
	public static Statistics statistics = new Statistics(0.0, 0.0, 0.0);

	public static void main(String[] args) {

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
			System.out.println("Clock corrente =" + systemClock.getNext());
			// ----------------------------------------------------------
			statistics
					.setFrontEnd(statistics.getFrontEnd()
							+ ((systemClock.getNext() - systemClock
									.getCurrent()) * frontEndRequestsNumber));
			statistics
					.setBackEnd(statistics.getBackEnd()
							+ ((systemClock.getNext() - systemClock
									.getCurrent()) * backEndRequestsNumber));
			statistics
					.setThinkTime(statistics.getThinkTime()
							+ ((systemClock.getNext() - systemClock
									.getCurrent()) * (arrivedSessions
									- completedSessions
									- frontEndRequestsNumber - backEndRequestsNumber)));
			// ----------------------------------------------------------

			systemClock.setCurrent(systemClock.getNext());

			if (systemClock.getCurrent() == nextArrivalTime) {
				// bound
				// if(frontEndRequestsNumber+backEndRequestsNumber<systemBound)
				// {
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
					// con un tempo di 50 il tempo supera il valore massimo del
					// double?

				}
				// bound
				// }
				// else
				// newSessionDropped++;

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
					sessionList.remove(currentSession);
					completedSessions++;
				} else {
					sessionList.get(currentSession).setBackEndCompletionTime(
							Double.MAX_VALUE);
					sessionList.get(currentSession).setThinkTimeCompletionTime(
							systemClock.getCurrent() + GetThinkTime());

				}

			} else if (sessionList.get(currentSession) != null
					&& systemClock.getCurrent() == sessionList.get(
							currentSession).getThinkTimeCompletionTime()) {
				//bound
				// if(frontEndRequestsNumber+backEndRequestsNumber<systemBound)
				// {
				sessionList.get(currentSession).setThinkTimeCompletionTime(
						Double.MAX_VALUE);
				sessionList.get(currentSession).setFrontEndCompletionTime(
						GetMaxFrontEndCompletionTime() + GetFrontEndService());
				frontEndRequestsNumber++;
				//bound
				// }
				// else
				// runninSessionDropped++;

			}

			System.out
					.println("-------------------------------------------------------------------------");
			System.out.println("sessioni nel sistema =" + sessionList.size());
			System.out.println("Numero di sessioni arrivate nel sistema ="
					+ arrivedSessions);
			System.out.println("Numero di sessioni partite dal sistema ="
					+ completedSessions);
			System.out.println("Numero di sessioni nel front end ="
					+ frontEndRequestsNumber);
			System.out.println("Numero di sessioni nel back end ="
					+ backEndRequestsNumber);
			System.out.println("Numero di sessioni in think time ="
					+ (arrivedSessions - completedSessions
							- frontEndRequestsNumber - backEndRequestsNumber));
			System.out.println("Prossimo istante di completamento ="
					+ nextCompletionTime);
			System.out.println("Prossimo istante di arrivo =" + arrival);
		}
		statistics.setThroughput(completedSessions / systemClock.getCurrent());
		System.out
				.println("<<<<<<<<<<<<<<Statistiche time averaged<<<<<<<<<<<<<<<<<<<<<<<<<<");

		System.out.println("Numero medio di sessioni nel front end ="
				+ (statistics.getFrontEnd() / systemClock.getCurrent()));
		System.out.println("Numero medio di sessioni nel back end ="
				+ (statistics.getBackEnd() / systemClock.getCurrent()));
		System.out.println("Numero medio di sessioni nel think time ="
				+ (statistics.getThinkTime() / systemClock.getCurrent()));
		System.out.println("Troughput di sistema ="
				+ statistics.getThroughput());

		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
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
