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

void fetch_wigle(ap_t** list, double lat, double lng, double offset, int months, int max) {
    char* json;

    curl_wigle(&json, lat, lng, offset);
    parse_json(list, json, months, max);
}

void curl_wigle(char** json, double lat, double lng, double offset) {
    char postdata[256];

    *json = NULL;

    sprintf(postdata, "latrange1=%lf&latrange2=%lf&longrange1=%lf&longrange2=%lf", lat, lat + offset, lng,
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
        strcat(*save, data);
    }

    (*save)[totalsize + previouslen + 1] = '\0';

    return totalsize;
}

void parse_json(ap_t** list, char* json, int months, int max) {
    json_t* root;
    json_t* results;
    json_t* result;
    json_error_t error;

    size_t parsed = 0;

    const char* bssid;
    const char* essid;
    const char* timestr;
    char timebuf[8];
    unsigned long long timestamp;

    root = json_loads(json, 0, &error);
    free(json);

    ap_t** current;
    *list = NULL;

    if (results = json_object_get(root, "results")) {
        current = list;
        for (int i = 0; i < json_array_size(results); i++) {
            result = json_array_get(results, i);

            essid = json_string_value(json_object_get(result, "ssid"));
            bssid = json_string_value(json_object_get(result, "netid"));
            timestr = json_string_value(json_object_get(result, "lastupdt"));

            timestamp = 0;
            // Add year
            strncpy(timebuf, timestr, 4);
            timebuf[4] = '\0';
            timestamp += (strtol(timebuf, NULL, 10) - 1969) * 12 * 30 * 24 * 3600;
            // Month
            strncpy(timebuf + 4, timestr, 2);
            timebuf[2] = '\0';
            timestamp += strtol(timebuf, NULL, 10) * 30 * 24 * 3600;
            // Day
            strncpy(timebuf + 6, timestr, 2);
            timebuf[2] = '\0';
            timestamp += strtol(timebuf, NULL, 10) * 24 * 3600;
            // Hour
            strncpy(timebuf + 8, timestr, 2);
            timebuf[2] = '\0';
            timestamp += strtol(timebuf, NULL, 10) * 3600;
            // Minutes
            strncpy(timebuf + 10, timestr, 2);
            timebuf[2] = '\0';
            timestamp += strtol(timebuf, NULL, 10) * 60;
            // Seconds
            timestamp += strtol(timebuf + 12, NULL, 10);

            if (time(NULL) - timestamp < months * 30 * 24 * 3600) {
                (*current) = malloc(sizeof(ap_t));
                (*current)->next = NULL;

                if (*list == NULL) {
                    *list = *current;
                }

                strcpy((*current)->essid, essid);

                for (int i = 0; i < 6; i++) {
                    (*current)->bssid[i] = strtol(bssid + 3 * i, NULL, 16);
                }

                current = &((*current)->next);
                parsed++;
            }
        }
    }

    json_decref(root);

    fprintf(stderr, "Parsed %d networks.\n", parsed);
}
