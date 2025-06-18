package com.licentarazu.turismapp.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class CityCoordinatesService {

    private static final Map<String, double[]> cityCoordinates = new HashMap<>();

    static {
        cityCoordinates.put("baia mare", new double[]{47.6588, 23.5681});
        cityCoordinates.put("cluj-napoca", new double[]{46.7712, 23.6236});
        cityCoordinates.put("bucuresti", new double[]{44.4268, 26.1025});
        cityCoordinates.put("brasov", new double[]{45.6579, 25.6012});
        cityCoordinates.put("iasi", new double[]{47.1585, 27.6014});
        cityCoordinates.put("constanta", new double[]{44.1598, 28.6348});
        cityCoordinates.put("timisoara", new double[]{45.7489, 21.2087});
        cityCoordinates.put("oradea", new double[]{47.0722, 21.9211});
        // poți adăuga oricâte orașe vrei
    }

    public double[] getCoordinates(String city) {
        return cityCoordinates.get(city.toLowerCase());
    }
}
