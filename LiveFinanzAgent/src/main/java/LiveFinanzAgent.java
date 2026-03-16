import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.mistralai.MistralAiChatModel;

public class LiveFinanzAgent {

    static class FinanzTools {
        private static final String ALPHA_KEY = "K5EOHVZ14I2HMECP";

        public String getDaten(String symbol) {
            try {
                // Hinweis: Für EURUSD nutzt Alpha Vantage oft die Function CURRENCY_EXCHANGE_RATE
                // Aber wir bleiben für den Test bei GLOBAL_QUOTE (funktioniert bei Aktien am besten)
                String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" 
                             + symbol + "&apikey=" + ALPHA_KEY;
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                
                JSONObject fullJson = new JSONObject(response);
                
                if (!fullJson.has("Global Quote") || fullJson.getJSONObject("Global Quote").isEmpty()) {
                    return "Fehler: Symbol nicht gefunden oder API-Limit erreicht.";
                }

                JSONObject quote = fullJson.getJSONObject("Global Quote");
                String preis = quote.getString("05. price");
                String changePercent = quote.getString("10. change percent");

                return String.format("Symbol: %s | Preis: %s | Änderung: %s", symbol, preis, changePercent);
            } catch (Exception e) {
                return "Fehler beim Datenabruf: " + e.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        // Das "Gehirn"
        MistralAiChatModel model = MistralAiChatModel.builder()
            .apiKey("qaZdYU7l7Jp1mK8o3YudmHcAepsaxVm1") 
            .modelName("open-mistral-7b")
            .temperature(0.2)
            .build();

        System.out.println("--- Multi-Agenten-System gestartet ---");

        FinanzTools tools = new FinanzTools();
        String marktDaten = tools.getDaten("NVDA"); // Teste am besten erst mit einer Aktie
        
        System.out.println("Rohdaten vom Server: " + marktDaten);

        String Newsscratcher = model.generate(
            SystemMessage.from("Durchsuche das Internet nach Tagesaktuellen Wirtschaftsnachrichten die relevant für die Finanzmärkte sind nutze freie Portale wie CrptoPanic.com bspw oder Investing.com oder ING NEWS Thinletter, diese fasst du bitte zusammen und übergibst sie dem Risiko Manager. Antworte nur mit 'SUMMARIZED' oder 'ERROR. Wenn ERROR ausgegeben wird, schreib dazu wieso ERROR auftritt."), 
            UserMessage.from(marktDaten)
        ).content().text();

        System.out.println("News Scratcher: " + Newsscratcher);

        if (Newsscratcher.contains("VALID")) {

            // --- SCHRITT 1: DATA AUDITOR ---
            String dataValidator = model.generate(
                SystemMessage.from("Du bist ein Data Auditor. Übprüfe ob die Kursdaten plausibel sind und checke sie gegen. Antworte NUR mit 'VALID' oder 'ERROR'." + "Wenn ERROR ausgegeben wird, schreib dazu wieso ERROR auftritt."),
                UserMessage.from(marktDaten)
            ).content().text();
        
            System.out.println("Audit Status: " + dataValidator);

            if (dataValidator.contains("VALID")) {
            
                // --- SCHRITT 2: ICT ANALYST ---
                String ictAnalysis = model.generate(
                    SystemMessage.from("Du bist ICT (Michael J. Huddelston). Aanalysiere in deinem Smart Money Concept Stil und nutze alle Konzepte."),
                    UserMessage.from(marktDaten)
                ).content().text();
                
                System.out.println("\n[ICT ANALYSE]: " + ictAnalysis);

                // --- SCHRITT 3: LARRY WILLIAMS ---
                String lwAnalysis = model.generate(
                    SystemMessage.from("Du bist Larry R. Williams. Analysiere genauso wie der Profi Trader bei der Weltmeisterschaft."),
                    UserMessage.from(marktDaten)
                ).content().text();
                
                System.out.println("\n[LARRY WILLIAMS ANALYSE]: " + lwAnalysis);

                // --- SCHRITT 4: RISK MANAGER ---
                String anfrageRisk = "Hier sind die Analysen von ICT und Larry Williams:\n" +
                                 "ICT: " + ictAnalysis + "\n" +
                                 "LW: " + lwAnalysis + "\n" +
                                 "NEWS " + Newsscratcher + "\n" +
                                 "Handle als Professeioneller Risk Manager und beziehe die ICT, LW & NEWS mit in deine Expertise mit und fasse diese Zusammen" +
                                 "erlaube den Trade nur wenn du mit deinen Profi Ketnissen einverstanden bist und alles Übereinstimmt";

                String endUrteil = model.generate(anfrageRisk);

                System.out.println("\n--- FINALES URTEIL (RISK MANAGER) ---");
                System.out.println(endUrteil);
            
                } else {
                System.out.println("Abbruch: Daten nicht valide.");
            }
        }
    }
}