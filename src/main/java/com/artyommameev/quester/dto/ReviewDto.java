package com.artyommameev.quester.dto;

import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.validation.annotation.ValidRating;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * A data transfer object with validation mechanism used to create a
 * {@link Review} objects.
 *
 * @author Artyom Mameev
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    @NotBlank(message = "{valid.blank-field}")
    @Size(min = 1, max = 1, message = "valid.rating-size")
    @ValidRating(message = "{valid.rating}")
    private String rating;
}
