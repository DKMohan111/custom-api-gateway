package com.apigateway.controller;


import com.apigateway.entity.Route;
import com.apigateway.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }
    @PostMapping
    public Route createRoute(@Valid  @RequestBody Route route){
        return  routeService.createRoute(route);
    }

    @GetMapping
    public List<Route> getAllRoutes(){
        return routeService.getAllRoutes();
    }

    @GetMapping("/{id}")
    public Route getRouteById(@PathVariable Long id){
        return routeService.getRouteById(id);
    }
}
