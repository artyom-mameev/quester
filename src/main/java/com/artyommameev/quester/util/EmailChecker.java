package com.artyommameev.quester.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.regex.Pattern;

/**
 * A simple utility class used to check email format.
 *
 * @author Craig Walls
 * <p>
 * Copyright 2015 Craig Walls
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@UtilityClass
public class EmailChecker {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

    /**
     * Checks if a e-mail string is in the correct format.
     *
     * @param email the email string that should be checked.
     * @return true if the email in the correct format, otherwise false.
     */
    public static boolean isEmail(String email) {
        val pattern = Pattern.compile(EMAIL_PATTERN);
        val matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
