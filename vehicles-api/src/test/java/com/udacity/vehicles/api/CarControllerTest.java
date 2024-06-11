package com.udacity.vehicles.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import com.udacity.vehicles.service.CarService;
import java.net.URI;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Implements testing of the CarController class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CarControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CarRepository repository;
    @MockBean
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private JacksonTester<Car> json;

    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;

    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @Before
    public void setup() {
        Car car = getCar();
        car.setId(1L);
        given(carService.save(any())).willReturn(car);
        given(carService.findById(any())).willReturn(car);
        given(carService.list()).willReturn(Collections.singletonList(car));
    }

    /**
     * Tests for successful creation of new car in the system
     * @throws Exception when car creation fails in the system
     */
//    @Test
//    public void createCar() throws Exception {
//        Car car = getCar();
//        mvc.perform(
//                post(new URI("/cars"))
//                        .content(json.write(car).getJson())
//                        .contentType(MediaType.APPLICATION_JSON_UTF8)
//                        .accept(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(status().isCreated());
//    }
    @Test
    public void createCar() throws Exception {
        Car car = getCar();

        if (car.getDetails() != null && car.getDetails().getManufacturer() != null) {
            Manufacturer manufacturer = car.getDetails().getManufacturer();
            if (manufacturer.getCode() == null || manufacturer.getCode() == 0) {
                Manufacturer savedManufacturer = new Manufacturer();
                savedManufacturer.setName(manufacturer.getName());
                manufacturer = manufacturerRepository.save(savedManufacturer);
                car.getDetails().setManufacturer(manufacturer);
            }
        }

        mvc.perform(
                        post(new URI("/cars"))
                                .content(json.write(car).getJson())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated());
    }


    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     * @throws Exception if the read operation of the vehicle list fails
     */
    @Test
    public void listCars() throws Exception {
        Car car = getCar();
        car.setId(1L);
        repository.save(car);
        mvc.perform(get(new URI("/cars")).accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isOk());
    }

    /**
     * Tests the read operation for a single car by ID.
     * @throws Exception if the read operation for a single car fails
     */
    @Test
    public void findCar() throws Exception {
        Car car = getCar();
        car.setId(1L);
        given(carService.findById(any())).willReturn(car);
        mvc.perform(MockMvcRequestBuilders.get("/cars/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.manufacturer.code").value(101))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.manufacturer.name").value("Chevrolet"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.model").value("Impala"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.mileage").value(32280))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.externalColor").value("white"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.body").value("sedan"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.engine").value("3.6L V6"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.fuelType").value("Gasoline"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.modelYear").value(2018))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.productionYear").value(2018))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.numberOfDoors").value(4));        ;
        verify(carService, times(1)).findById(1L);
    }

    /**
     * Tests the deletion of a single car by ID.
     * @throws Exception if the delete operation of a vehicle fails
     */
    @Test
    public void deleteCar() throws Exception {
        Car car = getCar();
        car.setId(1L);
        mvc.perform(delete(new URI("/cars/" + car.getId())).accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isNoContent());
    }

    @Test
    public void updateCar() throws Exception {
        Car car = getCar();
        car.setCondition(Condition.NEW);
        car.setId(1L);
        when(carService.save(any(Car.class))).thenReturn(car);

        mvc.perform(put(new URI("/cars/" + car.getId()))
                .content(json.write(car).getJson())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(car.getId()))
                .andExpect(jsonPath("$.condition").value(car.getCondition().toString()))
                .andReturn();
    }


    private Car getCar() {
        Car car = new Car();
        car.setLocation(new Location(40.730610, -73.935242));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(32280);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("3.6L V6");
        details.setFuelType("Gasoline");
        details.setModelYear(2018);
        details.setProductionYear(2018);
        details.setNumberOfDoors(4);
        car.setDetails(details);
        car.setCondition(Condition.USED);
        return car;
    }
}