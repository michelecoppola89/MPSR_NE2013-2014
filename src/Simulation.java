import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;

import javax.annotation.Generated;

public class Simulation {

	public static Rngs generator = new Rngs();
	public static double start = 0;
	public static double stop = 0;
	public static double arrival = 0;
	public static ArrayList<Session> sessionList = new ArrayList<Session>();
	public static Clock systemClock = new Clock();

	public static int arrivedSessions = 0;
	public static int frontEndRequestsNumber = 0;
	public static int backEndRequestsNumber = 0;
	public static int completedSessions = 0;

	public static void main(String[] args) {

		generator.plantSeeds(-1);
		// aggiornare stop prendendo valore dall'argomento

		double nextCompletionTime = Double.MAX_VALUE;
		double nextArrivalTime = Double.MAX_VALUE;

		Session currentSession = null;

		GetArrival();
		nextArrivalTime = arrival;

		while ((systemClock.getCurrent() <= stop) || (sessionList.size() != 0)) {

			nextCompletionTime = GetNextCompletionTime(currentSession);
			if (nextCompletionTime <= nextArrivalTime) {
				systemClock.setNext(nextCompletionTime);
			} else {
				systemClock.setNext(nextArrivalTime);
			}

			// aggiorno statistiche

			systemClock.setCurrent(systemClock.getNext());

			if (systemClock.getCurrent() == nextArrivalTime) {
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
					GetArrival();
					nextArrivalTime = arrival;
				} else {
					nextArrivalTime = Double.MAX_VALUE;
				}

			} else if (systemClock.getCurrent() == currentSession
					.getFrontEndCompletionTime()) {
				currentSession.setFrontEndCompletionTime(Double.MAX_VALUE);
				currentSession
						.setBackEndCompletionTime(GetMaxBACKEndCompletionTime()
								+ GetBackEndService());
				frontEndRequestsNumber--;
				backEndRequestsNumber++;

				// nextCompletionTime=Double.MAX_VALUE;

			} else if (systemClock.getCurrent() == currentSession
					.getBackEndCompletionTime()) {

				currentSession.setRequestNumber(currentSession
						.getRequestNumber() - 1);

				backEndRequestsNumber--;

				if (currentSession.getRequestNumber() == 0) {
					currentSession.setCompleted(true);
					removeSession();
					completedSessions++;
				} else {
					currentSession.setThinkTimeCompletionTime(currentSession
							.getBackEndCompletionTime() + GetThinkTime());
					currentSession.setBackEndCompletionTime(Double.MAX_VALUE);
				}

			} else if (systemClock.getCurrent() == currentSession
					.getThinkTimeCompletionTime()) {
				currentSession.setThinkTimeCompletionTime(Double.MAX_VALUE);
				currentSession
						.setFrontEndCompletionTime(GetMaxFrontEndCompletionTime()
								+ GetFrontEndService());
				frontEndRequestsNumber++;

			}

		}

	}

	private static void removeSession() {
		// TODO Auto-generated method stub

	}

	public static double GetNextCompletionTime(Session currentSession) {

		if (sessionList.size() > 0) {

			double ret = Double.MAX_VALUE;
			currentSession = null;

			for (int i = 0; i < sessionList.size(); i++) {
				if (sessionList.get(i).minEventTime() <= ret) {
					ret = sessionList.get(i).minEventTime();
					currentSession = sessionList.get(i);
				}
			}

			return ret;

		} else {
			currentSession = null;
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
		generator.selectStream(3);
		return Exponential(7);

	}

	public static int GetNewRequestsNumber() {
		generator.selectStream(4);
		return Equilikely(5, 35);
	}

	public static double Exponential(double m) {
		return (-m * Math.log(1 - generator.random()));
	}

	public static int Equilikely(int a, int b) {
		return (a + (int) (generator.random() * (b - a + 1)));
	}

}