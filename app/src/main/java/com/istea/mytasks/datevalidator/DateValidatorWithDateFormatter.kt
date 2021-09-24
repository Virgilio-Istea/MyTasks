package com.istea.mytasks.datevalidator

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateValidatorWithDateFormatter: DateValidator {

    var dateFormat: DateTimeFormatter


    public constructor(dateFormat: DateTimeFormatter){
        this.dateFormat = dateFormat
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun isValid(dateStr: String): Boolean {

        try {
            this.dateFormat.parse(dateStr)
        } catch (e: DateTimeParseException) {
            return false
        }
        return true
    }
}