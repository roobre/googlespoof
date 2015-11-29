//
// Created by roobre on 11/29/15.
//

#ifndef BEACONATOR_WIGLE_H
#define BEACONATOR_WIGLE_H

#include <stddef.h>
#include "beaconator.h"

extern const char WIGLE_BASE_URL[];
extern const char WILGE_AUTHCOOKIE[];

void fetch_wigle(ap_t** list, double lat, double lng, double offset, int months, int max);

static void curl_wigle(char** json, double lat, double lng, double offset);
static size_t curl_callback(char* data, size_t size, size_t nmemb, void* userdata);

static void parse_json(ap_t** list, char* json, int months, int max);

#endif //BEACONATOR_WIGLE_H
