/* This is so shitty but I put it on GIT so I can work on this from another laptop :D */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Optional;

public class Fetcher {

    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(new File("data.txt")));
        reader.readLine();
        String line;
        HashMap<String, Integer> map = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            String city = line.split("\\|")[1];
            int amount = Integer.valueOf(line.split("\\|")[20]);
            map.computeIfPresent(city, (key, old) -> old + amount);
            map.putIfAbsent(city, amount);
        }

        int count = 0;
        for (String city : map.keySet()) {
            HttpURLConnection con;
            //String url = "https://www.latlong.net/_spm4.php";
            String url = "https://api.opencagedata.com/geocode/v1/json?q=Belgium%20" + URLEncoder.encode(city, "UTF-8") + "&key=1284cc76281c4e118c341fbfe871236a";
//            String urlParameters = "c1=Belgium%20" + city + "&action=gpcm&cp=";
//            System.out.println(urlParameters);
//            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            try {
                con = (HttpURLConnection) new URL(url).openConnection();

                con.setDoOutput(true);
                con.setRequestMethod("GET");
                //con.setRequestProperty("accept", "*/*");
                //con.setRequestProperty("accept-language", "en-GB,en;q=0.9,en-US;q=0.8,nl;q=0.7");
                //con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                //con.setRequestProperty("cookie", "PHPSESSID=j564gecsmbj1rt89kcor1nb9e5; dvckli=ntf");
                con.setRequestProperty("dnt", "1");
                //con.setRequestProperty("origin", "https://www.latlong.net");
                //con.setRequestProperty("referer", "https://www.latlong.net/");
                con.setRequestProperty("referer", "http://www.mapcoordinates.net/en");
                con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");
                //con.setRequestProperty("x-requested-with", "XMLHttpRequest");

                //System.out.println(con.getResponseCode());

                //try (OutputStream wr = con.getOutputStream()) {
//                    wr.write(postData);
//                }

                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String line2;
                    StringBuilder response = new StringBuilder();
                    System.out.println(con.getResponseCode());
                    while ((line2 = in.readLine()) != null) {
                        response.append(line2);
                        response.append("\n");
                    }
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(response.toString());
                    JSONObject jsonObject = (JSONObject) obj;
                    JSONArray array = (JSONArray) jsonObject.get("results");
                    System.out.println("Looking for: " + city);
                    Optional<Object> temp = array.stream().filter(o -> ((JSONObject) ((JSONObject) o).get("components")).get("_type").toString().equals("city")).findFirst();
                    JSONObject result;
                    if (temp.isPresent())
                        result = (JSONObject) temp.get();
                    else {
                        temp = array.stream().filter(o -> ((JSONObject) ((JSONObject) o).get("components")).get("_type").toString().equals("neighbourhood")).findFirst();
                        if (temp.isPresent())
                            result = (JSONObject) temp.get();
                        else
                            result = (JSONObject) array.stream().filter(o -> ((JSONObject) ((JSONObject) o).get("components")).get("_type").toString().equals("village")).findFirst().get();
                    }


                    double lat = (double) ((JSONObject) result.get("geometry")).get("lat");
                    double lng = (double) ((JSONObject) result.get("geometry")).get("lng");

                    try (FileWriter fw = new FileWriter("output.txt", true); BufferedWriter writer = new BufferedWriter(fw)) {
                        writer.write(city + "|" + lat + "|" + lng + "|" + map.get(city));
                        writer.newLine();
                        System.out.println(++count + ", " + city + ": " + lat + ", " + lng + ": " + map.get(city));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
