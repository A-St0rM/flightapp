package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
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
            //List<FlightDTO> filteredFlights = totalFlightForAirline(flightList);
            //filteredFlights.forEach(System.out::println);
            //flightInfoDTOList.forEach(System.out::println);

            double avgTime = averageFlightTime(flightList);
            System.out.println("Gennemsnitlig flyvetid for Lufthansa: " + avgTime + " minutter");

            LocalDateTime cutoff = LocalDateTime.of(2024, 8, 15, 00, 10);
            List<FlightDTO> flightsBefore = flightsBeforeTime(flightList,cutoff);
            flightsBefore.forEach(System.out::println);
            System.out.println(" time: " + cutoff);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> totalFlightForAirline(List<FlightDTO> flightList) {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null
                        && flight.getAirline().getName() != null
                        && flight.getAirline().getName().equalsIgnoreCase("Lufthansa"))
                .collect(Collectors.toList());
    }

    //Add a new feature (e.g. calculate the average flight time for a specific airline. For example, calculate the average flight time for all flights operated by Lufthansa)

    public static double averageFlightTime(List<FlightDTO> flightList) {
        List<FlightDTO> specificAirlineFlights = totalFlightForAirline(flightList);

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


    //Add a new feature (make a list of flights that leaves before a specific time in the day/night. For example, all flights that leave before 01:00)
    public static List<FlightDTO> flightsBeforeTime(List<FlightDTO> flightList, LocalDateTime cutoffTime) {
        return flightList.stream()
                .filter(flight -> {
                    LocalDateTime departureTime = flight.getDeparture().getScheduled();
                    return departureTime != null && departureTime.isBefore(cutoffTime);
                })
                .collect(Collectors.toList());
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
