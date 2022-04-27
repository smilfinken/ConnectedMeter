package net.smilfinken.meter.collector.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Requested data is not available")
class DataUnavailableException : java.lang.Exception()