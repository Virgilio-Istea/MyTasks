package com.istea.mytasks.datevalidator

import java.text.ParseException
import java.text.SimpleDateFormat

class DateValidatorWithDateFormatter(private var dateFormat: SimpleDateFormat) : DateValidator {

    override fun isValid(dateStr: String): Boolean {

        try {
            this.dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            return false
        }
        return true
    }
}