
package com.apigateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Route path is required")
    @Pattern(
            regexp = "^/(?!/)[^?#]*$",
            message = "Path must start with a single slash and cannot contain a query or fragment"
    )
    @Column(nullable = false, unique = true)
    private String path;

    @NotBlank(message = "Target URL is required")
    @Pattern(
            regexp = "https?://.*",
            message = "Target URL must start with http:// or https://"
    )
    @Column(nullable = false)
    private String targetUrl;

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
