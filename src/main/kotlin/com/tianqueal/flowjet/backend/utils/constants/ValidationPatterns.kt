package com.tianqueal.flowjet.backend.utils.constants

object ValidationPatterns {
  const val USERNAME = "^[a-zA-Z][a-zA-Z0-9._]{2,49}$"
  const val NAME = "^[A-Za-zÀ-ÿ' -]{2,100}$"

  // - At least one digit.
  // - At least one lower case letter.
  // - At least one upper case letter.
  // - At least one special character.
  // - The password must only contain the allowed characters.
  // - Length between 8 and 64 characters.
  const val PASSWORD =
      "^(?=.*[0-9])" +
          "(?=.*[a-z])" +
          "(?=.*[A-Z])" +
          "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/? ])" +
          "[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/? ]{8,64}$"
}
