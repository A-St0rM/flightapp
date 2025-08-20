package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        try {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            //List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);
            List<FlightDTO> filteredFlights = totalFlightTimeForAirline(flightList);
            filteredFlights.forEach(System.out::println);
            //flightInfoDTOList.forEach(System.out::println);

            double avgTime = averageFlightTime(flightList);
            System.out.println("Gennemsnitlig flyvetid for Lufthansa: " + avgTime + " minutter");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> totalFlightTimeForAirline(List<FlightDTO> flightList) {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null
                        && flight.getAirline().getName() != null
                        && flight.getAirline().getName().equalsIgnoreCase("Lufthansa"))
                .collect(Collectors.toList());
    }

    //Add a new feature (e.g. calculate the average flight time for a specific airline. For example, calculate the average flight time for all flights operated by Lufthansa)

    public static double averageFlightTime(List<FlightDTO> flightList) {
        List<FlightDTO> specificAirlineFlights = totalFlightTimeForAirline(flightList);

        return specificAirlineFlights.stream()
                .mapToLong(flight -> {
                    LocalDateTime dep = flight.getDeparture().getScheduled();
                    LocalDateTime arr = flight.getArrival().getScheduled();
                    if (dep != null && arr != null) {
                        return Duration.between(dep, arr).toMinutes();
                    } else {
                        return 0L;
                    }
                })
                .average()
                .orElse(0.0); // hvis listen er tom
    }







    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList) {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
           .map(flight -> {
                LocalDateTime departure = flight.getDeparture().getScheduled();
                LocalDateTime arrival = flight.getArrival().getScheduled();
                Duration duration = Duration.between(departure, arrival);
                FlightInfoDTO flightInfo =
                        FlightInfoDTO.builder()
                            .name(flight.getFlight().getNumber())
                            .iata(flight.getFlight().getIata())
                            .airline(flight.getAirline().getName())
                            .duration(duration)
                            .departure(departure)
                            .arrival(arrival)
                            .origin(flight.getDeparture().getAirport())
                            .destination(flight.getArrival().getAirport())
                            .build();

                return flightInfo;
            })
        .toList();
        return flightInfoList;
    }

}
