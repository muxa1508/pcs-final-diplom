import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client {


    public static void main(String[] args) {
        clientStart();
    }


    protected static String HOST = "127.0.0.1";
    protected static int PORT = 8989;

    protected static void clientStart() {
        try (Socket clientSocket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println("Бизнес бизнес атмосфера");
            System.out.println(in.readLine());
            String inputFromServer = in.readLine();
//            System.out.print(inputFromServer);
            try {
                JSONParser parser = new JSONParser();
                List<PageEntry> inPageEntry = new ArrayList<>();
                JSONArray jsonArray = (JSONArray) parser.parse(inputFromServer);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    PageEntry pageEntry = new PageEntry((String) jsonObject.get("pdfName"), ((Long) jsonObject.get("page")).intValue(), ((Long) jsonObject.get("count")).intValue());
                    inPageEntry.add(pageEntry);
                }
                System.out.println(inPageEntry);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
