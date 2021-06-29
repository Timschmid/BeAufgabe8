
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

class BereichFinder implements Runnable {

	// Alle Dinge, die alle Threads gemeinsam benutzen sind static.
	static final int ZAHL_THREADS = 10;
	
	static Erzeuger erzeuger = new Erzeuger();

	static Collection<BereichFinder> alleFinder = new ConcurrentLinkedQueue();

	static Collection<BereichFinder> gibAlleFinder() {
		// zu implementieren
		return BereichFinder.alleFinder;
	}

	//</*hier ein Runnable Objekt uebergeben, dass die Methode
	static CyclicBarrier gemeinsamerStop
	        = new CyclicBarrier(ZAHL_THREADS, () -> {			//bereicheEinfaerben der Klasse Erzeuger aufruft*/>
				erzeuger.bereicheEinfaerben(alleFinder);
			});
	

	
	public static void main(String[] args) {
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(ZAHL_THREADS);
		ThreadPoolExecutor pool = new ThreadPoolExecutor(ZAHL_THREADS, ZAHL_THREADS, 1, TimeUnit.SECONDS, workQueue); // konstante Thread Zahl

		//Erzeuge so viele zufällige Positionen, wie es schwarze Pixel gibt
		int[] randomPositions = Utility.gibZufaelligeReihenfolgeMitVertauschen(Erzeuger.gibZahlSchwarzerPixel());
		
        for (int i = 0; i < ZAHL_THREADS; i++) {
        	// erzeuge Startposition (die Position eines zufaellige gewaehlten schwarzen Pixels)
			BereichFinder finder = new BereichFinder(i, erzeuger.gibKopieSchwarzePixel().get(randomPositions[i]));
        	// finder in den Pool "alleFinder" einfuegen - wird fuer Einfaerben der Bereich gebraucht
			alleFinder.add(finder);
			// finder in den ThreadPool einfuegen
			pool.execute(finder);
        }
	}
	
	// Die folgenden Attribute hat jeder Thread fuer sich alleine
	private int nummer = 0;
	
	int[] aktuellePosition = null;

	Bereich bereich = new Bereich();

	int gibNummer() {
		return nummer;
	}

	void setzeNummer(int nummer) {
		this.nummer = nummer;
	}
	
	
	
	public void run() {
		while (true) {
			// ein neues Schwarzes Pixel suchen
			// neues schwarzes Pixel dem Bereich dieses BereichFinders hinzufuegen
			sucheSchwarzesPixel(erzeuger.gibBild(), aktuellePosition);

			// neues schwarzes Pixel dem Bereich dieses BereichFinders hinzufuegen
			// Ced: dadurch, dass Bereich.add prüft, ob die Position eines schwarzen Pixels schon
			// im Bereich ist, kann man ohne vorherige Abfrage hinzufügen
			bereich.add(aktuellePosition);

			try {
				gemeinsamerStop.await(); // wenn alle Threads warten, werden die Bereiche in der Darstellung
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
			// eingefaerbt  - das macht das Runnable, das man dem
			                         // Konstruktor von CyclicBarrier mitgegeben hat
		}
	}
	

	int[] gibAktPosition() {
		return aktuellePosition;
	}

	/*
	 * die Position eines Pixels ist eine int Feld mit zwei Plaetzen: int[0] ist
	 * die Zeilennummer, int[1] ist die Spaltennummer des Pixels
	 */
	void setzeAktPosition(int[] position) {
		this.aktuellePosition = position;
	}

	Bereich gibBereich() {
		return bereich;
	}


	BereichFinder(int nummer, int[] startPosition) {
		setzeNummer(nummer);
		setzeAktPosition(startPosition);
		this.bereich.add(startPosition);
	}

	/*
	 * Diese Methode liefert true, wenn ein NachbarPixel gefunden wurde und
	 * false, wenn keines gefunden wurde. In aktPosition steht nach Ausführung der
	 * Methode die evtl. neu gefundene Postition eines schwarzen Pixels
	 */
	private boolean sucheSchwarzesPixel(boolean[][] bild, int[] aktPosition) {
		int zeilenNr = aktPosition[0];
		int spaltenNr = aktPosition[1];

		int[] zeilenShift = { -1, 0, 1, 0 }; // oberes Pixel, rechtes,
		// unteres, linkes
		int[] spaltenShift = { 0, 1, 0, -1 };

		// In welcher Reihenfolge sollen die Nachbarpixel untersucht werden ?
		int[] reihenfolge = Utility.gibZufaelligeReihenfolge(4);

		for (int i = 0; i < 4; i++) {
			int zeilenNrNachbar = zeilenNr + zeilenShift[reihenfolge[i]];
			int spaltenNrNachbar = spaltenNr + spaltenShift[reihenfolge[i]];

			// Ist Nachbar ausserhalb vom Bild?
			if (zeilenNrNachbar >= 0 && zeilenNrNachbar < bild.length
					&& spaltenNrNachbar >= 0
					&& spaltenNrNachbar < bild[0].length) {
				// ist Nachbar Pixel schwarz?
				if (bild[zeilenNrNachbar][spaltenNrNachbar]) {
					aktPosition[0] = zeilenNrNachbar;
					aktPosition[1] = spaltenNrNachbar;

					System.out.println("Finder Nr.: " + this.nummer
							+ " Neue aktPosition: " + zeilenNrNachbar + " "
							+ spaltenNrNachbar);

					return true;
				}
			}
		}

		// kein schwarzes Pixel benachbart = Einzelpixel
		return false;
	}

}