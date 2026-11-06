import java.io.InterruptedIOException;
import java.util.Scanner;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherAPI {
    public static void main(String[] args) {
        //Welcome Message
        System.out.println("Welcome to the Java Weather API Reader!");

        //Declare a Scanner
        Scanner input = new Scanner(System.in);

        //Prompt the user and get their choice
        System.out.println("Choose a location:");
        System.out.println("1. West Bend, Wisconsin");
        System.out.println("2. Milwaukee, Wisconsin");
        System.out.println("3. Chicago, Illinois");
        System.out.println("Enter your choice: ");

        int choice = input.nextInt();

        //Assign the values based on choice, or assume West Bend
        String locationName;
        double latitude;
        double longitude;

        if (choice == 2){
            locationName = "Milwaukee, Wisconsin";
            latitude = 43.0389;
            longitude = -87.9065;
        } else if (choice == 3){
            locationName = "Chicago, Illinois";
            latitude = 43.8781;
            longitude = -87.6298;
        } else {
            locationName = "West Bend, Wisconsin";
            latitude = 43.4253;
            longitude = -88.1834;
        }

        //Build the URL and Notify the user
        String apiUrl = buildWeatherUrl(latitude, longitude);

        System.out.println();
        System.out.println("The location: " + locationName);
        System.out.println("Attempting to connect to the weather API...");
        System.out.println();

        //Try sending the API Call
        try{
            String response = getApiResponse(apiUrl);

            //Print the Raw API Response
            System.out.println("Raw API Response: ");
            System.out.println(response);
            System.out.println();

            //Print the weather report as a summary
            System.out.println();
            System.out.println("Simple Weather Summary: ");

            String time = extractTextValue(response, "time");
            String temperature = extractNumberValue(response, "temperature_2m");
            String windSpeed = extractNumberValue(response, "wind_speed_10m");

            System.out.println("Current time: " + time);
            System.out.println("Temperature: " + temperature + " F");
            System.out.println("Wind Speed: " + windSpeed + " mph");

            System.out.println();
            System.out.println("Weather data retrieved successfully!");

        }catch (IOException e){
            System.out.println("Network or File Error: " + e.getMessage());
        }catch (InterruptedException e){
            System.out.println("The request was interrupted.");
            Thread.currentThread().interrupt();
        }catch(Exception e){
            System.out.println("Unexpected Error: " + e.getMessage());
        }
    }

    //String Method to build the API URL
    public static String buildWeatherUrl(double latitude, double longitude){
        return "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,wind_speed_10m"
                + "&temperature_unit=fahrenheit"
                + "&wind_speed_unit=mph";
    }

    //Method to GET the Weather API information
    public static String getApiResponse(String apiUrl) throws IOException, InterruptedException{
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200){
            throw new IOException("API request failed with status code: " + response.statusCode());
        }

        return response.body();
    }

    public static String extractTextValue(String json, String key){
        int currentIndex = json.indexOf("\"current\":{");

        if (currentIndex == -1){
            return "Not Found";
        }

        String searchText = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchText, currentIndex);

        if (startIndex == -1){
            return "Not Found";
        }

        startIndex += searchText.length();
        int endIndex = json.indexOf("\"", startIndex);

        if (endIndex == -1){
            return "Not Found";
        }

        return json.substring(startIndex, endIndex);
    }

    public static String extractNumberValue(String json, String key){
        int currentIndex = json.indexOf("\"current\":{");

        if (currentIndex == -1){
            return "Not Found";
        }

        String searchText = "\"" + key + "\":";
        int startIndex = json.indexOf(searchText, currentIndex);

        if (startIndex == -1){
            return "Not Found";
        }

        startIndex += searchText.length();

        int endIndex = startIndex;

        while (endIndex < json.length()){
            char currentChar = json.charAt(endIndex);

            if((currentChar >= '0' && currentChar <= '9')
                    || currentChar == '.'
                    || currentChar == '-'){
                endIndex++;
            }else{
                break;
            }
        }

        //Make sure the value is numeric
        if (startIndex == endIndex)
            return "Not Found";

        return json.substring(startIndex, endIndex);
    }
}
