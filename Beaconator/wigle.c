//
// Created by roobre on 11/29/15.
//

#include "wigle.h"

#include <time.h>
#include <curl/curl.h>
#include <curl/easy.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <jansson.h>

const char WIGLE_BASE_URL[] = "https://wigle.net/api/v1/jsonSearch";
const char WILGE_AUTHCOOKIE[] = "auth=cybercamp2:282401487:1448674819:GITL4702g/iluzMM8TdXbw";

void fetch_wigle(char** json, double lat, double lng, double offset) {
    char postdata[128];

    *json = NULL;

    sprintf(postdata, "latrange1=%.4lf&latrange2=%.4lf&longrange1=%.4lf&longrange2=%.4lf", lat, lat + offset, lng,
            lng - 3 * offset);

    CURL* res;
    res = curl_easy_init();

    curl_easy_setopt(res, CURLOPT_URL, WIGLE_BASE_URL);
    curl_easy_setopt(res, CURLOPT_COOKIE, WILGE_AUTHCOOKIE);
    curl_easy_setopt(res, CURLOPT_POST, 1);
    curl_easy_setopt(res, CURLOPT_POSTFIELDS, postdata);
    curl_easy_setopt(res, CURLOPT_WRITEFUNCTION, curl_callback);
    curl_easy_setopt(res, CURLOPT_WRITEDATA, json);

    curl_easy_perform(res);
    curl_easy_cleanup(res);
}

size_t curl_callback(char* data, size_t size, size_t nmemb, void* userdata) {
    char** save = (char**) userdata;

    size_t totalsize = size * nmemb;
    size_t previouslen = 0;

    if (*save == NULL) {
        *save = malloc(totalsize + 1);
        memcpy(*save, data, totalsize);
    } else {
        previouslen = strlen(*save);
        *save = realloc(*save, totalsize + previouslen);
        memcpy(*save + previouslen, data, totalsize);
    }

    (*save)[totalsize + previouslen + 1] = '\0';

    return totalsize;
}
