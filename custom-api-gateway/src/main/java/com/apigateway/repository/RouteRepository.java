package com.apigateway.repository;

import com.apigateway.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {


    Optional<Route> findByPath(String path);

}
