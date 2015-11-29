//
// Created by roobre on 11/29/15.
//

#ifndef BEACONATOR_WIGLE_H
#define BEACONATOR_WIGLE_H

#include <stddef.h>
#include "beaconator.h"

extern const char WIGLE_BASE_URL[];
extern const char WILGE_AUTHCOOKIE[];

void fetch_wigle(char** json, double lat, double lng, double offset);
static size_t curl_callback(char* data, size_t size, size_t nmemb, void* userdata);

#endif //BEACONATOR_WIGLE_H
