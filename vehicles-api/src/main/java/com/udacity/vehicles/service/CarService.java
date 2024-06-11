package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final PriceClient priceClient;
    private final MapsClient mapsClient;
    private final ManufacturerRepository manufacturerRepository;

    public CarService(CarRepository repository, PriceClient priceClient, MapsClient mapsClient, ManufacturerRepository manufacturerRepository) {
        this.repository = repository;
        this.priceClient = priceClient;
        this.mapsClient = mapsClient;
        this.manufacturerRepository = manufacturerRepository;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
//    public Car findById(Long id) {
//        Optional<Car> optionalCar = repository.findById(id);
//        if (optionalCar.isPresent()) {
//            Car car = optionalCar.get();
//            car.setPrice(priceClient.getPrice(id));
//            car.setLocation(mapsClient.getAddress(car.getLocation()));
//            return car;
//        } else {
//            throw new CarNotFoundException("Car Not Found");
//        }
//    }
    public Car findById(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be greater than 0");
        }
        return repository.findById(id).map(car -> {
            car.setPrice(priceClient.getPrice(id));
            car.setLocation(mapsClient.getAddress(car.getLocation()));
            return car;
        }).orElseThrow(() -> new CarNotFoundException("Car with ID " + id + " not found"));
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null && car.getId() != 0) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        carToBeUpdated.setCondition(car.getCondition());
                        carToBeUpdated.setPrice(car.getPrice());
                        carToBeUpdated.setModifiedAt(LocalDateTime.now());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        } else {
            if (car.getCreatedAt() == null) {
                car.setCreatedAt(LocalDateTime.now());
            }
            car.setModifiedAt(LocalDateTime.now());
            // Check if manufacturer is null and save if it is not null
            if (car.getDetails() != null && car.getDetails().getManufacturer() != null) {
                Manufacturer manufacturer = car.getDetails().getManufacturer();
                if (manufacturer.getCode() == null || manufacturer.getCode() == 0) {
                    // If manufacturer id is null or 0, it means the manufacturer is new and needs to be saved first
                    Manufacturer savedManufacturer = manufacturerRepository.save(manufacturer);
                    car.getDetails().setManufacturer(savedManufacturer);
                }
            }
            return repository.save(car);
        }
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isPresent()){
            repository.deleteById(id);
        }else{
            throw new CarNotFoundException("Car not found");
        }
    }
}
