package com.artyommameev.quester.validation;

import lombok.Data;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Represents a server response with validation results.
 *
 * @author Artyom Mameev
 */
@Data
public class ValidationResponse {

    private boolean hasErrors;
    private List<ObjectError> fieldErrors;

    /**
     * Instantiates a new Validation Response.
     *
     * @param hasErrors the boolean that indicates whether validation errors are
     *                  present.
     */
    public ValidationResponse(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}
