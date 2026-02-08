package com.mazasoft.ecommerce.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(String error, String error_description) {
}
