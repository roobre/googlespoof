//
// Created by roobre on 11/29/15.
//

#include <jansson.h>
#include <string.h>
#include <time.h>
#include "json.h"

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