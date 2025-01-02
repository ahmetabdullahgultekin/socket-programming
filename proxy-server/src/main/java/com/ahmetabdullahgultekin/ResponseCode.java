package com.ahmetabdullahgultekin;

public enum ResponseCode {

    OK("OK", 200),
    RequestUriTooLong("Request-URI too long", 414),
    NotFound("Not Found", 404);

    ResponseCode(String s, int i) {

    }
}
