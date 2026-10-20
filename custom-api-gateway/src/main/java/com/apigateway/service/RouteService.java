package com.apigateway.service;


import com.apigateway.entity.Route;
import com.apigateway.exception.DuplicateRouteException;
import com.apigateway.exception.RouteNotFoundException;
import com.apigateway.repository.RouteRepository;
import com.apigateway.security.TargetUrlValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final TargetUrlValidator targetUrlValidator;

    public RouteService(
            RouteRepository routeRepository,
            TargetUrlValidator targetUrlValidator) {
        this.routeRepository = routeRepository;
        this.targetUrlValidator = targetUrlValidator;
    }

    public Route createRoute(Route route) {
        targetUrlValidator.validate(route.getTargetUrl());
        if (routeRepository.findByPath(route.getPath()).isPresent()) {
            throw new DuplicateRouteException("Route already exists");
        }

        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes(){
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() ->
                        new RouteNotFoundException("Route not found"));
    }


    public Route findRouteForPath(String path) {
        String candidate = path;

        while (candidate != null && !candidate.isEmpty()) {
            java.util.Optional<Route> route =
                    routeRepository.findByPath(candidate);

            if (route.isPresent()) {
                return route.get();
            }

            int lastSlash = candidate.lastIndexOf('/');

            if (lastSlash <= 0) {
                break;
            }

            candidate = candidate.substring(0, lastSlash);
        }

        throw new RouteNotFoundException("Route not found");
    }


}
