## 🚀 Installation & Start

Da dieses Projekt sensible API-Keys nutzt, werden diese über **Umgebungsvariablen** geladen. Das schützt deine Zugangsdaten davor, öffentlich einsehbar zu sein.

### 1. API-Keys besorgen
Du benötigst zwei kostenlose Keys:
* **Alpha Vantage:** Für die Finanzdaten ([hier anfordern](https://www.alphavantage.co/support/#api-key)).
* **Mistral AI:** Für die KI-Agenten ([hier anfordern](https://console.mistral.ai/)).

### 2. Umgebungsvariablen setzen
Bevor du das Programm startest, musst du folgende Variablen auf deinem System (oder in deiner IDE wie IntelliJ/Eclipse) setzen:

| Variable | Beschreibung |
| :--- | :--- |
| `ALPHA_VANTAGE_KEY` | Dein API-Key von Alpha Vantage |
| `MISTRAL_API_KEY` | Dein API-Key von Mistral AI |

### 3. Programm ausführen
Stelle sicher, dass du **Java 21** installiert hast. Du kannst das Projekt mit Maven bauen und starten:

```bash
mvn clean compile exec:java -Dexec.mainClass="LiveFinanzAgent"

Es wird noch weiter dran gearbeitet & verändert.
